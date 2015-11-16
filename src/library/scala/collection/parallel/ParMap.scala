/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel

import scala.collection.Map
import scala.collection.GenMap
import scala.collection.mutable.Builder
import scala.collection.generic.ParMapFactory
import scala.collection.generic.GenericParMapTemplate
import scala.collection.generic.GenericParMapCompanion
import scala.collection.generic.CanCombineFrom

/** A template trait for parallel maps.
 *
 *  $sideeffects
 *
 *  @tparam K    the key type of the map
 *  @tparam V    the value type of the map
 *
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait ParMap[L, K, +V]
extends GenMap[L, K, V]
   with GenericParMapTemplate[L, K, V, ParMap]
   with ParIterable[L, (K, V)]
   with ParMapLike[L, K, V, ParMap[L, K, V], Map[L, K, V]]
{
self =>

  def mapCompanion: GenericParMapCompanion[L, ParMap] = ParMap

  //protected[this] override def newCombiner: Combiner[L, (K, V), ParMap[L, K, V]] = ParMap.newCombiner[K, V]

  def empty: ParMap[L, K, V] = new mutable.ParHashMap[L, K, V]

  override def stringPrefix = "ParMap"

  override def updated [U >: V](key: K, value: U): ParMap[L, K, U] = this + ((key, value))

  def + [U >: V](kv: (K, U)): ParMap[L, K, U]
}



object ParMap extends ParMapFactory[L, ParMap] {
  def empty[K, V]: ParMap[L, K, V] = new mutable.ParHashMap[L, K, V]

  def newCombiner[K, V]: Combiner[L, (K, V), ParMap[L, K, V]] = mutable.ParHashMapCombiner[K, V]

  implicit def canBuildFrom[K, V]: CanCombineFrom[L, Coll, (K, V), ParMap[L, K, V]] = new CanCombineFromMap[K, V]

  /** An abstract shell used by { mutable, immutable }.Map but not by collection.Map
   *  because of variance issues.
   */
  abstract class WithDefault[A, +B](underlying: ParMap[L, A, B], d: A => B) extends ParMap[L, A, B] {
    override def size               = underlying.size
    def get(key: A)                 = underlying.get(key)
    def splitter                    = underlying.splitter
    override def default(key: A): B = d(key)
  }
}
