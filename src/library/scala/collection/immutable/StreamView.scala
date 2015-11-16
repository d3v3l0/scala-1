package scala
package collection
package immutable

trait StreamView[L, +A, +Coll] extends StreamViewLike[L, A, Coll, StreamView[L, A, Coll]] { }
