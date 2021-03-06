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
import mutable.ArrayBuffer
import scala.annotation.tailrec

/** A template trait for indexed sequences of type `IndexedSeq[A]` which optimizes
 *  the implementation of several methods under the assumption of fast random access.
 *
 *  $indexedSeqInfo
 *
 *  @define willNotTerminateInf
 *  @define mayNotTerminateInf
 */
trait IndexedSeqOptimized[+A, +Repr] extends Any with IndexedSeqLike[A, Repr] { self =>

  type LT


  override /*IterableLike*/
  def isEmpty(implicit @local mct: MaybeCanThrow = mct): Boolean = { length == 0 }

  override /*IterableLike*/
  def foreach[U](@local f: A => U)(implicit @local mct: MaybeCanThrow = mct): Unit = {
    var i = 0
    val len = length
    while (i < len) { f(this(i)); i += 1 }
  }

  private def prefixLengthImpl(@plocal p: A => Boolean, expectTrue: Boolean): Int = {
    var i = 0
    while (i < length && p(apply(i)) == expectTrue) i += 1
    i
  }

  override /*IterableLike*/
  def forall(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): Boolean = prefixLengthImpl(p, expectTrue = true) == length

  override /*IterableLike*/
  def exists(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): Boolean = prefixLengthImpl(p, expectTrue = false) != length

  override /*IterableLike*/
  def find(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): Option[A] = {
    @plocal val q = (x: A) => !p(x)
    val i = prefixLength(q)
    if (i < length) Some(this(i)) else None
  }

  @tailrec
  private def foldl[B](start: Int, end: Int, z: B, @local op: (B, A) => B): B =
    if (start == end) z
    else foldl(start + 1, end, op(z, this(start)), op)

  @tailrec
  private def foldr[B](start: Int, end: Int, z: B, op: (A, B) => B): B =
    if (start == end) z
    else foldr(start, end - 1, op(this(end - 1), z), op)

  override /*TraversableLike*/
  def foldLeft[B](z: B)(@local op: (B, A) => B): B =
    foldl(0, length, z, op)

  override /*IterableLike*/
  def foldRight[B](z: B)(op: (A, B) => B): B =
    foldr(0, length, z, op)

  override /*TraversableLike*/
  def reduceLeft[B >: A](@plocal op: (B, A) => B): B =
    if (length > 0) foldl(1, length, this(0), op) else super.reduceLeft(op)

  override /*IterableLike*/
  def reduceRight[B >: A](op: (A, B) => B): B =
    if (length > 0) foldr(0, length - 1, this(length - 1), op) else super.reduceRight(op)

  override /*IterableLike*/
  def zip[A1 >: A, B, That](that: GenIterable[B])(implicit bf: CanBuildFrom[Repr, (A1, B), That], @local mct: MaybeCanThrow = mct): That = that match {
    case that: IndexedSeq[_] =>
      val b = bf(repr)
      var i = 0
      val len = this.length min that.length
      b.sizeHint(len)
      while (i < len) {
        b += ((this(i), that(i).asInstanceOf[B]))
        i += 1
      }
      b.result()
    case _ =>
      super.zip[A1, B, That](that)(bf)
  }

  override /*IterableLike*/
  def zipWithIndex[A1 >: A, That](implicit bf: CanBuildFrom[Repr, (A1, Int), That], @local mct: MaybeCanThrow = mct): That = {
    val b = bf(repr)
    val len = length
    b.sizeHint(len)
    var i = 0
    while (i < len) {
      b += ((this(i), i))
      i += 1
    }
    b.result()
  }

  override /*IterableLike*/
  def slice(from: Int, until: Int)(implicit @local mct: MaybeCanThrow = mct): Repr = {
    val lo    = math.max(from, 0)
    val hi    = math.min(math.max(until, 0), length)
    val elems = math.max(hi - lo, 0)
    val b     = newBuilder
    b.sizeHint(elems)

    var i = lo
    while (i < hi) {
      b += self(i)
      i += 1
    }
    b.result()
  }

  override /*IterableLike*/
  def head: A = if (isEmpty) super.head else this(0)

  override /*TraversableLike*/
  def tail(implicit @local mct: MaybeCanThrow = mct): Repr = if (isEmpty) super.tail else slice(1, length)

  override /*TraversableLike*/
  def last: A = if (length > 0) this(length - 1) else super.last

  override /*IterableLike*/
  def init(implicit @local mct: MaybeCanThrow = mct): Repr = if (length > 0) slice(0, length - 1) else super.init

  override /*TraversableLike*/
  def take(n: Int)(implicit @local mct: MaybeCanThrow = mct): Repr = slice(0, n)

  override /*TraversableLike*/
  def drop(n: Int)(implicit @local mct: MaybeCanThrow = mct): Repr = slice(n, length)

  override /*IterableLike*/
  def takeRight(n: Int): Repr = slice(length - math.max(n, 0), length)

  override /*IterableLike*/
  def dropRight(n: Int): Repr = slice(0, length - math.max(n, 0))

  override /*TraversableLike*/
  def splitAt(n: Int)(implicit @local mct: MaybeCanThrow = mct): (Repr, Repr) = (take(n), drop(n))

  override /*IterableLike*/
  def takeWhile(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): Repr = take(prefixLength(p))

  override /*TraversableLike*/
  def dropWhile(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): Repr = drop(prefixLength(p))

  override /*TraversableLike*/
  def span(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): (Repr, Repr) = splitAt(prefixLength(p))

  override /*IterableLike*/
  def sameElements[B >: A](that: GenIterable[B])(implicit @local mct: MaybeCanThrow = mct): Boolean = that match {
    case that: IndexedSeq[_] =>
      val len = length
      len == that.length && {
        var i = 0
        while (i < len && this(i) == that(i)) i += 1
        i == len
      }
    case _ =>
      super.sameElements(that)
  }

  override /*IterableLike*/
  def copyToArray[B >: A](xs: Array[B], start: Int, len: Int)(implicit @local mct: MaybeCanThrow = mct) {
    var i = 0
    var j = start
    val end = length min len min (xs.length - start)
    while (i < end) {
      xs(j) = this(i)
      i += 1
      j += 1
    }
  }

  // Overridden methods from Seq

  override /*SeqLike*/
  def lengthCompare(len: Int): Int = length - len

  override /*SeqLike*/
  def segmentLength(@plocal p: A => Boolean, from: Int)(implicit @local mct: MaybeCanThrow = mct): Int = {
    val len = length
    var i = from
    while (i < len && p(this(i))) i += 1
    i - from
  }

  private def negLength(n: Int) = if (n >= length) -1 else n

  override /*SeqLike*/
  def indexWhere(p: A => Boolean, from: Int)(implicit @local mct: MaybeCanThrow = mct): Int = {
    val start = from max 0
    negLength(start + segmentLength(!p(_), start))
  }

  override /*SeqLike*/
  def lastIndexWhere(p: A => Boolean, end: Int)(implicit @local mct: MaybeCanThrow = mct): Int = {
    var i = math.min(end, length - 1)
    while (i >= 0 && !p(this(i))) i -= 1
    i
  }

  override /*SeqLike*/
  def reverse(implicit @local mct: MaybeCanThrow = mct): Repr = {
    val b = newBuilder
    b.sizeHint(length)
    var i = length
    while (0 < i) {
      i -= 1
      b += this(i)
    }
    b.result()
  }

  override /*SeqLike*/
  def reverseIterator: Iterator[A] = new AbstractIterator[A] {
    private var i = self.length
    def hasNext: Boolean = 0 < i
    @local def next(): A =
      if (0 < i) {
        i -= 1
        self(i)
      } else Iterator.empty.next()
  }

  override /*SeqLike*/
  def startsWith[B](that: GenSeq[B], offset: Int)(implicit @local mct: MaybeCanThrow = mct): Boolean = that match {
    case that: IndexedSeq[_] =>
      var i = offset
      var j = 0
      val thisLen = length
      val thatLen = that.length
      while (i < thisLen && j < thatLen && this(i) == that(j)) {
        i += 1
        j += 1
      }
      j == thatLen
    case _ =>
      var i = offset
      val thisLen = length
      val thatElems = that.iterator
      while (i < thisLen && thatElems.hasNext) {
        if (this(i) != thatElems.next())
          return false

        i += 1
      }
      !thatElems.hasNext
  }

  override /*SeqLike*/
  def endsWith[B](that: GenSeq[B])(implicit @local mct: MaybeCanThrow = mct): Boolean = that match {
    case that: IndexedSeq[_] =>
      var i = length - 1
      var j = that.length - 1

      (j <= i) && {
        while (j >= 0) {
          if (this(i) != that(j))
            return false
          i -= 1
          j -= 1
        }
        true
      }
    case _ =>
      super.endsWith(that)
  }
}
