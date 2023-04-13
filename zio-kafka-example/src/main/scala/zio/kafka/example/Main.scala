package zio.kafka.example

import zio._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.serde.Serde

object Main extends ZIOAppDefault {

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
    ZIO.logLevel(LogLevel.Debug) {
      ZIO.addFinalizer(ZIO.logInfo("Stopping app")) *>
        (
          for {
            _ <- ZIO.logInfo(s"Starting app")
            stream = Consumer
                       .plainStream(Subscription.topics("wikipedia.parsed"), Serde.string, Serde.string)
                       .provideLayer(consumerLayer)
            _        <- ZIO.logInfo(s"Consuming messages...")
            consumed <- stream.take(1000).runCount
            _        <- ZIO.logInfo(s"Consumed $consumed records")
          } yield ()
        )
    }

}
