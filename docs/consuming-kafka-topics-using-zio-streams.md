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
val consumer: ZLayer[Clock, Throwable, Consumer] =
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

val data: RIO[Clock, 
              Chunk[CommittableRecord[String, String]]] = 
  (Consumer.subscribe(Subscription.topics("topic")) *>
  Consumer.plainStream(Serde.string, Serde.string).take(50).runCollect)
    .provideSomeLayer(consumer)
```

You may stream data from Kafka using the `subscribeAnd` and `plainStream`
methods:

```scala
import zio.Clock, zio.Console.printLine
import zio.kafka.consumer._

Consumer.subscribeAnd(Subscription.topics("topic150"))
  .plainStream(Serde.string, Serde.string)
  .tap(cr => printLine(s"key: ${cr.record.key}, value: ${cr.record.value}"))
  .map(_.offset)
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
```

If you need to distinguish between the different partitions assigned
to the consumer, you may use the `Consumer#partitionedStream` method,
which creates a nested stream of partitions:

```scala
import zio.Clock, zio.Console.printLine
import zio.kafka.consumer._

Consumer.subscribeAnd(Subscription.topics("topic150"))
  .partitionedStream(Serde.string, Serde.string)
  .tap(tpAndStr => printLine(s"topic: ${tpAndStr._1.topic}, partition: ${tpAndStr._1.partition}"))
  .flatMap(_._2)
  .tap(cr => printLine(s"key: ${cr.record.key}, value: ${cr.record.value}"))
  .map(_.offset)
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
```
