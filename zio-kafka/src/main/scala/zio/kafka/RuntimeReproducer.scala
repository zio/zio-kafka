package zio.kafka

import zio.internal.ExecutionMetrics
import zio.{Executor, Runtime, RuntimeFlag, RuntimeFlags, Scope, Task, Trace, Unsafe, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object RuntimeReproducer extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    for {
      runtime <- ZIO.runtime[Any]
      _ <- ZIO.attempt {
             println("main thread id" + Thread.currentThread().getId)
             unsafeRunOnThisThread(runtime) {
               ZIO.attempt {
                 println("other thread id" + Thread.currentThread().getId)
               }
             }
             ()
           }
    } yield ()

  def unsafeRunOnThisThread(runtime: Runtime[Any])(task: Task[Unit]): Unit =
    Unsafe.unsafe { implicit u =>
      runtime.unsafe
        .run(
          task.provideLayer(SameThreadRuntimeLayer)
        )
        .getOrThrowFiberFailure()
      ()
    }

  private def disableCooperativeYielding(implicit trace: Trace): ZLayer[Any, Nothing, Unit] =
    ZLayer.scoped {
      ZIO.withRuntimeFlagsScoped(RuntimeFlags.disable(RuntimeFlag.CooperativeYielding))
    }

  private def disableWorkSteeling(implicit trace: Trace): ZLayer[Any, Nothing, Unit] =
    ZLayer.scoped {
      ZIO.withRuntimeFlagsScoped(RuntimeFlags.disable(RuntimeFlag.WorkStealing))
    }

  private val SameThreadRuntimeLayer: ZLayer[Any, Nothing, Unit] = {
    val sameThreadExecutor = new Executor() {
      override def metrics(implicit unsafe: Unsafe): Option[ExecutionMetrics] = None
      override def submit(runnable: Runnable)(implicit unsafe: Unsafe): Boolean = {
        runnable.run()
        true
      }
    }
    Runtime.setExecutor(sameThreadExecutor) ++
      Runtime.setBlockingExecutor(sameThreadExecutor) ++
      disableCooperativeYielding ++
      disableWorkSteeling
  }
}
