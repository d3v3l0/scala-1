/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala
package collection
package immutable

import generic._
import mutable.Builder
import parallel.immutable.ParIterable

/** A base trait for iterable collections that are guaranteed immutable.
 *  $iterableInfo
 *
 *  @define Coll `immutable.Iterable`
 *  @define coll immutable iterable collection
 */
trait Iterable[L, +A] extends Traversable[L, A]
//                      with GenIterable[L, A]
                      with scala.collection.Iterable[L, A]
                      with GenericTraversableTemplate[L, A, Iterable]
                      with IterableLike[L, A, Iterable[L, A]]
                      with Parallelizable[L, A, ParIterable[L, A]]
{
  override def companion: GenericCompanion[L, Iterable] = Iterable
  protected[this] override def parCombiner = ParIterable.newCombiner[A] // if `immutable.IterableLike` gets introduced, please move this there!
  override def seq: Iterable[L, A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `List`.
 *  @define Coll `immutable.Iterable`
 *  @define coll immutable iterable collection
 */
object Iterable extends TraversableFactory[L, Iterable] {
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, Iterable[L, A]] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  def newBuilder[A]: Builder[L, A, Iterable[L, A]] = new mutable.ListBuffer
}
