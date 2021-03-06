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
import mutable.{ Builder, ArrayBuffer }
import scala.annotation.migration
import scala.language.implicitConversions

trait ViewMkString[+A] {
  self: Traversable[A] =>

  // It is necessary to use thisSeq rather than toSeq to avoid cycles in the
  // eager evaluation of vals in transformed view subclasses, see #4558.
  protected[this] def thisSeq: Seq[A] = (new ArrayBuffer[A] ++= self).result()

  // Have to overload all three to work around #4299.  The overload
  // is because mkString should force a view but toString should not.
  override def mkString: String = mkString("")
  override def mkString(sep: String): String = mkString("", sep, "")
  override def mkString(start: String, sep: String, end: String): String = {
    thisSeq.addString(new StringBuilder(), start, sep, end).toString
  }
  override def addString(b: StringBuilder, start: String, sep: String, end: String): StringBuilder = {
    var first = true
    b append start
    for (x <- self) {
      if (first) first = false else b append sep
      b append x
    }
    b append end
    b
  }
}

/** A template trait for non-strict views of traversable collections.
 *  $traversableViewInfo
 *
 *  Implementation note: Methods such as `map` or `flatMap` on this view will not invoke the implicitly passed
 *  `Builder` factory, but will return a new view directly, to preserve by-name behavior.
 *  The new view is then cast to the factory's result type. This means that every `CanBuildFrom`
 *  that takes a `View` as its `From` type parameter must yield the same view (or a generic
 *  superclass of it) as its result parameter. If that assumption is broken, cast errors might result.
 *
 *  @define viewInfo
 *  A view is a lazy version of some collection. Collection transformers such as
 *  `map` or `filter` or `++` do not traverse any elements when applied on a view.
 *  Instead they create a new view which simply records that fact that the operation
 *  needs to be applied. The collection elements are accessed, and the view operations are applied,
 *  when a non-view result is needed, or when the `force` method is called on a view.
 *  @define traversableViewInfo
 *  $viewInfo
 *
 *  All views for traversable collections are defined by creating a new `foreach` method.
 *
 *  @author Martin Odersky
 *  @version 2.8
 *  @since   2.8
 *  @tparam A    the element type of the view
 *  @tparam Coll the type of the underlying collection containing the elements.
 *  @tparam This the type of the view itself
 */
trait TraversableViewLike[+A,
                          +Coll,
                          +This <: TraversableView[A, Coll] with TraversableViewLike[A, Coll, This]]
  extends Traversable[A] with TraversableLike[A, This] with ViewMkString[A]
{
  self =>

  type LT

  protected def underlying: Coll
  protected[this] def viewIdentifier: String = ""
  protected[this] def viewIdString: String = ""
  def viewToString = stringPrefix + viewIdString + "(...)"
  override def stringPrefix = "TraversableView"

  override protected[this] def newBuilder: Builder[A, This] =
    throw new UnsupportedOperationException(this+".newBuilder")

  def force[B >: A, That](implicit bf: CanBuildFrom[Coll, B, That]) = {
    val b = bf(underlying)
    b ++= this
    b.result()
  }

  /** Explicit instantiation of the `Transformed` trait to reduce class file size in subclasses. */
  private[collection] abstract class AbstractTransformed[+B] extends Traversable[B] with Transformed[B]


  /** The implementation base trait of this view.
   *  This trait and all its subtraits has to be re-implemented for each
   *  ViewLike class.
   */
  trait Transformed[+B] extends TraversableView[B, Coll] {
    def foreach[U](@local f: B => U)(implicit @local mct: MaybeCanThrow = mct): Unit

    lazy val underlying = self.underlying
    final override protected[this] def viewIdString = self.viewIdString + viewIdentifier

    // Methods whose standard implementations use "isEmpty" need to be rewritten
    // for views, else they will end up traversing twice in a situation like:
    //   xs.view.flatMap(f).headOption
    override def headOption(implicit @local mct: MaybeCanThrow = mct): Option[B] = {
      for (x <- this)
        return Some(x)

      None
    }
    override def lastOption(implicit @local mct: MaybeCanThrow = mct): Option[B] = {
      // (Should be) better than allocating a Some for every element.
      var empty = true
      var result: B = null.asInstanceOf[B]
      for (x <- this) {
        empty = false
        result = x
      }
      if (empty) None else Some(result)
    }

    // XXX: As yet not dealt with, tail and init both call isEmpty.
    override def stringPrefix = self.stringPrefix
    override def toString = viewToString
  }

  trait EmptyView extends Transformed[Nothing] {
    final override def isEmpty(implicit @local mct: MaybeCanThrow = mct) = true
    final override def foreach[U](@local f: Nothing => U)(implicit @local mct: MaybeCanThrow = mct): Unit = ()
  }

  /** A fall back which forces everything into a vector and then applies an operation
   *  on it. Used for those operations which do not naturally lend themselves to a view
   */
  trait Forced[B] extends Transformed[B] {
    protected[this] val forced: GenSeq[B]
    def foreach[U](@local f: B => U)(implicit @local mct: MaybeCanThrow) = ESC.TRY { cc => forced.foreach(f)(cc) } // XXX(leo) not sure how to properly fix this
    final override protected[this] def viewIdentifier = "C"
  }

  trait Sliced extends Transformed[A] {
    protected[this] val endpoints: SliceInterval
    protected[this] def from  = endpoints.from
    protected[this] def until = endpoints.until
    // protected def newSliced(_endpoints: SliceInterval): Transformed[A] =
    //   self.newSliced(endpoints.recalculate(_endpoints))

    def foreach[U](@local f: A => U)(implicit @local mct: MaybeCanThrow = mct) {
      var index = 0
      for (x <- self) {
        if (from <= index) {
          if (until <= index) return
          f(x)
        }
        index += 1
      }
    }
    final override protected[this] def viewIdentifier = "S"
  }

  trait Mapped[B] extends Transformed[B] {
    protected[this] val mapping: A => B
    def foreach[U](@local f: B => U)(implicit @local mct: MaybeCanThrow = mct) {
      for (x <- self)
        f(mapping(x))
    }
    final override protected[this] def viewIdentifier = "M"
  }

  trait FlatMapped[B] extends Transformed[B] {
    protected[this] val mapping: A => GenTraversableOnce[B]
    def foreach[U](@local f: B => U)(implicit @local mct: MaybeCanThrow = mct) {
      for (x <- self)
        for (y <- mapping(x).seq)
          f(y)
    }
    final override protected[this] def viewIdentifier = "N"
  }

  trait Appended[B >: A] extends Transformed[B] {
    protected[this] val rest: GenTraversable[B]
    def foreach[U](@local f: B => U)(implicit @local mct: MaybeCanThrow) { @local implicit val mcc = new CanThrow {}; // XXX(leo) not sure how to properly fix this
      self foreach f
      rest foreach f
    }
    final override protected[this] def viewIdentifier = "A"
  }

  trait Filtered extends Transformed[A] {
    protected[this] val pred: A => Boolean
    def foreach[U](@local f: A => U)(implicit @local mct: MaybeCanThrow = mct) {
      for (x <- self)
        if (pred(x)) f(x)
    }
    final override protected[this] def viewIdentifier = "F"
  }

  trait TakenWhile extends Transformed[A] {
    protected[this] val pred: A => Boolean
    def foreach[U](@local f: A => U)(implicit @local mct: MaybeCanThrow = mct) {
      for (x <- self) {
        if (!pred(x)) return
        f(x)
      }
    }
    final override protected[this] def viewIdentifier = "T"
  }

  trait DroppedWhile extends Transformed[A] {
    protected[this] val pred: A => Boolean
    def foreach[U](@local f: A => U)(implicit @local mct: MaybeCanThrow = mct) {
      var go = false
      for (x <- self) {
        if (!go && !pred(x)) go = true
        if (go) f(x)
      }
    }
    final override protected[this] def viewIdentifier = "D"
  }

  override def ++[B >: A, That](xs: GenTraversableOnce[B])(implicit bf: CanBuildFrom[This, B, That], @local mct: MaybeCanThrow): That = {
    newAppended(xs.seq.toTraversable).asInstanceOf[That]
// was:    if (bf.isInstanceOf[ByPassCanBuildFrom]) newAppended(that).asInstanceOf[That]
//         else super.++[B, That](that)(bf)
  }

  override def map[B, That](f: A => B)(implicit bf: CanBuildFrom[This, B, That], @local mct: MaybeCanThrow = mct): That = {
    newMapped(f).asInstanceOf[That]
//    val b = bf(repr)
//          if (b.isInstanceOf[NoBuilder[_]]) newMapped(f).asInstanceOf[That]
//    else super.map[B, That](f)(bf)
  }

  override def collect[B, That](pf: PartialFunction[A, B])(implicit bf: CanBuildFrom[This, B, That], @local mct: MaybeCanThrow = mct): That =
    filter(pf.isDefinedAt).map(pf)(bf)

  override def flatMap[B, That](f: A => GenTraversableOnce[B])(implicit bf: CanBuildFrom[This, B, That], @local mct: MaybeCanThrow = mct): That = {
    newFlatMapped(f).asInstanceOf[That]
// was:    val b = bf(repr)
//     if (b.isInstanceOf[NoBuilder[_]]) newFlatMapped(f).asInstanceOf[That]
//    else super.flatMap[B, That](f)(bf)
  }
  override def flatten[B](implicit asTraversable: A => /*<:<!!!*/ GenTraversableOnce[B]) =
    newFlatMapped(asTraversable)
  private[this] implicit def asThis(xs: Transformed[A]): This = xs.asInstanceOf[This]

  /** Boilerplate method, to override in each subclass
   *  This method could be eliminated if Scala had virtual classes
   */
  protected def newForced[B](xs: => GenSeq[B]): Transformed[B] = new { val forced = xs } with AbstractTransformed[B] with Forced[B]
  protected def newAppended[B >: A](that: GenTraversable[B]): Transformed[B] = new { val rest = that } with AbstractTransformed[B] with Appended[B]
  protected def newMapped[B](f: A => B): Transformed[B] = new { val mapping = f } with AbstractTransformed[B] with Mapped[B]
  protected def newFlatMapped[B](f: A => GenTraversableOnce[B]): Transformed[B] = new { val mapping = f } with AbstractTransformed[B] with FlatMapped[B]
  protected def newFiltered(@local p: A => Boolean): Transformed[A] = new { val pred = p } with AbstractTransformed[A] with Filtered
  protected def newSliced(_endpoints: SliceInterval): Transformed[A] = new { val endpoints = _endpoints } with AbstractTransformed[A] with Sliced
  protected def newDroppedWhile(@plocal p: A => Boolean): Transformed[A] = new { val pred = p } with AbstractTransformed[A] with DroppedWhile
  protected def newTakenWhile(@plocal p: A => Boolean): Transformed[A] = new { val pred = p } with AbstractTransformed[A] with TakenWhile

  protected def newTaken(n: Int): Transformed[A] = newSliced(SliceInterval(0, n))
  protected def newDropped(n: Int): Transformed[A] = newSliced(SliceInterval(n, Int.MaxValue))

  override def filter(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): This = newFiltered(p)
  override def withFilter(@local p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): This = newFiltered(p)
  override def partition(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): (This, This) = (newFiltered(p), newFiltered(!p(_)))
  override def init(implicit @local mct: MaybeCanThrow = mct): This = newSliced(SliceInterval(0, size - 1)) // !!! can't call size here.
  override def drop(n: Int)(implicit @local mct: MaybeCanThrow = mct): This = newDropped(n)
  override def take(n: Int)(implicit @local mct: MaybeCanThrow = mct): This = newTaken(n)
  override def slice(from: Int, until: Int)(implicit @local mct: MaybeCanThrow = mct): This = newSliced(SliceInterval(from, until))
  override def dropWhile(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): This = newDroppedWhile(p)
  override def takeWhile(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): This = newTakenWhile(p)
  override def span(@plocal p: A => Boolean)(implicit @local mct: MaybeCanThrow = mct): (This, This) = (newTakenWhile(p), newDroppedWhile(p))
  override def splitAt(n: Int)(implicit @local mct: MaybeCanThrow = mct): (This, This) = (newTaken(n), newDropped(n))

  override def scanLeft[B, That](z: B)(op: (B, A) => B)(implicit bf: CanBuildFrom[This, B, That]): That =
    newForced(thisSeq.scanLeft(z)(op)).asInstanceOf[That]

  @migration("The behavior of `scanRight` has changed. The previous behavior can be reproduced with scanRight.reverse.", "2.9.0")
  override def scanRight[B, That](z: B)(op: (A, B) => B)(implicit bf: CanBuildFrom[This, B, That]): That =
    newForced(thisSeq.scanRight(z)(op)).asInstanceOf[That]

  override def groupBy[K](f: A => K)(implicit @local mct: MaybeCanThrow = mct): immutable.Map[K, This] =
    thisSeq groupBy f mapValues (xs => newForced(xs))

  override def unzip[A1, A2](implicit asPair: A => (A1, A2)) =
    (newMapped(x => asPair(x)._1), newMapped(x => asPair(x)._2))  // TODO - Performance improvements.

  override def unzip3[A1, A2, A3](implicit asTriple: A => (A1, A2, A3)) =
    (newMapped(x => asTriple(x)._1), newMapped(x => asTriple(x)._2), newMapped(x => asTriple(x)._3))  // TODO - Performance improvements.

  override def filterNot(@local p: (A) => Boolean)(implicit @local mct: MaybeCanThrow = mct): This =
    newFiltered(a => !(p(a)))

  override def inits: Iterator[This] =
    thisSeq.inits.map(as => newForced(as).asInstanceOf[This])

  override def tails: Iterator[This] =
    thisSeq.tails.map(as => newForced(as).asInstanceOf[This])

  override def tail(implicit @local mct: MaybeCanThrow = mct): This =
    // super.tail would also work as it is currently implemented in terms of drop(Int).
    if (isEmpty) super.tail else newDropped(1)

  override def toString = viewToString
}
