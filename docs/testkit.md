---
id: testkit
title: "Test Kit"
description: "Reference for zio-kafka-testkit: embedded Kafka layers, KafkaTestUtils helpers, KafkaRandom and ZIOSpecWithKafka."
keywords:
  - "Kafka testing"
  - "embedded Kafka"
  - "KafkaTestUtils"
  - "zio-kafka-testkit"
---

`zio-kafka-testkit` is a test-only module that gives ZIO Test suites a Kafka broker and ready-made zio-kafka clients for it. It starts an embedded broker inside the test JVM (or points at a broker that is already running), and builds producers, consumers and admin clients with settings tuned for fast tests. It has four parts: the `Kafka` service, the `KafkaTestUtils` helpers, the `KafkaRandom` trait and the `ZIOSpecWithKafka` trait. All of them live in the `zio.kafka.testkit` package:

```scala
trait Kafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}

object KafkaTestUtils { /* settings, clients and produce/consume helpers that need a Kafka */ }

trait KafkaRandom { def kafkaPrefix: String /* random topic, group and client names */ }

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka]
```

For a step-by-step guide to writing tests with these parts, see [Test Producers and Consumers with the Test Kit](writing-tests.md).

## Installation

Add the test kit to the test dependencies in your `build.sbt`:

```scala
// Testkit for running tests against an embedded Kafka broker (JVM)
libraryDependencies += "dev.zio" %% "zio-kafka-testkit" % "@VERSION@" % Test
```

The test kit is available for the JVM only. On Scala 3 it needs one extra build setting, see [Using the Test Kit With Scala 3](getting-started.md#using-the-test-kit-with-scala-3).

## How They Work Together

A test gets a broker from a `Kafka` layer and builds its clients from that broker's address:

1. The suite provides a `Kafka` layer, usually `Kafka.embedded`. The layer starts a broker when the suite (or the test) starts and stops it at the end.
2. A test calls a `KafkaTestUtils` helper, such as `KafkaTestUtils.makeProducer`. The helper reads `Kafka#bootstrapServers` from the environment, builds test settings from it, and creates the client in the test's `Scope`.
3. The test produces and consumes records with ordinary zio-kafka calls, or with helpers such as `KafkaTestUtils.produceMany`.
4. `KafkaRandom` supplies unique topic, group and client names, so tests that share one broker do not see each other's records.
5. `ZIOSpecWithKafka` provides one `Kafka.embedded` broker to every suite of the test run that extends it.

The diagram shows where the broker address flows:

```
 Kafka.embedded / Kafka.local / ...        (a ZLayer that provides a Kafka)
        │
        │ Kafka#bootstrapServers
        ▼
 KafkaTestUtils.producerSettings, consumerSettings(...), adminSettings, ...
        │
        ▼
 KafkaTestUtils.makeProducer, makeConsumer(...), makeAdminClient, ...   (need Scope & Kafka)
        │
        ▼
 Producer / Consumer / TransactionalProducer / AdminClient  ──>  broker
```

## Kafka

`Kafka` is the service that describes a broker under test. `Kafka#bootstrapServers` is the address that clients connect to, and `Kafka#stop` stops the broker; the layers below call it for you when their scope ends. Every `KafkaTestUtils` helper that builds settings or a client requires a `Kafka` (or, for the SASL helpers, a `Kafka.Sasl`) in its environment.

The `Kafka` companion object provides layers for an embedded broker, embedded brokers with security enabled, and a broker that already runs on your machine.

### Embedded Brokers

`Kafka.embedded` starts a single-node Kafka broker inside the test JVM, using [embedded-kafka](https://github.com/embeddedkafka/embedded-kafka), and stops it when the layer's scope closes. `Kafka.embeddedWith` does the same with extra or changed broker properties:

```scala
object Kafka {
  val embedded: ZLayer[Any, Throwable, Kafka]
  def embeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka]

  final case class Ports(kafkaPort: Int, controllerPort: Int)
}

final case class EmbeddedKafkaStartException(msg: String, cause: Throwable = null) extends RuntimeException(msg, cause)
```

Each embedded broker gets a free broker port and a free controller port, so several brokers can run at the same time. The function you pass to `Kafka.embeddedWith` receives these ports as a `Ports` value, which is useful for properties such as `listeners`. Its properties are applied after the defaults, so they can override them. The defaults speed up group joins (`group.min.session.timeout.ms=500` and `group.initial.rebalance.delay.ms=0`) and enable the `StandardAuthorizer` with `User:ANONYMOUS` as super user, so tests can also manage ACLs. To start a broker that does not create topics automatically:

```scala mdoc:compile-only
import zio._
import zio.kafka.testkit.Kafka

val strictKafka: ZLayer[Any, Throwable, Kafka] =
  Kafka.embeddedWith(_ => Map("auto.create.topics.enable" -> "false"))
```

When the embedded broker cannot start, the layer fails with an `EmbeddedKafkaStartException`, whose `cause` holds the original error.

Starting a broker takes a few seconds, so provide the layer once per suite with `provideSomeShared` rather than once per test; [Share the Broker Between Tests](writing-tests.md#share-the-broker-between-tests) compares the options.

### Secured Embedded Brokers

Two more layers start an embedded broker with security enabled, each with a `*With` variant that takes custom broker properties in the same way as `Kafka.embeddedWith`:

```scala
object Kafka {
  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl]
  def saslEmbeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka.Sasl]

  val sslEmbedded: ZLayer[Any, Throwable, Kafka]
  def sslEmbeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka]

  final case class Sasl(value: Kafka) extends AnyVal
}
```

`Kafka.saslEmbedded` starts a broker whose listeners use `SASL_PLAINTEXT` with the `PLAIN` mechanism. It knows the user `admin` with password `admin-secret`, who is the only super user, and it denies access to everyone else unless an ACL allows it (`allow.everyone.if.no.acl.found=false`). Clients must log in with these SASL properties:

```
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";
```

The layer provides a `Kafka.Sasl`, a wrapper around the `Kafka`, so that helpers that connect without SASL, such as `KafkaTestUtils.makeProducer`, cannot pick it up by mistake. The SASL helpers `KafkaTestUtils.saslAdminSettings` and `KafkaTestUtils.makeSaslAdminClient` require a `Kafka.Sasl`. To use the other helpers, turn the layer into a plain `Kafka` layer with `Kafka.saslEmbedded.project(_.value)` and add the SASL properties to the settings; [Test Against a Secured Broker](writing-tests.md#test-against-a-secured-broker) shows how.

`Kafka.sslEmbedded` starts a broker whose client listener uses SSL with client authentication required, using a key store and trust store that ship with the test kit. Clients cannot actually work with this broker, because its authentication is not fully configured. It exists to test the check that zio-kafka runs before it connects to a broker with plain-text settings; see [Security](security.md). `KafkaTestUtils.sslAdminSettings` and `KafkaTestUtils.makeSslAdminClient` build an admin client with matching SSL settings.

### Connecting to a Running Broker

`Kafka.local` does not start anything. It provides `Kafka.DefaultLocal`, whose `Kafka#bootstrapServers` returns `localhost:9092`, for a broker that you run yourself, for example in Docker:

```scala
object Kafka {
  val local: ULayer[Kafka]

  case object DefaultLocal extends Kafka
}
```

`DefaultLocal#stop` does nothing, so the broker keeps running after the tests. For a broker at another address, implement the `Kafka` trait yourself; [Test Against an External Broker](writing-tests.md#test-against-an-external-broker) has an example.

### Implementation Classes

`Kafka.EmbeddedKafkaService` is the `Kafka` that the embedded layers provide. It wraps the running embedded-kafka instance (`EmbeddedK`), reports `localhost:<broker port>` as its bootstrap server, and stops the broker in `EmbeddedKafkaService#stop`. You rarely use it directly: the embedded layers create and stop it for you.

## KafkaTestUtils

`KafkaTestUtils` is an object with three kinds of helpers: settings, clients (producers, consumers and admin clients), and helpers that produce or consume records. Every helper reads the broker address from the `Kafka` (or `Kafka.Sasl`) in its environment. The helpers that create a client directly, such as `KafkaTestUtils.makeProducer`, return a scoped effect (`ZIO[Scope & Kafka, Throwable, ...]`) that closes the client when the scope ends. The helpers named after a client, such as `KafkaTestUtils.producer`, return a `ZLayer` instead, for sharing one client between tests. The scaladoc of these layers recommends the scoped effects, which give each test its own client.

### Settings

The settings helpers return the settings that the client helpers use, so you can change them before creating a client yourself:

```scala
object KafkaTestUtils {
  val producerSettings: ZIO[Kafka, Nothing, ProducerSettings]
  val transactionalProducerSettings: ZIO[Kafka, Nothing, TransactionalProducerSettings]
  def transactionalProducerSettings(transactionalId: String): ZIO[Kafka, Nothing, TransactionalProducerSettings]

  def consumerSettings(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    rebalanceSafeCommits: Boolean = false,
    maxRebalanceDuration: Duration = 3.minutes,
    maxPollInterval: Duration = 5.minutes,
    `max.poll.records`: Int = 100,
    commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
    diagnostics: Consumer.ConsumerDiagnostics = Diagnostics.NoOp,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings]

  def transactionalConsumerSettings(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    rebalanceSafeCommits: Boolean = false,
    diagnostics: Consumer.ConsumerDiagnostics = Diagnostics.NoOp,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings]

  val adminSettings: ZIO[Kafka, Nothing, AdminClientSettings]
  def saslAdminSettings(username: String, password: String): ZIO[Kafka.Sasl, Nothing, AdminClientSettings]
  val sslAdminSettings: ZIO[Kafka, Nothing, AdminClientSettings]
}
```

`KafkaTestUtils.producerSettings` and `KafkaTestUtils.adminSettings` are the default settings with the broker's bootstrap servers. The transactional producer settings wrap them with a transactional id. The variant without a parameter always uses the id `test-transaction`, so tests that run in parallel must pass their own id instead.

`KafkaTestUtils.consumerSettings` starts from `ConsumerSettings(bootstrapServers)` and tunes it for tests. On top of the parameters above, it always sets:

| Setting                             | Value     | Why                                                   |
| ----------------------------------- | --------- | ----------------------------------------------------- |
| `ConsumerSettings#withCloseTimeout` | 5 seconds | Tests end quickly.                                    |
| `ConsumerSettings#withPollTimeout`  | 100 ms    | Short polls let streams react quickly.                |
| `metadata.max.age.ms`               | `100`     | New topics and partitions are noticed quickly.        |
| `session.timeout.ms`                | `3000`    | A consumer that stops is removed from its group soon. |
| `heartbeat.interval.ms`             | `1000`    | Must be lower than the session timeout.               |
| `allow.auto.create.topics`          | parameter | Set from `allowAutoCreateTopics`.                     |

The parameters map to the `ConsumerSettings` method of the same name. `clientInstanceId` sets the group instance id (`ConsumerSettings#withGroupInstanceId`), for static membership. `max.poll.records` defaults to 100, lower than Kafka's default of 500; the source notes that higher values can hide concurrency bugs. `properties` is applied last, so it can override any of the values above.

`KafkaTestUtils.transactionalConsumerSettings` calls `KafkaTestUtils.consumerSettings` with a required group id and then sets `ConsumerSettings#withReadCommitted`, so the consumer only reads records of committed transactions. The group id comes first here, and the client id comes first in the other consumer helpers.

`KafkaTestUtils.saslAdminSettings` adds the `SASL_PLAINTEXT`, `PLAIN` and JAAS properties for the given user, as listed under [Secured Embedded Brokers](#secured-embedded-brokers). `KafkaTestUtils.sslAdminSettings` adds `security.protocol=SSL` and the test kit's key store and trust store.

### Producers

The producer helpers create a `Producer` or a `TransactionalProducer` from the settings above:

```scala
object KafkaTestUtils {
  val makeProducer: ZIO[Scope & Kafka, Throwable, Producer]
  val producer: ZLayer[Kafka, Throwable, Producer]

  def makeTransactionalProducer(transactionalId: String, consumer: Consumer): ZIO[Scope & Kafka, Throwable, TransactionalProducer]
  val transactionalProducer: ZLayer[Kafka with Consumer, Throwable, TransactionalProducer]
  def transactionalProducer(transactionalId: String): ZLayer[Kafka with Consumer, Throwable, TransactionalProducer]
}
```

A `TransactionalProducer` works together with a consumer, which it receives as a parameter (or from the environment, for the layers). `TransactionalProducer.make` requires that consumer to have rebalance-safe commits enabled and not to use the `CooperativeStickyAssignor`, see [Consumer Requirements](transactional-producer.md#consumer-requirements). The test kit's consumer helpers default to `rebalanceSafeCommits = false`, so pass `rebalanceSafeCommits = true` to the consumer you pair with a transactional producer. The `transactionalProducer` layer without a parameter uses the fixed id `test-transaction`.

### Consumers

The consumer helpers take the same parameters as the settings helpers (minus `maxPollInterval` and `max.poll.records`) and create a `Consumer`:

```scala
object KafkaTestUtils {
  def makeConsumer(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    rebalanceSafeCommits: Boolean = false,
    maxRebalanceDuration: Duration = 3.minutes,
    commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
    properties: Map[String, String] = Map.empty
  ): ZIO[Scope & Kafka, Throwable, Consumer]

  def consumer(/* same parameters as makeConsumer */): ZLayer[Kafka, Throwable, Consumer]

  def makeTransactionalConsumer(
    clientId: String,
    groupId: String,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    rebalanceSafeCommits: Boolean = false,
    properties: Map[String, String] = Map.empty
    // ...
  ): ZIO[Scope & Kafka, Throwable, Consumer]

  def transactionalConsumer(/* same parameters as makeTransactionalConsumer */): ZLayer[Kafka, Throwable, Consumer]

  @deprecated("Use Consumer.live instead", since = "3.0.0")
  def minimalConsumer(): ZLayer[ConsumerSettings, Throwable, Consumer]
}
```

By default the consumers have no group id and, without a committed offset, start at the earliest offset, so a test reads records that were produced before the consumer started. A consumer that subscribes to topics with `Subscription.topics` or `Subscription.pattern` needs a group, so pass `groupId` for those; a consumer with `Subscription.manual` does not. The `diagnostics` parameter installs a [Diagnostics](diagnostics.md) callback, for example a `SlidingDiagnostics` to check the events a consumer emitted. `KafkaTestUtils.makeTransactionalConsumer` and `KafkaTestUtils.transactionalConsumer` build their settings with `KafkaTestUtils.transactionalConsumerSettings`, so they read committed records only.

`KafkaTestUtils.minimalConsumer` is deprecated. It returns `Consumer.live`, which needs a `ConsumerSettings` layer; use `Consumer.live` directly, or `KafkaTestUtils.makeConsumer`.

### Producing Records

The record helpers produce records with `String` keys and values, using `Serde.string`:

```scala
object KafkaTestUtils {
  def produceOne(producer: Producer, topic: String, key: String, message: String): ZIO[Any, Throwable, RecordMetadata]
  def produceOne(producer: Producer, topic: String, partition: Int, key: String, message: String): ZIO[Any, Throwable, RecordMetadata]

  def produceMany(producer: Producer, topic: String, kvs: Iterable[(String, String)]): ZIO[Any, Throwable, Chunk[RecordMetadata]]
  def produceMany(producer: Producer, topic: String, partition: Int, kvs: Iterable[(String, String)]): ZIO[Any, Throwable, Chunk[RecordMetadata]]

  def scheduledProduce[R](producer: Producer, topic: String, schedule: Schedule[R, Any, Long]): ZStream[R, Throwable, RecordMetadata]

  val kafka19811Workaround: UIO[Unit]
}
```

`KafkaTestUtils.produceOne` produces one record with `Producer#produce`, and `KafkaTestUtils.produceMany` produces all pairs as one chunk with `Producer#produceChunk`. The overloads with a `partition` write every record to that partition; the others let Kafka's partitioner choose. All of them wait until the broker has acknowledged the records.

`KafkaTestUtils.scheduledProduce` is a stream that produces one record each time `schedule` emits, for as long as the stream runs. The key and value are built from the schedule's output `n` as `key<n>` and `msg<n>`. To produce a record every 100 milliseconds in the background of a test, for example while a rebalance happens:

```scala mdoc:compile-only
import zio._
import zio.kafka.producer.Producer
import zio.kafka.testkit.KafkaTestUtils

def produceInBackground(producer: Producer, topic: String): ZIO[Scope, Nothing, Fiber.Runtime[Throwable, Unit]] =
  KafkaTestUtils
    .scheduledProduce(producer, topic, Schedule.fixed(100.millis))
    .runDrain
    .forkScoped
```

`KafkaTestUtils.kafka19811Workaround` sleeps for 5 milliseconds. A record that the broker has acknowledged, even with `acks=all`, is not always visible to consumers right away (see [KAFKA-19811](https://issues.apache.org/jira/browse/KAFKA-19811)), which makes tests that consume right after producing flaky. Every `KafkaTestUtils.produceOne` and `KafkaTestUtils.produceMany` overload, and so `KafkaTestUtils.scheduledProduce` too, runs this sleep after producing. When a test produces with its own `Producer#produce` call and then consumes immediately, run `KafkaTestUtils.kafka19811Workaround` after the produce call. The sleep needs the live clock (`TestAspect.withLiveClock`), which zio-kafka tests need anyway.

### Consuming Records

`KafkaTestUtils.consumeWithStrings` runs `Consumer.consumeWith` with test consumer settings and `String` deserializers, and calls your function for every record:

```scala
object KafkaTestUtils {
  def consumeWithStrings(clientId: String, groupId: Option[String] = None, subscription: Subscription)(
    r: ConsumerRecord[String, String] => URIO[Any, Unit]
  ): RIO[Kafka, Unit]
}
```

Like `Consumer.consumeWith`, the effect commits the offsets of handled records and only completes when it is interrupted, so fork it, or race it with the check that ends the test. A topic subscription needs a group, so pass `groupId`. To collect the values of the first three records of a topic in a `Promise`:

```scala mdoc:compile-only
import org.apache.kafka.clients.consumer.ConsumerRecord
import zio._
import zio.kafka.consumer.Subscription
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }

def firstThreeValues(topic: String): RIO[Kafka, Chunk[String]] =
  for {
    seen <- Ref.make(Chunk.empty[String])
    done <- Promise.make[Nothing, Unit]
    handle = (record: ConsumerRecord[String, String]) =>
               seen.updateAndGet(_ :+ record.value()).flatMap(values => done.succeed(()).when(values.size >= 3)).unit
    _      <- KafkaTestUtils.consumeWithStrings("client", Some("group"), Subscription.topics(topic))(handle).race(done.await)
    values <- seen.get
  } yield values.take(3)
```

### Admin Clients

The admin helpers create an `AdminClient` for setting up topics and checking the broker's state, and create topics directly:

```scala
object KafkaTestUtils {
  def makeAdminClient: ZIO[Scope & Kafka, Throwable, AdminClient]
  def makeSaslAdminClient(username: String = "admin", password: String = "admin-secret"): ZIO[Scope & Kafka.Sasl, Throwable, AdminClient]
  def makeSslAdminClient: ZIO[Scope & Kafka, Throwable, AdminClient]

  def createCustomTopic(topic: String, partitionCount: Int = 1): ZIO[Kafka, Throwable, Unit]
}
```

`KafkaTestUtils.makeSaslAdminClient` logs in as the super user of `Kafka.saslEmbedded` by default; pass another user to test ACLs. `KafkaTestUtils.createCustomTopic` creates a topic with replication factor 1 through a short-lived admin client. Brokers create topics automatically by default, so you only need it to choose the number of partitions, or when the broker does not create topics automatically. See [Admin Client](admin-client.md) for the operations of `AdminClient`.

## KafkaRandom

`KafkaRandom` generates unique names, so that tests that share a broker use different topics, consumer groups and client ids. Mix it into a spec and define `kafkaPrefix`:

```scala
trait KafkaRandom {
  def kafkaPrefix: String

  def randomThing(prefix: String): Task[String]
  def randomTopic: Task[String]
  def randomGroup: Task[String]
  def randomClient: Task[String]
}
```

`KafkaRandom#randomThing` returns `<prefix>-<random UUID>`. `KafkaRandom#randomTopic`, `KafkaRandom#randomGroup` and `KafkaRandom#randomClient` call it with `<kafkaPrefix>-topic`, `<kafkaPrefix>-group` and `<kafkaPrefix>-client`. Use `KafkaRandom#randomThing` for other names, such as transactional ids. The UUIDs come from `java.util.UUID`, not from ZIO's `Random`, so the names differ between tests even without the `TestAspect.withLiveRandom` aspect. Give each suite its own `kafkaPrefix`, so you can tell from a topic name which suite created it.

## ZIOSpecWithKafka

`ZIOSpecWithKafka` is a ZIO Test spec whose bootstrap layer adds a `Kafka.embedded` broker to the test environment:

```scala
trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {
  override val bootstrap: ZLayer[Any, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded
}
```

When the sbt test runner runs several spec objects that extend it, it combines their bootstrap layers into one runtime and builds the shared `Kafka.embedded` layer only once, so all these suites use one broker. A spec that extends it declares `Kafka` in the environment of `spec` (`Spec[TestEnvironment & Scope & Kafka, Any]`). [Share the Broker Between Tests](writing-tests.md#share-the-broker-between-tests) shows it in use; the ZIO Test documentation explains [sharing layers between multiple files](https://zio.dev/reference/test/sharing-layers-between-multiple-files/).
