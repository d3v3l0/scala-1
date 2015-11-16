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

/** A generic trait for mutable sets.
 *  $setNote
 *  $setTags
 *
 *  @since 1.0
 *  @author Matthias Zenger
 *  @define Coll `mutable.Set`
 *  @define coll mutable set
 */
trait Set[L, A] extends Iterable[L, A]
//                with GenSet[L, A]
                with scala.collection.Set[L, A]
                with GenericSetTemplate[L, A, Set]
                with SetLike[L, A, Set[L, A]] {
  override def companion: GenericCompanion[L, Set] = Set
  override def seq: Set[L, A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `HashSet`.
 *  @define coll mutable set
 *  @define Coll `mutable.Set`
 */
object Set extends MutableSetFactory[L, Set] {
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, Set[L, A]] = setCanBuildFrom[A]
  override def empty[A]: Set[L, A] = HashSet.empty[A]
}

/** Explicit instantiation of the `Set` trait to reduce class file size in subclasses. */
abstract class AbstractSet[L, A] extends AbstractIterable[L, A] with Set[L, A]
