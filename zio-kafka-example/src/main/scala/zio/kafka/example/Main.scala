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

  // TODO: Use your own server
  private val boostrapServers = ???

  private def consumerLayer: ZLayer[Any, Throwable, Consumer] = {
    val consumerSettings =
      ConsumerSettings(boostrapServers)
        .withPollTimeout(500.millis)
        .withGroupId("test")
        .withProperties(
          "max.poll.records" -> "1000"
        )

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
                     .plainStream(Subscription.topics("wikipedia.parsed"), Serde.string, Serde.string)
                     .provideLayer(consumerLayer)
          _        <- ZIO.logInfo(s"Consuming messages...")
          consumed <- stream.take(100000).runCount
          _        <- ZIO.logInfo(s"Consumed $consumed records")
        } yield ()
      )

}
