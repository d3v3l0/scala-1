/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection

import generic._
import mutable.Builder

/** A map whose keys are sorted.
 *
 *  @author Sean McDirmid
 *  @author Martin Odersky
 *  @version 2.8
 *  @since   2.4
 */
trait SortedMap[L, A, +B] extends Map[L, A, B] with SortedMapLike[L, A, B, SortedMap[L, A, B]] {
  /** Needs to be overridden in subclasses. */
  override def empty: SortedMap[L, A, B] = SortedMap.empty[A, B]

  override protected[this] def newBuilder: Builder[L, (A, B), SortedMap[L, A, B]] =
    immutable.SortedMap.newBuilder[A, B]
}

/**
 * @since 2.8
 */
object SortedMap extends SortedMapFactory[L, SortedMap] {
  def empty[A, B](implicit ord: Ordering[A]): SortedMap[L, A, B] = immutable.SortedMap.empty[A, B](ord)

  implicit def canBuildFrom[A, B](implicit ord: Ordering[A]): CanBuildFrom[L, Coll, (A, B), SortedMap[L, A, B]] = new SortedMapCanBuildFrom[A, B]

  private[collection] trait Default[A, +B] extends SortedMap[L, A, B] {
  self =>
    override def +[B1 >: B](kv: (A, B1)): SortedMap[L, A, B1] = {
      val b = SortedMap.newBuilder[A, B1]
      b ++= this
      b += ((kv._1, kv._2))
      b.result()
    }

    override def - (key: A): SortedMap[L, A, B] = {
      val b = newBuilder
      for (kv <- this; if kv._1 != key) b += kv
      b.result()
    }
  }
}
