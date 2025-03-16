package zio.kafka.consumer.diagnostics

import zio.kafka.consumer.Consumer

object Diagnostics {

  /**
   * A diagnostics implementation for [[DiagnosticEvent consumer DiagnosticsEvent]]s that does nothing.
   *
   * Defined for backward compatibility with zio-kafka 2.x. Please use `Consumer.NoDiagnostics` instread.
   */
  val NoOp: Consumer.ConsumerDiagnostics = Consumer.NoDiagnostics

}
