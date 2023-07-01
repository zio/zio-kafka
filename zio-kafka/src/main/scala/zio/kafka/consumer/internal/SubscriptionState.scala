package zio.kafka.consumer.internal

import zio.kafka.consumer.Subscription

private[internal] sealed trait SubscriptionState {
  def isSubscribed: Boolean =
    this match {
      case _: SubscriptionState.Subscribed => true
      case SubscriptionState.NotSubscribed => false
    }
}
private[internal] object SubscriptionState {
  case object NotSubscribed                                                          extends SubscriptionState
  final case class Subscribed(subscriptions: Set[Subscription], union: Subscription) extends SubscriptionState
}
