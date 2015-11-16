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

/** A base trait for maps that can be mutated.
 *  $mapNote
 *  $mapTags
 *  @since 1.0
 *  @author  Matthias Zenger
 */
trait Map[L, A, B]
  extends Iterable[L, (A, B)]
//     with GenMap[L, A, B]
     with scala.collection.Map[L, A, B]
     with MapLike[L, A, B, Map[L, A, B]] {

  override def empty: Map[L, A, B] = Map.empty

  override def seq: Map[L, A, B] = this

  /** The same map with a given default function.
   *
   *  Invoking transformer methods (e.g. `map`) will not preserve the default value.
   *
   *  @param d     the function mapping keys to values, used for non-present keys
   *  @return      a wrapper of the map with a default value
   */
  def withDefault(d: A => B): mutable.Map[L, A, B] = new Map.WithDefault[A, B](this, d)

  /** The same map with a given default value.
   *
   *  Invoking transformer methods (e.g. `map`) will not preserve the default value.
   *
   *  @param d     default value used for non-present keys
   *  @return      a wrapper of the map with a default value
   */
  def withDefaultValue(d: B): mutable.Map[L, A, B] = new Map.WithDefault[A, B](this, x => d)
}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `HashMap`.
 *  @define coll mutable map
 *  @define Coll `mutable.Map`
 */
object Map extends MutableMapFactory[L, Map] {
  /** $canBuildFromInfo */
  implicit def canBuildFrom[A, B]: CanBuildFrom[L, Coll, (A, B), Map[L, A, B]] = new MapCanBuildFrom[A, B]

  def empty[A, B]: Map[L, A, B] = new HashMap[L, A, B]

  class WithDefault[A, B](underlying: Map[L, A, B], d: A => B) extends scala.collection.Map.WithDefault(underlying, d) with Map[L, A, B] {
    override def += (kv: (A, B)) = {underlying += kv; this}
    def -= (key: A) = {underlying -= key; this}
    override def empty = new WithDefault(underlying.empty, d)
    override def updated[B1 >: B](key: A, value: B1): WithDefault[A, B1] = new WithDefault[A, B1](underlying.updated[B1](key, value), d)
    override def + [B1 >: B](kv: (A, B1)): WithDefault[A, B1] = updated(kv._1, kv._2)
    override def - (key: A): WithDefault[A, B] = new WithDefault(underlying - key, d)

    /** If these methods aren't overridden to thread through the underlying map,
     *  successive calls to withDefault* have no effect.
     */
    override def withDefault(d: A => B): mutable.Map[L, A, B] = new WithDefault[A, B](underlying, d)
    override def withDefaultValue(d: B): mutable.Map[L, A, B] = new WithDefault[A, B](underlying, x => d)
  }
}

/** Explicit instantiation of the `Map` trait to reduce class file size in subclasses. */
abstract class AbstractMap[L, A, B] extends scala.collection.AbstractMap[L, A, B] with Map[L, A, B]
