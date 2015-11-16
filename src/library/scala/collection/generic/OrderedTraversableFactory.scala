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

import scala.language.higherKinds

abstract class OrderedTraversableFactory[L, CC[X] <: Traversable[L, X] with GenericOrderedTraversableTemplate[L, X, CC]]
extends GenericOrderedCompanion[L, CC] {

  class GenericCanBuildFrom[A](implicit ord: Ordering[A]) extends CanBuildFrom[L, CC[_], A, CC[A]] {
    def apply(from: CC[_]) = from.genericOrderedBuilder[A]
    def apply = newBuilder[A]
  }

}
