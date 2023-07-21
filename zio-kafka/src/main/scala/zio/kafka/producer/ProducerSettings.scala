package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio._
import zio.kafka.security.KafkaCredentialStore

final case class ProducerSettings(
  bootstrapServers: List[String],
  closeTimeout: Duration = 30.seconds,
  sendBufferSize: Int = 4096,
  properties: Map[String, AnyRef] = Map.empty
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

  def withProperties(kvs: (String, AnyRef)*): ProducerSettings =
    withProperties(kvs.toMap)

  def withProperties(kvs: Map[String, AnyRef]): ProducerSettings =
    copy(properties = properties ++ kvs)

  def withCredentials(credentialsStore: KafkaCredentialStore): ProducerSettings =
    withProperties(credentialsStore.properties)

  def withSendBufferSize(sendBufferSize: Int) = copy(sendBufferSize = sendBufferSize)
}
