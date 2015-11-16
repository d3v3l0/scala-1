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
import mutable.Builder

/** A base trait for iterable collections.
 *  $iterableInfo
 */
trait Iterable[L, +A] extends Traversable[L, A]
                      with GenIterable[A]
                      with GenericTraversableTemplate[A, Iterable]
                      with IterableLike[L, A, Iterable[L, A]] {
  type LT

  override def companion: GenericCompanion[Iterable] = Iterable

  override def seq = this

  /* The following methods are inherited from trait IterableLike
   *
  override def iterator: Iterator[A]
  override def takeRight(n: Int): Iterable[L, A]
  override def dropRight(n: Int): Iterable[L, A]
  override def sameElements[B >: A](that: GenIterable[B]): Boolean
  override def view
  override def view(from: Int, until: Int)
  */

}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `List`.
 *  @define coll iterable collection
 *  @define Coll `Iterable`
 */
object Iterable extends TraversableFactory[Iterable] {

  /** $genericCanBuildFromInfo */
  implicit def canBuildFrom[A]: CanBuildFrom[Coll, A, Iterable[L, A]] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]

  def newBuilder[A]: Builder[A, Iterable[L, A]] = immutable.Iterable.newBuilder[A]
}

/** Explicit instantiation of the `Iterable` trait to reduce class file size in subclasses. */
abstract class AbstractIterable[+A] extends AbstractTraversable[A] with Iterable[L, A]
