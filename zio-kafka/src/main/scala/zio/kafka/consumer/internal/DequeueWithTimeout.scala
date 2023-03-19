package zio.kafka.consumer.internal
import zio.{ Chunk, Dequeue, Duration, Fiber, Ref, Scope, UIO, ZIO }

/**
 * Avoids race conditions between dequeueing and timeout, which would lead to lost dequeued elements, by storing the
 * interrupted dequeue action and finishing it before starting a new dequeue
 */
class DequeueWithTimeout[A](q: Dequeue[A], previousDequeue: Ref[Option[Fiber[Nothing, Chunk[A]]]], scope: Scope) {

  /**
   * Takes all current commands in the queue without blocking, unless there was a previously interrupted dequeue, in
   * which case it awaits it up to the timeout and then adds all currently available commands
   */
  def takeAll(timeout: Duration): UIO[Chunk[A]] =
    finishPreviousDequeueOrExecuteNew(_.flatMap(as => q.takeAll.map(as ++ _)), q.takeAll, timeout)

  /**
   * Takes between min and max elements from the queue within the timeout
   */
  def takeBetween(min: Int, max: Int, timeout: Duration): UIO[Chunk[A]] =
    finishPreviousDequeueOrExecuteNew(ZIO.identityFn, q.takeBetween(min, max), timeout)

  private def finishPreviousDequeueOrExecuteNew(
    finishPrevious: UIO[Chunk[A]] => UIO[Chunk[A]],
    newDequeue: UIO[Chunk[A]],
    timeout: Duration
  ): UIO[Chunk[A]] =
    for {
      previousAwait <- previousDequeue.get
      awaitAction = previousAwait match {
                      case Some(fib) =>
                        finishPrevious(fib.join)
                      case None =>
                        newDequeue
                    }
      result <- ZIO.interruptibleMask { restore =>
                  // The forking is necessary to prevent interrupted exceptions
                  awaitAction.forkIn(scope).flatMap { awaitFib =>
                    awaitFib.join
                      .raceWith[Any, Nothing, Nothing, Chunk[A], Chunk[A]](
                        restore(ZIO.sleep(timeout).as(Chunk.empty[A]))
                      )(
                        leftDone = { case (leftExit, sleepFiber) =>
                          previousDequeue.set(None) *> sleepFiber.interrupt *> ZIO.done(leftExit)
                        },
                        rightDone = { case (rightExit, actionFiber) =>
                          actionFiber.interrupt *> previousDequeue.set(Some(awaitFib)) *> ZIO.done(rightExit)
                        }
                      )
                  }
                }
    } yield result
}

object DequeueWithTimeout {
  def make[A](queue: Dequeue[A]): ZIO[Scope, Nothing, DequeueWithTimeout[A]] =
    ZIO
      .acquireRelease(
        Ref
          .make(Option.empty[Fiber[Nothing, Chunk[A]]])
      )(ref => ref.get.some.flatMap(_.interrupt.ignore).option)
      .flatMap(ref => ZIO.scope.map(new DequeueWithTimeout(queue, ref, _)))
}
