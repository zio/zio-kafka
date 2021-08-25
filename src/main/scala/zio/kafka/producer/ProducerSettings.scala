package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio.duration._

class ProducerSettings private[producer] (
  val bootstrapServers: List[String],
  val closeTimeout: Duration,
  val properties: Map[String, AnyRef]
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

  private final def copy(
    bootstrapServers: List[String] = bootstrapServers,
    closeTimeout: Duration = closeTimeout,
    properties: Map[String, AnyRef] = properties
  ) =
    new ProducerSettings(bootstrapServers, closeTimeout, properties)
}

object ProducerSettings {
  def apply(bootstrapServers: List[String]): ProducerSettings =
    new ProducerSettings(bootstrapServers, 30.seconds, Map())
}
