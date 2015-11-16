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

import scala.collection.mutable.Builder
import scala.collection.parallel.Combiner
import scala.collection.parallel.ParSet
import scala.collection.parallel.ParSetLike
import scala.language.higherKinds

/**
 *  @author Aleksandar Prokopec
 *  @since 2.8
 */
abstract class ParSetFactory[L, CC[X] <: ParSet[L, X] with ParSetLike[L, X, CC[X], _] with GenericParTemplate[L, X, CC]]
  extends GenSetFactory[L, CC]
     with GenericParCompanion[L, CC]
{
  def newBuilder[A]: Combiner[L, A, CC[A]] = newCombiner[A]

  def newCombiner[A]: Combiner[L, A, CC[A]]

  class GenericCanCombineFrom[A] extends CanCombineFrom[L, CC[_], A, CC[A]] {
    override def apply(from: Coll) = from.genericCombiner[A]
    override def apply() = newCombiner[A]
  }
}

