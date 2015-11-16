/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection

import generic._

/** A base trait for all sets, mutable as well as immutable.
 *
 * $setNote
 * '''Implementation note:''' If your additions and mutations return the same kind of set as the set
 *       you are defining, you should inherit from `SetLike` as well.
 * $setTags
 *
 * @since 1.0
 * @author Matthias Zenger
 */
trait Set[L, A] extends (A => Boolean)
                with Iterable[L, A]
                with GenSet[A]
                with GenericSetTemplate[A, Set]
                with SetLike[L, A, Set[L, A]] {
  override protected type LT = Any

  override def companion: GenericCompanion[Set] = Set

  override def seq: Set[L, A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is one of `EmptySet`, `Set1`, `Set2`, `Set3`, `Set4` in
 *  class `immutable.Set` for sets of sizes up to 4, and a `immutable.HashSet` for sets of larger sizes.
 *  @define coll set
 *  @define Coll `Set`
 */
object Set extends SetFactory[Set] {
  def newBuilder[A] = immutable.Set.newBuilder[A]
  override def empty[A]: Set[L, A] = immutable.Set.empty[A]
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, Set[L, A]] = setCanBuildFrom[A]
}

/** Explicit instantiation of the `Set` trait to reduce class file size in subclasses. */
abstract class AbstractSet[A] extends AbstractIterable[L, A] with Set[L, A]
