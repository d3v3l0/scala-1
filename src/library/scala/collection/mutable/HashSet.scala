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
import scala.collection.parallel.mutable.ParHashSet

/** This class implements mutable sets using a hashtable.
 *
 *  @author  Matthias Zenger
 *  @author  Martin Odersky
 *  @version 2.0, 31/12/2006
 *  @since   1
 *  @see [[http://docs.scala-lang.org/overviews/collections/concrete-mutable-collection-classes.html#hash_tables "Scala's Collection Library overview"]]
 *  section on `Hash Tables` for more information.
 *
 *  @define Coll `mutable.HashSet`
 *  @define coll mutable hash set
 *  @define thatinfo the class of the returned collection. In the standard library configuration,
 *    `That` is always `HashSet[L, B]` because an implicit of type `CanBuildFrom[L, HashSet, B, HashSet[L, B]]`
 *    is defined in object `HashSet`.
 *  @define bfinfo an implicit value of class `CanBuildFrom` which determines the
 *    result class `That` from the current representation type `Repr`
 *    and the new element type `B`. This is usually the `canBuildFrom` value
 *    defined in object `HashSet`.
 *  @define mayNotTerminateInf
 *  @define willNotTerminateInf
 */
@SerialVersionUID(1L)
class HashSet[L, A] private[collection] (contents: FlatHashTable.Contents[A])
extends AbstractSet[L, A]
   with Set[L, A]
   with GenericSetTemplate[L, A, HashSet]
   with SetLike[L, A, HashSet[L, A]]
   with FlatHashTable[L, A]
   with CustomParallelizable[L, A, ParHashSet[L, A]]
   with Serializable
{
  initWithContents(contents)

  override protected type plocal = local[LT]

  def this() = this(null)

  override def companion: GenericCompanion[L, HashSet] = HashSet

  override def size: Int = tableSize

  def contains(elem: A): Boolean = containsElem(elem)

  def += (elem: A): this.type = { addElem(elem); this }

  def -= (elem: A): this.type = { removeElem(elem); this }

  override def par = new ParHashSet(hashTableContents)

  override def add(elem: A): Boolean = addElem(elem)

  override def remove(elem: A): Boolean = removeElem(elem)

  override def clear() { clearTable() }

  override def iterator: Iterator[L, A] = super[FlatHashTable].iterator

  override def foreach[U](f: A =>  U) {
    var i = 0
    val len = table.length
    while (i < len) {
      val curEntry = table(i)
      if (curEntry ne null) f(entryToElem(curEntry))
      i += 1
    }
  }

  override def clone() = new HashSet[L, A] ++= this

  private def writeObject(s: java.io.ObjectOutputStream) {
    ESC.TRY(cc=>serializeTo(s)(cc))
  }

  private def readObject(in: java.io.ObjectInputStream) {
    ESC.TRY(cc=>init(in, x => ())(cc))
  }

  /** Toggles whether a size map is used to track hash map statistics.
   */
  def useSizeMap(t: Boolean) = if (t) {
    if (!isSizeMapDefined) sizeMapInitAndRebuild()
  } else sizeMapDisable()

}

/** $factoryInfo
 *  @define Coll `mutable.HashSet`
 *  @define coll mutable hash set
 */
object HashSet extends MutableSetFactory[L, HashSet] {
  implicit def canBuildFrom[A]: CanBuildFrom[L, Coll, A, HashSet[L, A]] = setCanBuildFrom[A]
  override def empty[A]: HashSet[L, A] = new HashSet[L, A]
}
