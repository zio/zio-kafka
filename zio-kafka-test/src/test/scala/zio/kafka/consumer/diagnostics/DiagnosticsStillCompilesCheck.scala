package zio.kafka.consumer.diagnostics

import zio._

import scala.annotation.nowarn

/**
 * Check that the type aliases introduced with zio-kafka 2.12.0 still compile code written against zio-kafka 2.11.x.
 */
//noinspection ScalaDeprecation
@nowarn("msg=deprecated")
object DiagnosticsStillCompilesCheck {

  class MyDiagnostics extends zio.kafka.consumer.diagnostics.Diagnostics {
    override def emit(event: => DiagnosticEvent): UIO[Unit] =
      event match {
        case _: DiagnosticEvent.Poll                            => ZIO.unit
        case _: DiagnosticEvent.Request                         => ZIO.unit
        case _: DiagnosticEvent.Commit                          => ZIO.unit
        case _: DiagnosticEvent.Rebalance                       => ZIO.unit
        case DiagnosticEvent.Finalization.SubscriptionFinalized => ZIO.unit
        case DiagnosticEvent.Finalization.RunloopFinalized      => ZIO.unit
        case DiagnosticEvent.Finalization.ConsumerFinalized     => ZIO.unit
      }
  }

}
