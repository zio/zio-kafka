package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.openjdk.jmh.annotations._
import zio.kafka.admin.AdminClient.TopicPartition
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils.{ consumerSettings, produceMany, producer, simpleConsumer }
import zio.{ durationInt, ULayer, ZIO, ZLayer }

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._

object ConsumersComparisonBenchmark {
  type LowLevelKafka = KafkaConsumer[Array[Byte], Array[Byte]]

  type Env = Kafka with Consumer with Producer with LowLevelKafka with ConsumerSettings

  def zAssert(p: => Boolean, message: => String): ZIO[Any, AssertionError, Unit] =
    ZIO.when(!p)(ZIO.fail(new AssertionError(s"Assertion failed: $message"))).unit
}
import zio.kafka.bench.ConsumersComparisonBenchmark._

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ConsumersComparisonBenchmark extends ZioBenchmark[Env] {
  private val topic1: String                        = "topic1"
  private val nrPartitions: Int                     = 6
  private val topicPartitions: List[TopicPartition] = (0 until nrPartitions).map(TopicPartition(topic1, _)).toList
  private val numberOfMessages: Int                 = 1000000
  private val kvs: Iterable[(String, String)]       = Iterable.tabulate(numberOfMessages)(i => (s"key$i", s"msg$i"))

  private val kafkaConsumer: ZLayer[ConsumerSettings, Throwable, LowLevelKafka] =
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

  private val settings: ZLayer[Kafka, Nothing, ConsumerSettings] =
    ZLayer.fromZIO(
      consumerSettings(
        clientId = randomThing("client"),
        groupId = Some(randomThing("client")),
        `max.poll.records` = 1000, // A more production worthy value
        runloopTimeout =
          1.hour // Absurdly high timeout to avoid the runloop from being interrupted while we're benchmarking other stuff
      )
    )

  override protected def bootstrap: ULayer[Env] =
    ZLayer
      .make[Env](
        Kafka.embedded,
        producer,
        settings,
        kafkaConsumer,
        simpleConsumer()
      )
      .orDie

  override def initialize: ZIO[Env, Throwable, Any] =
    for {
      _ <- ZIO.succeed(EmbeddedKafka.deleteTopics(List(topic1))).ignore
      _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
      _ <- produceMany(topic1, kvs)
    } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def kafkaClients(): Any =
    runZIO {
      ZIO.service[ConsumerSettings].flatMap { settings =>
        ZIO.serviceWithZIO[LowLevelKafka] { consumer =>
          ZIO.attemptBlocking {
            consumer.subscribe(java.util.Arrays.asList(topic1))

            var count = 0L
            while (count < numberOfMessages) {
              val records = consumer.poll(settings.pollTimeout)
              count += records.count()
            }

            consumer.unsubscribe()
            count
          }.flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
        }
      }
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def manualKafkaClients(): Any =
    runZIO {
      ZIO.service[ConsumerSettings].flatMap { settings =>
        ZIO
          .serviceWithZIO[LowLevelKafka] { consumer =>
            ZIO.attemptBlocking {
              consumer.assign(topicPartitions.map(_.asJava).asJava)

              var count = 0L
              while (count < numberOfMessages) {
                val records = consumer.poll(settings.pollTimeout)
                count += records.count()
              }

              consumer.unsubscribe()
              count
            }.flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
          }
      }
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def zioKafka(): Any =
    runZIO {
      Consumer
        .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
        .take(numberOfMessages.toLong)
        .runCount
        .flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def manualZioKafka(): Any =
    runZIO {
      Consumer
        .plainStream(
          Subscription.manual(topicPartitions.map(tp => tp.name -> tp.partition): _*),
          Serde.byteArray,
          Serde.byteArray
        )
        .take(numberOfMessages.toLong)
        .runCount
        .flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
    }
}
