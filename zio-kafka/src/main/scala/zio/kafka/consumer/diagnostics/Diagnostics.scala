package zio.kafka.consumer.diagnostics

import zio.stream.ZStream
import zio._
import zio.kafka.consumer.Consumer
import zio.kafka.consumer.diagnostics.DiagnosticEvent.ConsumerFinalized

object Diagnostics {

  /** A diagnostics implementation for [[DiagnosticEvent consumer DiagnosticsEvent]]s that does nothing. */
  @deprecated("Use zio.kafka.consumer.Consumer.NoDiagnostics instead", "2.12.0")
  val NoOp: Consumer.ConsumerDiagnostics = Consumer.NoDiagnostics

  final case class SlidingQueue private[Diagnostics] (queue: Queue[DiagnosticEvent])
      extends Consumer.ConsumerDiagnostics {
    override def emit(event: => DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
  }

  object SlidingQueue {
    @deprecated("Use zio.kafka.diagnostics.SlidingDiagnostics.make[DiagnosticEvent] instead", "2.12.0")
    def make(queueSize: Int = 16): ZIO[Scope, Nothing, SlidingQueue] =
      ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown).map(SlidingQueue(_))
  }

  object ConcurrentDiagnostics {

    /**
     * @return
     *   a `Diagnostics` that runs the wrapped `Diagnostics` concurrently in a separate fiber. Events are emitting to
     *   the fiber via an unbounded queue
     * @deprecated
     *   This wrapper is automatically applied to all user diagnostics. It is therefore not meant to be used by users of
     *   the zio-kafka library. This API will no longer by public starting with zio-kafka 3.x.
     */
    @deprecated("Will be removed in zio-kafka 3.x", "2.12.0")
    def make(wrapped: Consumer.ConsumerDiagnostics): ZIO[Scope, Nothing, Consumer.ConsumerDiagnostics] =
      if (wrapped == Consumer.NoDiagnostics) ZIO.succeed(Consumer.NoDiagnostics)
      else {
        for {
          queue <- ZIO.acquireRelease(Queue.unbounded[DiagnosticEvent])(_.shutdown)
          fib   <- ZStream.fromQueue(queue).tap(wrapped.emit(_)).takeUntil(_ == ConsumerFinalized).runDrain.forkScoped
          _     <- ZIO.addFinalizer(queue.offer(ConsumerFinalized) *> fib.await)
        } yield new Consumer.ConsumerDiagnostics {
          override def emit(event: => DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
        }
      }
  }
}
