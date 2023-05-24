---
id: writing-tests
title: "Writing Tests with the `zio-kafka-testkit` library"
---

zio-kafka provides a `zio-kafka-testkit` library to help you test your code using zio-kafka.

To add it in your project, add the following dependency in your `build.sbt`:
```scala
libraryDependencies += "dev.zio" %% "zio-kafka-testkit" % "<latest-version>" % Test
```

Let's study some examples of tests you can write with the `zio-kafka-testkit` and `zio-test` and let's see what this library provides you.

## Testing a producer

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils._ // An object containing several utilities to simplify writing your tests
import zio.kafka.testkit.Kafka // A trait representing a Kafka instance in your tests
import zio.test.TestAspect.{ sequential, timeout }
import zio.test._

object ProducerSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Producer test suite")(
        test("minimal example") {
          for {
            _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
          } yield assertCompletes
        }
        // ... more tests ...
      )
        .provideSome[Kafka](producer)             // Here, we provide a new instance of Producer per test
        .provideSomeShared[Scope](Kafka.embedded) // Here, we provide an instance of Kafka for the entire suite
    ) @@ timeout(2.minutes) @@ sequential
}
```

This test is a very minimal example.    
It uses the `Producer.produce` method from zio-kafka to produce a record to the Kafka cluster.    
The `assertCompletes` assertion from zio-test is used to check that the effect completes successfully.    

In this example, we decided to instantiate a new `Producer` for each test, with the `.provideSome[Kafka](producer)`.      
We could have decided to share one instance of `Producer` between all the tests of this suite by moving the `producer` layer to the `provideSomeShared`, which would have
looked like this:
```scala
suite("producer test suite")(
  // ... tests ...
).provideSomeShared[Scope](Kafka.embedded, producer)
```
This `producer` layer comes from the `KafkaTestUtils` object in zio-kafka-testkit. It is a layer that bootstraps a `Producer` instance.

In this example, we decided to share an instance of Kafka for the entire suite, with the `.provideSomeShared[Scope](Kafka.embedded)`.     
Kafka is slow to start, so it is better to only start it once and share it between all tests of the suite.     

We could have decided to instantiate a new instance of Kafka for each test by moving the `Kafka.embedded` layer to the `provideSome`, which would have
looked like this:
```scala
suite("producer test suite")(
  // ... tests ...
).provideSome[Scope](Kafka.embedded, producer)
```

We could also have decided to share one instance of `Kafka` between different test suites (i.e. between different test files) by mixing the `ZIOSpecWithKafka` trait,
which would have looked like this:
```scala
object ProducerSpec extends ZIOSpecWithKafka { // Note the `ZIOSpecWithKafka` trait usage here instead of `ZIOSpecDefault`
  override def spec: Spec[TestEnvironment & Kafka, Any] =
    (
      suite("Producer test suite")(
        // ... tests ...
      )
        .provideSome[Kafka](producer)             // No need here to provide a Kafka instance, it is already provided by the `ZIOSpecWithKafka` trait
    ) @@ timeout(2.minutes) @@ sequential
}
```
More details about this `ZIOSpecWithKafka` trait [below](#ziospecwithkafka-trait).

Finally, we annotate the suite with the `timeout` and `sequential` aspects.   
The `timeout` aspect from zio-test is used to specify a timeout for the entire suite. If the suite takes more than 5 minutes to run, it will fail.    
The `sequential` aspect from zio-test is used to specify that the tests in the suite must be run sequentially. This is necessary because Kafka is a shared resource.
We don't want tests to interfere with each other.

## Testing a consumer

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils.{ consumer, produceMany, producer }
import zio.kafka.testkit._
import zio.test.Assertion.hasSameElements
import zio.test.TestAspect.{ sequential, timeout }
import zio.test._

object ConsumerSpec extends ZIOSpecDefault with KafkaRandom {
  override def kafkaPrefix: String = "consumer-spec"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Consumer test suite")(
        test("minimal example") {
          val kvs: List[(String, String)] = (1 to 5).toList.map(i => (s"key-$i", s"msg-$i"))
          for {
            topic  <- randomTopic
            client <- randomClient
            group  <- randomGroup

            _ <- produceMany(topic, kvs) // Comes from `KafkaTestUtils`. Produces messages to the topic.

            records <- Consumer
                         .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                         .take(5)
                         .runCollect
                         .provideSome[Kafka](
                           // Comes from `KafkaTestUtils`
                           consumer(clientId = client, groupId = Some(group))
                         )
            consumed = records.map(r => (r.record.key, r.record.value)).toList
          } yield assert(consumed)(hasSameElements(kvs))
        },
        // ... more tests ...
      )
        .provideSome[Kafka](producer)             // Here, we provide a new instance of Producer per test
        .provideSomeShared[Scope](Kafka.embedded) // Here, we provide an instance of Kafka for the entire suite
    ) @@ timeout(2.minutes) @@ sequential
}
```

This test is also a quite minimal example.    
We produce 5 messages thanks to the `KafkaTestUtils.produceMany` method from zio-kafka-testkit, then we consume them with the `Consumer.plainStream` method from zio-kafka.     
Finally, we use the `hasSameElements` assertion from zio-test to check that the consumed records are the same as the ones we produced.

In this example, we're reusing the `producer` and the `Kafka.embedded` layers we've seen in the [Producer test example](#testing-a-producer).    
We're also using the `KafkaTestUtils.consumer` layer from zio-kafka-testkit to instantiate a new `Consumer`.    

Finally, we use the `KafkaRandom` trait from zio-kafka-testkit and its methods to generate random values for the Consumer client ID, the Consumer group ID and the topic name.     
More details about this `KafkaRandom` trait [later in this page](#kafkarandom-trait).    
Using random values for these parameters is important to avoid conflicts between tests as we share one Kafka instance between all the tests of the suite.

## Utilities provided by the `zio-kafka-testkit` library

### `Kafka` service

This trait represents a Kafka instance in your tests.    
It is used to provide the bootstrap servers to the `Producer` and `Consumer` layers.

```scala
trait Kafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}
```

The companion object provides a few layers to provide a Kafka instance in your tests:
```scala
object Kafka {
  /**
   * Creates an in-memory Kafka instance with a random port.
   */
  val embedded: ZLayer[Any, Throwable, Kafka]

  /**
   * Will connect to a Kafka instance running on localhost:9092 (with Docker, for example).
   */
  val local: ULayer[Kafka]

  /**
   * Creates an in-memory Kafka instance with a random port and SASL authentication configured.
   */
  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl]

  /**
   * Creates an in-memory Kafka instance with a random port and SSL authentication configured.
   */
  val sslEmbedded: ZLayer[Any, Throwable, Kafka]
}
```

The in-memory Kafka instances are created using [embedded-kafka](https://github.com/embeddedkafka/embedded-kafka).

### `KafkaTestUtils` utilities

This object provides several utilities to simplify writing your tests, like layers to boot a `Producer`, a `Consumer`, or an `AdminClient`.     
It also provides several functions to produce records, and more.     
Each utility function is documented in the source code. Please have a look at the source code for more details.     
You can also look at `zio-katka` tests in the `zio-kafka-test` module to have examples on how to use these utilities.    

### `ZIOSpecWithKafka` trait

This trait can be used if you want to share one Kafka instance between different test suites.    
This allows you to speed up your tests by booting a Kafka instance only once for all your test suites using this trait.

Usage example:
```scala
// In `src/test/scala/io/example/producer/ProducerSpec.scala`
object ProducerSpec extends ZIOSpecWithKafka { // Note the `ZIOSpecWithKafka` trait usage here instead of `ZIOSpecDefault`
  override def spec: Spec[TestEnvironment & Kafka, Any] =
    (
      suite("Producer test suite")(
        // ... tests ...
      )
        .provideSome[Kafka](producer)             // No need here to provide a Kafka instance, it is already provided by the `ZIOSpecWithKafka` trait
    ) @@ timeout(2.minutes) @@ sequential
}

// In `src/test/scala/io/example/consumer/ConsumerSpec.scala`
object ConsumerSpec extends ZIOSpecWithKafka { // Note the `ZIOSpecWithKafka` trait usage here instead of `ZIOSpecDefault`
  override def spec: Spec[TestEnvironment & Kafka, Any] =
    (
      suite("Consumer test suite")(
        // ... tests ...
      )
        .provideSome[Kafka](producer)             // No need here to provide a Kafka instance, it is already provided by the `ZIOSpecWithKafka` trait
    ) @@ timeout(2.minutes) @@ sequential
}
```

This is a capability offered by ZIO2.    
See related zio-test documentation: https://zio.dev/reference/test/sharing-layers-between-multiple-files/

### `KafkaRandom` trait

The `KafkaRandom` trait provides a few methods to generate random values.
To use it, you need to mix it in your test suite, like this:
```scala mdoc:compile-only
import zio.kafka.consumer.Consumer
import zio.kafka.testkit.KafkaRandom
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils.consumer
import zio.test.{ assertTrue, Spec, TestEnvironment, ZIOSpecDefault }
import zio._

object MyServiceSpec extends ZIOSpecDefault with KafkaRandom {
  // Required when mixing in the `KafkaRandom` trait
  // The best is to use a different prefix for each test suite
  override def kafkaPrefix: String = "my-service" 

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("MyService")(
      test("minimal example") {
        for {
          group    <- randomGroup // Comes from `KafkaRandom`
          clientId <- randomClient // Comes from `KafkaRandom`
          metrics  <- Consumer.metrics
                        .provideSome[Kafka](
                          // Comes from `KafkaTestUtils`
                          consumer(clientId = clientId, groupId = Some(group))
                        )
        } yield assertTrue(metrics.nonEmpty)
      }
    ).provideSomeShared[Scope](Kafka.embedded)
}
```