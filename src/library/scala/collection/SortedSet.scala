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

/** A sorted set.
 *
 *  @author Sean McDirmid
 *  @author Martin Odersky
 *  @version 2.8
 *  @since   2.4
 */
trait SortedSet[L, A] extends Set[L, A] with SortedSetLike[L, A, SortedSet[L, A]] {
  /** Needs to be overridden in subclasses. */
  override def empty: SortedSet[L, A] = SortedSet.empty[A]
}

/**
 * @since 2.8
 */
object SortedSet extends SortedSetFactory[L, SortedSet] {
  def empty[A](implicit ord: Ordering[A]): immutable.SortedSet[L, A] = immutable.SortedSet.empty[A](ord)
  def canBuildFrom[A](implicit ord: Ordering[A]): CanBuildFrom[L, Coll, A, SortedSet[L, A]] = newCanBuildFrom[A]
  // Force a declaration here so that BitSet's (which does not inherit from SortedSetFactory) can be more specific
  override implicit def newCanBuildFrom[A](implicit ord : Ordering[A]) : CanBuildFrom[L, Coll, A, SortedSet[L, A]] = super.newCanBuildFrom
}
