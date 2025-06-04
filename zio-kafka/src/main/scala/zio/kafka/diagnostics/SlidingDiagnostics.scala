package zio.kafka.diagnostics

import zio.{ Queue, Scope, Trace, UIO, ZIO }
//import zio.stacktracer.TracingImplicits.disableAutoTrace

object SlidingDiagnostics {

  /**
   * Create a [[Diagnostics]] implementation that keeps the last `queueSize` emitted events in a queue.
   *
   * Useful for testing, or any other case where you don't want to risk high memory usage due to a slow diagnostics
   * implementation.
   *
   * For example, for a Diagnostics that keeps the last 100
   * [[zio.kafka.consumer.diagnostics.DiagnosticEvent consumer DiagnosticEvent]]s use:
   * {{{
   *   import zio.kafka.consumer.diagnostics.{DiagnosticEvent => ConsumerDiagnosticEvent}
   *   for {
   *     diagnostics <- SlidingDiagnostics.make[ConsumerDiagnosticEvent](100)
   *     ... use diagnostics
   *     events <- diagnostics.queue.takeAll
   *   } yield {
   *     assert(events.length == 100)
   *   }
   * }}}
   *
   * @param queueSize
   *   number of events to retain in the queue
   * @tparam DiagnosticEvent
   *   the type of event to keep
   */
  def make[DiagnosticEvent](queueSize: Int = 16)(implicit
    trace: Trace
  ): ZIO[Scope, Nothing, QueuingDiagnostics[DiagnosticEvent]] =
    ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown).map(QueuingDiagnostics(_))

}

final case class QueuingDiagnostics[DiagnosticEvent] private[diagnostics] (queue: Queue[DiagnosticEvent])
    extends Diagnostics[DiagnosticEvent] {
  override def emit(event: => DiagnosticEvent)(implicit trace: Trace): UIO[Unit] = queue.offer(event).unit
}
