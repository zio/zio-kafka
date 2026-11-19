---
id: preventing-duplicates
title: "Prevent Duplicates After a Rebalance"
sidebar_label: "Prevent Duplicates After a Rebalance"
---

In this guide we configure a zio-kafka consumer so that a rebalance does not make another consumer process the same records again. We enable rebalance-safe commits, size the timeouts so that the consumer can keep its promise, and then cover the two cases where offsets are not committed to Kafka directly: commits to an external database and commits through a transactional producer.

## The Problem

A rebalance moves partitions from one consumer of a group to another, for example when an instance of your service starts or stops. The consumer that takes over a partition continues from the last committed offset. In zio-kafka, processing runs asynchronously from partition management: when a partition is revoked, its stream may still be processing records, or committing their offsets. Every record whose offset is not committed when the new owner starts is processed a second time, possibly at the same time on both consumers.

Committing more often makes the window smaller but does not close it. Since version 2.7.1, zio-kafka can close it: with *rebalance-safe commits*, the consumer holds up the rebalance until the offsets of all records it consumed from the revoked partitions are committed.

## Prerequisites

This guide assumes a consumer that commits offsets, as described in [Consume Topics with ZIO Streams](consuming-kafka-topics-using-zio-streams.md), and a group id. Rebalance-safe commits need Kafka's classic group protocol, which is the default; see [The Consumer Group Protocol](consumer-settings.md#the-consumer-group-protocol).

## Enable Rebalance-Safe Commits

Rebalance-safe commits are off by default. To turn them on, together with the two timeouts they depend on:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.ConsumerSettings

val consumerSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("group")
    .withRebalanceSafeCommits(true)       // hold up rebalances until commits are done
    .withMaxRebalanceDuration(30.seconds) // default: 3/5 of max.poll.interval.ms
    .withCommitTimeout(15.seconds)        // default: 15 seconds
```

During a rebalance, the consumer now waits for the streams of revoked partitions to process their records and commit the offsets, for at most the max rebalance duration. Once the commits are done, another consumer can safely take over the partitions. While the rebalance waits, no stream of this consumer receives new records, not even the streams of partitions that are not revoked.

When rebalance-safe commits are on and you do not set `partition.assignment.strategy`, zio-kafka configures Kafka's `RangeAssignor` (since zio-kafka 3.2.0), which revokes all partitions in every rebalance. If your consumer handles fewer than 10 times the minimum record rate computed in the next section, keep the `RangeAssignor` and do not switch to a cooperative assignor; [PR 1576](https://github.com/zio/zio-kafka/pull/1576) explains why.

## Size the Timeouts

The rebalance can only wait so long, so your program must process and commit a chunk of records within a fixed budget. The wait starts with the rebalance, and in the worst case a full chunk (up to `max.poll.records` records) has just been pushed into the stream at that moment. Zio-kafka reserves the commit timeout for the final commit, so the time left to process the chunk is the max rebalance duration minus the commit timeout.

With the defaults, `max.poll.interval.ms` is 5 minutes, so the max rebalance duration is 3/5 of that, 3 minutes. Minus the 15-second commit timeout, your program has 2 minutes and 45 seconds to process and commit 500 records, about 3 records per second. **This is a hard threshold.** Leave plenty of head-room for busy services, network delays, garbage collection and CPU contention.

For a slow consumer, lower `max.poll.records` and raise `max.poll.interval.ms` and the max rebalance duration. The max rebalance duration is only useful between the commit timeout and `max.poll.interval.ms`: a lower value ends the wait at once, and a higher value makes Kafka consider the consumer dead and reassign its partitions. To give a consumer that takes several seconds per record enough time:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.ConsumerSettings

val slowConsumerSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("group")
    .withRebalanceSafeCommits(true)
    .withMaxPollRecords(1)
    .withMaxPollInterval(15.minutes)
    .withMaxRebalanceDuration(4.minutes)
    .withCommitTimeout(10.seconds)
```

[Tune the Consumer](consumer-tuning.md#long-running-processes--slow-consumers) explains how `max.poll.records` and `max.poll.interval.ms` relate to slow processing in general.

## Commit to an External System

When you store offsets in an external system, such as a relational database, instead of committing them to Kafka, the consumer cannot see those commits and would wait in vain during a rebalance. Report every external commit with `Consumer#registerExternalCommits` (since zio-kafka 2.10.0), and let the consumer resume from the stored offsets with `OffsetRetrieval.External` (see [Partition Assignment and Offset Retrieval](partition-assignment-and-offset-retrieval.md#consumer-starting-offsets--offset-retrieval)).

The following program stores the highest next offset per partition once per chunk, registers the same batch with the consumer, and resumes from the stored offsets after a rebalance:

```scala mdoc:compile-only
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer._
import zio.kafka.consumer.Consumer.{ OffsetEpoch, OffsetRetrieval }
import zio.kafka.serde.Serde

import scala.jdk.OptionConverters._

trait OffsetStore {
  def save(partition: TopicPartition, nextOffset: OffsetEpoch): Task[Unit]
  def load(partitions: Set[TopicPartition]): Task[Map[TopicPartition, OffsetEpoch]]
}

def settings(store: OffsetStore): ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("my-group")
    .withRebalanceSafeCommits(true)
    .withOffsetRetrieval(OffsetRetrieval.External(store.load))

def consumeWithExternalCommits(consumer: Consumer, store: OffsetStore): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("my-topic"), Serde.string, Serde.string)
    .mapChunksZIO { records =>
      // Process the records here, then store the highest next offset per partition.
      val batch = OffsetBatch(records.map(_.offset))
      ZIO.foreachDiscard(batch.nextOffsets) { case (tp, next) =>
        store.save(tp, OffsetEpoch(next.offset(), next.leaderEpoch().toScala.map(_.intValue)))
      } *> consumer.registerExternalCommits(batch).as(records)
    }
    .runDrain
```

In a real program, save the offsets in the same database transaction as the processing results, so that results and progress never disagree.

### Choose Which Offset to Persist

Kafka commits the *next offset*, the offset from which a consumer continues, not the offset of the processed record; [Kafka Commits the Next Offset](offsets-and-committing.md#kafka-commits-the-next-offset) explains the difference. Decide which of the two you persist, and keep your `OffsetRetrieval.External` function consistent with it, or you introduce an off-by-one bug:

- To persist the next offset (recommended, and what the program above does), store `record.nextOffset.offset()` and its `leaderEpoch()`, and return the stored values as they are.
- To persist the consumed offset, store `record.offset.offset` and `record.offset.leaderEpoch`, and return the stored offset plus 1.

Storing the leader epoch is optional, but it prevents consuming from a broker that no longer leads the partition. `OffsetEpoch` takes it as an `Option[Int]`, `None` when it is unknown.

## Commit With a Transactional Producer

A [transactional producer](transactions.md) commits the consumed offsets as part of its transaction and registers them with the consumer for you, so no extra code is needed. It requires rebalance-safe commits, and the timing rules above apply to it as well.

## Verify the Result

To check that rebalances no longer cause duplicates, run two instances of your service and log the topic, partition and offset of every record each instance processes. Start the second instance while the first is busy, which triggers a rebalance, and then stop it again. With rebalance-safe commits, no partition and offset pair appears in the logs of both instances. With rebalance-safe commits off, a few pairs usually do.

The [diagnostics](diagnostics.md) events `DiagnosticEvent.Rebalance` and `DiagnosticEvent.Commit` show when each rebalance and commit happened, which helps when a rebalance still takes longer than expected.

## More Information

The Scaladoc of `ConsumerSettings#withRebalanceSafeCommits` and the description of [pull request #1098](https://github.com/zio/zio-kafka/pull/1098), which introduced this feature, have more background. The presentation [Making ZIO-Kafka Safer And Faster](https://www.youtube.com/watch?v=MJoRwEyyVxM) covers it from 10:24.
