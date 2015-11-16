/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala
package collection

import generic._
import mutable.Builder

/** A base trait for indexed sequences.
 *  $indexedSeqInfo
 */
trait IndexedSeq[L, +A] extends Seq[L, A]
                    with GenericTraversableTemplate[L, A, IndexedSeq]
                    with IndexedSeqLike[L, A, IndexedSeq[L, A]] {
  override def companion: GenericCompanion[L, IndexedSeq] = IndexedSeq
  override def seq: IndexedSeq[L, A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `Vector`.
 *  @define coll indexed sequence
 *  @define Coll `IndexedSeq`
 */
object IndexedSeq extends IndexedSeqFactory[L, IndexedSeq] {
  // A single CBF which can be checked against to identify
  // an indexed collection type.
  override val ReusableCBF: GenericCanBuildFrom[Nothing] = new GenericCanBuildFrom[Nothing] {
    override def apply() = newBuilder[Nothing]
  }
  def newBuilder[A]: Builder[L, A, IndexedSeq[L, A]] = immutable.IndexedSeq.newBuilder[A]
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, IndexedSeq[L, A]] =
    ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
}
