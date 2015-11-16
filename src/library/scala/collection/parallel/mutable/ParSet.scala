/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel.mutable

import scala.collection.generic._
import scala.collection.parallel.Combiner

/** A mutable variant of `ParSet`.
 *
 *  @author Aleksandar Prokopec
 */
trait ParSet[L, T]
extends scala.collection/*.mutable*/.GenSet[L, T]
   with ParIterable[L, T]
   with scala.collection.parallel.ParSet[L, T]
   with GenericParTemplate[L, T, ParSet]
   with ParSetLike[L, T, ParSet[L, T], scala.collection.mutable.Set[L, T]]
{
self =>
  override def companion: GenericCompanion[L, ParSet] with GenericParCompanion[L, ParSet] = ParSet
  override def empty: ParSet[L, T] = ParHashSet()
  def seq: scala.collection.mutable.Set[L, T]
}


/** $factoryInfo
 *  @define Coll `mutable.ParSet`
 *  @define coll mutable parallel set
 */
object ParSet extends ParSetFactory[L, ParSet] {
  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParSet[L, T]] = new GenericCanCombineFrom[T]

  override def newBuilder[T]: Combiner[L, T, ParSet[L, T]] = ParHashSet.newBuilder

  override def newCombiner[T]: Combiner[L, T, ParSet[L, T]] = ParHashSet.newCombiner
}
