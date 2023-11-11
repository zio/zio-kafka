package zio.kafka.consumer.diagnostics

import zio.stream.ZStream
import zio._

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
        // Run the diagnostics from a separate fiber. The fiber is interrupted when it tries to poll from the queue
        // while that queue is shut down. To give the fiber a chance to get the last item in the queue, we delay the
        // queue shut down by 10ms.
        for {
          queue <- ZIO.acquireRelease(Queue.unbounded[DiagnosticEvent])(_.shutdown.delay(10.millis))
          _     <- ZStream.fromQueue(queue).tap(wrapped.emit(_)).runDrain.forkDaemon
        } yield new Diagnostics {
          override def emit(event: => DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
        }
      }
  }

}
