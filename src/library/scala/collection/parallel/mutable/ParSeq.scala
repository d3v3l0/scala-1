/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel.mutable

import scala.collection.generic.GenericParTemplate
import scala.collection.generic.GenericCompanion
import scala.collection.generic.GenericParCompanion
import scala.collection.generic.CanCombineFrom
import scala.collection.generic.ParFactory
import scala.collection.parallel.ParSeqLike
import scala.collection.parallel.Combiner

/** A mutable variant of `ParSeq`.
 *
 *  @define Coll `mutable.ParSeq`
 *  @define coll mutable parallel sequence
 */
trait ParSeq[L, T] extends scala.collection/*.mutable*/.GenSeq[L, T] // was: scala.collection.mutable.Seq[L, T]
                   with ParIterable[L, T]
                   with scala.collection.parallel.ParSeq[L, T]
                   with GenericParTemplate[L, T, ParSeq]
                   with ParSeqLike[L, T, ParSeq[L, T], scala.collection.mutable.Seq[L, T]] {
self =>
  override def companion: GenericCompanion[L, ParSeq] with GenericParCompanion[L, ParSeq] = ParSeq
  //protected[this] override def newBuilder = ParSeq.newBuilder[T]

  def update(i: Int, elem: T): Unit

  def seq: scala.collection.mutable.Seq[L, T]

  override def toSeq: ParSeq[L, T] = this
}


/** $factoryInfo
 *  @define Coll `mutable.ParSeq`
 *  @define coll mutable parallel sequence
 */
object ParSeq extends ParFactory[L, ParSeq] {
  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParSeq[L, T]] = new GenericCanCombineFrom[T]

  def newBuilder[T]: Combiner[L, T, ParSeq[L, T]] = ParArrayCombiner[T]

  def newCombiner[T]: Combiner[L, T, ParSeq[L, T]] = ParArrayCombiner[T]
}
