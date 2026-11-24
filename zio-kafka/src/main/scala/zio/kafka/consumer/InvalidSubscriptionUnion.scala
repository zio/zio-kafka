package zio.kafka.consumer

import zio.Chunk

/**
 * The error that a subscription fails with when the new subscription cannot be combined with the subscriptions the
 * consumer already has. Pattern subscriptions combine only with pattern subscriptions, topic-list subscriptions only
 * with topic-list subscriptions, and manual partition assignments only with manual partition assignments.
 *
 * @param subscriptions
 *   the subscriptions that could not be combined, the new one first
 */
final case class InvalidSubscriptionUnion(subscriptions: Chunk[Subscription])
    extends RuntimeException(s"Unable to calculate union of subscriptions: ${subscriptions.mkString(",")}")
