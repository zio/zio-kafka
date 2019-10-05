[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]

# Welcome to ZIO Kafka

ZIO Kafka provides a purely functional, streams-based interface to the Kafka
client. It integrates effortlessly with ZIO and ZIO Streams.

## Quickstart

Add the following dependencies to your `build.sbt` file:
```
libraryDependencies ++= Seq(
  "dev.zio" %% "zio-streams" % "1.0.0-RC13",
  "dev.zio" %% "zio-kafka"   % "<version>"
)
```

Somewhere in your application, configure the `zio.kafka.ConsumerSettings` 
data type:
```scala
import zio._, zio.duration._
import zio.kafka.client._

val consumerSettings: ConsumerSettings = 
  ConsumerSettings(
    bootstrapServers          = List("localhost:9092"),
    groupId                   = "group",
    clientId                  = "client",
    closeTimeout              = 30.seconds,
    extraDriverSettings       = Map(),
    pollInterval              = 250.millis,
    pollTimeout               = 50.millis,
    perPartitionChunkPrefetch = 2
  )
```


And use it to create a consumer:
```scala
import zio.ZManaged, zio.blocking.Blocking, zio.clock.Clock 
import zio.kafka.client.{ Consumer, ConsumerSettings }

val consumerSettings: ConsumerSettings = ???
val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = 
  Consumer.make(consumerSettings)
```

The consumer returned from `Consumer.make` is wrapped in a `ZManaged`
to ensure its proper release. To get access to it, use the `ZManaged#use` method:
```scala
import zio._, zio.blocking.Blocking, zio.clock.Clock 
import zio.kafka.client.Consumer

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = ???
consumer.use { c =>
  // Consumer now available as `c`
  ZIO.unit
}
```

You may stream data from Kafka using the `subscribe` and
`plainStream` methods. `plainStream` takes as parameters deserializers for the key and values of the Kafka messages. Serializers and deserializers (Serdes) for common data types are available in scope and can be used via `Serde.of[T]`:

```scala
import zio.ZManaged, zio.blocking.Blocking, zio.clock.Clock, zio.console.putStrLn
import zio.stream._
import zio.kafka.client._
import zio.kafka.client.serde._

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = ???

consumer.use { c =>
  c.subscribeAnd(Subscription.Topics(Set("topic150")))
    .plainStream(Serde.string, Serde.string)
    .flattenChunks
    .tap(cr => putStrLn(s"key: ${cr.record.key}, value: ${cr.record.value}"))
    .aggregate(Consumer.batchingSink)
    .mapM(_.commit)
    .runDrain
}
```

If you need to distinguish between the different partitions assigned
to the consumer, you may use the `Consumer#partitionedStream` method,
which creates a nested stream of partitions:
```scala
import zio.ZManaged, zio.blocking.Blocking, zio.clock.Clock, zio.console.putStrLn
import zio.stream._
import zio.kafka.client._
import zio.kafka.client.serde._

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = ???

consumer.use { c =>
  c.subscribeAnd(Subscription.Topics(Set("topic150")))
    .partitionedStream(Serde.string, Serde.string)
    .tap(tpAndStr => putStrLn(s"topic: ${tpAndStr._1.topic}, partition: ${tpAndStr._1.partition}"))
    .flatMap(_._2.flattenChunks)
    .tap(cr => putStrLn(s"key: ${cr.record.key}, value: ${cr.record.value}"))
    .aggregate(Consumer.batchingSink)
    .mapM(_.commit)
    .runDrain
}
```

## Example: consuming, producing and committing offset
This example shows how to consume messages from topic `topic_a` and produce transformed messages to `topic_b`, after which consumer offsets are committed. Processing is done in chunks using `ZStreamChunk` for more efficiency.

```scala
import zio.kafka.client._
import zio.kafka.client.serde._
import org.apache.kafka.clients.producer.ProducerRecord

val consumerSettings: ConsumerSettings = ???
val producerSettings: ProducerSettings = ???

(Consumer.make(consumerSettings) zip Producer.make(producerSettings, Serde.int, Serde.string)).use {
case (consumer, producer) =>
  consumer
      .subscribeAnd(Subscription.topics("my-input-topic"))
      .plainStream(Serde.int, Serde.long)
      .map { record =>
        val key: Int    = record.record.key()
        val value: Long = record.record.value()
        val newValue: String = value.toString

        val producerRecord: ProducerRecord[Int, String] = new ProducerRecord("my-output-topic", key, newValue)
        (producerRecord, record.offset)
      }
      .chunks
      .mapM { chunk =>
        val records     = chunk.map(_._1)
        val offsetBatch = OffsetBatch(chunk.map(_._2).toSeq)

        producer.produceChunk(records) *> offsetBatch.commit
      }
      .runDrain
}
```

## Custom data type serdes
Serializers and deserializers (serdes) for custom data types can be constructed from scratch or by converting existing serdes. For example, to create a serde for an `Instant`:

```scala
import java.time.Instant
import zio.kafka.client.serde._

implicit val instantSerde: Serde[Any, Instant] = Serde.long.inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)

```

For a lot of use cases where you just want to do something with all messages on a Kafka topic, ZIO Kafka provides the convenience method `Consumer.consumeWith`. This method lets you execute a ZIO effect for each message. Topic partitions will be processed in parallel and offsets are committed after running the effect automatically. 

```scala
import zio._
import zio.console._
import zio.kafka.client._
import zio.kafka.client.serde._

val subscription: Subscription = ???
val settings: ConsumerSettings = ???

Consumer.consumeWith(settings, subscription, Serde.string, Serde.string) { case (key, value) =>
  putStrLn(s"Received message ${key}: ${value}")
  // Perform an effect with the received message
}
```

## Getting help

Please feel free to use the [Gitter](https://gitter.im/zio/zio-kafka)
channel for any and all questions you may have.

## Credits

This library is heavily inspired and made possible by the research and implementation done in [Alpakka Kafka](https://github.com/akka/alpakka-kafka), a library maintained by the Akka team and originally written as Reactive Kafka by SoftwareMill.

## Legal

Copyright 2019 Itamar Ravid and the zio-kafka contributors. All rights reserved.

[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-kafka_2.12/ "Sonatype Releases"
[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-kafka_2.12.svg "Sonatype Releases"
