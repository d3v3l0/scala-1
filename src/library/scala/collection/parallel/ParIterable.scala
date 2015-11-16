/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel

import scala.collection.GenIterable
import scala.collection.generic._
import scala.collection.parallel.mutable.ParArrayCombiner

/** A template trait for parallel iterable collections.
 *
 *  $paralleliterableinfo
 *
 *  $sideeffects
 *
 *  @tparam T    the element type of the collection
 *
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait ParIterable[L, +T]
extends GenIterable[L, T]
   with GenericParTemplate[L, T, ParIterable]
   with ParIterableLike[L, T, ParIterable[L, T], Iterable[L, T]] {
  override def companion: GenericCompanion[L, ParIterable] with GenericParCompanion[L, ParIterable] = ParIterable
  //protected[this] override def newBuilder = ParIterable.newBuilder[T]

  def stringPrefix = "ParIterable"
}

/** $factoryInfo
 */
object ParIterable extends ParFactory[L, ParIterable] {
  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParIterable[L, T]] = new GenericCanCombineFrom[T]

  def newBuilder[T]: Combiner[L, T, ParIterable[L, T]] = ParArrayCombiner[T]

  def newCombiner[T]: Combiner[L, T, ParIterable[L, T]] = ParArrayCombiner[T]
}

