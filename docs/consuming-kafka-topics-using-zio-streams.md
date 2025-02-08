---
id: consuming-kafka-topics-using-zio-streams
title: "Consuming Kafka topics using ZIO Streams"
---

You can stream data from Kafka using the `plainStream` method:

```scala
import zio.Console.printLine
import zio.kafka.consumer._

consumer
  .plainStream(Subscription.topics("topic150"), Serde.string, Serde.string) // (1)
  .tap(cr => printLine(s"key: ${cr.record.key}, value: ${cr.record.value}")) // (2)
  .map(_.offset) // (3)
  .aggregateAsync(Consumer.offsetBatches) // (4)
  .mapZIO(_.commit) // (5)
  .runDrain
```

(1) There are several types of subscriptions: a set of topics, a topic name pattern, or fully specified set of
topic-partitions (see [subscriptions](partition-assignment-and-offset-retrieval.md)).

(2) The `plainStream` method returns a stream of `CommittableRecord` objects. In this line, we access the key
and value from the `CommittableRecord` and print them to the console.

:::caution
Some stream operators (like `tap` and `mapZIO`) break the chunking structure of the stream which heavily reduces
throughput. See [a warning about mapZIO](serialization-and-deserialization.md#a-warning-about-mapzio) for more
information and alternatives.
:::

(3) Here we get the offset of the record, so we now have a stream of offsets.

(4) The offsets are aggregated into an `OffsetBatch`. The offset batch keeps the highest seen offset per partition.

(5) All offsets in the `OffsetBatch` are committed. As soon as the commit is done, the next offset batch is pulled
from the stream. Because the aggregation in (4) is asynchronous, this application is continuously committing while
concurrently processing records.

## Consuming with more parallelism

To process partitions (assigned to the consumer) in parallel, you may use the `partitionedStream` method, which creates
a nested stream of partitions:

```scala
import zio.Console.printLine
import zio.kafka.consumer._

consumer
  .partitionedStream(Subscription.topics("topic150"), Serde.string, Serde.string)
  .flatMapPar(Int.MaxValue) { case (topicPartition, partitionStream) => // (1)
    ZStream.fromZIO(
      printLine(s"Starting stream for topic '${topicPartition.topic}' partition ${topicPartition.partition}")
    ) *>
      partitionStream
        .tap(record => printLine(s"key: ${record.key}, value: ${record.value}")) // (2)
        .map(_.offset) // (3)
  }
  .aggregateAsync(Consumer.offsetBatches) // (4)
  .mapZIO(_.commit)
  .runDrain
```

(1) When using partitionedStream with `flatMapPar(n)`, it is recommended to set n to `Int.MaxValue`. N must be equal to
or greater than the number of partitions your consumer subscribes to otherwise there will be unhandled partitions and
Kafka eventually evicts your consumer.

Each time a new partition is assigned to the consumer, a new partition stream is created and the body of the
`flatMapPar` is executed.

(2) As before, here we process the received `CommittableRecord`.

(3) and (4) Note how the offsets from all the sub-streams are merged into a single stream of `OffsetBatch`es. This
ensures only a single stream is doing commits.

In this example, each partition is processed in parallel, on separate fibers, while a single fiber is doing commits.
