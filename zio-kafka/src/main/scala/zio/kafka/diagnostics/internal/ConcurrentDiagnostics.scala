package zio.kafka.diagnostics.internal

import zio._
import zio.kafka.diagnostics.Diagnostics
import zio.stream._
//import zio.stacktracer.TracingImplicits.disableAutoTrace

object ConcurrentDiagnostics {

  /**
   * @return
   *   a `Diagnostics` that runs the wrapped `Diagnostics` concurrently in a separate fiber. Events are emitting to the
   *   fiber via an unbounded queue. Upon finalization, `finalEvent` is emitted as the last event.
   */
  def make[DiagnosticEvent](
    wrapped: Diagnostics[DiagnosticEvent],
    finalEvent: DiagnosticEvent
  )(implicit trace: Trace): ZIO[Scope, Nothing, Diagnostics[DiagnosticEvent]] =
    for {
      queue <- ZIO.acquireRelease(Queue.unbounded[DiagnosticEvent])(_.shutdown)
      fib   <- ZStream.fromQueue(queue).tap(wrapped.emit(_)).takeUntil(_ == finalEvent).runDrain.forkScoped
      _     <- ZIO.addFinalizer(queue.offer(finalEvent) *> fib.await)
    } yield new Diagnostics[DiagnosticEvent] {
      override def emit(event: => DiagnosticEvent)(implicit trace: Trace): UIO[Unit] = queue.offer(event).unit
    }
}
