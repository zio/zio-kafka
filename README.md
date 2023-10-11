[//]: # (This file was autogenerated using `zio-sbt-website` plugin via `sbt generateReadme` command.)
[//]: # (So please do not edit it manually. Instead, change "docs/index.md" file or sbt setting keys)
[//]: # (e.g. "readmeDocumentation" and "readmeSupport".)

# ZIO Kafka

[ZIO Kafka](https://github.com/zio/zio-kafka) is a Kafka client for ZIO. It provides a purely functional, streams-based interface to the Kafka client and integrates effortlessly with ZIO and ZIO Streams.

[![Production Ready](https://img.shields.io/badge/Project%20Stage-Production%20Ready-brightgreen.svg)](https://github.com/zio/zio/wiki/Project-Stages) ![CI Badge](https://github.com/zio/zio-kafka/workflows/CI/badge.svg) [![Sonatype Releases](https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-kafka_2.13.svg?label=Sonatype%20Release)](https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-kafka_2.13/) [![Sonatype Snapshots](https://img.shields.io/nexus/s/https/oss.sonatype.org/dev.zio/zio-kafka_2.13.svg?label=Sonatype%20Snapshot)](https://oss.sonatype.org/content/repositories/snapshots/dev/zio/zio-kafka_2.13/) [![javadoc](https://javadoc.io/badge2/dev.zio/zio-kafka-docs_2.13/javadoc.svg)](https://javadoc.io/doc/dev.zio/zio-kafka-docs_2.13) [![ZIO Kafka](https://img.shields.io/github/stars/zio/zio-kafka?style=social)](https://github.com/zio/zio-kafka)

## Introduction

Apache Kafka is a distributed event streaming platform that acts as a distributed publish-subscribe messaging system. It enables us to build distributed streaming data pipelines and event-driven applications.

Kafka has a mature Java client for producing and consuming events, but it has a low-level API. ZIO Kafka is a ZIO native client for Apache Kafka. It has a high-level streaming API on top of the Java client. So we can produce and consume events using the declarative concurrency model of ZIO Streams.

## Installation

In order to use this library, we need to add the following line in our `build.sbt` file:

```scala
libraryDependencies += "dev.zio" %% "zio-kafka"         % "2.5.0"
libraryDependencies += "dev.zio" %% "zio-kafka-testkit" % "2.5.0" % Test
```

## Example

Let's write a simple Kafka producer and consumer using ZIO Kafka with ZIO Streams. Before everything, we need a running instance of Kafka. We can do that by saving the following docker-compose script in the `docker-compose.yml` file and run `docker-compose up`:

```yaml
version: '2'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181
  
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Now, we can run our ZIO Kafka Streaming application:

```scala
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream

object MainApp extends ZIOAppDefault {
  val producer: ZStream[Producer, Throwable, Nothing] =
    ZStream
      .repeatZIO(Random.nextIntBetween(0, Int.MaxValue))
      .schedule(Schedule.fixed(2.seconds))
      .mapZIO { random =>
        Producer.produce[Any, Long, String](
          topic = "random",
          key = random % 4,
          value = random.toString,
          keySerializer = Serde.long,
          valueSerializer = Serde.string
        )
      }
      .drain

  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("random"), Serde.long, Serde.string)
      .tap(r => Console.printLine(r.value))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  def producerLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("localhost:29092"))
      )
    )

  def consumerLayer =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group")
      )
    )

  override def run =
    producer.merge(consumer)
      .runDrain
      .provide(producerLayer, consumerLayer)
}
```

## Resources

- [An Introduction to ZIO Kafka](https://ziverge.com/blog/introduction-to-zio-kafka/)
- [Streaming microservices with ZIO and Kafka](https://scalac.io/streaming-microservices-with-zio-and-kafka/) by Aleksandar Skrbic (February 2021)
- [ZIO WORLD - ZIO Kafka](https://www.youtube.com/watch?v=GECv1ONieLw) by Aleksandar Skrbic (March 2020) — Aleksandar Skrbic presented ZIO Kafka, a critical library for the modern Scala developer, which hides some of the complexities of Kafka.

## Adopters

Here is a partial list of companies using zio-kafka in production.

Want to see your company here? [Submit a PR](https://github.com/zio/zio-kafka/edit/master/docs/index.md)!

* [Conduktor](https://www.conduktor.io)
* [KelkooGroup](https://www.kelkoogroup.com)
* [Rocker](https://rocker.com)

## Documentation

Learn more on the [ZIO Kafka homepage](https://zio.dev/zio-kafka)!

## Contributing

For the general guidelines, see ZIO [contributor's guide](https://zio.dev/contributor-guidelines).

## Code of Conduct

See the [Code of Conduct](https://zio.dev/code-of-conduct)

## Support

Come chat with us on [![Badge-Discord]][Link-Discord].

[Badge-Discord]: https://img.shields.io/discord/629491597070827530?logo=discord "chat on discord"
[Link-Discord]: https://discord.gg/2ccFBr4 "Discord"

## Credits

This library is heavily inspired and made possible by the research and implementation done in [Alpakka Kafka](https://github.com/akka/alpakka-kafka), a library maintained by the Akka team and originally written as Reactive Kafka by SoftwareMill.

## License

[License](LICENSE)

Copyright 2021-2023 Itamar Ravid and the zio-kafka contributors.
