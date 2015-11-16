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
import mutable.Builder

/** A base trait for linear sequences.
 *
 *  $linearSeqInfo
 *
 *  @define  linearSeqInfo
 *  Linear sequences have reasonably efficient `head`, `tail`, and `isEmpty` methods.
 *  If these methods provide the fastest way to traverse the collection, a 
 *  collection `Coll` that extends this trait should also extend
 *  `LinearSeqOptimized[L, A, Coll[A]]`.
 */
trait LinearSeq[L, +A] extends Seq[L, A]
                            with GenericTraversableTemplate[L, A, LinearSeq]
                            with LinearSeqLike[L, A, LinearSeq[L, A]] {
  override def companion: GenericCompanion[L, LinearSeq] = LinearSeq
  override def seq: LinearSeq[L, A] = this
}

/** $factoryInfo
 *  The current default implementation of a $Coll is a `List`.
 *  @define coll linear sequence
 *  @define Coll `LinearSeq`
 */
object LinearSeq extends SeqFactory[L, LinearSeq] {
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, LinearSeq[L, A]] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  def newBuilder[A]: Builder[L, A, LinearSeq[L, A]] = immutable.LinearSeq.newBuilder[A]
}
