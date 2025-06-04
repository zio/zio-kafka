package zio.kafka.admin

import org.apache.kafka.clients.admin.AdminClientConfig
import zio._
import zio.kafka.security.KafkaCredentialStore
//import zio.stacktracer.TracingImplicits.disableAutoTrace

/**
 * Settings for the admin client.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings as follows:
 * {{{
 *   AdminClientSettings(bootstrapServers)
 *     .withCredentials(...)
 *     .withProperty(...)
 *     .... etc.
 * }}}
 */
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
