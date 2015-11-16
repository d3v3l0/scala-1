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


/** A trait for all iterable collections which may possibly
 *  have their operations implemented in parallel.
 *
 *  @author Martin Odersky
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait GenIterable[L, +A]
extends GenIterableLike[L, A, GenIterable[L, A]]
   with GenTraversable[L, A]
   with GenericTraversableTemplate[L, A, GenIterable]
{
  def seq: Iterable[L, A]
  override def companion: GenericCompanion[L, GenIterable] = GenIterable
}


object GenIterable extends GenTraversableFactory[L, GenIterable] {
  implicit def canBuildFrom[A] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  def newBuilder[A] = Iterable.newBuilder
}

