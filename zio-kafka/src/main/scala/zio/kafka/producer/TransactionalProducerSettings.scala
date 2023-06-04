package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio._

sealed abstract case class TransactionalProducerSettings private (
  producerSettings: ProducerSettings
)

object TransactionalProducerSettings {
  def apply(producerSettings: ProducerSettings, transactionalId: String): TransactionalProducerSettings =
    new TransactionalProducerSettings(
      producerSettings.withProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    ) {}

  def apply(bootstrapServers: List[String], transactionalId: String): TransactionalProducerSettings =
    new TransactionalProducerSettings(
      ProducerSettings(
        bootstrapServers,
        30.seconds,
        4096,
        Map(ProducerConfig.TRANSACTIONAL_ID_CONFIG -> transactionalId),
      )
    ) {}

  def apply(
    bootstrapServers: List[String],
    closeTimeout: Duration,
    properties: Map[String, AnyRef],
    transactionalId: String,
    sendBufferSize: Int,
  ): TransactionalProducerSettings =
    new TransactionalProducerSettings(
      ProducerSettings(
        bootstrapServers,
        closeTimeout,
        sendBufferSize,
        properties.updated(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId),
      )
    ) {}
}
