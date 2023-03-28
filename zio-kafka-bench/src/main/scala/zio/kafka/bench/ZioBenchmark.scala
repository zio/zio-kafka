package zio.kafka.bench
import org.openjdk.jmh.annotations.{ Setup, TearDown }
import zio.logging.backend.SLF4J
import zio.{ ZLayer, _ }

import java.util.UUID

trait ZioBenchmark[Environment] {
  var runtime: Runtime.Scoped[Environment] = _

  protected val enableLogging: Boolean = true

  protected val timeout: Duration = 180.seconds

  @Setup
  def setup(): Unit =
    runtime = Unsafe.unsafe(implicit unsafe =>
      zio.Runtime.unsafe.fromLayer(
        bootstrap >+>
          (
            zio.Runtime.removeDefaultLoggers >>>
              Runtime.addLogger(SLF4J.slf4jLogger(SLF4J.logFormatDefault, _ => "zio-kafka bench"))
          ) >+>
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
}
