package zio.kafka.diagnostics

import zio.{ Queue, Scope, UIO, ZIO }

/**
 * Creates [[QueuingDiagnostics]] that keep the most recent events.
 */
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
   * import zio._
   * import zio.kafka.consumer.diagnostics.{ DiagnosticEvent => ConsumerDiagnosticEvent }
   *
   * val lastEvents: ZIO[Scope, Nothing, Chunk[ConsumerDiagnosticEvent]] =
   *   for {
   *     diagnostics <- SlidingDiagnostics.make[ConsumerDiagnosticEvent](100)
   *     // give `diagnostics` to the consumer settings and use the consumer here
   *     events <- diagnostics.queue.takeAll
   *   } yield events
   * }}}
   *
   * @param queueSize
   *   number of events to retain in the queue
   * @tparam DiagnosticEvent
   *   the type of event to keep
   */
  def make[DiagnosticEvent](queueSize: Int = 16): ZIO[Scope, Nothing, QueuingDiagnostics[DiagnosticEvent]] =
    ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown).map(QueuingDiagnostics(_))

}

/**
 * A [[Diagnostics]] implementation that offers each event to a queue. Create one with [[SlidingDiagnostics.make]].
 *
 * @param queue
 *   the queue that holds the emitted events
 */
final case class QueuingDiagnostics[DiagnosticEvent] private[diagnostics] (queue: Queue[DiagnosticEvent])
    extends Diagnostics[DiagnosticEvent] {
  override def emit(event: => DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
}
