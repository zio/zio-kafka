package zio.kafka.consumer.diagnostics

import zio.{ Queue, Scope, UIO, ZIO }

trait Diagnostics {
  val enabled: Boolean = true

  def emitIfEnabled(event: => DiagnosticEvent): UIO[Unit] =
    if (enabled) emit(event) else UIO.unit

  protected def emit(event: DiagnosticEvent): UIO[Unit]
}
object Diagnostics {
  case object NoOp extends Diagnostics {
    override val enabled: Boolean                        = false
    override def emit(event: DiagnosticEvent): UIO[Unit] = UIO.unit
  }

  final case class SlidingQueue(queue: Queue[DiagnosticEvent]) extends Diagnostics {
    override def emit(event: DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
  }

  object SlidingQueue {
    def make(queueSize: Int = 16): ZIO[Scope, Nothing, SlidingQueue] =
      ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown).map(SlidingQueue(_))
  }
}
