package zio.kafka

import zio.clock.Clock
import zio.{ ExitCode, Schedule, URIO, ZLayer }
import zio.kafka.consumer._
import zio.duration._
import zio.console.{ putStrLn, Console }
import zio.kafka.serde.Serde
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object ConsumerPlayground extends zio.App {
  val settings = ConsumerSettings(List("localhost:9092"))
    .withPollInterval(50.millis)
    .withPollTimeout(20000.millis)
    .withProperty("group.id", "test-group")
    .withProperties("fetch.max.wait.ms" -> "10000")
    .withProperties("fetch.min.bytes" -> "1", "max.poll.records" -> "10")

  val loggingLayer: ZLayer[Any, Nothing, Logging] =
    (Console.live ++ Clock.live) >>>
      Slf4jLogger.make((_, s) => s) >>>
      Logging.withRootLoggerName(getClass.getName)

  val env = Consumer.make(settings).toLayer

  val stream = Consumer
    .subscribeAnd(Subscription.topics("test-topic"))
    .partitionedStream(Serde.string, Serde.string)
    .flatMapPar(Int.MaxValue) { case (tp @ _, partitionStream) =>
      partitionStream.mapM(record => putStrLn(s"Got ${record.value}").as(record.offset))
    }
    .aggregateAsyncWithin(Consumer.offsetBatches, Schedule.spaced(1.second))
    .mapM(_.commit)

  val program = stream.runDrain

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.provideCustomLayer(env ++ loggingLayer).exitCode

}
