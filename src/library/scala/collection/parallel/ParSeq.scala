/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel

import scala.collection.generic.GenericCompanion
import scala.collection.generic.GenericParCompanion
import scala.collection.generic.GenericParTemplate
import scala.collection.generic.ParFactory
import scala.collection.generic.CanCombineFrom
import scala.collection.GenSeq
import scala.collection.parallel.mutable.ParArrayCombiner

/** A template trait for parallel sequences.
 *
 *  $parallelseqinfo
 *
 *  $sideeffects
 *
 *  @tparam T      the type of the elements in this parallel sequence
 *
 *  @author Aleksandar Prokopec
 */
trait ParSeq[L, +T] extends GenSeq[L, T]
                    with ParIterable[L, T]
                    with GenericParTemplate[L, T, ParSeq]
                    with ParSeqLike[L, T, ParSeq[L, T], Seq[L, T]]
{
  override def companion: GenericCompanion[L, ParSeq] with GenericParCompanion[L, ParSeq] = ParSeq
  //protected[this] override def newBuilder = ParSeq.newBuilder[T]

  def apply(i: Int): T

  override def toString = super[ParIterable].toString

  override def stringPrefix = getClass.getSimpleName
}

object ParSeq extends ParFactory[L, ParSeq] {
  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParSeq[L, T]] = new GenericCanCombineFrom[T]

  def newBuilder[T]: Combiner[L, T, ParSeq[L, T]] = ParArrayCombiner[T]
  def newCombiner[T]: Combiner[L, T, ParSeq[L, T]] = ParArrayCombiner[T]
}
