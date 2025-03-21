package zio.kafka.consumer

package object diagnostics {

  /**
   * A callback for consumer diagnostic events.
   *
   * Defined for backward compatibility with zio-kafka 2.x. Please use `Consumer.ConsumerDiagnostics` instead.
   */
  type Diagnostics = Consumer.ConsumerDiagnostics

}
