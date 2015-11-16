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

/** Type class witnessing that a collection representation type `Repr` has
 *  elements of type `A` and has a conversion to `GenTraversableOnce[L, A]`.
 *
 *  This type enables simple enrichment of `GenTraversableOnce`s with extension
 *  methods which can make full use of the mechanics of the Scala collections
 *  framework in their implementation.
 *
 *  Example usage,
 * {{{
 *    class FilterMapImpl[A, Repr](val r: GenTraversableOnce[L, A]) {
 *      final def filterMap[B, That](f: A => Option[B])(implicit cbf: CanBuildFrom[L, Repr, B, That]): That = {
 *        val b = cbf()
 *        for(e <- r.seq) f(e) foreach (b +=)
 *        b.result
 *      }
 *    }
 *    implicit def filterMap[Repr, A](r: Repr)(implicit fr: IsTraversableOnce[L, Repr]): FilterMapImpl[fr.A,Repr] =
 *      new FilterMapImpl[fr.A, Repr](fr.conversion(r))
 *
 *    val l = List(1, 2, 3, 4, 5)
 *    List(1, 2, 3, 4, 5) filterMap (i => if(i % 2 == 0) Some(i) else None)
 *    // == List(2, 4)
 * }}}
 *
 * @author Miles Sabin
 * @author J. Suereth
 * @since 2.10
 */
trait IsTraversableOnce[L, Repr] {
  /** The type of elements we can traverse over. */
  type A
  /** A conversion from the representation type `Repr` to a `GenTraversableOnce[L, A]`. */
  val conversion: Repr => GenTraversableOnce[L, A]
}

object IsTraversableOnce {
  import scala.language.higherKinds

  implicit val stringRepr: IsTraversableOnce[L, String] { type A = Char } =
    new IsTraversableOnce[L, String] {
      type A = Char
      val conversion = implicitly[String => GenTraversableOnce[L, Char]]
    }

  implicit def genTraversableLikeRepr[C[_], A0](implicit conv: C[A0] => GenTraversableOnce[L, A0]): IsTraversableOnce[L, C[A0]] { type A = A0 } =
    new IsTraversableOnce[L, C[A0]] {
      type A = A0
      val conversion = conv
    }
}

