---
id: sharing-consumer
title: "Share One Consumer Between Several Streams"
sidebar_label: "Share One Consumer Between Several Streams"
---

In this guide we consume several Kafka topics with one zio-kafka `Consumer`, giving each topic its own stream, its own key and value types, and its own processing. One consumer is easier to provide than one per topic, and it uses fewer resources: all streams share one Kafka consumer, its network connections to the brokers, and its membership in the consumer group.

## Decide Whether the Topics Can Share a Consumer

Each stream on a shared consumer has its own subscription and its own deserializers. All other settings are common to all streams, because they belong to the one Kafka consumer underneath. For example, the poll timeout, the offset retrieval strategy and `max.poll.records` apply to all subscriptions combined: `max.poll.records` limits the records of one poll over all topics together. If the topics need different settings, create one `Consumer` per topic instead.

All subscriptions on one consumer must also be of the same kind: all `Topics`, all `Pattern` or all `Manual`. A stream whose subscription is of a different kind fails. Every `Topics` subscription can be written as a `Pattern` subscription, so to mix the two, use `Pattern` for both.

## Run One Stream per Topic

The following program runs two streams on one consumer, each for a different topic and with different key and value types:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

// A single Consumer instance
val consumerSettings: ConsumerSettings = ConsumerSettings(List("localhost:9092")).withGroupId("group")
val consumer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped(Consumer.make(consumerSettings))

// Two streams, each for a different topic and key-value types
val stream1: ZIO[Consumer, Throwable, Unit] = ZIO.serviceWithZIO[Consumer] { consumer =>
  consumer
    .plainStream(Subscription.topics("topic1"), Serde.string, Serde.string)
    .mapZIOChunked(cr => ZIO.logInfo(s"For topic1, got key: ${cr.key}, value: ${cr.value}").as(cr.offset))
    .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
    .mapZIO(_.commit)
    .runDrain
}

val stream2: ZIO[Consumer, Throwable, Unit] = ZIO.serviceWithZIO[Consumer] { consumer =>
  consumer
    .plainStream(Subscription.topics("topic2"), Serde.uuid, Serde.int)
    .mapZIOChunked(cr => ZIO.logInfo(s"For topic2, got key: ${cr.key}, value: ${cr.value}").as(cr.offset))
    .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
    .mapZIO(_.commit)
    .runDrain
}

// Run both streams with the same consumer instance
val program: Task[Unit] = (stream1 zipPar stream2).provideLayer(consumer)
```

Zio-kafka subscribes the Kafka consumer to the union of both subscriptions and routes the records of each topic to the matching stream. Each stream has its own lifetime: when a stream completes, whether successfully, by failure or by interruption, its subscription is removed and the other stream continues. Avoid overlapping subscriptions, such as `Subscription.topics("topic1", "topic2")` and `Subscription.topics("topic2", "topic3")`: the records of `topic2` would be divided over both streams in a non-deterministic way. The [Consumer](consumer.md#multiple-subscriptions-on-one-consumer) reference page has the exact rules.

## Limit Duplicates When Streams Start and Stop

With `Topics` and `Pattern` subscriptions, starting or ending a stream changes the consumer's subscription, which can trigger a rebalance of the consumer group. With the classic group protocol and Kafka's default assignor, a rebalance revokes and reassigns all partitions of the consumer, including those of the _other_ streams. Those streams then continue from the last committed offset, so records whose offsets were not committed yet are processed again, just as in any other rebalance. To keep this to a minimum, commit frequently or enable rebalance-safe commits (see [Prevent Duplicates After a Rebalance](preventing-duplicates.md)).

## Verify the Result

Produce a few records to both topics while the program runs. Each record appears in the log of the stream for its topic, with the key and value types of that stream. `Consumer#subscription` returns the topics of both subscriptions while both streams run, and only those of the remaining stream after one completes.
