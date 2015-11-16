/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2010-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection
package generic

import scala.language.higherKinds
import scala.reflect.ClassTag

/** A template for companion objects of `ClassTagTraversable` and
 *  subclasses thereof.
 *
 *  @define coll collection
 *  @define Coll `Traversable`
 *  @define genericCanBuildFromInfo
 *    The standard `CanBuildFrom` instance for $Coll objects.
 *    @author Aleksandar Prokopec
 *    @since 2.8
 */
abstract class ClassTagTraversableFactory[L, CC[X] <: Traversable[L, X] with GenericClassTagTraversableTemplate[L, X, CC]]
              extends GenericClassTagCompanion[L, CC] {

  class GenericCanBuildFrom[A](implicit tag: ClassTag[A]) extends CanBuildFrom[L, CC[_], A, CC[A]] {
    def apply(from: CC[_]) = from.genericClassTagBuilder[A]
    def apply = newBuilder[A]
  }
}
