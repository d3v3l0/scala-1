/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2010-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package generic

import scala.collection.parallel._

/** A base trait for parallel builder factories.
 *
 *  @tparam From  the type of the underlying collection that requests a
 *                builder to be created.
 *  @tparam Elem  the element type of the collection to be created.
 *  @tparam To    the type of the collection to be created.
 *  @since 2.8
 */
trait CanCombineFrom[L, -From, -Elem, +To] extends CanBuildFrom[L, From, Elem, To] with Parallel {
  def apply(from: From): Combiner[L, Elem, To]
  def apply(): Combiner[L, Elem, To]
}

