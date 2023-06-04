package zio.kafka.consumer.internal

import zio.{Executor, Scope, ZIO}

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

private[consumer] object RunloopExecutor {

  private val counter: AtomicLong = new AtomicLong(0)

  private def newSingleThreadedExecutor(i: Long): ZIO[Scope, Throwable, Executor] =
    ZIO
      .acquireRelease {
        ZIO.attempt {
          val javaExecutor =
            Executors.newSingleThreadExecutor(runnable => new Thread(runnable, s"zio-kafka-runloop-thread-$i"))

          Executor.fromJavaExecutor(javaExecutor) -> javaExecutor
        }
      } { case (_, executor) => ZIO.attempt(executor.shutdown()).orDie }
      .map(_._1)

  val newInstance: ZIO[Scope, Throwable, Executor] = newSingleThreadedExecutor(counter.getAndIncrement())

}
