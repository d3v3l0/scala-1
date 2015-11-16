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

/** Buffers are used to create sequences of elements incrementally by
 *  appending, prepending, or inserting new elements. It is also
 *  possible to access and modify elements in a random access fashion
 *  via the index of the element in the current sequence.
 *
 *  @author Matthias Zenger
 *  @author Martin Odersky
 *  @version 2.8
 *  @since   1
 *
 *  @tparam A    type of the elements contained in this buffer.
 *
 *  @define Coll `Buffer`
 *  @define coll buffer
 */
trait Buffer[L, A] extends Seq[L, A]
                   with GenericTraversableTemplate[L, A, Buffer]
                   with BufferLike[L, A, Buffer[L, A]]
                   with scala.Cloneable {
  override def companion: GenericCompanion[L, Buffer] = Buffer
}

/** $factoryInfo
 *  @define coll buffer
 *  @define Coll `Buffer`
 */
object Buffer extends SeqFactory[L, Buffer] {
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, Buffer[L, A]] = ReusableCBF.asInstanceOf[GenericCanBuildFrom[A]]
  def newBuilder[A]: Builder[L, A, Buffer[L, A]] = new ArrayBuffer
}

/** Explicit instantiation of the `Buffer` trait to reduce class file size in subclasses. */
abstract class AbstractBuffer[L, A] extends AbstractSeq[L, A] with Buffer[L, A]
