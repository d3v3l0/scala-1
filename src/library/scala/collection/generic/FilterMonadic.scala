/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package generic

/** A template trait that contains just the `map`, `flatMap`, `foreach` and `withFilter` methods
 *  of trait `TraversableLike`.
 */
trait FilterMonadic[L, +A, +Repr] extends Any {
  def map[B, That](f: A => B)(implicit bf: CanBuildFrom[L, Repr, B, That]): That
  def flatMap[B, That](f: A => scala.collection.GenTraversableOnce[L, B])(implicit bf: CanBuildFrom[L, Repr, B, That]): That
  def foreach[U](f: A => U): Unit
  def withFilter(p: A => Boolean): FilterMonadic[L, A, Repr]
}
