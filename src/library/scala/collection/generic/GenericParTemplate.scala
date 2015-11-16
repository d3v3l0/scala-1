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

import scala.collection.parallel.Combiner
import scala.collection.parallel.ParIterable
import scala.collection.parallel.ParMap
import scala.collection.parallel.TaskSupport

import scala.annotation.unchecked.uncheckedVariance
import scala.language.higherKinds

/** A template trait for collections having a companion.
 *
 *  @tparam A    the element type of the collection
 *  @tparam CC   the type constructor representing the collection class
 *  @author Aleksandar Prokopec
 *  @since 2.8
 */
trait GenericParTemplate[L, +A, +CC[X] <: ParIterable[L, X]]
extends GenericTraversableTemplate[L, A, CC]
   with HasNewCombiner[L, A, CC[A] @uncheckedVariance]
{
  def companion: GenericCompanion[L, CC] with GenericParCompanion[L, CC]

  protected[this] override def newBuilder: scala.collection.mutable.Builder[L, A, CC[A]] = newCombiner

  protected[this] override def newCombiner: Combiner[L, A, CC[A]] = {
    val cb = companion.newCombiner[A]
    cb
  }

  override def genericBuilder[B]: Combiner[L, B, CC[B]] = genericCombiner[B]

  def genericCombiner[B]: Combiner[L, B, CC[B]] = {
    val cb = companion.newCombiner[B]
    cb
  }

}


trait GenericParMapTemplate[L, K, +V, +CC[X, Y] <: ParMap[L, X, Y]] extends GenericParTemplate[L, (K, V), ParIterable]
{
  protected[this] override def newCombiner: Combiner[L, (K, V), CC[K, V]] = {
    val cb = mapCompanion.newCombiner[K, V]
    cb
  }

  def mapCompanion: GenericParMapCompanion[L, CC]

  def genericMapCombiner[P, Q]: Combiner[L, (P, Q), CC[P, Q]] = {
    val cb = mapCompanion.newCombiner[P, Q]
    cb
  }
}

