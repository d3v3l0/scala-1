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


/** A trait for sets which may possibly
 *  have their operations implemented in parallel.
 *
 *  @author Martin Odersky
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait GenSet[L, A]
extends GenSetLike[L, A, GenSet[L, A]]
   with GenIterable[L, A]
   with GenericSetTemplate[L, A, GenSet]
{
  override def companion: GenericCompanion[L, GenSet] = GenSet
  def seq: Set[L, A]
}


object GenSet extends GenTraversableFactory[L, GenSet] {
  implicit def canBuildFrom[A] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  def newBuilder[A] = Set.newBuilder
}

