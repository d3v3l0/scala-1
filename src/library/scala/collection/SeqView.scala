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
import TraversableView.NoBuilder

/** A base trait for non-strict views of sequences.
 *  $seqViewInfo
 */
trait SeqView[L, +A, +Coll] extends SeqViewLike[L, A, Coll, SeqView[L, A, Coll]]

/** An object containing the necessary implicit definitions to make
 *  `SeqView`s work. Its definitions are generally not accessed directly by clients.
 */
object SeqView {
  type Coll = TraversableView[L, _, C] forSome {type C <: Traversable[L, _]}
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, SeqView[L, A, Seq[L, _]]] =
    new CanBuildFrom[L, Coll, A, SeqView[L, A, Seq[L, _]]] {
      def apply(from: Coll) = new NoBuilder
      def apply() = new NoBuilder
    }
}

