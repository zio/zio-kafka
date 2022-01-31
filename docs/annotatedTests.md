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
object ProducerTest
    extends DefaultRunnableSpec(
      suite("consumer test suite")(
        testM("one record") {
          withProducerStrings { producer =>
            for {
              _ <- producer.produce(new ProducerRecord("topic", "boo", "baa"))
            } yield assertCompletes
          }
        }
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
    )
```

First note the .provideManagedShared. This gives the tests a Kafka.Service
added to a full TestEnvironment (this is needed because we want to provide both
Live clock and the Kafka service)

### Kafka.Service

This follows the module pattern (see main zio docs)
```scala
object Kafka {
  trait Service {
    def bootstrapServers: List[String]
    def stop(): UIO[Unit]
  }
  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka.Service {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit] = ZIO.effectTotal(embeddedK.stop(true))
  }

  case object DefaultLocal extends Kafka.Service {
    override def bootstrapServers: List[String] = List(s"localhost:9092")

    override def stop(): UIO[Unit] = UIO.unit
  }

  val makeEmbedded: Managed[Nothing, Kafka] =
    ZManaged.make(ZIO.effectTotal(new Kafka {
      override val kafka: Service = EmbeddedKafkaService(EmbeddedKafka.start())
    }))(_.kafka.stop())

  val makeLocal: Managed[Nothing, Kafka] =
    ZManaged.make(ZIO.effectTotal(new Kafka {
      override val kafka: Service = DefaultLocal
    }))(_.kafka.stop())
```

In fact there are 2 provided implementations of service. The first is for the unit
tests and makes use of [EmbeddedKafka](https://github.com/embeddedkafka/embedded-kafka)

The second uses the default local port and is suitable for a stand-alone kafka
(I used docker installation). You could create your own Kafka.Service for testing
against remove servers (but security would need to be added).

Note the use of ZManaged to ensure the service is also stopped.

### KafkaTestEnvironment

```scala
object KafkaTestUtils {

 def kafkaEnvironment(kafkaE: Managed[Nothing, Kafka]): Managed[Nothing, KafkaTestEnvironment] =
    for {
      testEnvironment <- TestEnvironment.Value
      kafkaS <- kafkaE
    } yield new TestEnvironment(
      testEnvironment.blocking,
      testEnvironment.clock,
      testEnvironment.console,
      testEnvironment.live,
      testEnvironment.random,
      testEnvironment.sized,
      testEnvironment.system
    ) with Kafka {
      val kafka = kafkaS.kafka
    }

  val embeddedKafkaEnvironment: Managed[Nothing, KafkaTestEnvironment] =
    kafkaEnvironment(Kafka.makeEmbedded)
```

## Back to the producer
The producer function is wrapped in a withProducerStrings:

```scala
  def producerSettings =
    for {
      servers <- ZIO.access[Kafka](_.kafka.bootstrapServers)
    } yield ProducerSettings(
      servers,
      5.seconds,
      Map.empty
    )

  def withProducer[A, K, V](
    r: Producer[Any, K, V] => RIO[Any with Clock with Kafka with Blocking, A],
    kSerde: Serde[Any, K],
    vSerde: Serde[Any, V]
  ): RIO[KafkaTestEnvironment, A] =
    for {
      settings <- producerSettings
      producer = Producer.make(settings, kSerde, vSerde)
      lcb      <- Kafka.liveClockBlocking
      produced <- producer.use { p =>
                   r(p).provide(lcb)
                 }
    } yield produced

  def withProducerStrings[A](r: Producer[Any, String, String] => RIO[Any with Clock with Kafka with Blocking, A]) =
    withProducer(r, Serde.string, Serde.string)
```
withProducerStrings simply wraps withProducer with the (String, String) type

withProducer creates settings and a ZManaged\[Producer\]. It then creates liveClockBlocking - a
zio environment with the Live clock (which is essential when running code that
uses scheduling or other timing features - as does much of zio-kafka)

The actual producer operation function is simply wrapped in the producer.use
```scala
              _ <- producer.produce(new ProducerRecord("topic", "boo", "baa"))
```
producer.produce takes a ProducerRecord (defined in the java kafka client on which
this library ios based). In this case the topic is "topic" and the key and value
boo and baa




