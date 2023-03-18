package zio.kafka.consumer.internal
import zio.{ Chunk, Dequeue, Duration, Fiber, Ref, UIO, ZIO }

/**
 * Avoids race conditions between dequeueing and timeout, which would lead to lost dequeued elements, by storing the
 * interrupted dequeue action and finishing it before starting a new dequeue
 */
class DequeueWithTimeout[A](q: Dequeue[A], previousDequeue: Ref[Option[Fiber[Nothing, Chunk[A]]]]) {
  // Takes all available commands without blocking, unless there was a previously interrupted dequeue
  def takeAllWithTimeout(timeout: Duration): UIO[Chunk[A]] =
    finishPreviousDequeueOrExecuteNew(q.takeAll, timeout)

  def takeBetweenWithTimeout(min: Int, max: Int, timeout: Duration): UIO[Chunk[A]] =
    finishPreviousDequeueOrExecuteNew(q.takeBetween(min, max), timeout)

  private def finishPreviousDequeueOrExecuteNew(f: UIO[Chunk[A]], timeout: Duration): UIO[Chunk[A]] =
    for {
      previousAwait <- previousDequeue.get
      awaitAction = previousAwait match {
                      case Some(fib) => fib.join.flatMap(as => q.takeAll.map(as ++ _))
                      case None      => f
                    }
      result <- awaitAction
                  .raceWith[Any, Nothing, Nothing, Chunk[A], Chunk[A]](
                    ZIO.sleep(timeout).as(Chunk.empty[A])
                  )(
                    leftDone = { case (leftExit, sleepFiber) =>
                      sleepFiber.interrupt *> ZIO.done(leftExit)
                    },
                    rightDone = { case (rightExit, actionFiber) =>
                      previousDequeue.set(Some(actionFiber)) *> ZIO.done(rightExit)
                    }
                  )
    } yield result
}

object DequeueWithTimeout {
  def make[A](queue: Dequeue[A]): UIO[DequeueWithTimeout[A]] =
    Ref
      .make(Option.empty[Fiber[Nothing, Chunk[A]]])
      .map(new DequeueWithTimeout(queue, _))
}
