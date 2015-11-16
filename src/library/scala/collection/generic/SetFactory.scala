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

import mutable.Builder
import scala.language.higherKinds

abstract class SetFactory[L, CC[X] <: Set[L, X] with SetLike[L, X, CC[X]]]
  extends GenSetFactory[L, CC] with GenericSeqCompanion[L, CC]
