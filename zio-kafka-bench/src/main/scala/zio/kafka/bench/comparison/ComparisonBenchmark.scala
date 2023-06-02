package zio.kafka.bench.comparison

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import zio.kafka.admin.AdminClient.TopicPartition
import zio.kafka.bench.ZioBenchmark
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.bench.comparison.ComparisonBenchmark._
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.producer.Producer
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils.{ consumerSettings, minimalConsumer, produceMany, producer }
import zio.{ durationInt, ULayer, ZIO, ZLayer }

import scala.jdk.CollectionConverters._

trait ComparisonBenchmark extends ZioBenchmark[Env] {

  protected final val topic1: String    = "topic1"
  protected final val nrPartitions: Int = 6
  protected final val topicPartitions: List[TopicPartition] =
    (0 until nrPartitions).map(TopicPartition(topic1, _)).toList
  protected final val numberOfMessages: Int           = 1000000
  protected final val kvs: Iterable[(String, String)] = Iterable.tabulate(numberOfMessages)(i => (s"key$i", s"msg$i"))

  private val javaKafkaConsumer: ZLayer[ConsumerSettings, Throwable, LowLevelKafka] =
    ZLayer.scoped {
      ZIO.acquireRelease {
        ZIO.serviceWithZIO[ConsumerSettings] { settings =>
          ZIO.attemptBlocking {
            new KafkaConsumer[Array[Byte], Array[Byte]](
              settings.driverSettings.asJava,
              new ByteArrayDeserializer(),
              new ByteArrayDeserializer()
            )
          }
        }
      }(c => ZIO.attemptBlocking(c.close()).orDie)
    }

  protected def settings: ZLayer[Kafka, Nothing, ConsumerSettings] =
    ZLayer.fromZIO(
      consumerSettings(
        clientId = randomThing("client"),
        groupId = Some(randomThing("client")),
        `max.poll.records` = 1000, // A more production worthy value
        runloopTimeout =
          1.hour // Absurdly high timeout to avoid the runloop from being interrupted while we're benchmarking other stuff
      )
    )

  override final def bootstrap: ULayer[Env] =
    ZLayer
      .make[Env](
        Kafka.embedded,
        producer,
        settings,
        javaKafkaConsumer,
        minimalConsumer()
      )
      .orDie

  override final def initialize: ZIO[Env, Throwable, Any] =
    for {
      _ <- ZIO.succeed(EmbeddedKafka.deleteTopics(List(topic1))).ignore
      _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
      _ <- produceMany(topic1, kvs)
    } yield ()

}

object ComparisonBenchmark {
  type LowLevelKafka = KafkaConsumer[Array[Byte], Array[Byte]]

  type Env = Kafka with Consumer with Producer with LowLevelKafka with ConsumerSettings

  def zAssert(p: => Boolean, message: => String): ZIO[Any, AssertionError, Unit] =
    ZIO.when(!p)(ZIO.fail(new AssertionError(s"Assertion failed: $message"))).unit
}
