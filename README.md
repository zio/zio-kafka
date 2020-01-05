[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]

# Welcome to ZIO Kafka

ZIO Kafka provides a purely functional, streams-based interface to the Kafka
client. It integrates effortlessly with ZIO and ZIO Streams.

## Contents

- [Quickstart](#quickstart)
- [Consuming Kafka topics using ZIO Streams](#consuming-kafka-topics-using-zio-streams)
- [Example: consuming, producing and committing offset](#example--consuming--producing-and-committing-offset)
- [Partition assignment and offset retrieval](#partition-assignment-and-offset-retrieval)
- [Custom data type serdes](#custom-data-type-serdes)
- [Handling deserialization failures](#handling-deserialization-failures)
- [Getting help](#getting-help)
- [Credits](#credits)
- [Legal](#legal)

## Quickstart

Add the following dependencies to your `build.sbt` file:
```
libraryDependencies ++= Seq(
  "dev.zio" %% "zio-streams" % "1.0.0-RC17",
  "dev.zio" %% "zio-kafka"   % "<version>"
)
```

Somewhere in your application, configure the `zio.kafka.ConsumerSettings` 
data type:
```scala
import zio._, zio.duration._
import zio.kafka.client._

val settings: ConsumerSettings = 
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

For a lot of use cases where you just want to do something with all messages on a Kafka topic, ZIO Kafka provides the convenience method `Consumer.consumeWith`. This method lets you execute a ZIO effect for each message. Topic partitions will be processed in parallel and offsets are committed after running the effect automatically. 

```scala
import zio._
import zio.console._
import zio.kafka.client._
import zio.kafka.client.serde._

val subscription = Subscription.topics("topic")

Consumer.consumeWith(settings, subscription, Serde.string, Serde.string) { case (key, value) =>
  putStrLn(s"Received message ${key}: ${value}")
  // Perform an effect with the received message
}
```

If you require more control over the consumption process, read on!

## Consuming Kafka topics using ZIO Streams

First, create a consumer using the ConsumerSettings instance:
```scala
import zio.ZManaged, zio.blocking.Blocking, zio.clock.Clock 
import zio.kafka.client.{ Consumer, ConsumerSettings }

val consumerSettings: ConsumerSettings = ???
val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = 
  Consumer.make(consumerSettings)
```

The consumer returned from `Consumer.make` is wrapped in a `ZManaged`
to ensure its proper release. To get access to it, you can use the `ZManaged#use` method
or compose other `ZManaged` instances with it using a for-comprehension. Here's an example
using `ZManaged#use`:
```scala
import zio._, zio.blocking.Blocking, zio.clock.Clock 
import zio.kafka.client.Consumer

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = ???
consumer.use { c =>
  // Consumer now available as `c`
  ZIO.unit
}
```

You may stream data from Kafka using the `subscribe` and `plainStream` methods. `plainStream` takes as parameters deserializers for the key and values of the Kafka messages. Serializers and deserializers (Serdes) for common data types are available in the `Deserializer`, `Serializer`, and `Serde` companion objects:

```scala
import zio.ZManaged, zio.blocking.Blocking, zio.clock.Clock, zio.console.putStrLn
import zio.stream._
import zio.kafka.client._
import zio.kafka.client.serde._

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = ???

consumer.use { c =>
  c.subscribeAnd(Subscription.topics("topic150"))
    .plainStream(Serde.string, Serde.string)
    .flattenChunks
    .tap(cr => putStrLn(s"key: ${cr.record.key}, value: ${cr.record.value}"))
    .map(_.offset)
    .aggregateAsync(Consumer.offsetBatches)
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
  c.subscribeAnd(Subscription.topics("topic150"))
    .partitionedStream(Serde.string, Serde.string)
    .tap(tpAndStr => putStrLn(s"topic: ${tpAndStr._1.topic}, partition: ${tpAndStr._1.partition}"))
    .flatMap(_._2.flattenChunks)
    .tap(cr => putStrLn(s"key: ${cr.record.key}, value: ${cr.record.value}"))
    .map(_.offset)
    .aggregateAsync(Consumer.offsetBatches)
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

## Partition assignment and offset retrieval

`zio-kafka` offers several way to control which Kafka topics and partitions are assigned to your application. 


| Use case | Method |
| --- | --- |
| One or more topics, automatic partition assignment | `consumer.subscribe(Subscription.topics("my_topic", "other_topic"))` |
| Topics matching a pattern | `consumer.subscribe(Subscription.pattern("topic.*"))` |
| Manual partition assignment | `consumer.subscribe(Subscription.manual("my_topic" -> 1, "my_topic" -> 2))` |

By default `zio-kafka` will start streaming a partition from the last committed offset for the consumer group, or the latest message on the topic if no offset has yet been committed. You can also choose to store offsets outside of kafka. This can be useful in cases where consistency between data stores and consumer offset is required.

| Use case | Method |
| --- | --- |
| Offsets in kafka, start at latest message if no offset committed | `OffsetRetrieval.Auto` |
| Offsets in kafka, start at earliest message if no offset committed | `OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest)` |
| Manual/external offset storage | `Manual(getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, Long]])` |

For manual offset retrieval, the `getOffsets` function will be called for each topic-partition that is assigned to the consumer, either via Kafka's rebalancing or via a manual assignment. 

## Custom data type serdes

Serializers and deserializers (serdes) for custom data types can be constructed from scratch or by converting existing serdes. For example, to create a serde for an `Instant`:

```scala
import java.time.Instant
import zio.kafka.client.serde._

val instantSerde: Serde[Any, Instant] = Serde.long.inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)
```

## Handling deserialization failures
The default behavior for a consumer stream when encountering a deserialization failure is to fail the stream. In many cases you may want to handle this situation differently, eg by skipping the message that failed to deserialize or by executing an alternative effect. For this purpose, any `Deserializer[T]` for some type `T` can be easily converted into a `Deserializer[Try[T]]` where deserialization failures are converted to a `Failure` using the `asTry` method.

Below is an example of skipping messages that fail to deserialize. The offset is passed downstream to be committed.

```scala
import zio.ZManaged, zio.blocking.Blocking, zio.clock.Clock, zio.console.putStrLn
import zio.stream._
import zio.kafka.client._
import zio.kafka.client.serde._
import scala.util.{Success, Failure}
import zio._

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = ???

consumer.use { c =>
  val stream = c.subscribeAnd(Subscription.topics("topic150"))
    .plainStream(Serde.string, Serde.string.asTry)

  stream 
      .mapM { record => 
        val tryValue: Try[String] = record.record.value()
        val offset: Offset = record.offset
    
        tryValue match {
            case Success(value) =>
              // Action for successful deserialization
              someEffect(value).as(offset)
            case Failure(exception) =>
              // Possibly log the exception or take alternative action
              ZIO.succeed(offset)
        }
    }
    .flattenChunks
    .aggregateAsync(Consumer.offsetBatches)
    .mapM(_.commit)
    .runDrain
}
```

## Getting help

Join us on the [ZIO Discord server](https://discord.gg/2ccFBr4) at the `#zio-kafka` channel.

## Credits

This library is heavily inspired and made possible by the research and implementation done in [Alpakka Kafka](https://github.com/akka/alpakka-kafka), a library maintained by the Akka team and originally written as Reactive Kafka by SoftwareMill.

## Legal

Copyright 2019 Itamar Ravid and the zio-kafka contributors. All rights reserved.

[Link-SonatypeReleases]: https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-kafka_2.12/ "Sonatype Releases"
[Badge-SonatypeReleases]: https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-kafka_2.12.svg "Sonatype Releases"
