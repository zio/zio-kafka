---
id: consuming-kafka-topics-using-zio-streams
title: "Consuming Kafka topics using ZIO Streams"
---

First, create a consumer using the ConsumerSettings instance:

```scala
import zio.*
import zio.kafka.consumer.{ Consumer, ConsumerSettings }

val consumerSettings: ConsumerSettings = ConsumerSettings(List("localhost:9092")).withGroupId("group")
val consumerScoped: ZIO[Scope, Throwable, Consumer] =
  Consumer.make(consumerSettings)
val consumer: ZLayer[Any, Throwable, Consumer] =
  ZLayer.scoped(consumerScoped)
```

The consumer returned from `Consumer.make` is wrapped in a `ZLayer`
to allow for easy composition with other ZIO environment components.
You may provide that layer to effects that require a consumer. Here's
an example:

```scala
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

val data: Task[Chunk[CommittableRecord[String, String]]] = 
  Consumer.plainStream(Subscription.topics("topic"), Serde.string, Serde.string).take(50).runCollect
    .provideSomeLayer(consumer)
```

You may stream data from Kafka using the `plainStream` method:

```scala
import zio.Console.printLine
import zio.kafka.consumer._

Consumer.plainStream(Subscription.topics("topic150"), Serde.string, Serde.string)
  .tap(cr => printLine(s"key: ${cr.record.key}, value: ${cr.record.value}"))
  .map(_.offset)
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
```

To process partitions assigned to the consumer in parallel, you may use the `Consumer#partitionedStream` method, which creates a nested stream of partitions:

```scala
import zio.Console.printLine
import zio.kafka.consumer._

Consumer.partitionedStream(Subscription.topics("topic150"), Serde.string, Serde.string)
  .flatMapPar(Int.MaxValue) { case (topicPartition, partitionStream) =>
    ZStream.fromZIO(printLine(s"Starting stream for topic '${topicPartition.topic}' partition ${topicPartition.partition}")) *>
      partitionStream
        .tap(record => printLine(s"key: ${record.key}, value: ${record.value}")) // Replace with a custom message handling effect
        .map(_.offset)
  }
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
```

## Controlled shutdown

The examples above will keep processing records forever, or until the fiber is interrupted, typically at application shutdown. When interrupted, some records may be 'in-flight', e.g. being processed by one of the stages of your consumer stream user code. Those records will not be processed fully and their offsets may not be committed. For fast shutdown in an at-least-once processing scenario this is fine. 

zio-kafka also supports a _graceful shutdown_, where the fetching of records for the subscribed topics/partitions is stopped, the streams are ended and all downstream stages are completed, allowing in-flight records to be fully processed.

Use the `*withControl` variants of `plainStream`, `partitionedStream` and `partitionedAssignmentStream` for this purpose. These return a ZIO producing a `SubscriptionStreamControl` and requiring a `Scope`. When provided with a `Scope`, e.g. using `ZIO.scoped { .. }`, the consumer remains subscribed to the given topics for the lifetime of the `Scope`. Commits are possible for as long as the consumer is subscribed. The graceful shutdown as described above may be initiated by calling `SubscriptionStreamControl#stop`.

```scala
import zio.Console.printLine
import zio.kafka.consumer._

ZIO.scoped {
  for {
    streamControl <- Consumer.partitionedStreamWithControl(
      Subscription.topics("topic150"), 
      Serde.string, 
      Serde.string)
    stream = streamControl.stream.flatMapPar(Int.MaxValue) { case (topicPartition, partitionStream) =>
        ZStream.fromZIO(
          printLine(s"Starting stream for topic '${topicPartition.topic}' partition ${topicPartition.partition}")
        ) *>
          partitionStream
            .tap(record => printLine(s"key: ${record.key}, value: ${record.value}")) // Replace with a custom message handling effect
            .map(_.offset)
      }
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .runDrain
    streamFiber <- stream.forkScoped
    _ <- ZIO.sleep(10.seconds) *> streamControl.stop
    _ <- streamFiber.join
  } yield ()
}
```

In most use cases you don't need to explicitly control when to shutdown the stream, but you simply want a graceful shutdown on interruption when the application terminates. The `Consumer#runWithGracefulShutdown` method implements this pattern and can be used as follows:

```scala
import zio.Console.printLine
import zio.kafka.consumer._

Consumer.runWithGracefulShutdown(Consumer.partitionedStreamWithControl(Subscription.topics("topic150"), Serde.string, Serde.string)) { stream =>
stream.flatMapPar(Int.MaxValue) { case (topicPartition, partitionStream) =>
    partitionStream
      .tap(record => printLine(s"key: ${record.key}, value: ${record.value}"))
      .map(_.offset)
  }
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
}
```