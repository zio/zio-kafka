package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio._
//import zio.stacktracer.TracingImplicits.disableAutoTrace

/**
 * Settings for a transactional producer.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings as follows:
 * {{{
 *   val producerSettings = ProducerSettings(bootstrapServers)
 *     .withLinger(500.millis)
 *     .withCompression(ProducerCompression.Zstd(3))
 *     .... etc.
 *   TransactionalProducerSettings(producerSettings, transactionalId)
 * }}}
 */
final case class TransactionalProducerSettings private (producerSettings: ProducerSettings)

object TransactionalProducerSettings {
  def apply(producerSettings: ProducerSettings, transactionalId: String): TransactionalProducerSettings =
    TransactionalProducerSettings(
      producerSettings.withProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    )

  def apply(bootstrapServers: List[String], transactionalId: String): TransactionalProducerSettings =
    TransactionalProducerSettings(
      ProducerSettings(bootstrapServers)
        .withProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    )

  def apply(
    bootstrapServers: List[String],
    closeTimeout: Duration,
    properties: Map[String, AnyRef],
    transactionalId: String,
    sendBufferSize: Int
  ): TransactionalProducerSettings =
    TransactionalProducerSettings(
      ProducerSettings(bootstrapServers)
        .withCloseTimeout(closeTimeout)
        .withSendBufferSize(sendBufferSize)
        .withProperties(properties)
        .withProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    )
}
