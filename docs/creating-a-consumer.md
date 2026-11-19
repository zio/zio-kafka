---
id: creating-a-consumer
title: "Create a Consumer"
sidebar_label: "Create a Consumer"
---

In this guide we create a zio-kafka `Consumer` and make sure it closes its connection to the Kafka cluster when the program no longer needs it. We create it directly in a scope first, then provide it as a layer, and finally build it from settings that come from another layer.

## Create a Consumer in a Scope

`Consumer.make` creates a consumer from `ConsumerSettings`. The consumer owns a connection to the Kafka cluster, so `Consumer.make` needs a `Scope`: when the scope closes, the consumer stops consuming and closes the connection. `ZIO.scoped` creates such a scope around the code that uses the consumer:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.{ Consumer, ConsumerSettings }

val consumerSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092")).withGroupId("group")

val program: Task[Unit] =
  ZIO.scoped {
    for {
      consumer <- Consumer.make(consumerSettings)
      topics   <- consumer.listTopics()
      _        <- ZIO.logInfo(s"The cluster has ${topics.size} topics")
    } yield ()
  } // here the scope closes, and with it the consumer's connection
```

`Consumer.make` checks the settings before it connects and fails with an `IllegalArgumentException` when they are invalid. The settings also configure zio-kafka features such as rebalance-safe commits (see [Prevent Duplicates After a Rebalance](preventing-duplicates.md)), the offset to start from (see [Partition Assignment and Offset Retrieval](partition-assignment-and-offset-retrieval.md)) and pre-fetching (see [Tune the Consumer](consumer-tuning.md)). [Consumer Settings](consumer-settings.md) lists every setting.

:::caution
When no explicit `Scope` is provided, for example when the consumer is created in a `ZIOAppDefault`'s `run`, the consumer uses the application's scope. It then closes its connection only when the application exits.
:::

## Provide a Consumer as a Layer

If your application uses the [ZIO service pattern](https://zio.dev/reference/service-pattern/), provide the consumer as a layer. `ZLayer.scoped` turns the scoped `Consumer.make` into a layer, and closes the consumer when the layer is no longer needed:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.{ Consumer, ConsumerSettings }

val consumerLayer: ZLayer[Any, Throwable, Consumer] =
  ZLayer.scoped {
    Consumer.make(ConsumerSettings(List("localhost:9092")).withGroupId("group"))
  }
```

## Read the Settings From a Layer

When the `ConsumerSettings` come from another layer, for example from your configuration, use `Consumer.live`. It takes the settings from the environment and produces a scoped `Consumer` layer. To provide a program with a consumer this way:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.{ Consumer, ConsumerSettings }

val consumerSettingsLayer: ULayer[ConsumerSettings] =
  ZLayer.succeed(ConsumerSettings(List("localhost:9092")).withGroupId("group"))

// Any program that needs a Consumer
val program: ZIO[Consumer, Throwable, Unit] =
  ZIO.serviceWithZIO[Consumer](_.listTopics()).unit

val app: Task[Unit] =
  program.provide(
    consumerSettingsLayer,
    Consumer.live
  )
```

## Verify the Result

Run one of the programs against your cluster with an SLF4J logging backend on the class path. When the consumer is created, the Kafka client logs its configuration at `INFO` level in a `ConsumerConfig values:` block; check that `bootstrap.servers` and `group.id` are what you expect. When the scope or layer closes, the client logs `App info kafka.consumer for <client id> unregistered`, which shows that the consumer was closed. If `Consumer.make` fails with an `IllegalArgumentException` instead, its message lists the settings to fix; see [Settings Validation](consumer-settings.md#settings-validation).

Next, [Consume Topics with ZIO Streams](consuming-kafka-topics-using-zio-streams.md) shows how to read records with the consumer. The [Consumer](consumer.md) reference page describes every operation.
