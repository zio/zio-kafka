package zio.kafka.consumer.diagnostics

import zio.stream.ZStream
import zio.{ Queue, Scope, UIO, ZIO }

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

  object QueuedDiagnostics {

    /**
     * @return
     *   a new `Diagnostics` that emits the events to a queue first and runs the wrapped `Diagnostics` in a separate
     *   fiber
     */
    def make(wrapped: Diagnostics, queueSize: Int = 16): ZIO[Scope, Nothing, Diagnostics] =
      if (wrapped == Diagnostics.NoOp) ZIO.succeed(Diagnostics.NoOp)
      else {
        for {
          queue <- ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown)
          _     <- ZStream.fromQueue(queue).tap(wrapped.emit(_)).runDrain.forkScoped
        } yield SlidingQueue(queue)
      }
  }

}
