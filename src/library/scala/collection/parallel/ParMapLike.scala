/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel

import scala.collection.MapLike
import scala.collection.GenMapLike
import scala.collection.Map
import scala.collection.mutable.Builder
import scala.annotation.unchecked.uncheckedVariance
import scala.collection.generic.IdleSignalling
import scala.collection.generic.Signalling

/** A template trait for mutable parallel maps. This trait is to be mixed in
 *  with concrete parallel maps to override the representation type.
 *
 *  $sideeffects
 *
 *  @tparam K    the key type of the map
 *  @tparam V    the value type of the map
 *  @define Coll `ParMap`
 *  @define coll parallel map
 *
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait ParMapLike[L, K,
                 +V,
                 +Repr <: ParMapLike[L, K, V, Repr, Sequential] with ParMap[L, K, V],
                 +Sequential <: Map[L, K, V] with MapLike[L, K, V, Sequential]]
extends GenMapLike[L, K, V, Repr]
   with ParIterableLike[L, (K, V), Repr, Sequential]
{
self =>

  def default(key: K): V = throw new NoSuchElementException("key not found: " + key)

  def empty: Repr

  def apply(key: K) = get(key) match {
    case Some(v) => v
    case None => default(key)
  }

  def getOrElse[U >: V](key: K, default: => U): U = get(key) match {
    case Some(v) => v
    case None => default
  }

  def contains(key: K): Boolean = get(key).isDefined

  def isDefinedAt(key: K): Boolean = contains(key)

  private[this] def keysIterator(s: IterableSplitter[L, (K, V)] @uncheckedVariance): IterableSplitter[L, K] =
    new IterableSplitter[L, K] {
      i =>
      val iter = s
      def hasNext = iter.hasNext
      def next() = iter.next()._1
      def split = {
        val ss = iter.split.map(keysIterator(_))
        ss.foreach { _.signalDelegate = i.signalDelegate }
        ss
      }
      def remaining = iter.remaining
      def dup = keysIterator(iter.dup)
    }

  def keysIterator: IterableSplitter[L, K] = keysIterator(splitter)

  private[this] def valuesIterator(s: IterableSplitter[L, (K, V)] @uncheckedVariance): IterableSplitter[L, V] =
    new IterableSplitter[L, V] {
      i =>
      val iter = s
      def hasNext = iter.hasNext
      def next() = iter.next()._2
      def split = {
        val ss = iter.split.map(valuesIterator(_))
        ss.foreach { _.signalDelegate = i.signalDelegate }
        ss
      }
      def remaining = iter.remaining
      def dup = valuesIterator(iter.dup)
    }

  def valuesIterator: IterableSplitter[L, V] = valuesIterator(splitter)

  protected class DefaultKeySet extends ParSet[L, K] {
    def contains(key : K) = self.contains(key)
    def splitter = keysIterator(self.splitter)
    def + (elem: K): ParSet[L, K] =
      (ParSet[L, K]() ++ this + elem).asInstanceOf[ParSet[L, K]] // !!! concrete overrides abstract problem
    def - (elem: K): ParSet[L, K] =
      (ParSet[L, K]() ++ this - elem).asInstanceOf[ParSet[L, K]] // !!! concrete overrides abstract problem
    override def size = self.size
    override def foreach[S](f: K => S) = for ((k, v) <- self) f(k)
    override def seq = self.seq.keySet
  }

  protected class DefaultValuesIterable extends ParIterable[L, V] {
    def splitter = valuesIterator(self.splitter)
    override def size = self.size
    override def foreach[S](f: V => S) = for ((k, v) <- self) f(v)
    def seq = self.seq.values
  }

  def keySet: ParSet[L, K] = new DefaultKeySet

  def keys: ParIterable[L, K] = keySet

  def values: ParIterable[L, V] = new DefaultValuesIterable

  def filterKeys(p: K => Boolean): ParMap[L, K, V] = new ParMap[L, K, V] {
    lazy val filtered = self.filter(kv => p(kv._1))
    override def foreach[S](f: ((K, V)) => S): Unit = for (kv <- self) if (p(kv._1)) f(kv)
    def splitter = filtered.splitter
    override def contains(key: K) = self.contains(key) && p(key)
    def get(key: K) = if (!p(key)) None else self.get(key)
    def seq = self.seq.filterKeys(p)
    def size = filtered.size
    def + [U >: V](kv: (K, U)): ParMap[L, K, U] = ParMap[L, K, U]() ++ this + kv
    def - (key: K): ParMap[L, K, V] = ParMap[L, K, V]() ++ this - key
  }

  def mapValues[S](f: V => S): ParMap[L, K, S] = new ParMap[L, K, S] {
    override def foreach[Q](g: ((K, S)) => Q): Unit = for ((k, v) <- self) g((k, f(v)))
    def splitter = self.splitter.map(kv => (kv._1, f(kv._2)))
    override def size = self.size
    override def contains(key: K) = self.contains(key)
    def get(key: K) = self.get(key).map(f)
    def seq = self.seq.mapValues(f)
    def + [U >: S](kv: (K, U)): ParMap[L, K, U] = ParMap[L, K, U]() ++ this + kv
    def - (key: K): ParMap[L, K, S] = ParMap[L, K, S]() ++ this - key
  }

  // note - should not override toMap (could be mutable)
}
