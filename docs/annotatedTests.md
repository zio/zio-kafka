# Annotated Tests

By way of examples, we present some annotated tests from the test suite.

Tests are written using zio-test.

# General Notes

The tests make use of KafkaTestUtils.scala which comprises a number of helper methods
for testing zio-kafka. You may find it useful to copy this file into your own test
folder for writing your kafka-based tests (there is no zio-test-utils project
at present). Relevant portions of the KafkaTestUtils will be introduced as we work
through the tests.

# First Producer Test
```scala
object ProducerSpec extends DefaultRunnableSpec {
  override def spec =
    suite("producer test suite")(
      test("one record") {
        for {
          _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
        } yield assertCompletes
      },
      // ...
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded >>> producer) ++
        (Kafka.embedded >>> transactionalProducer) ++
        Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    )
}
```

First note the `.provideSomeLayerShared`. This gives the tests a `Kafka` service
added to a full `TestEnvironment` (this is needed because we want to provide both
Live clock and the Kafka service)

### Kafka service

This follows the module pattern (see main ZIO docs)

```scala
trait Kafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}

object Kafka {
  case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka.Service {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
  }

  case object DefaultLocal extends Kafka.Service {
    override def bootstrapServers: List[String] = List(s"localhost:9092")
    override def stop(): UIO[Unit]              = UIO.unit
  }

  val embedded: ZLayer[Any, Throwable, Has[Kafka]] = ZLayer.fromManaged {
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      customBrokerProperties = Map("group.min.session.timeout.ms" -> "500", "group.initial.rebalance.delay.ms" -> "0")
    )
    ZManaged.acquireReleaseWith(ZIO.attempt(EmbeddedKafkaService(EmbeddedKafka.start())))(_.stop())
  }

  val local: ZLayer[Any, Nothing, Has[Kafka]] = ZLayer.succeed(DefaultLocal)
```

In fact there are 2 provided implementations of service. The first is for the unit
tests and makes use of [EmbeddedKafka](https://github.com/embeddedkafka/embedded-kafka)

The second uses the default local port and is suitable for a stand-alone kafka
(I used docker installation). You could create your own `Kafka` service for testing
against remote servers (but security would need to be added).

Note the use of ZManaged to ensure the service is also stopped.

### Kafka Test Environment

```scala
object KafkaTestUtils {

  val producerSettings: ZIO[Has[Kafka], Nothing, ProducerSettings] =
    ZIO.access[Has[Kafka]](_.get[Kafka].bootstrapServers).map(ProducerSettings(_))

  val producer: ZLayer[Has[Kafka], Throwable, Has[Producer]] =
    (ZLayer.fromZIO(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) >>>
      Producer.live

  val transactionalProducerSettings: ZIO[Has[Kafka], Nothing, TransactionalProducerSettings] =
    ZIO.access[Has[Kafka]](_.get[Kafka].bootstrapServers).map(TransactionalProducerSettings(_, "test-transaction"))

  val transactionalProducer: ZLayer[Has[Kafka], Throwable, Has[TransactionalProducer]] =
    (ZLayer.fromZIO(transactionalProducerSettings) ++ ZLayer.succeed(
      Serde.string: Serializer[Any, String]
    )) >>>
      TransactionalProducer.live

  // ...
```

#### Back to the producer

These `ZLayer`s are provided to the ZIO environment for the `ProducerSpec` shown
above, along with the Live clock (which is essential when running code that
uses scheduling or other timing features - as does much of zio-kafka)

With the resource management encapsulated in the layers, the actual producer
operation function is simply used from the environment
```scala
        for {
          _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
        } yield // ...
```
`Producer.produce` takes a `ProducerRecord` (defined in the Java Kafka client on which
this library is based). In this case the topic is "topic" and the key and value
"boo" and "baa", corresponding to the given string de/serializers.
