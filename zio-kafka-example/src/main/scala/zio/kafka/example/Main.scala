package zio.kafka.example

import zio._
import zio.kafka.consumer.diagnostics.Diagnostics
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

  private def consumerLayer: ZLayer[Any, Throwable, Consumer] = {
    val consumerSettings =
      ConsumerSettings(List("localhost:9092"))
        .withPollTimeout(500.millis)
        .withGroupId("test")

    ZLayer.make[Consumer](
      ZLayer.succeed(consumerSettings),
      ZLayer.succeed(Diagnostics.NoOp),
      Consumer.live
    )
  }

  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    ZIO.addFinalizer(ZIO.logInfo("Stopping app")) *>
      (
        for {
          _ <- ZIO.logInfo(s"Starting app")
          stream = Consumer
                     .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                     .provideLayer(consumerLayer)
          _        <- ZIO.logInfo(s"Consuming messages...")
          consumed <- stream.take(1000).tap(r => ZIO.logInfo(s"Consumed record $r")).runCount
          _        <- ZIO.logInfo(s"Consumed $consumed records")
        } yield ()
      )

}
