package zio.kafka.bench

import org.apache.kafka.clients.consumer.{ Consumer => IConsumer, KafkaConsumer }
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.{ Scope => _, _ }

import scala.jdk.CollectionConverters._

object Layers {

  type LowLevelKafka = IConsumer[Array[Byte], Array[Byte]]

  val embeddedKafka: ZLayer[Any, Nothing, Kafka] =
    Kafka.embedded.orDie

  def makeJavaKafkaConsumer(consumerSettings: ConsumerSettings): ZIO[zio.Scope, Throwable, LowLevelKafka] =
    ZIO.acquireRelease {
      ZIO.attemptBlocking {
        val wrapped = new KafkaConsumer[Array[Byte], Array[Byte]](
          consumerSettings.driverSettings.asJava,
          new ByteArrayDeserializer(),
          new ByteArrayDeserializer()
        )
        new SlowKafkaConsumer(wrapped, 5.millis)
      }
    }(c => ZIO.attemptBlocking(c.close()).orDie)

  def javaKafkaConsumer: ZLayer[ConsumerSettings, Nothing, LowLevelKafka] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[ConsumerSettings]
        consumer <- makeJavaKafkaConsumer(settings)
      } yield consumer
    }.orDie

  def makeConsumerSettings: ZIO[Kafka, Nothing, ConsumerSettings] =
    KafkaTestUtils
      .consumerSettings(
        clientId = randomThing("client"),
        groupId = Some(randomThing("client")),
        `max.poll.records` = 1000
      )
      .map(_.withPartitionPreFetchBufferLimit(8192))

  def consumerSettings: ZLayer[Kafka, Nothing, ConsumerSettings] =
    ZLayer.fromZIO(makeConsumerSettings)

  val makeConsumer: ZIO[zio.Scope & Kafka, Throwable, Consumer] =
    for {
      // Weird workaround, without the following line, the
      // zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits
      // benchmark locks up after the first iteration:
      _ <- ZIO.addFinalizer(ZIO.sleep(10.millis))

      settings     <- Layers.makeConsumerSettings
      javaConsumer <- Layers.makeJavaKafkaConsumer(settings)
      access       <- Semaphore.make(1L)
      consumer     <- Consumer.fromJavaConsumerWithPermit(javaConsumer, settings, access, Diagnostics.NoOp)
    } yield consumer

  val consumerLayer: ZLayer[Kafka, Nothing, Consumer] =
    ZLayer.scoped[Kafka](makeConsumer).orDie

}
