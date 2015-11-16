/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package parallel

import scala.collection.generic._

/** A template trait for parallel sets.
 *
 *  $sideeffects
 *
 *  @tparam T    the element type of the set
 *
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait ParSet[L, T]
   extends GenSet[L, T]
   with GenericParTemplate[L, T, ParSet]
   with ParIterable[L, T]
   with ParSetLike[L, T, ParSet[L, T], Set[L, T]]
{ self =>

  override def empty: ParSet[L, T] = mutable.ParHashSet[L, T]()

  //protected[this] override def newCombiner: Combiner[L, T, ParSet[L, T]] = ParSet.newCombiner[T]

  override def companion: GenericCompanion[L, ParSet] with GenericParCompanion[L, ParSet] = ParSet

  override def stringPrefix = "ParSet"
}

object ParSet extends ParSetFactory[L, ParSet] {
  def newCombiner[T]: Combiner[L, T, ParSet[L, T]] = mutable.ParHashSetCombiner[T]

  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParSet[L, T]] = new GenericCanCombineFrom[T]
}
