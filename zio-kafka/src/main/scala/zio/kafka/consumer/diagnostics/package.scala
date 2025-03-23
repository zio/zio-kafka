package zio.kafka.consumer

package object diagnostics {

  /**
   * A callback for consumer diagnostic events.
   */
  @deprecated("Use zio.kafka.consumer.Consumer.ConsumerDiagnostics instead", "2.12.0")
  type Diagnostics = Consumer.ConsumerDiagnostics

}
