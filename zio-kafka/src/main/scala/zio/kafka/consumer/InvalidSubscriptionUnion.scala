package zio.kafka.consumer

import zio.Chunk

final case class InvalidSubscriptionUnion(subscriptions: Chunk[Subscription])
    extends RuntimeException(s"Unable to calculate union of subscriptions: ${subscriptions.mkString(",")}")
