package zio.kafka.admin

import org.apache.kafka.clients.admin.AdminClientConfig
import zio._
import zio.kafka.security.KafkaCredentialStore

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
  /**
   * The properties that are passed to the Java admin client. The same as `properties`.
   */
  def driverSettings: Map[String, AnyRef] = properties

  /**
   * Sets the `bootstrap.servers` property to `servers`, joined with commas. Replaces an earlier value.
   */
  def withBootstrapServers(servers: List[String]): AdminClientSettings =
    withProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers.mkString(","))

  /**
   * Sets a property of the Java admin client. Replaces an earlier value for the same key.
   */
  def withProperty(key: String, value: AnyRef): AdminClientSettings =
    copy(properties = properties + (key -> value))

  /**
   * Sets several properties of the Java admin client. Replaces earlier values for the same keys.
   */
  def withProperties(kvs: (String, AnyRef)*): AdminClientSettings =
    withProperties(kvs.toMap)

  /**
   * Sets several properties of the Java admin client. Replaces earlier values for the same keys.
   */
  def withProperties(kvs: Map[String, AnyRef]): AdminClientSettings =
    copy(properties = properties ++ kvs)

  /**
   * Adds the properties of `credentialsStore`, see [[zio.kafka.security.KafkaCredentialStore]]. Replaces earlier values
   * for the same keys.
   */
  def withCredentials(credentialsStore: KafkaCredentialStore): AdminClientSettings =
    withProperties(credentialsStore.properties)
}

/**
 * Constructor of [[AdminClientSettings]].
 */
object AdminClientSettings {

  /**
   * Creates settings for the given bootstrap servers, with a close timeout of 30 seconds and no other properties.
   */
  def apply(bootstrapServers: List[String]): AdminClientSettings =
    AdminClientSettings(
      closeTimeout = 30.seconds,
      properties = Map.empty
    ).withBootstrapServers(bootstrapServers)
}
