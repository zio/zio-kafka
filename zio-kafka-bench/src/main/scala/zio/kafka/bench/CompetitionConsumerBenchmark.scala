package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.{ ConsumerConfig, KafkaConsumer }
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.openjdk.jmh.annotations._
import zio.kafka.KafkaTestUtils.{ consumerSettings, produceMany, producer, simpleConsumer }
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{ durationInt, ULayer, ZIO, ZLayer }

import java.time.Duration
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.MapHasAsJava

object CompetitionConsumerBenchmark {
  type LowLevelKafka = KafkaConsumer[Array[Byte], Array[Byte]]
}
import zio.kafka.bench.CompetitionConsumerBenchmark._

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class CompetitionConsumerBenchmark extends ZioBenchmark[Kafka with Producer with Consumer with LowLevelKafka] {
  val topic1       = "topic1"
  val nrPartitions = 6
  val nrMessages   = 50000
  val kvs          = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

  val kafkaConsumer: ZLayer[ConsumerSettings, Throwable, LowLevelKafka] =
    ZLayer.scoped {
      ZIO.acquireRelease {
        ZIO.attemptBlocking {
          ZIO.serviceWith[ConsumerSettings] { settings =>
            new KafkaConsumer[Array[Byte], Array[Byte]](
              settings.driverSettings.asJava,
              new ByteArrayDeserializer(),
              new ByteArrayDeserializer()
            )
          }
        }.flatten
      }(c => ZIO.attemptBlocking(c.close()).orDie)
    }

  override protected def bootstrap: ULayer[
    Kafka with Producer with ConsumerSettings with Consumer with LowLevelKafka
  ] =
    ZLayer
      .make[Kafka with Producer with ConsumerSettings with Consumer with LowLevelKafka](
        Kafka.embedded,
        producer,
        ZLayer.fromZIO(
          consumerSettings(
            clientId = randomThing("client"),
            groupId = Some(randomThing("client")),
            properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1000")
          )
        ),
        simpleConsumer(),
        kafkaConsumer
      )
      .orDie

  override def initialize: ZIO[Kafka with Producer, Throwable, Any] =
    for {
      _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
      _ <- produceMany(topic1, kvs)
    } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def kafkaClients(): Any =
    runZIO {
      ZIO.service[LowLevelKafka].flatMap { consumer =>
        ZIO.attemptBlocking {
          consumer.subscribe(java.util.Arrays.asList(topic1))

          var count = 0L
          while (count < nrMessages) {
            val records = consumer.poll(Duration.ofMillis(1000))
            count += records.count()
          }

          consumer.unsubscribe()
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
        .timeoutFail(new RuntimeException("Timeout"))(30.seconds)
    }
}
