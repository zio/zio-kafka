package zio.kafka.consumer

import zio._
import zio.internal.ExecutionMetrics

package object internal {

  private[internal] type NanoTime = Long

  /**
   * A runtime layer that can be used to run everything on the thread of the caller.
   *
   * Provided by Adam Fraser in Discord:
   * https://discord.com/channels/629491597070827530/630498701860929559/1094279123880386590 but with cooperative
   * yielding enabled.
   *
   * WARNING! Unfortunately some ZIO operations, like `ZIO.timeout`, inherently need to work multi-threaded and will
   * therefore shift the fiber to another thread, even when this runtime is used.
   */
  private[internal] val SameThreadRuntimeLayer: ZLayer[Any, Nothing, Unit] = {
    val sameThreadExecutor = new Executor() {
      override def metrics(implicit unsafe: Unsafe): Option[ExecutionMetrics] = None

      override def submit(runnable: Runnable)(implicit unsafe: Unsafe): Boolean = {
        runnable.run()
        true
      }
    }

    Runtime.setExecutor(sameThreadExecutor) ++ Runtime.setBlockingExecutor(sameThreadExecutor)
  }

  /**
   * A sleep that is safe to use from the same-thread-runtime.
   */
  private[internal] def blockingSleep(sleepTime: Duration)(implicit trace: Trace): Task[Unit] =
    ZIO.attempt(Thread.sleep(sleepTime.toMillis))

}
