package zio.kafka.example

import zio._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.serde.Serde
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  /**
   * See `zio-logging` documentation: https://zio.github.io/zio-logging/docs/overview/overview_slf4j
   */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val topic = "test-topic"

  private def consumerSettings: ZLayer[MyKafka, Throwable, ConsumerSettings] = ZLayer {
    for {
      kafka <- ZIO.service[MyKafka]
    } yield ConsumerSettings(kafka.bootstrapServers)
      .withPollTimeout(500.millis)
      .withGroupId("test")
  }

  private val runConsumerStream: ZIO[Consumer, Throwable, Unit] =
    for {
      _        <- ZIO.logInfo("Consuming messages...")
      consumer <- ZIO.service[Consumer]
      consumed <- consumer
                    .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                    .take(1000)
                    .tap(r => ZIO.logInfo(s"Consumed record $r"))
                    .runCount
      _ <- ZIO.logInfo(s"Consumed $consumed records")
    } yield ()

  override def run: ZIO[Scope, Any, Any] =
    ZIO.logInfo("Starting app") *>
      ZIO.addFinalizer(ZIO.logInfo("Stopping app")) *>
      runConsumerStream
        .provide(
          consumerSettings,
          ZLayer.succeed(Consumer.NoDiagnostics),
          Consumer.live,
          MyKafka.embedded
        )

}
