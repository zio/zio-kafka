package zio.kafka.consumer

final case class InvalidSubscriptionUnion(subscriptions: Seq[Subscription])
    extends Exception(s"Unable to calculate union of subscriptions: ${subscriptions.mkString(",")}")
