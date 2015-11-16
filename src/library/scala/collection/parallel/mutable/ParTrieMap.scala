/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel.mutable

import scala.collection.generic._
import scala.collection.parallel.Combiner
import scala.collection.parallel.IterableSplitter
import scala.collection.parallel.Task
import scala.collection.concurrent.BasicNode
import scala.collection.concurrent.TNode
import scala.collection.concurrent.LNode
import scala.collection.concurrent.CNode
import scala.collection.concurrent.SNode
import scala.collection.concurrent.INode
import scala.collection.concurrent.TrieMap
import scala.collection.concurrent.TrieMapIterator

/** Parallel TrieMap collection.
 *
 *  It has its bulk operations parallelized, but uses the snapshot operation
 *  to create the splitter. This means that parallel bulk operations can be
 *  called concurrently with the modifications.
 *
 *  @author Aleksandar Prokopec
 *  @since 2.10
 *  @see  [[http://docs.scala-lang.org/overviews/parallel-collections/concrete-parallel-collections.html#parallel_concurrent_tries Scala's Parallel Collections Library overview]]
 *  section on `ParTrieMap` for more information.
 */
final class ParTrieMap[L, K, V] private[collection] (private val ctrie: TrieMap[L, K, V])
extends ParMap[L, K, V]
   with GenericParMapTemplate[L, K, V, ParTrieMap]
   with ParMapLike[L, K, V, ParTrieMap[L, K, V], TrieMap[L, K, V]]
   with ParTrieMapCombiner[K, V]
   with Serializable
{
  def this() = this(new TrieMap)

  override def mapCompanion: GenericParMapCompanion[L, ParTrieMap] = ParTrieMap

  override def empty: ParTrieMap[L, K, V] = ParTrieMap.empty

  protected[this] override def newCombiner = ParTrieMap.newCombiner

  override def seq = ctrie

  def splitter = new ParTrieMapSplitter(0, ctrie.readOnlySnapshot().asInstanceOf[TrieMap[L, K, V]], true)

  override def clear() = ctrie.clear()

  def result = this

  def get(key: K): Option[V] = ctrie.get(key)

  def put(key: K, value: V): Option[V] = ctrie.put(key, value)

  def update(key: K, value: V): Unit = ctrie.update(key, value)

  def remove(key: K): Option[V] = ctrie.remove(key)

  def +=(kv: (K, V)): this.type = {
    ctrie.+=(kv)
    this
  }

  def -=(key: K): this.type = {
    ctrie.-=(key)
    this
  }

  override def size = {
    val in = ctrie.readRoot()
    val r = in.gcasRead(ctrie)
    r match {
      case tn: TNode[_, _] => tn.cachedSize(ctrie)
      case ln: LNode[_, _] => ln.cachedSize(ctrie)
      case cn: CNode[_, _] =>
        tasksupport.executeAndWaitResult(new Size(0, cn.array.length, cn.array))
        cn.cachedSize(ctrie)
    }
  }

  override def stringPrefix = "ParTrieMap"

  /* tasks */

  /** Computes TrieMap size in parallel. */
  class Size(offset: Int, howmany: Int, array: Array[BasicNode]) extends Task[L, Int, Size] {
    var result = -1
    def leaf(prev: Option[Int]) = {
      var sz = 0
      var i = offset
      val until = offset + howmany
      while (i < until) {
        array(i) match {
          case sn: SNode[_, _] => sz += 1
          case in: INode[K, V] => sz += in.cachedSize(ctrie)
        }
        i += 1
      }
      result = sz
    }
    def split = {
      val fp = howmany / 2
      Seq(new Size(offset, fp, array), new Size(offset + fp, howmany - fp, array))
    }
    def shouldSplitFurther = howmany > 1
    override def merge(that: Size) = result = result + that.result
  }
}

private[collection] class ParTrieMapSplitter[K, V](lev: Int, ct: TrieMap[L, K, V], mustInit: Boolean)
extends TrieMapIterator[K, V](lev, ct, mustInit)
   with IterableSplitter[L, (K, V)]
{
  // only evaluated if `remaining` is invoked (which is not used by most tasks)
  lazy val totalsize = ct.par.size
  var iterated = 0

  protected override def newIterator(_lev: Int, _ct: TrieMap[L, K, V], _mustInit: Boolean) = new ParTrieMapSplitter[K, V](_lev, _ct, _mustInit)

  override def shouldSplitFurther[S](coll: scala.collection.parallel.ParIterable[L, S], parallelismLevel: Int) = {
    val maxsplits = 3 + Integer.highestOneBit(parallelismLevel)
    level < maxsplits
  }

  def dup = {
    val it = newIterator(0, ct, _mustInit = false)
    dupTo(it)
    it.iterated = this.iterated
    it
  }

  override def next() = {
    iterated += 1
    super.next()
  }

  def split: Seq[L, IterableSplitter[L, (K, V)]] = subdivide().asInstanceOf[Seq[L, IterableSplitter[L, (K, V)]]]

  override def isRemainingCheap = false

  def remaining: Int = totalsize - iterated
}

/** Only used within the `ParTrieMap`. */
private[mutable] trait ParTrieMapCombiner[K, V] extends Combiner[L, (K, V), ParTrieMap[L, K, V]] {

  def combine[N <: (K, V), NewTo >: ParTrieMap[L, K, V]](other: Combiner[L, N, NewTo]): Combiner[L, N, NewTo] = if (this eq other) this else {
    throw new UnsupportedOperationException("This shouldn't have been called in the first place.")

    val thiz = this.asInstanceOf[ParTrieMap[L, K, V]]
    val that = other.asInstanceOf[ParTrieMap[L, K, V]]
    val result = new ParTrieMap[L, K, V]

    result ++= thiz.iterator
    result ++= that.iterator

    result
  }

  override def canBeShared = true
}

object ParTrieMap extends ParMapFactory[L, ParTrieMap] {
  def empty[K, V]: ParTrieMap[L, K, V] = new ParTrieMap[L, K, V]
  def newCombiner[K, V]: Combiner[L, (K, V), ParTrieMap[L, K, V]] = new ParTrieMap[L, K, V]

  implicit def canBuildFrom[K, V]: CanCombineFrom[L, Coll, (K, V), ParTrieMap[L, K, V]] = new CanCombineFromMap[K, V]
}
