/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package parallel.immutable

import scala.collection.generic.{GenericParTemplate, CanCombineFrom, ParFactory}
import scala.collection.parallel.ParSeqLike
import scala.collection.parallel.Combiner
import scala.collection.parallel.SeqSplitter
import mutable.ArrayBuffer
import immutable.Vector
import immutable.VectorBuilder
import immutable.VectorIterator

/** Immutable parallel vectors, based on vectors.
 *
 *  $paralleliterableinfo
 *
 *  $sideeffects
 *
 *  @tparam T    the element type of the vector
 *
 *  @author Aleksandar Prokopec
 *  @since 2.9
 *  @see  [[http://docs.scala-lang.org/overviews/parallel-collections/concrete-parallel-collections.html#parallel_vector Scala's Parallel Collections Library overview]]
 *  section on `ParVector` for more information.
 *
 *  @define Coll `immutable.ParVector`
 *  @define coll immutable parallel vector
 */
class ParVector[L, +T](private[this] val vector: Vector[L, T])
extends ParSeq[L, T]
   with GenericParTemplate[L, T, ParVector]
   with ParSeqLike[L, T, ParVector[L, T], Vector[L, T]]
   with Serializable
{
  override def companion = ParVector

  def this() = this(Vector())

  def apply(idx: Int) = vector.apply(idx)

  def length = vector.length

  def splitter: SeqSplitter[L, T] = {
    val pit = new ParVectorIterator(vector.startIndex, vector.endIndex)
    vector.initIterator(pit)
    pit
  }

  override def seq: Vector[L, T] = vector

  override def toVector: Vector[L, T] = vector

  class ParVectorIterator(_start: Int, _end: Int) extends VectorIterator[L, T](_start, _end) with SeqSplitter[L, T] {
    def remaining: Int = remainingElementCount
    def dup: SeqSplitter[L, T] = (new ParVector(remainingVector)).splitter
    def split: Seq[L, ParVectorIterator] = {
      val rem = remaining
      if (rem >= 2) psplit(rem / 2, rem - rem / 2)
      else Seq(this)
    }
    def psplit(sizes: Int*): Seq[L, ParVectorIterator] = {
      var remvector = remainingVector
      val splitted = new ArrayBuffer[L, Vector[L, T]]
      for (sz <- sizes) {
        splitted += remvector.take(sz)
        remvector = remvector.drop(sz)
      }
      splitted.map(v => new ParVector(v).splitter.asInstanceOf[ParVectorIterator])
    }
  }
}

/** $factoryInfo
 *  @define Coll `immutable.ParVector`
 *  @define coll immutable parallel vector
 */
object ParVector extends ParFactory[L, ParVector] {
  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParVector[L, T]] =
    new GenericCanCombineFrom[T]

  def newBuilder[T]: Combiner[L, T, ParVector[L, T]] = newCombiner[T]

  def newCombiner[T]: Combiner[L, T, ParVector[L, T]] = new LazyParVectorCombiner[T] // was: with EPC[T, ParVector[L, T]]
}

private[immutable] class LazyParVectorCombiner[T] extends Combiner[L, T, ParVector[L, T]] {
//self: EnvironmentPassingCombiner[T, ParVector[L, T]] =>
  var sz = 0
  val vectors = new ArrayBuffer[L, VectorBuilder[L, T]] += new VectorBuilder[L, T]

  def size: Int = sz

  def +=(elem: T): this.type = {
    vectors.last += elem
    sz += 1
    this
  }

  def clear() = {
    vectors.clear()
    vectors += new VectorBuilder[L, T]
    sz = 0
  }

  def result: ParVector[L, T] = {
    val rvb = new VectorBuilder[L, T]
    for (vb <- vectors) {
      rvb ++= vb.result
    }
    new ParVector(rvb.result)
  }

  def combine[U <: T, NewTo >: ParVector[L, T]](other: Combiner[L, U, NewTo]) = if (other eq this) this else {
    val that = other.asInstanceOf[LazyParVectorCombiner[T]]
    sz += that.sz
    vectors ++= that.vectors
    this
  }
}
