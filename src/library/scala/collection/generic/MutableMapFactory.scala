/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala
package collection
package generic

import mutable.Builder
import scala.language.higherKinds

/** A template for companion objects of `mutable.Map` and subclasses thereof.
 *    @author Martin Odersky
 *    @version 2.8
 *    @since 2.8
 */
abstract class MutableMapFactory[L, CC[A, B] <: mutable.Map[L, A, B] with mutable.MapLike[L, A, B, CC[A, B]]]
  extends MapFactory[L, CC] {

  /** The default builder for $Coll objects.
   *  @tparam A      the type of the keys
   *  @tparam B      the type of the associated values
   */
  override def newBuilder[A, B]: Builder[L, (A, B), CC[A, B]] = empty[A, B]
}
