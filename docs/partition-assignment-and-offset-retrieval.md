---
id: partition-assignment-and-offset-retrieval
title: "Partition Assignment and Offset Retrieval"
---

Two decisions shape what a zio-kafka consumer reads: which partitions it gets, set by the `Subscription` of each stream, and where it starts reading a newly assigned partition, set by the `OffsetRetrieval` strategy in `ConsumerSettings`. This page is the reference for both.

## Consumer Partition Assignment

A `Subscription` tells the consumer which Kafka topics and partitions a stream reads. There are three kinds:

| Use case                                           | Method                                                  |
| -------------------------------------------------- | ------------------------------------------------------- |
| One or more topics, automatic partition assignment | `Subscription.topics("my_topic", "other_topic")`        |
| Topics matching a pattern                          | `Subscription.pattern("topic.*".r)`                     |
| Manual partition assignment                        | `Subscription.manual("my_topic" -> 1, "my_topic" -> 2)` |

With `Topics` and `Pattern` subscriptions, Kafka's consumer group protocol divides the partitions over the consumers of the group and rebalances them when consumers come and go. A `Manual` subscription reads exactly the given partitions, without a consumer group rebalance; the example `Subscription.manual("my_topic" -> 1, "my_topic" -> 2)` reads partitions `1` and `2` of topic `my_topic`.

`Subscription` is a sealed trait with one case class per kind. Its companion object has a constructor for each way to describe a subscription:

```scala
sealed trait Subscription

object Subscription {
  final case class Topics(topics: Set[String])                  extends Subscription
  final case class Pattern(pattern: Regex)                      extends Subscription
  final case class Manual(topicPartitions: Set[TopicPartition]) extends Subscription

  def topics(name: String, names: String*): Subscription
  def pattern(pattern: Regex): Subscription
  def pattern(pattern: java.util.regex.Pattern): Subscription
  def pattern(pattern: String): Task[Subscription]
  def manual(topicPartitions: (String, Int)*): Manual
  def manual(topic: String, partition: Int): Manual
}
```

`Subscription.pattern` accepts a Scala `Regex` or a `java.util.regex.Pattern`. The overload that takes a `String`, `Subscription.pattern("topic.*")`, returns a `Task[Subscription]` that fails when the pattern is not a valid regular expression. `Subscription.manual(topic, partition)` is a shorthand for a single partition. One consumer can run several streams with different subscriptions of the same kind; see [Multiple Subscriptions on One Consumer](consumer.md#multiple-subscriptions-on-one-consumer).

## Consumer Starting Offsets / Offset Retrieval

By default zio-kafka starts streaming a partition from the last committed offset of the consumer group, or from the latest offset of the partition when the group has not committed one yet.

You can also store offsets outside of Kafka. This is useful when the offset must stay consistent with external data. For example, you can store the offset in a transactional database together with the data derived from the record at that offset. Kafka recommends storing the `leaderEpoch` together with the offset, although it is optional: it prevents consuming from a broker that does not know it is no longer the leader. See [Guide to Consumer Offsets](https://www.confluent.io/blog/guide-to-consumer-offsets/#part-ii-in-depth-analysis-and-insights) for why the leader epoch matters.

| Use case                                                          | `OffsetRetrieval` method                                            |
| ----------------------------------------------------------------- | ------------------------------------------------------------------- |
| Offsets in Kafka, start at latest record if no offset committed   | `Auto()`                                                            |
| Offsets in Kafka, start at earliest record if no offset committed | `Auto(AutoOffsetStrategy.Earliest)`                                 |
| Offsets in Kafka, fail if no offset committed                     | `Auto(AutoOffsetStrategy.None)`                                     |
| External offset storage                                           | `External(getOffsets, defaultStrategy = AutoOffsetStrategy.Latest)` |

`OffsetRetrieval`, `AutoOffsetStrategy` and `OffsetEpoch` are defined in the `Consumer` companion object; bring them into scope with `import zio.kafka.consumer.Consumer._`:

```scala
object Consumer {
  sealed trait OffsetRetrieval
  object OffsetRetrieval {
    final case class Auto(reset: AutoOffsetStrategy = AutoOffsetStrategy.Latest) extends OffsetRetrieval
    final case class External(
      getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, OffsetEpoch]],
      defaultStrategy: AutoOffsetStrategy = AutoOffsetStrategy.Latest
    ) extends OffsetRetrieval
  }

  sealed trait AutoOffsetStrategy
  object AutoOffsetStrategy {
    case object Earliest extends AutoOffsetStrategy
    case object Latest   extends AutoOffsetStrategy
    case object None     extends AutoOffsetStrategy
  }

  final case class OffsetEpoch(offset: Long, leaderEpoch: Option[Int])
}
```

For external offset retrieval, the `getOffsets` function is called with the topic-partitions that are newly assigned to the consumer, either by Kafka's rebalancing or by a manual subscription. For each partition, it returns an `OffsetEpoch`: the offset to continue from, and its leader epoch, or `None` when the epoch is unknown. When the returned map has no entry for a topic-partition, consuming continues from the last committed offset. When the returned offset is out of range, or there is no committed offset to fall back to, `defaultStrategy` decides where consuming starts.

Offset retrieval is configured with `ConsumerSettings#withOffsetRetrieval`, which also sets Kafka's `auto.offset.reset` property. To use Kafka 4's `by_duration` reset strategy, which `AutoOffsetStrategy` cannot express, see [Starting Offsets and Isolation](consumer-settings.md#starting-offsets-and-isolation).

:::note[Deprecated: `OffsetRetrieval.Manual`]
`OffsetRetrieval.Manual` is the deprecated (since zio-kafka 3.8.0) predecessor of `OffsetRetrieval.External`: its `getOffsets` function returns a plain `Long` offset without a leader epoch. `ConsumerSettings#withOffsetRetrieval` converts it to `External`; when you build settings without that method, convert it yourself with `OffsetRetrieval.Manual#toExternal`, because `Consumer.make` rejects a `Manual` strategy.
:::

:::caution
To prevent off-by-one bugs, decide whether you store the consumed offset or the next offset, and make `getOffsets` return the offset to continue from. See [Choose Which Offset to Persist](preventing-duplicates.md#choose-which-offset-to-persist).
:::
