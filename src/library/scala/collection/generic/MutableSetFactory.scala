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

import mutable.{ Builder, GrowingBuilder }
import scala.language.higherKinds

abstract class MutableSetFactory[L, CC[X] <: mutable.Set[L, X] with mutable.SetLike[L, X, CC[X]]]
  extends SetFactory[L, CC] {

  def newBuilder[A]: Builder[L, A, CC[A]] = new GrowingBuilder[L, A, CC[A]](empty[A])
}
