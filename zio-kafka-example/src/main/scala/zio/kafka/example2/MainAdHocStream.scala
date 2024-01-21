package zio.kafka.example2

import zio._
import zio.logging.backend.SLF4J
import zio.stream.{ZSink, ZStream}

object MainAdHocStream extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  def consumer: ZIO[Any, Throwable, Unit] = for {
    stop <- Promise.make[Nothing, Unit]
    c <- (
      ZStream.finalizer(ZIO.logInfo("Consumer STOPPING")) *>
      ZStream.fromSchedule(Schedule.fixed(100.millis).jittered)
        .map(n => s"msg-$n")
        .chunksWith(_.takeUntilZIO(_ => stop.isDone))
        .tap(r => ZIO.logInfo("Value received: " + r))
        .mapZIO(ZIO.succeed(_).delay(205.millis))
        .aggregateAsync(ZSink.collectAll[String])
        .tap(collected => ZIO.logInfo(s"collected: ${collected}").delay(500.millis))
      )
      .runDrain
      .zipLeft(ZIO.logInfo("Consumer done"))
      .uninterruptible <& ZIO.never.onInterrupt(ZIO.logInfo("Stopping the consumer") *> stop.succeed(()))
  } yield c

  override def run = {
    for {
      _ <- ZIO.addFinalizer(ZIO.logDebug("-------- Main shutdown started --------"))
      _ <- ZIO.logInfo("Starting")
      _ <- consumer.timeout(4.seconds)
      _ <- ZIO.logInfo("DONE")
    } yield ()
  }
}
