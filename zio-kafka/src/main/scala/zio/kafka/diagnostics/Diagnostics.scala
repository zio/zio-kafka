package zio.kafka.diagnostics

import zio.{ UIO, ZIO }

/**
 * A callback interface for diagnostic events.
 *
 * Implement `emit` and pass the instance to the settings of a consumer or producer, for example with
 * [[zio.kafka.producer.ProducerSettings.withDiagnostics]].
 *
 * @see
 *   [[https://zio.dev/zio-kafka/diagnostics/ Diagnostics]]
 */
trait Diagnostics[-DiagnosticEvent] {

  /**
   * Called with each diagnostic event. The event is passed by name.
   */
  def emit(event: => DiagnosticEvent): UIO[Unit]
}

/**
 * Provides the [[Diagnostics.NoOp]] implementation.
 */
object Diagnostics {

  /**
   * A diagnostics implementation that does nothing.
   */
  val NoOp: Diagnostics[Any] = new Diagnostics[Any] {
    override def emit(event: => Any): UIO[Unit] = ZIO.unit
  }

}
