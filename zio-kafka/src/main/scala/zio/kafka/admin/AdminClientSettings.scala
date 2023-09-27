package zio.kafka.admin

import org.apache.kafka.clients.admin.AdminClientConfig
import zio._
import zio.kafka.security.KafkaCredentialStore

final case class AdminClientSettings(
  closeTimeout: Duration,
  properties: Map[String, AnyRef]
) {
  def driverSettings: Map[String, AnyRef] = properties

  def withBootstrapServers(servers: List[String]): AdminClientSettings =
    withProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers.mkString(","))

  def withProperty(key: String, value: AnyRef): AdminClientSettings =
    copy(properties = properties + (key -> value))

  def withProperties(kvs: (String, AnyRef)*): AdminClientSettings =
    withProperties(kvs.toMap)

  def withProperties(kvs: Map[String, AnyRef]): AdminClientSettings =
    copy(properties = properties ++ kvs)

  def withCredentials(credentialsStore: KafkaCredentialStore): AdminClientSettings =
    withProperties(credentialsStore.properties)
}

object AdminClientSettings {
  def apply(bootstrapServers: List[String]): AdminClientSettings =
    AdminClientSettings(
      closeTimeout = 30.seconds,
      properties = Map.empty
    ).withBootstrapServers(bootstrapServers)
}
