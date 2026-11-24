package zio.kafka.admin

import org.apache.kafka.clients.admin.AdminClientConfig
import zio._
import zio.kafka.security.KafkaCredentialStore

/**
 * Settings for the admin client: the properties passed to the Java admin client, and the time to wait for it to close.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings with
 * `AdminClientSettings(bootstrapServers)` and refine them with the `with*` methods:
 * {{{
 * import org.apache.kafka.clients.admin.AdminClientConfig
 * import zio._
 *
 * val settings: AdminClientSettings =
 *   AdminClientSettings(List("broker-1:9092", "broker-2:9092"))
 *     .withProperty(AdminClientConfig.CLIENT_ID_CONFIG, "inventory-admin")
 *     .withProperties(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG -> "15000")
 *     .copy(closeTimeout = 10.seconds)
 *
 * // settings.properties:
 * //   Map(bootstrap.servers -> broker-1:9092,broker-2:9092, client.id -> inventory-admin, request.timeout.ms -> 15000)
 * // settings.closeTimeout: PT10S
 * }}}
 *
 * @param closeTimeout
 *   the longest time to wait for the Java admin client to close when the scope of the `AdminClient` ends. There is no
 *   `with` method for it; use `copy`.
 * @param properties
 *   the Java admin client configuration (see `org.apache.kafka.clients.admin.AdminClientConfig`)
 */
final case class AdminClientSettings(
  closeTimeout: Duration,
  properties: Map[String, AnyRef]
) {

  /** Returns the properties passed to the Java admin client: the same map as `properties`. */
  def driverSettings: Map[String, AnyRef] = properties

  /**
   * Sets the `bootstrap.servers` property to `servers` joined with commas, replacing any earlier value.
   *
   * @param servers
   *   the `host:port` addresses of the brokers to connect to first
   */
  def withBootstrapServers(servers: List[String]): AdminClientSettings =
    withProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers.mkString(","))

  /** Sets one Java admin client property, replacing an earlier value for the same key. */
  def withProperty(key: String, value: AnyRef): AdminClientSettings =
    copy(properties = properties + (key -> value))

  /** Sets several Java admin client properties, replacing earlier values for the same keys. */
  def withProperties(kvs: (String, AnyRef)*): AdminClientSettings =
    withProperties(kvs.toMap)

  /** Sets several Java admin client properties, replacing earlier values for the same keys. */
  def withProperties(kvs: Map[String, AnyRef]): AdminClientSettings =
    copy(properties = properties ++ kvs)

  /**
   * Adds the properties of `credentialsStore`, replacing earlier values for the same keys. A store made with
   * `KafkaCredentialStore.fromPemStrings` sets `security.protocol` to `SSL` and the PEM key store and trust store.
   */
  def withCredentials(credentialsStore: KafkaCredentialStore): AdminClientSettings =
    withProperties(credentialsStore.properties)
}

/** Constructors for [[AdminClientSettings]]. */
object AdminClientSettings {

  /**
   * Creates settings with the given bootstrap servers, a `closeTimeout` of 30 seconds, and no other property.
   *
   * @param bootstrapServers
   *   the `host:port` addresses of the brokers to connect to first; they become the `bootstrap.servers` property
   */
  def apply(bootstrapServers: List[String]): AdminClientSettings =
    AdminClientSettings(
      closeTimeout = 30.seconds,
      properties = Map.empty
    ).withBootstrapServers(bootstrapServers)
}
