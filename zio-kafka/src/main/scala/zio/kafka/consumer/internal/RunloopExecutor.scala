package zio.kafka.consumer.internal

import zio.{ Executor, Scope, URIO, ZIO }

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

private[consumer] object RunloopExecutor {

  private val counter: AtomicLong = new AtomicLong(0)

  private val newSingleThreadedExecutor: URIO[Scope, Executor] =
    ZIO.acquireRelease {
      ZIO.succeed {
        val javaExecutor =
          Executors.newSingleThreadExecutor { runnable =>
            new Thread(runnable, s"zio-kafka-runloop-thread-${counter.getAndIncrement()}")
          }

        Executor.fromJavaExecutor(javaExecutor) -> javaExecutor
      }
    } { case (_, executor) => ZIO.attempt(executor.shutdown()).orDie }.map(_._1)

  val newInstance: URIO[Scope, Executor] = newSingleThreadedExecutor

}
