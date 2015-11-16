/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package parallel.immutable

import scala.collection.generic._
import scala.collection.parallel.ParSetLike
import scala.collection.parallel.Combiner

/** An immutable variant of `ParSet`.
 *
 *  @define Coll `mutable.ParSet`
 *  @define coll mutable parallel set
 */
trait ParSet[L, T]
extends scala.collection/*.immutable*/.GenSet[L, T]
   with GenericParTemplate[L, T, ParSet]
   with parallel.ParSet[L, T]
   with ParIterable[L, T]
   with ParSetLike[L, T, ParSet[L, T], scala.collection.immutable.Set[L, T]]
{
self =>
  override def empty: ParSet[L, T] = ParHashSet[L, T]()

  override def companion: GenericCompanion[L, ParSet] with GenericParCompanion[L, ParSet] = ParSet

  override def stringPrefix = "ParSet"

  // ok, because this could only violate `apply` and we can live with that
  override def toSet[U >: T]: ParSet[L, U] = this.asInstanceOf[ParSet[L, U]]
}

/** $factoryInfo
 *  @define Coll `mutable.ParSet`
 *  @define coll mutable parallel set
 */
object ParSet extends ParSetFactory[L, ParSet] {
  def newCombiner[T]: Combiner[L, T, ParSet[L, T]] = HashSetCombiner[T]

  implicit def canBuildFrom[T]: CanCombineFrom[L, Coll, T, ParSet[L, T]] = new GenericCanCombineFrom[T]
}
