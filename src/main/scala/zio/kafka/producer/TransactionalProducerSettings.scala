package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio.duration.{ durationInt, Duration }

class TransactionalProducerSettings private[producer] (
  override val bootstrapServers: List[String],
  override val closeTimeout: Duration,
  override val properties: Map[String, AnyRef]
) extends ProducerSettings(bootstrapServers, closeTimeout, properties)

object TransactionalProducerSettings {
  def apply(bootstrapServers: List[String], transactionalId: String): TransactionalProducerSettings =
    new TransactionalProducerSettings(
      bootstrapServers,
      30.seconds,
      Map(ProducerConfig.TRANSACTIONAL_ID_CONFIG -> transactionalId)
    )

  def apply(
    bootstrapServers: List[String],
    closeTimeout: Duration,
    properties: Map[String, AnyRef],
    transactionalId: String
  ): TransactionalProducerSettings =
    new TransactionalProducerSettings(
      bootstrapServers,
      closeTimeout,
      properties.updated(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
    )
}
