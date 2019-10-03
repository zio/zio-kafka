package zio.kafka.client.diagnostics

import zio.{ Managed, Queue, UIO }

trait Diagnostics {
  def emit(event: DiagnosticEvent): UIO[Unit]
}
object Diagnostics {
  case object NoOp extends Diagnostics {
    override def emit(event: DiagnosticEvent): UIO[Unit] = UIO.unit
  }

  case class SlidingQueue(queue: Queue[DiagnosticEvent]) extends Diagnostics {
    override def emit(event: DiagnosticEvent): UIO[Unit] = queue.offer(event).unit
  }
  object SlidingQueue {
    def make: Managed[Nothing, SlidingQueue] =
      Queue.sliding[DiagnosticEvent](16).toManaged(_.shutdown).map(SlidingQueue(_))
  }
}
