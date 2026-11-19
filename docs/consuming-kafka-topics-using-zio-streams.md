---
id: consuming-kafka-topics-using-zio-streams
title: "Consume Topics with ZIO Streams"
sidebar_label: "Consume Topics with ZIO Streams"
---

In this guide we consume a Kafka topic as a ZIO stream, process every record, and commit the offsets in batches so that a restarted consumer continues where it left off. We start with a single stream for all partitions, then process partitions in parallel, and finally stop the stream gracefully at shutdown.

You need a `Consumer`; [Create a Consumer](creating-a-consumer.md) shows how to make one. The [Consumer](consumer.md) reference page describes every stream method.

## Consume and Commit a Plain Stream

`Consumer#plainStream` emits the records of all assigned partitions in one stream. To process each record and commit the offsets of processed records in batches:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

def process(key: String, value: String): Task[Unit] =
  ZIO.logInfo(s"key: $key, value: $value")

def consume(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("topic150"), Serde.string, Serde.string)        // (1)
    .mapZIOChunked(record => process(record.key, record.value).as(record.offset))  // (2)
    .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))      // (3)
    .mapZIO(_.commit)                                                              // (4)
    .runDrain
```

(1) The subscription names the topics to consume. There are also subscriptions for a topic name pattern and for a fixed set of partitions (see [Partition Assignment and Offset Retrieval](partition-assignment-and-offset-retrieval.md)). The deserializers turn the key and value bytes into a `String` each.

(2) Each `CommittableRecord` is processed, and the stream continues with the record's `Offset`. `ZStream#mapZIOChunked` keeps the stream's chunks intact; `ZStream#mapZIO` and `ZStream#tap` would split them into single records and lower the throughput considerably (see [Keep Stream Chunks Intact](avoiding-chunk-breakers.md)).

(3) `Consumer.collectOffsets` collects the offsets into an `OffsetBatch`, which keeps the highest next offset (the offset to resume from) per partition. `ZStream#aggregateAsyncWithin` runs the upstream on a separate fiber, so consuming and committing run in parallel, and emits a batch at most every 100 milliseconds.

(4) All offsets in the `OffsetBatch` are committed. As soon as the commit is done, the next batch is pulled from upstream.

When processing works better on a whole chunk at once, for example to write all records of a poll to a database in one statement, use `ZStream#mapChunksZIO` and emit the offsets of the chunk:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

def processAll(records: Chunk[CommittableRecord[String, String]]): Task[Unit] =
  ZIO.logInfo(s"Processing ${records.size} records")

def consumeChunks(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("topic150"), Serde.string, Serde.string)
    .mapChunksZIO(records => processAll(records).as(records.map(_.offset)))
    .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
    .mapZIO(_.commit)
    .runDrain
```

## Choose the Commit Rate

The schedule of `ZStream#aggregateAsyncWithin` sets how often a commit starts. Choose an interval that is longer than a typical commit in your system. For example, with an interval of 2 ms and commits that take 10 ms, `ZStream#aggregateAsyncWithin` emits a batch 2 ms after the first records arrive, and the commit starts. Another 2 ms later it tries to emit the next batch, but it has to wait because the commit is still running. While it waits, it stops accepting offsets from upstream, which halts the consumer until the commit completes.

There are two alternatives, each with its own trade-off:

- For the lowest commit latency, commit continuously: replace step (3) with `ZStream#aggregateAsync(Consumer.collectOffsets)`, which starts a new commit as soon as the previous one completes. Committing is expensive for the Kafka brokers, so with dozens of consumers committing continuously the load may slow everything down.
- For applications that see only a few records per second, drop steps (3) and (4) and commit each record with `Offset#commit` after processing it. The commit then runs on the same fiber as the processing, which lowers throughput, so this is almost never the best choice for busier topics.

[Offsets and Committing](offsets-and-committing.md) describes the commit API, including retries and commit metadata.

## Consuming With More Parallelism

To process the partitions assigned to the consumer in parallel, use `Consumer#partitionedStream`, which emits one stream per partition:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._
import zio.stream._

def process(key: String, value: String): Task[Unit] =
  ZIO.logInfo(s"key: $key, value: $value")

def consumePartitions(consumer: Consumer): Task[Unit] =
  consumer
    .partitionedStream(Subscription.topics("topic150"), Serde.string, Serde.string)
    .flatMapPar(Int.MaxValue) { case (topicPartition, partitionStream) => // (1)
      ZStream.logInfo(s"Starting stream for $topicPartition") *>
        partitionStream
          .mapZIOChunked(record => process(record.key, record.value).as(record.offset)) // (2)
    }
    .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis)) // (3)
    .mapZIO(_.commit)
    .runDrain
```

Each partition is processed on its own fiber, while a single fiber commits.

(1) Set the parallelism of `ZStream#flatMapPar` to `Int.MaxValue`, or at least to the number of partitions the consumer can be assigned. Otherwise some partition streams are never pulled. Once such a stream has buffered records and has not been pulled for the max stream pull interval (by default `max.poll.interval.ms`), zio-kafka fails the stream and shuts down the consumer. Every time a partition is assigned to the consumer, a new partition stream starts and the body of `ZStream#flatMapPar` runs for it.

(2) The records of one partition are processed in order, as in the plain stream.

(3) The offsets of all partition streams are merged into one stream of `Offset`s, so only one fiber commits. They are batched and committed as in the plain stream.

When each partition stream processes whole chunks, it can emit one `OffsetBatch` per chunk; combine those with `Consumer.collectOffsetBatches` instead of `Consumer.collectOffsets`:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

def processChunk(records: Chunk[CommittableRecord[String, String]]): Task[Unit] =
  ZIO.logInfo(s"Processing ${records.size} records")

def consumePartitionChunks(consumer: Consumer): Task[Unit] =
  consumer
    .partitionedStream(Subscription.topics("topic150"), Serde.string, Serde.string)
    .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
      partitionStream.mapChunksZIO { records =>
        processChunk(records).as(Chunk.single(OffsetBatch(records.map(_.offset))))
      }
    }
    .aggregateAsyncWithin(Consumer.collectOffsetBatches, Schedule.fixed(100.millis))
    .mapZIO(_.commit)
    .runDrain
```

## Controlled Shutdown (Experimental)

The examples above process records until the fiber is interrupted, typically at application shutdown. At that moment some records may be in flight in one of the stream's stages: they are partly processed, and their offsets may not be committed. In an at-least-once setup this is fine, because those records are consumed again after the restart.

For a graceful shutdown instead, zio-kafka can stop fetching records, end the streams and let all downstream stages finish, so that in-flight records are fully processed and committed. Use the `*WithControl` variants of the stream methods (`Consumer#plainStreamWithControl`, `Consumer#partitionedStreamWithControl` and `Consumer#partitionedAssignmentStreamWithControl`). They return a `StreamControl`, which gives access to the stream and lets you end it.

`Consumer.runWithGracefulShutdown` takes a `StreamControl` and ends the stream gracefully when the fiber is interrupted, waiting at most `shutdownTimeout` for processing to finish. To run a partitioned stream that shuts down gracefully with the application:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

def process(key: String, value: String): Task[Unit] =
  ZIO.logInfo(s"key: $key, value: $value")

def consumeUntilShutdown(consumer: Consumer): Task[Unit] =
  ZIO.scoped {
    for {
      control <- consumer.partitionedStreamWithControl(
                   Subscription.topics("topic150"),
                   Serde.string,
                   Serde.string
                 )
      _ <- Consumer.runWithGracefulShutdown(control, shutdownTimeout = 30.seconds) { stream =>
             stream
               .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
                 partitionStream.mapZIOChunked(record => process(record.key, record.value).as(record.offset))
               }
               .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
               .mapZIO(_.commit)
               .runDrain
           }
    } yield ()
  }
```

To decide yourself when the stream ends, run `StreamControl#stream` and call `StreamControl#end` when your condition is met. `StreamControl#end` stops fetching records; the stream then completes after the records already fetched are processed:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

def process(key: String, value: String): Task[Unit] =
  ZIO.logInfo(s"key: $key, value: $value")

def consumeUntil(consumer: Consumer, stopSignal: Promise[Nothing, Unit]): Task[Unit] =
  ZIO.scoped {
    for {
      control <- consumer.partitionedStreamWithControl(
                   Subscription.topics("topic150"),
                   Serde.string,
                   Serde.string
                 )
      fiber <- control.stream
                 .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
                   partitionStream.mapZIOChunked(record => process(record.key, record.value).as(record.offset))
                 }
                 .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
                 .mapZIO(_.commit)
                 .runDrain
                 .forkScoped
      _ <- stopSignal.await // wait until some condition is met
      _ <- control.end      // stop fetching more records
      _ <- fiber.join       // wait until the stream has processed and committed the fetched records
    } yield ()
  }
```

These methods are experimental: their API may change in incompatible ways in any zio-kafka version. The [Consumer](consumer.md#streams-with-control) reference page describes their exact behavior.

## Verify the Result

To check that offsets are committed, run the program, produce some records to the topic, and look at the consumer group with Kafka's command-line tool:

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group <your-group-id>
```

The `CURRENT-OFFSET` column moves forward while the program runs, and the `LAG` column stays close to 0 when the consumer keeps up. After a restart, the program continues from `CURRENT-OFFSET` instead of reprocessing the topic from the start.
