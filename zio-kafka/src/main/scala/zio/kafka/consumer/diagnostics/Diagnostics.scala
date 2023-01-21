package zio.kafka.consumer.diagnostics

import zio.{ Queue, Scope, UIO, ZIO }

abstract class Diagnostics(enabled: Boolean, emit: DiagnosticEvent => UIO[Unit]) {
  final def emitIfEnabled(event: => DiagnosticEvent): UIO[Unit] =
    if (enabled) emit(event) else ZIO.unit
}
object Diagnostics {
  case object NoOp extends Diagnostics(enabled = false, emit = _ => ZIO.unit)
  final case class SlidingQueue(queue: Queue[DiagnosticEvent])
      extends Diagnostics(enabled = true, event => queue.offer(event).unit)

  object SlidingQueue {
    def make(queueSize: Int = 16): ZIO[Scope, Nothing, SlidingQueue] =
      ZIO.acquireRelease(Queue.sliding[DiagnosticEvent](queueSize))(_.shutdown).map(SlidingQueue(_))
  }
}
