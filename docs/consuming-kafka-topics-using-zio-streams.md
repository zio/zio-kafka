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
