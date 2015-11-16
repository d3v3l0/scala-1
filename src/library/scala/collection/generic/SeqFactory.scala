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
import scala.language.higherKinds

/** A template for companion objects of Seq and subclasses thereof.
 *
 *  @since 2.8
 */
abstract class SeqFactory[L, CC[X] <: Seq[L, X] with GenericTraversableTemplate[L, X, CC]]
extends GenSeqFactory[L, CC] with TraversableFactory[L, CC] {

  /** This method is called in a pattern match { case Seq(...) => }.
   *
   *  @param x the selector value
   *  @return  sequence wrapped in an option, if this is a Seq, otherwise none
   */
  def unapplySeq[A](x: CC[A]): Some[CC[A]] = Some(x)

}

