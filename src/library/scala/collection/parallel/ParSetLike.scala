/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection.parallel

import scala.collection.SetLike
import scala.collection.GenSetLike
import scala.collection.GenSet
import scala.collection.Set

/** A template trait for parallel sets. This trait is mixed in with concrete
 *  parallel sets to override the representation type.
 *
 *  $sideeffects
 *
 *  @tparam T    the element type of the set
 *  @define Coll `ParSet`
 *  @define coll parallel set
 *
 *  @author Aleksandar Prokopec
 *  @since 2.9
 */
trait ParSetLike[L, T,
                 +Repr <: ParSetLike[L, T, Repr, Sequential] with ParSet[L, T],
                 +Sequential <: Set[L, T] with SetLike[L, T, Sequential]]
extends GenSetLike[L, T, Repr]
   with ParIterableLike[L, T, Repr, Sequential]
{ self =>

  def empty: Repr

  // note: should not override toSet (could be mutable)

  def union(that: GenSet[L, T]): Repr = sequentially {
    _ union that
  }

  def diff(that: GenSet[L, T]): Repr = sequentially {
    _ diff that
  }
}
