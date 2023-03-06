package zio.kafka.bench
import org.openjdk.jmh.annotations.{ Setup, TearDown }
import zio.{ Runtime, Task, Unsafe, ZIO, ZLayer }

import java.util.UUID

trait ZioBenchmark[Environment] {
  var runtime: Runtime.Scoped[Environment] = _

  @Setup
  def setup(): Unit =
    runtime = Unsafe.unsafe(implicit unsafe => zio.Runtime.unsafe.fromLayer(bootstrap))

  @TearDown
  def tearDown(): Unit =
    runtime.shutdown0()

  protected def bootstrap: ZLayer[Any, Nothing, Environment]

  protected def runZIO(program: ZIO[Environment, Throwable, Any]) =
    Unsafe.unsafe(implicit unsafe => runtime.unsafe.run(program).getOrThrow())
}

object ZioBenchmark {
  def randomThing(prefix: String): Task[String] =
    ZIO.attempt(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")

}
