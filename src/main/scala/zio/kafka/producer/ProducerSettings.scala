package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio.duration._
import zio.prelude.Subtype

case class ProducerSettings private[producer] (
  bootstrapServers: List[String],
  closeTimeout: Duration,
  properties: Map[String, AnyRef]
) {
  def driverSettings: Map[String, AnyRef] =
    Map(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers.mkString(",")) ++
      properties

  def withBootstrapServers(servers: List[String]): ProducerSettings =
    copy(bootstrapServers = servers)

  def withClientId(clientId: String): ProducerSettings =
    withProperty(ProducerConfig.CLIENT_ID_CONFIG, clientId)

  def withCloseTimeout(duration: Duration): ProducerSettings =
    copy(closeTimeout = duration)

  def withProperty(key: String, value: AnyRef): ProducerSettings =
    copy(properties = properties + (key -> value))

  def withProperties(kvs: (String, AnyRef)*): ProducerSettings   =
    withProperties(kvs.toMap)

  def withProperties(kvs: Map[String, AnyRef]): ProducerSettings =
    copy(properties = properties ++ kvs)
}

object ProducerSettings {
  def apply(bootstrapServers: List[String]): ProducerSettings =
    new ProducerSettings(bootstrapServers, 30.seconds, Map())

  object TransactionalProducerSettings extends Subtype[ProducerSettings] {
    def apply(bootstrapServers: List[String], transactionalId: String) =
      TransactionalProducerSettings.wrap(
        new ProducerSettings(
          bootstrapServers,
          30.seconds,
          Map(ProducerConfig.TRANSACTIONAL_ID_CONFIG -> transactionalId)
        )
      )

    def apply(
      bootstrapServers: List[String],
      closeTimeout: Duration,
      properties: Map[String, AnyRef],
      transactionalId: String
    ): TransactionalProducerSettings = TransactionalProducerSettings.wrap(
      ProducerSettings(
        bootstrapServers,
        closeTimeout,
        properties.updated(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId)
      )
    )
  }
}
