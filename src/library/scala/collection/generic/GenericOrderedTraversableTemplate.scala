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

import mutable.Builder
import scala.annotation.unchecked.uncheckedVariance
import scala.language.higherKinds

/** This trait represents collections classes which require
 *  ordered element types.
 *
 *  @author Aleksandar Prokopec
 */
trait GenericOrderedTraversableTemplate[L, +A, +CC[X] <: Traversable[L, X]] extends HasNewBuilder[L, A, CC[A] @uncheckedVariance] {
  implicit protected[this] val ord: Ordering[A]
  def orderedCompanion: GenericOrderedCompanion[L, CC]
  def genericOrderedBuilder[B](implicit ord: Ordering[B]): Builder[L, B, CC[B]] = orderedCompanion.newBuilder[B]
}

