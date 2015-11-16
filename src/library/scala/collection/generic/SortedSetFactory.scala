/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala
package collection
package generic

import mutable.{Builder, SetBuilder}
import scala.language.higherKinds

/** A template for companion objects of Set and subclasses thereof.
 *
 *  @since 2.8
 */
abstract class SortedSetFactory[L, CC[A] <: SortedSet[L, A] with SortedSetLike[L, A, CC[A]]] {
  type Coll = CC[_]

  def empty[A](implicit ord: Ordering[A]): CC[A]

  def apply[A](elems: A*)(implicit ord: Ordering[A]): CC[A] = (newBuilder[A](ord) ++= elems).result()

  def newBuilder[A](implicit ord: Ordering[A]): Builder[L, A, CC[A]] = new SetBuilder[L, A, CC[A]](empty)

  implicit def newCanBuildFrom[A](implicit ord : Ordering[A]) : CanBuildFrom[L, Coll, A, CC[A]] = new SortedSetCanBuildFrom()(ord)

  class SortedSetCanBuildFrom[A](implicit ord: Ordering[A]) extends CanBuildFrom[L, Coll, A, CC[A]] {
    def apply(from: Coll) = newBuilder[A](ord)
    def apply() = newBuilder[A](ord)
  }
}
