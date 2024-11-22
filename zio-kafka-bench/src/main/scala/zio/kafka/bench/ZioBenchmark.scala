package zio.kafka.bench
import org.openjdk.jmh.annotations.{ Setup, TearDown }
import zio.kafka.bench.ZioBenchmark.logger
import zio.{ ZLayer, _ }

import java.util.UUID

trait ConsumerZioBenchmark[Environment] extends ZioBenchmark[Environment] {
  private val recordDataSize            = 512
  private def genString(i: Int): String = i.toString + scala.util.Random.alphanumeric.take(recordDataSize).mkString

  protected val recordCount: Int                = 50000
  protected val kvs: Iterable[(String, String)] = Iterable.tabulate(recordCount)(i => (s"key$i", genString(i)))
  protected val topic1                          = "topic1"
  protected val partitionCount                  = 6
}

trait ProducerZioBenchmark[Environment] extends ZioBenchmark[Environment] {
  protected val recordCount                 = 500
  protected val kvs: List[(String, String)] = List.tabulate(recordCount)(i => (s"key$i", s"msg$i"))
  protected val topic1                      = "topic1"
  protected val partitionCount              = 6
}

trait ZioBenchmark[Environment] {
  var runtime: Runtime.Scoped[Environment] = _

  protected val enableLogging: Boolean = true

  protected val timeout: Duration = 180.seconds

  @Setup
  def setup(): Unit =
    runtime = Unsafe.unsafe(implicit unsafe =>
      zio.Runtime.unsafe.fromLayer(
        bootstrap >+>
          (Runtime.removeDefaultLoggers >+>
            (if (enableLogging) Runtime.addLogger(logger) else ZLayer.empty)) >+>
          ZLayer.fromZIO(initialize)
      )
    )

  @TearDown
  def tearDown(): Unit =
    runtime.shutdown0()

  protected def bootstrap: ZLayer[Any, Nothing, Environment]

  protected def initialize: ZIO[Environment, Throwable, Any] = ZIO.unit

  protected def runZIO(program: ZIO[Environment, Throwable, Any]): Any =
    Unsafe.unsafe(implicit unsafe =>
      runtime.unsafe
        .run(
          program
            .tapErrorCause(e => ZIO.debug("Error in benchmark run: " + e.prettyPrint))
            .timeoutFail(new RuntimeException("Benchmark run timed out"))(timeout)
            .tapError(_ => Fiber.dumpAll)
        )
        .getOrThrow()
    )
}

object ZioBenchmark {
  def randomThing(prefix: String): String = s"$prefix-${UUID.randomUUID()}"

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
              .now()} ${logLevel.label} [fiber=${fiberId.threadName},${annotations.map { case (k, v) => s"$k=$v" }
              .mkString(",")}] ${message()} ${if (cause.isEmpty) "" else cause.prettyPrint}"
        )
    }.filterLogLevel(_ >= LogLevel.Info).map(_ => ())
}
