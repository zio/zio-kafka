package zio.kafka.diagnostics

import zio.{ UIO, ZIO }

/**
 * A callback interface for diagnostic events.
 */
trait Diagnostics[-DiagnosticEvent] {
  def emit(event: => DiagnosticEvent): UIO[Unit]
}

object Diagnostics {

  /**
   * A diagnostics implementation that does nothing.
   */
  val NoOp: Diagnostics[Any] = new Diagnostics[Any] {
    override def emit(event: => Any): UIO[Unit] = ZIO.unit
  }

}
