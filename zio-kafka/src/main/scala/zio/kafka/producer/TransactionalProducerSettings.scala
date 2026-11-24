package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio._

/**
 * Settings for a transactional producer: a [[ProducerSettings]] that has the Kafka property `transactional.id`.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings as follows:
 * {{{
 * import zio._
 *
 * val producerSettings: ProducerSettings =
 *   ProducerSettings(List("localhost:9092"))
 *     .withLinger(500.millis)
 *     .withCompression(ProducerCompression.Zstd(3))
 *
 * val settings: TransactionalProducerSettings =
 *   TransactionalProducerSettings(producerSettings, "my-transactional-id")
 * }}}
 *
 * @see
 *   [[https://zio.dev/zio-kafka/transactions/ Transactions Tutorial]]
 */
final case class TransactionalProducerSettings private (producerSettings: ProducerSettings)

/**
 * Creates [[TransactionalProducerSettings]].
 */
object TransactionalProducerSettings {

  /** Creates the settings from the given producer settings, with the Kafka property `transactional.id` set. */
  def apply(producerSettings: ProducerSettings, transactionalId: String): TransactionalProducerSettings =
    TransactionalProducerSettings(
      producerSettings.withProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    )

  /**
   * Creates the settings from the given bootstrap servers and the defaults for all other producer settings, with the
   * Kafka property `transactional.id` set.
   */
  def apply(bootstrapServers: List[String], transactionalId: String): TransactionalProducerSettings =
    TransactionalProducerSettings(
      ProducerSettings(bootstrapServers)
        .withProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    )

  /**
   * Creates the settings from the given bootstrap servers, close timeout, Kafka properties and send buffer size, with
   * the Kafka property `transactional.id` set. The other producer settings have their defaults.
   */
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
