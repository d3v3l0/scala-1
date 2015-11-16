/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package mutable

import generic._

/**
 * Base trait for mutable sorted set.
 *
 * @define Coll `mutable.SortedSet`
 * @define coll mutable sorted set
 *
 * @author Lucien Pereira
 *
 */
trait SortedSet[L, A] extends scala.collection.SortedSet[L, A] with scala.collection.SortedSetLike[L, A,SortedSet[L, A]]
  with mutable.Set[L, A] with mutable.SetLike[L, A, SortedSet[L, A]] {

  /** Needs to be overridden in subclasses. */
  override def empty: SortedSet[L, A] = SortedSet.empty[A]

}

/**
 * A template for mutable sorted set companion objects.
 *
 * @define Coll `mutable.SortedSet`
 * @define coll mutable sorted set
 * @define factoryInfo
 *   This object provides a set of operations needed to create sorted sets of type mutable.SortedSet.
 * @define sortedSetCanBuildFromInfo
 *   Standard `CanBuildFrom` instance for sorted sets.
 *
 * @author Lucien Pereira
 *
 */
object SortedSet extends MutableSortedSetFactory[L, SortedSet] {
  implicit def canBuildFrom[A](implicit ord: Ordering[A]): CanBuildFrom[L, Coll, A, SortedSet[L, A]] = new SortedSetCanBuildFrom[A]

  def empty[A](implicit ord: Ordering[A]): SortedSet[L, A] = TreeSet.empty[A]

}
