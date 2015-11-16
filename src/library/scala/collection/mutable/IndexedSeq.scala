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

/** A subtrait of `collection.IndexedSeq` which represents sequences
 *  that can be mutated.
 *
 *  $indexedSeqInfo
 */
trait IndexedSeq[L, A] extends Seq[L, A]
                   with scala.collection.IndexedSeq[L, A]
                   with GenericTraversableTemplate[L, A, IndexedSeq]
                   with IndexedSeqLike[L, A, IndexedSeq[L, A]] {
  override def companion: GenericCompanion[L, IndexedSeq]  = IndexedSeq
  override def seq: IndexedSeq[L, A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is an `ArrayBuffer`.
 *  @define coll mutable indexed sequence
 *  @define Coll `mutable.IndexedSeq`
 */
object IndexedSeq extends SeqFactory[L, IndexedSeq] {
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, IndexedSeq[L, A]] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  def newBuilder[A]: Builder[L, A, IndexedSeq[L, A]] = new ArrayBuffer[L, A]
}
