package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.openjdk.jmh.annotations._
import zio.kafka.KafkaTestUtils.{ consumerSettings, produceMany, producer, simpleConsumer }
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{ durationInt, ULayer, ZIO, ZLayer }

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._

object ConsumersComparisonBenchmark {
  type LowLevelKafka = KafkaConsumer[Array[Byte], Array[Byte]]

  type Env = Kafka with Consumer with Producer with LowLevelKafka with ConsumerSettings
}
import zio.kafka.bench.ConsumersComparisonBenchmark._

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ConsumersComparisonBenchmark extends ZioBenchmark[Env] {
  val topic1                          = "topic1"
  val nrPartitions                    = 6
  val nrMessages                      = 1000000
  val kvs: Iterable[(String, String)] = Iterable.tabulate(nrMessages)(i => (s"key$i", s"msg$i"))

  val kafkaConsumer: ZLayer[ConsumerSettings, Throwable, LowLevelKafka] =
    ZLayer.scoped {
      ZIO.acquireRelease {
        ZIO.service[ConsumerSettings].flatMap { settings =>
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

  val settings: ZLayer[Kafka, Nothing, ConsumerSettings] =
    ZLayer.fromZIO(
      consumerSettings(
        clientId = randomThing("client"),
        groupId = Some(randomThing("client")),
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
        ZIO.service[LowLevelKafka].flatMap { consumer =>
          ZIO.attemptBlocking {
            consumer.subscribe(java.util.Arrays.asList(topic1))

            var count = 0L
            while (count < nrMessages) {
              val records = consumer.poll(settings.pollTimeout)
              count += records.count()
            }

            consumer.unsubscribe()
          }
        }
      }
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def zioKafka(): Any =
    runZIO {
      Consumer
        .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
        .take(nrMessages.toLong)
        .runDrain
    }
}
