[![Release Artifacts][Badge-SonatypeReleases]][Link-SonatypeReleases]

# Welcome to ZIO Kafka

ZIO Kafka provides a purely functional, streams-based to the Kafka
client. It integrates effortlessly with ZIO and ZIO Streams.

## Quickstart

Add the following dependencies to your `build.sbt` file:
```scala
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
import zio.blocking.Blocking, zio.clock.Clock

val consumer: ZManaged[Clock with Blocking, Throwable, Consumer] = 
  Consumer.make(consumerSettings)
```

The consumer returned from `Consumer.make` is wrapped in a `ZManaged`
to ensure its proper release. To get access to it, use the `ZManaged#use` method:
```scala
consumer.use { c =>
  // Consumer now available as `c`
}
```

You may stream data from Kafka using the `subscribe` and
`plainStream` methods. `plainStream` takes as parameters deserializers for the key and values of the Kafka messages. Serializers and deserializers (Serdes) for common data types are available in scope and can be used via `Serde.of[T]`:

```scala
import zio.console.putStrLn
import zio.stream._
import zio.kafka.client.serde._

consumer.use { c =>
  val records = 
    ZStream.unwrap(
      c.subscribe(Subscription.Topics(Set("topic")))
        .as(c.plainStream(Serde.of[String], Serde.of[String]).tap(record => putStrLn(record.toString)).chunks)
    )
     
  records.runDrain
}
```

If you need to distinguish between the different partitions assigned
to the consumer, you may use the `Consumer#partitionedStream` method,
which creates a nested stream of partitions:
``` scala
  def partitionedStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[ Clock with Blocking, Throwable, (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]]) ]
```

## Custom data type serdes
Serializers and deserializers (serdes) for custom data types can be constructed from scratch or by converting existing serdes. For example, to create a serde for an `Instant`:

```scala
import java.time.Instant
import zio.kafka.client.serde._

implicit val instantSerde: Serde[Any, Instant] = Serde.of[Long].inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)

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
