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
  maxConcurrentWriteOperations: Int,
  properties: Map[String, AnyRef]
) {
  def driverSettings: Map[String, AnyRef] = properties

  def withBootstrapServers(servers: List[String]): AdminClientSettings =
    withProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers.mkString(","))

  /**
   * Set the maximum number of write operations (create topic, delete topic, delete offsets, etc.) that can be executed
   * concurrently by the admin client. Keep this number low to prevent the operation to be lost, or not being able to
   * see the effect of the operation. Experimentally, we have established that `5` is a good value.
   *
   * Defaults to 5.
   *
   * Note: this setting is ignored when the client is created with a custom [[Semaphore]]. See `AdminClient.make()`. for
   * more details.
   */
  def withMaxConcurrentWriteOperations(maxConcurrentWriteOperations: Int): AdminClientSettings = {
    require(
      maxConcurrentWriteOperations >= 0,
      s"maxConcurrentWriteOperations must be strictly positive, got $maxConcurrentWriteOperations"
    )
    copy(maxConcurrentWriteOperations = maxConcurrentWriteOperations)
  }

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
      maxConcurrentWriteOperations = 5,
      properties = Map.empty
    ).withBootstrapServers(bootstrapServers)
}
