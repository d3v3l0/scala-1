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


/** `Publisher[L, A,This]` objects publish events of type `A`
 *  to all registered subscribers. When subscribing, a subscriber may specify
 *  a filter which can be used to constrain the number of events sent to the
 *  subscriber. Subscribers may suspend their subscription, or reactivate a
 *  suspended subscription. Class `Publisher` is typically used
 *  as a mixin. The abstract type `Pub` models the type of the publisher itself.
 *
 *  @tparam Evt      type of the published event.
 *
 *  @author  Matthias Zenger
 *  @author  Martin Odersky
 *  @version 2.8
 *  @since   1
 */
trait Publisher[L, Evt] {

  type Pub <: Publisher[L, Evt]
  type Sub = Subscriber[L, Evt, Pub]
  type Filter = Evt => Boolean

  /** The publisher itself of type `Pub`. Implemented by a cast from `this` here.
   *  Needs to be overridden if the actual publisher is different from `this`.
   */
  protected val self: Pub = this.asInstanceOf[Pub]

  private val filters = new HashMap[L, Sub, Set[L, Filter]] with MultiMap[L, Sub, Filter]
  private val suspended = new HashSet[L, Sub]

  def subscribe(sub: Sub) { subscribe(sub, event => true) }
  def subscribe(sub: Sub, filter: Filter) { filters.addBinding(sub, filter) }
  def suspendSubscription(sub: Sub) { suspended += sub }
  def activateSubscription(sub: Sub) { suspended -= sub }
  def removeSubscription(sub: Sub) { filters -= sub }
  def removeSubscriptions() { filters.clear() }

  protected def publish(event: Evt) {
    filters.keys.foreach(sub =>
      if (!suspended.contains(sub) &&
          filters.entryExists(sub, p => p(event)))
        sub.notify(self, event)
    )
  }

  /** Checks if two publishers are structurally identical.
   *
   *  @return true, iff both publishers contain the same sequence of elements.
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: Publisher[L, _] => filters == that.filters && suspended == that.suspended
    case _                  => false
  }
}
