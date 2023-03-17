package zio.kafka.bench
import org.openjdk.jmh.annotations.{ Setup, TearDown }
import zio.{ Runtime, Unsafe, ZIO, ZLayer, _ }

import java.util.UUID

trait ZioBenchmark[Environment] {
  var runtime: Runtime.Scoped[Environment] = _

  protected def enableLogging: Boolean = false

  @Setup
  def setup(): Unit =
    runtime = Unsafe.unsafe(implicit unsafe =>
      zio.Runtime.unsafe.fromLayer(
        bootstrap >+>
//          Runtime.removeDefaultLoggers >+>
          ZLayer.fromZIO(initialize) >+>
          ZLayer.empty
            .flatMap(_ =>
              if (enableLogging) {
                Runtime.addLogger(logger)
              } else ZLayer.empty
            )
      )
    )

  @TearDown
  def tearDown(): Unit =
    runtime.shutdown0()

  protected def bootstrap: ZLayer[Any, Nothing, Environment]

  protected def initialize: ZIO[Environment, Throwable, Any] = ZIO.unit

  protected def runZIO(program: ZIO[Environment, Throwable, Any]): Any =
    Unsafe.unsafe(implicit unsafe => runtime.unsafe.run(program).getOrThrow())

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
    }
      .filterLogLevel(_ >= LogLevel.Trace)
      .map(_ => ())
}

object ZioBenchmark {
  def randomThing(prefix: String): String = s"$prefix-${UUID.randomUUID()}"
}
