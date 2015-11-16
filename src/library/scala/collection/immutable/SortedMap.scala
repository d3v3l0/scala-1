/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2013, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala
package collection
package immutable

import generic._
import mutable.Builder
import scala.annotation.unchecked.uncheckedVariance

/** A map whose keys are sorted.
 *
 *  @tparam A     the type of the keys contained in this sorted map.
 *  @tparam B     the type of the values associated with the keys.
 *
 *  @author Sean McDirmid
 *  @author Martin Odersky
 *  @version 2.8
 *  @since   2.4
 *  @define Coll immutable.SortedMap
 *  @define coll immutable sorted map
 */
trait SortedMap[L, A, +B] extends Map[L, A, B]
                         with scala.collection.SortedMap[L, A, B]
                         with MapLike[L, A, B, SortedMap[L, A, B]]
                         with SortedMapLike[L, A, B, SortedMap[L, A, B]]
{
self =>

  override protected[this] def newBuilder : Builder[L, (A, B), SortedMap[L, A, B]] =
    SortedMap.newBuilder[A, B]

  override def empty: SortedMap[L, A, B] = SortedMap.empty
  override def updated [B1 >: B](key: A, value: B1): SortedMap[L, A, B1] = this + ((key, value))
  override def keySet: immutable.SortedSet[L, A] = new DefaultKeySortedSet

  protected class DefaultKeySortedSet extends super.DefaultKeySortedSet with immutable.SortedSet[L, A] {
    override def + (elem: A): SortedSet[L, A] =
      if (this(elem)) this
      else SortedSet[L, A]() ++ this + elem
    override def - (elem: A): SortedSet[L, A] =
      if (this(elem)) SortedSet[L, A]() ++ this - elem
      else this
    override def rangeImpl(from : Option[A], until : Option[A]) : SortedSet[L, A] = {
      val map = self.rangeImpl(from, until)
      new map.DefaultKeySortedSet
    }
  }

  /** Add a key/value pair to this map.
   *  @param    kv the key/value pair
   *  @return   A new map with the new binding added to this map
   *  @note     needs to be overridden in subclasses
   */
  def + [B1 >: B](kv: (A, B1)): SortedMap[L, A, B1] = throw new AbstractMethodError("SortedMap.+")

  /** Adds two or more elements to this collection and returns
   *  a new collection.
   *
   *  @param elem1 the first element to add.
   *  @param elem2 the second element to add.
   *  @param elems the remaining elements to add.
   */
  override def + [B1 >: B] (elem1: (A, B1), elem2: (A, B1), elems: (A, B1) *): SortedMap[L, A, B1] =
    this + elem1 + elem2 ++ elems

  /** Adds a number of elements provided by a traversable object
   *  and returns a new collection with the added elements.
   *
   *  @param xs     the traversable object.
   */
  override def ++[B1 >: B](xs: GenTraversableOnce[L, (A, B1)]): SortedMap[L, A, B1] =
    ((repr: SortedMap[L, A, B1]) /: xs.seq) (_ + _)

  override def filterKeys(p: A => Boolean): SortedMap[L, A, B] = new FilteredKeys(p) with SortedMap.Default[A, B] {
    implicit def ordering: Ordering[A] = self.ordering
    override def rangeImpl(from : Option[A], until : Option[A]): SortedMap[L, A, B] = self.rangeImpl(from, until).filterKeys(p)
    override def iteratorFrom(start: A) = self iteratorFrom start filter {case (k, _) => p(k)}
    override def keysIteratorFrom(start : A) = self keysIteratorFrom start filter p
    override def valuesIteratorFrom(start : A) = self iteratorFrom start collect {case (k,v) if p(k) => v}
  }

  override def mapValues[C](f: B => C): SortedMap[L, A, C] = new MappedValues(f) with SortedMap.Default[A, C] {
    implicit def ordering: Ordering[A] = self.ordering
    override def rangeImpl(from : Option[A], until : Option[A]): SortedMap[L, A, C] = self.rangeImpl(from, until).mapValues(f)
    override def iteratorFrom(start: A) = self iteratorFrom start map {case (k, v) => (k, f(v))}
    override def keysIteratorFrom(start : A) = self keysIteratorFrom start
    override def valuesIteratorFrom(start : A) = self valuesIteratorFrom start map f
  }

}

/** $factoryInfo
 *  @define Coll immutable.SortedMap
 *  @define coll immutable sorted map
 */
object SortedMap extends ImmutableSortedMapFactory[L, SortedMap] {
  /** $sortedMapCanBuildFromInfo */
  implicit def canBuildFrom[A, B](implicit ord: Ordering[A]): CanBuildFrom[L, Coll, (A, B), SortedMap[L, A, B]] = new SortedMapCanBuildFrom[A, B]
  def empty[A, B](implicit ord: Ordering[A]): SortedMap[L, A, B] = TreeMap.empty[A, B]

  private[collection] trait Default[A, +B] extends SortedMap[L, A, B] with scala.collection.SortedMap.Default[A, B] {
  self =>
    override def +[B1 >: B](kv: (A, B1)): SortedMap[L, A, B1] = {
      val b = SortedMap.newBuilder[A, B1]
      b ++= this
      b += ((kv._1, kv._2))
      b.result()
    }

    override def - (key: A): SortedMap[L, A, B] = {
      val b = newBuilder
      for (kv <- this; if kv._1 != key) b += kv
      b.result()
    }
  }
}
