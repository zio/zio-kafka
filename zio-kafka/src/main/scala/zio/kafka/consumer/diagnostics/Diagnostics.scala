package zio.kafka.consumer.diagnostics

import zio.stream.ZStream
import zio._
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization.ConsumerFinalized

trait Diagnostics {
  def emit(event: => DiagnosticEvent): UIO[Unit]
}
object Diagnostics {
  case object NoOp extends Diagnostics {
    override def emit(event: => DiagnosticEvent): UIO[Unit] = ZIO.unit
  }

  final case class SlidingQueue private[Diagnostics] (queue: Queue[DiagnosticEvent]) extends Diagnostics {
    override def emit(event: => DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
  }

  object SlidingQueue {
    def make(queueSize: Int = 16): ZIO[Scope, Nothing, SlidingQueue] =
      ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown).map(SlidingQueue(_))
  }

  object ConcurrentDiagnostics {

    /**
     * @return
     *   a `Diagnostics` that runs the wrapped `Diagnostics` concurrently in a separate fiber. Events are emitting to
     *   the fiber via an unbounded queue
     */
    def make(wrapped: Diagnostics): ZIO[Scope, Nothing, Diagnostics] =
      if (wrapped == Diagnostics.NoOp) ZIO.succeed(Diagnostics.NoOp)
      else {
        for {
          queue <- ZIO.acquireRelease(Queue.unbounded[DiagnosticEvent])(_.shutdown)
          fib   <- ZStream.fromQueue(queue).tap(wrapped.emit(_)).takeUntil(_ == ConsumerFinalized).runDrain.forkScoped
          _     <- ZIO.addFinalizer(queue.offer(ConsumerFinalized) *> fib.await)
        } yield new Diagnostics {
          override def emit(event: => DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
        }
      }
  }
}
