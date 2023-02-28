package zio.kafka

import zio._

import java.util.UUID

trait ZIOKafkaSpec extends ZIOSpecWithKafka {

  def kafkaPrefix: String

  def randomThing(prefix: String): Task[String] =
    ZIO.attempt(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")

  def randomTopic: Task[String] = randomThing(s"$kafkaPrefix-topic")

  def randomGroup: Task[String] = randomThing(s"$kafkaPrefix-group")

  def randomClient: Task[String] = randomThing(s"$kafkaPrefix-client")

  val logger: ZLogger[String, Unit] =
    new ZLogger[String, Unit] {
      override def apply(
        trace: Trace,
        fiberId: FiberId,
        logLevel: LogLevel,
        message: () => String,
        cause: Cause[Any],
        context: FiberRefs,
        spans: List[LogSpan],
        annotations: Map[String, String]
      ): Unit =
        println(
          s"${java.time.Instant
              .now()} ${logLevel.label} [${annotations.map { case (k, v) => s"$k=$v" }
              .mkString(",")}] ${message()} ${if (cause.isEmpty) "" else cause.prettyPrint}"
        )
    }.filterLogLevel(_ >= LogLevel.Debug).map(_ => ())
}
