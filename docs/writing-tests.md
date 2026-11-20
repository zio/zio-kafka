---
id: writing-tests
title: "Test Producers and Consumers with the Test Kit"
---

This guide shows how to test code that produces and consumes Kafka records, against a real Kafka broker that starts inside the test JVM. By the end, you will have ZIO Test suites that produce records, read them back with a consumer, run transactions, and connect to a secured or an external broker, without installing Kafka.

The tests use the `zio-kafka-testkit` module. Its `Kafka.embedded` layer starts a broker, and `KafkaTestUtils` creates producers, consumers and admin clients that connect to it. Every member used here is described in the [Test Kit](testkit.md) reference.

## Prerequisites

Add `zio-kafka-testkit` to your test dependencies, as shown in [Test Kit installation](testkit.md#installation). On Scala 3, also apply the build setting in [Using the Test Kit With Scala 3](getting-started.md#using-the-test-kit-with-scala-3). The examples assume that you know how to write a ZIO Test spec, and how to create a [Producer](producer.md) and a [Consumer](consumer.md).

## Test a Producer

The smallest useful test creates a producer, produces a record and checks that the call succeeds. The suite gets its broker from `Kafka.embedded`:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object ProducerSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Producer test suite")(
        test("produces a record") {
          for {
            producer <- KafkaTestUtils.makeProducer
            _        <- producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
          } yield assertCompletes
        }
        // ... more tests ...
      )
        .provideSomeShared[Scope](Kafka.embedded)
    ) @@ withLiveClock @@ timeout(2.minutes)
}
```

`KafkaTestUtils.makeProducer` reads the broker address from the `Kafka` service in the environment and creates a producer in the test's `Scope`, which closes it when the test ends. `assertCompletes` passes when the effect before it succeeds.

`provideSomeShared[Scope](Kafka.embedded)` starts one embedded broker for the whole suite and stops it after the last test. The type parameter lists the services that this line does not provide; here that is the `Scope`, which the test framework provides.

Zio-kafka needs the live clock, so add `TestAspect.withLiveClock` to every suite: with ZIO Test's default `TestClock`, timeouts and schedules inside zio-kafka never fire. The `timeout` aspect makes a stuck test fail instead of hanging the build.

## Test a Consumer

A consumer test produces records to a new topic and reads them back. Because the tests of a suite share one broker, each test needs its own topic, group and client id; the `KafkaRandom` trait generates them:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaRandom, KafkaTestUtils }
import zio.test.Assertion.hasSameElements
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object ConsumerSpec extends ZIOSpecDefault with KafkaRandom {
  override def kafkaPrefix: String = "consumer-spec"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Consumer test suite")(
        test("reads back the produced records") {
          val kvs: List[(String, String)] = (1 to 5).toList.map(i => (s"key-$i", s"msg-$i"))
          for {
            topic  <- randomTopic
            client <- randomClient
            group  <- randomGroup

            _ <- KafkaTestUtils.createCustomTopic(topic, partitionCount = 3)

            producer <- KafkaTestUtils.makeProducer
            _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

            consumer <- KafkaTestUtils.makeConsumer(clientId = client, groupId = Some(group))
            records <- consumer
                         .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                         .take(5)
                         .runCollect
            consumed = records.map(r => (r.record.key, r.record.value)).toList
          } yield assert(consumed)(hasSameElements(kvs))
        }
      )
        .provideSomeShared[Scope](Kafka.embedded)
    ) @@ withLiveClock @@ timeout(2.minutes)
}
```

The test works in four steps:

1. `KafkaTestUtils.createCustomTopic` creates the topic with 3 partitions. The broker creates topics automatically, so this step is only needed to choose the number of partitions.
2. `KafkaTestUtils.produceMany` produces the 5 records and waits until the broker has acknowledged them.
3. `KafkaTestUtils.makeConsumer` creates a consumer that starts at the earliest offset, so it also sees records produced before it started.
4. The consumer stream takes 5 records and ends. The records come from 3 partitions, so the test compares them with `Assertion.hasSameElements`, which ignores their order.

Be careful with `ZStream#take` when a test checks what a consumer has *not* read: the consumer prefetches records, so it may have fetched more from the topic than the stream emitted.

When you generate names with ZIO's `Random` instead of `KafkaRandom`, add `TestAspect.withLiveRandom`. ZIO Test's `TestRandom` starts every test with the same seed, so every test would otherwise get the same "random" names.

## Share the Broker Between Tests

Starting Kafka takes a few seconds, so a test run should start as few brokers as it can. Choose where to provide the `Kafka` layer:

| Where                                  | How                                                      | Brokers started        |
| -------------------------------------- | -------------------------------------------------------- | ---------------------- |
| Per test                               | `.provideSome[Scope](Kafka.embedded)` on the suite       | one per test           |
| Per suite                              | `.provideSomeShared[Scope](Kafka.embedded)` on the suite | one per suite          |
| Per test run, for every suite using it | extend `ZIOSpecWithKafka` instead of `ZIOSpecDefault`    | one for all such specs |

A shared broker means that tests run concurrently against the same Kafka. Give each test its own topic (see [Test a Consumer](#test-a-consumer)), or run the tests of a suite one by one with the `TestAspect.sequential` aspect.

With `ZIOSpecWithKafka`, the spec's environment includes `Kafka`, and every spec object that extends the trait uses the same broker:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaTestUtils, ZIOSpecWithKafka }
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object SharedProducerSpec extends ZIOSpecWithKafka {
  override def spec: Spec[TestEnvironment & Scope & Kafka, Any] =
    (
      suite("Producer test suite")(
        test("produces a record") {
          for {
            producer <- ZIO.service[Producer]
            _        <- producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
          } yield assertCompletes
        }
      )
        .provideSomeShared[Scope & Kafka](KafkaTestUtils.producer)
    ) @@ withLiveClock @@ timeout(2.minutes)
}
```

This suite also shares one producer between its tests, with the `KafkaTestUtils.producer` layer. Because the framework now provides both `Scope` and `Kafka`, both go in the type parameter of `provideSomeShared`. The `KafkaTestUtils.consumer` layer shares a consumer in the same way.

## Test a Transactional Producer

A `TransactionalProducer` needs a consumer that has rebalance-safe commits enabled. The test kit's consumer helpers turn them off by default, so pass `rebalanceSafeCommits = true`. Each producer also needs its own transactional id when tests run in parallel. To produce a record in a transaction and read it back with a read-committed consumer:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaRandom, KafkaTestUtils }
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object TransactionSpec extends ZIOSpecDefault with KafkaRandom {
  override def kafkaPrefix: String = "transaction-spec"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Transactions")(
        test("a committed record is visible") {
          for {
            topic         <- randomTopic
            group         <- randomGroup
            client        <- randomClient
            transactionId <- randomThing("transaction")
            consumer <- KafkaTestUtils.makeTransactionalConsumer(
                          clientId = client,
                          groupId = group,
                          rebalanceSafeCommits = true
                        )
            producer <- KafkaTestUtils.makeTransactionalProducer(transactionId, consumer)
            _ <- ZIO.scoped {
                   producer.createTransaction.flatMap { transaction =>
                     transaction.produce(topic, "key", "value", Serde.string, Serde.string, offset = None)
                   }
                 }
            records <- consumer
                         .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                         .take(1)
                         .runCollect
          } yield assertTrue(records.map(_.value) == Chunk("value"))
        }
      )
        .provideSomeShared[Scope](Kafka.embedded)
    ) @@ withLiveClock @@ timeout(2.minutes)
}
```

The transaction commits when the scope of `TransactionalProducer#createTransaction` closes, so the record becomes visible to the read-committed consumer after `ZIO.scoped`. See [Transactional Producer](transactional-producer.md) for how transactions work.

## Test Against a Secured Broker

`Kafka.saslEmbedded` starts an embedded broker that requires a SASL login (`SASL_PLAINTEXT` with the `PLAIN` mechanism), with `admin` / `admin-secret` as its super user. It provides a `Kafka.Sasl` instead of a `Kafka`. `KafkaTestUtils.makeSaslAdminClient` uses it directly:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object SaslAdminSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("SASL broker")(
        test("the admin user can create a topic") {
          for {
            admin  <- KafkaTestUtils.makeSaslAdminClient()
            _      <- admin.createTopic(AdminClient.NewTopic("secured-topic", 1, replicationFactor = 1))
            topics <- admin.listTopics()
          } yield assertTrue(topics.contains("secured-topic"))
        }
      )
        .provideSomeShared[Scope](Kafka.saslEmbedded)
    ) @@ withLiveClock @@ timeout(2.minutes)
}
```

There are no SASL variants of the producer and consumer helpers. To create a producer or a consumer for the SASL broker, turn the layer into a plain `Kafka` layer with `Kafka.saslEmbedded.project(_.value)`, and add the login properties to the test settings:

```scala mdoc:compile-only
import zio._
import zio.kafka.producer.Producer
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }

val saslKafka: ZLayer[Any, Throwable, Kafka] = Kafka.saslEmbedded.project(_.value)

val saslLogin: Map[String, AnyRef] = Map(
  "security.protocol" -> "SASL_PLAINTEXT",
  "sasl.mechanism"    -> "PLAIN",
  "sasl.jaas.config"  ->
    """org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";"""
)

val saslProducer: ZIO[Scope & Kafka, Throwable, Producer] =
  KafkaTestUtils.producerSettings.flatMap(settings => Producer.make(settings.withProperties(saslLogin)))
```

Provide `saslKafka` to the suite in place of `Kafka.embedded`. For a consumer, pass the same properties to `KafkaTestUtils.makeConsumer` through its `properties` parameter (a `Map[String, String]`).

`Kafka.sslEmbedded` starts a broker with an SSL listener, but clients cannot work with it; it only serves to test zio-kafka's check for plain-text clients that connect to an SSL broker. See [Secured Embedded Brokers](testkit.md#secured-embedded-brokers) for the details of both layers, and [Security](security.md) for configuring security in your application.

## Test Against an External Broker

Some tests need a broker that the test kit cannot embed, for example one with a specific Kafka version or with plugins. `Kafka.local` connects to a broker on `localhost:9092` that you start yourself, for example with Docker, and does not stop it afterwards. Provide it in place of `Kafka.embedded`:

```scala mdoc:compile-only
import zio._
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object LocalBrokerSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Against a local broker")(
        test("can connect") {
          KafkaTestUtils.makeAdminClient.flatMap(_.listTopics()).as(assertCompletes)
        }
      )
        .provideSomeShared[Scope](Kafka.local)
    ) @@ withLiveClock @@ timeout(2.minutes)
}
```

For a broker at another address, provide your own implementation of the `Kafka` trait. `Kafka#stop` is called when the layer's scope closes; for a broker you do not own, it does nothing:

```scala mdoc:compile-only
import zio._
import zio.kafka.testkit.Kafka

def externalKafka(servers: List[String]): ULayer[Kafka] =
  ZLayer.succeed(new Kafka {
    override def bootstrapServers: List[String] = servers
    override def stop(): UIO[Unit]              = ZIO.unit
  })
```

Unlike an embedded broker, an external broker keeps its topics between test runs. Use random topic names, or delete the topics at the end of a test.

## Going Further

The [Test Kit](testkit.md) reference lists every helper, including `KafkaTestUtils.consumeWithStrings`, `KafkaTestUtils.scheduledProduce` and the settings helpers, with their defaults. To check which events a consumer or producer went through in a test, combine the `diagnostics` parameter of `KafkaTestUtils.makeConsumer` with `SlidingDiagnostics`, described in [Diagnostics](diagnostics.md#slidingdiagnostics). The tests of zio-kafka itself, in the `zio-kafka-test` module, use the test kit throughout.
