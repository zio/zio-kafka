package zio.kafka.consumer

import zio._
import zio.internal.ExecutionMetrics

package object internal {

  /**
   * A runtime layer that can be used to run everything on the thread of the caller.
   *
   * Provided by Adam Fraser in Discord:
   * https://discord.com/channels/629491597070827530/630498701860929559/1094279123880386590 but with cooperative
   * yielding enabled.
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

}
