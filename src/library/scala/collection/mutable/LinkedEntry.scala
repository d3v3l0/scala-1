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

/** Class for the linked hash map entry, used internally.
 *  @since 2.8
 */
final class LinkedEntry[L, A, B](val key: A, var value: B)
      extends HashEntry[A, LinkedEntry[L, A, B]] with Serializable {
  var earlier: LinkedEntry[L, A, B] = null
  var later: LinkedEntry[L, A, B] = null
}

