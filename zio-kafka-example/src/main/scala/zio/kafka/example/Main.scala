package zio.kafka.example

import io.github.embeddedkafka.EmbeddedKafka
import zio._
import zio.kafka.KafkaTestUtils.{ produceMany, producer }
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  /**
   * See `zio-logging` documentation: https://zio.github.io/zio-logging/docs/overview/overview_slf4j
   */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private def consumerLayer(kafka: Kafka): ZLayer[Any, Throwable, Consumer] = {
    val consumerSettings = ConsumerSettings(kafka.bootstrapServers).withGroupId("test")

    ZLayer.make[Consumer](
      ZLayer.succeed(consumerSettings),
      ZLayer.succeed(Diagnostics.NoOp),
      Consumer.live
    )
  }

  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    (
      for {
        _     <- ZIO.logInfo(s"Starting app")
        kafka <- ZIO.service[Kafka]
        topic = "test"
        data  = List.tabulate(1000)(i => (s"key$i", s"msg$i"))
        _               <- ZIO.logInfo(s"Producing messages...")
        _               <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = 6))
        producedRecords <- produceMany(topic, data).provideLayer(producer)
        _               <- ZIO.logInfo(s"Produced ${producedRecords.size} messages")
        stream = Consumer
                   .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                   .provideLayer(consumerLayer(kafka))
        _        <- ZIO.logInfo(s"Consuming messages...")
        consumed <- stream.take(1000).runCount
        _        <- ZIO.logInfo(s"Consumed $consumed records")
      } yield ()
    ).provideSomeLayer[ZIOAppArgs with Scope](Kafka.embedded)

}
