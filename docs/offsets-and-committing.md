---
id: offsets-and-committing
title: "Offsets and Committing"
---

Kafka remembers how far a consumer group has read each partition by storing a *committed offset* per partition. When a consumer restarts, or when a partition moves to another consumer during a rebalance, consumption continues from that committed offset. Zio-kafka never commits on its own (Kafka's `enable.auto.commit` is not supported, and `Consumer.make` fails when you set it to `true`), so your program decides when an offset is committed. This page is the reference for what an offset is in zio-kafka, how offsets are committed one by one or in batches, how failed commits are retried, and how commits relate to external storage and transactions.

The core types are `Offset`, the position of one consumed record, and `OffsetBatch`, the combined positions of many records, at most one per partition. You get an `Offset` from a `CommittableRecord`, the record type that the [consumer](consumer.md) streams emit:

```scala
final case class CommittableRecord[K, V](record: ConsumerRecord[K, V], nextOffset: OffsetAndMetadata, ...) {
  def offset: Offset
}

trait Offset {
  def offset: Long
  def nextOffset: OffsetAndMetadata
  def commit: Task[Unit]
  def batch: OffsetBatch
}

sealed trait OffsetBatch {
  def nextOffsets: Map[TopicPartition, OffsetAndMetadata]
  def commit: Task[Unit]
  def add(offset: Offset): OffsetBatch
  def merge(offsets: OffsetBatch): OffsetBatch
}
```

You rarely need to commit by hand when you use `Consumer.consumeWith`: it commits the offsets of processed records for you (see [Committing With consumeWith](#committing-with-consumewith)).

## How They Work Together

A consumer stream emits records, and each record carries the offset to commit once it has been processed. The typical flow is:

1. The consumer stream emits a `CommittableRecord` for every record it reads.
2. Your program processes the record and keeps its `Offset` (`CommittableRecord#offset`).
3. Offsets are collected into an `OffsetBatch`, which keeps only the highest next offset per partition. The `Consumer.collectOffsets` sink does this for a stream of offsets.
4. Your program calls `OffsetBatch#commit` (or `Offset#commit` for a single record). The commit goes through the consumer to the Kafka brokers.
5. After a restart or rebalance, a consumer of the same group starts from the committed offsets.

The diagram shows how the types relate:

```
 CommittableRecord ──offset──> Offset ──add──> OffsetBatch ──commit──> Kafka broker
        │                        │                  ▲                (committed offsets
        └─ nextOffset            ├─ commit          │                 per partition)
                                 └─ batch ──────────┘
                                                    merge
                                        OffsetBatch ─────> OffsetBatch
```

## Kafka Commits the Next Offset

In Kafka you never commit the offset of the record you processed. You commit the *next offset*: the offset from which a consumer should continue reading. If you processed the record at offset 42, you commit 43.

Most of the time the next offset is the record's offset plus one. It can be larger when a partition contains *control records*. A transactional producer writes such a control record (a transaction marker) when it commits or aborts a transaction. Control records occupy an offset but are never delivered to your program. In the diagram, the record at offset 44 is the last record that a poll returned for partition 0, and it is followed by a control record at offset 45:

```
  offset          40     41     42     43     44     45     46
               ┌──────┬──────┬──────┬──────┬──────┬──────┬──────┐
  partition 0  │  r   │  r   │  r   │  r   │  r   │ ctrl │  r   │
               └──────┴──────┴──────┴──────┴──────┴──────┴──────┘

  record at 42:  Offset#offset = 42   Offset#nextOffset.offset() = 43
  record at 44:  Offset#offset = 44   Offset#nextOffset.offset() = 46  (last record of its poll; skips the control record)
```

Zio-kafka gives you both values, so you don't have to compute the next offset yourself. `Offset#offset` (a `Long`) is the offset of the consumed record. `Offset#nextOffset` (a Kafka `OffsetAndMetadata`) is the offset to commit; it also carries the leader epoch and, optionally, metadata. The same value is available directly on the record as `CommittableRecord#nextOffset`.

For most records `Offset#nextOffset` is `Offset#offset + 1`. For the last record of each partition in a poll, zio-kafka uses the position that the Kafka consumer reports, which already skips any control records that follow the record. As a result, committing the offsets of all processed records moves the committed offset past the control records, and the consumer lag of a transactionally written topic can reach 0.

:::note[Changed in zio-kafka 4.0.0]
`Offset#nextOffset` and `CommittableRecord#nextOffset` were added in zio-kafka 4.0.0, when commits started to skip control records. In the same release `OffsetBatch#offsets` was replaced by `OffsetBatch#nextOffsets`, whose values are already the next offsets to commit. See [Migrating to zio-kafka 4](migrating-to-zio-kafka-4.md).
:::

## Offset

`Offset` is the position of one consumed record. You get it from `CommittableRecord#offset`; there is no public constructor. Besides the record's position, an `Offset` knows how to commit itself through the consumer that read the record:

```scala
trait Offset {
  def topic: String
  def partition: Int
  lazy val topicPartition: TopicPartition
  def leaderEpoch: java.util.Optional[Integer]

  def offset: Long
  def nextOffset: OffsetAndMetadata

  def commit: Task[Unit]
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit]

  def batch: OffsetBatch
  def withMetadata(metadata: String): Offset
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]
}
```

The members fall into four groups:

- **Position:** `Offset#topic`, `Offset#partition` and `Offset#topicPartition` identify the partition. `Offset#offset` is the consumed offset, `Offset#nextOffset` the offset to commit, and `Offset#leaderEpoch` the leader epoch stored with the next offset.
- **Committing:** `Offset#commit` commits `Offset#nextOffset` for this record's partition. `Offset#commitOrRetry` does the same and retries retriable failures (see [Retrying Commits](#retrying-commits)).
- **Batching:** `Offset#batch` turns the offset into an `OffsetBatch` with one entry.
- **Metadata:** `Offset#withMetadata` returns a copy whose next offset carries the given metadata string (see [Committing Metadata](#committing-metadata)). `Offset#consumerGroupMetadata` is the consumer group metadata that a `TransactionalProducer` needs to commit offsets as part of a transaction.

To commit after every record, call `Offset#commit` once the record is processed:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

def process(key: String, value: String): Task[Unit] = ZIO.logInfo(s"$key -> $value")

def consumeAndCommitEach(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("my-topic"), Serde.string, Serde.string)
    .mapZIOChunked(record => process(record.key, record.value) *> record.offset.commit)
    .runDrain
```

Committing every record is simple but slow: each commit is a round trip to the broker, and a high commit rate puts a heavy load on the brokers. Unless your topic sees only a few records per second, commit in batches instead.

## OffsetBatch

`OffsetBatch` holds the next offsets of many records, at most one per partition. When an offset is added for a partition that already has one, the batch keeps the highest next offset. Committing a batch therefore commits the progress of every record that went into it, with a single commit request:

```scala
sealed trait OffsetBatch {
  def nextOffsets: Map[TopicPartition, OffsetAndMetadata]
  def commit: Task[Unit]
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit]
  def add(offset: Offset): OffsetBatch
  def merge(offsets: OffsetBatch): OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]
}

object OffsetBatch {
  val empty: OffsetBatch
  def apply(offset: Offset): OffsetBatch
  def apply(offsets: Iterable[Offset]): OffsetBatch
}

case object EmptyOffsetBatch extends OffsetBatch
```

### Creating Offset Batches

There are three ways to start a batch. `OffsetBatch.empty` is the batch without offsets. It is the `EmptyOffsetBatch` object, whose `EmptyOffsetBatch.commit` does nothing. `OffsetBatch.apply(offset)` creates a batch from one offset (the same as `Offset#batch`), and `OffsetBatch.apply(offsets)` creates a batch from a collection of offsets.

To build one batch from the records of a chunk:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._

def batchOf(records: Chunk[CommittableRecord[String, String]]): OffsetBatch =
  OffsetBatch(records.map(_.offset))
```

### Combining and Committing

Batches grow with `OffsetBatch#add`, which adds one `Offset`, and `OffsetBatch#merge`, which combines two batches. Both keep, per partition, the entry with the highest next offset, so the order in which offsets arrive doesn't matter. `OffsetBatch#nextOffsets` returns the result as a map from partition to the `OffsetAndMetadata` that is committed.

`OffsetBatch#commit` commits all next offsets in one request, and `OffsetBatch#commitOrRetry` adds retries. `OffsetBatch#consumerGroupMetadata` is used by the `TransactionalProducer`, see [Transactional Commits](#transactional-commits).

## Committing in Streams

In a stream, you rarely build batches by hand. The `Consumer` object provides sinks that fold a stream of offsets or batches into one `OffsetBatch`:

```scala
object Consumer {
  val collectOffsets: ZSink[Any, Nothing, Offset, Nothing, OffsetBatch]
  val collectOffsetBatches: ZSink[Any, Nothing, OffsetBatch, Nothing, OffsetBatch]
}
```

`Consumer.collectOffsets` adds each `Offset` to a batch with `OffsetBatch#add`, and `Consumer.collectOffsetBatches` merges each incoming `OffsetBatch` with `OffsetBatch#merge`. `Consumer.offsetBatches` is an alias of `Consumer.collectOffsets`; both names are supported.

Combine these sinks with `ZStream#aggregateAsync` or `ZStream#aggregateAsyncWithin`. Both run the upstream on a separate fiber: while a commit is in progress, the offsets of newly processed records are collected into the next batch, so consuming and committing run in parallel. The end of such a stream looks like this:

```scala
  .map(_.offset)                            // ZStream[R, Throwable, Offset]
  .aggregateAsync(Consumer.collectOffsets)  // ZStream[R, Throwable, OffsetBatch]
  .mapZIO(_.commit)                         // ZStream[R, Throwable, Unit]
```

Use `Consumer.collectOffsets` when the stream carries one `Offset` per record, and `Consumer.collectOffsetBatches` when it already carries an `OffsetBatch` per chunk, for example after `ZStream#mapChunksZIO`. [Consume Topics with ZIO Streams](consuming-kafka-topics-using-zio-streams.md) shows complete programs for a plain and a partitioned stream and explains how to choose between `ZStream#aggregateAsync` and a commit interval with `ZStream#aggregateAsyncWithin`.

## Retrying Commits

A commit can fail. Some failures are temporary: Kafka signals them with a `RetriableCommitFailedException`, and zio-kafka fails a commit with `Consumer.CommitTimeout` when it does not complete within the commit timeout. `Offset#commitOrRetry` and `OffsetBatch#commitOrRetry` retry exactly these two failures, for as long as the given schedule allows. Any other failure, and the last failure once the schedule is exhausted, fails the effect.

To retry a batch commit up to three times with exponential back-off:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

def consumeWithRetriedCommits(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("my-topic"), Serde.string, Serde.string)
    .map(_.offset)
    .aggregateAsync(Consumer.collectOffsets)
    .mapZIO(_.commitOrRetry(Schedule.exponential(100.millis) && Schedule.recurs(3)))
    .runDrain
```

The commit timeout is set with `ConsumerSettings#withCommitTimeout` and defaults to 15 seconds (`ConsumerSettings.defaultCommitTimeout`). It must be larger than twice the poll timeout (`ConsumerSettings#withPollTimeout`); otherwise `Consumer.make` fails. A timed-out commit may still complete later.

## Committing With consumeWith

`Consumer.consumeWith` (and the `Consumer#consumeWith` instance method) runs your effect for each record and commits for you; see [consumeWith](consumer.md#consumewith) for how it processes records. After the effect for a record completes, the record's offset joins a batch: offsets are aggregated with `ZStream#aggregateAsync(Consumer.collectOffsets)` while the previous commit is still running, and each batch is committed with `OffsetBatch#commitOrRetry`.

The retry schedule is the `commitRetryPolicy` parameter, with the default `Schedule.exponential(1.second) && Schedule.recurs(3)`. To use a different policy:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

val settings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092")).withGroupId("my-group")

val program: Task[Unit] =
  Consumer.consumeWith(
    settings = settings,
    subscription = Subscription.topics("my-topic"),
    keyDeserializer = Serde.string,
    valueDeserializer = Serde.string,
    commitRetryPolicy = Schedule.spaced(500.millis) && Schedule.recurs(10)
  ) { record =>
    ZIO.logInfo(s"${record.key()} -> ${record.value()}")
  }
```

When a commit still fails after the retries, `Consumer.consumeWith` fails.

## Committing Metadata

Kafka stores an optional metadata string with each committed offset. Use it to record, for example, which service or version committed. `Offset#withMetadata` returns a copy of the offset whose next offset carries the metadata. When offsets are combined into a batch, the batch keeps the metadata of the offset it keeps for that partition, which is the one with the highest next offset.

`Consumer#committed` reads the committed offsets of a set of partitions back from the broker. A partition without a committed offset maps to `None`. To commit with metadata and read the metadata back:

```scala mdoc:compile-only
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

def commitWithMetadata(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("my-topic"), Serde.string, Serde.string)
    .map(_.offset.withMetadata("committed-by=my-service"))
    .aggregateAsync(Consumer.collectOffsets)
    .mapZIO(_.commit)
    .runDrain

def committedMetadata(consumer: Consumer): Task[Option[String]] = {
  val partition0 = new TopicPartition("my-topic", 0)
  consumer
    .committed(Set(partition0))
    .map(_.get(partition0).flatten.map(_.metadata()))
}
```

The offset that `Consumer#committed` returns is a next offset, the offset from which consumption continues.

## Delivery Semantics

When you commit an offset only after the record has been processed, as all examples on this page do, you get *at-least-once* processing: every record is processed, but some records may be processed more than once. Records whose processing finished but whose offset was not yet committed are consumed again after a crash, a restart or a rebalance. Committing *before* processing gives at-most-once processing instead: records whose processing was interrupted are lost.

Rebalances are a common source of duplicates, because a partition can move to another consumer while its last offsets are still being committed. Enable rebalance-safe commits with `ConsumerSettings#withRebalanceSafeCommits` to hold up a rebalance until the offsets of all consumed records are committed, for at most the duration set by `ConsumerSettings#withMaxRebalanceDuration`. See [Prevent Duplicates After a Rebalance](preventing-duplicates.md) for the details and the timing constraints. For exactly-once processing from Kafka to Kafka, use [transactions](transactions.md).

## External Commits

Instead of committing to Kafka, you can store offsets in an external system, for example in the same database transaction that stores the processing results. The database is then the source of truth for the consumer's progress. Two settings work with it: `OffsetRetrieval.External` makes a newly assigned partition start from the stored offset (see [Partition Assignment and Offset Retrieval](partition-assignment-and-offset-retrieval.md#consumer-starting-offsets--offset-retrieval)), and with rebalance-safe commits on, `Consumer#registerExternalCommits` tells the consumer which offsets you stored, so it does not wait in vain for commits to Kafka:

```scala
trait Consumer {
  def registerExternalCommits(offsetBatch: OffsetBatch): Task[Unit]
}
```

`Consumer#registerExternalCommits` only records the batch as committed inside the consumer; it does not commit anything to Kafka. Store the next offset (`Offset#nextOffset`) rather than the consumed offset, so that `OffsetRetrieval.External` can return the stored value as is. [Commit to an External System](preventing-duplicates.md#commit-to-an-external-system) shows a complete program that stores offsets, registers them and resumes from them.

## Transactional Commits

With a `TransactionalProducer`, consumed offsets are committed as part of a Kafka transaction, together with the records the transaction produces. You don't call `OffsetBatch#commit` yourself. Instead, you pass the offsets to the transaction: `Transaction#produce` and `Transaction#produceChunk` take an `offset: Option[Offset]`, and `Transaction#produceChunkBatch` takes an `OffsetBatch`. The offsets are committed when the transaction commits, using the consumer group metadata that `Offset#consumerGroupMetadata` and `OffsetBatch#consumerGroupMetadata` carry, and the producer registers them with the consumer as external commits. A transactional producer therefore requires rebalance-safe commits on its consumer. [Transactions](transactions.md) walks through a complete consume-transform-produce program.
