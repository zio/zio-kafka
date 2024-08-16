package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio._
import zio.kafka.security.KafkaCredentialStore

/**
 * Settings for the producer.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings as follows:
 * {{{
 *   ProducerSettings(bootstrapServers)
 *     .withClientId(clientId)
 *     .withProperties(properties)
 *     .... etc.
 * }}}
 */
final case class ProducerSettings(
  closeTimeout: Duration = 30.seconds,
  sendBufferSize: Int = 4096,
  properties: Map[String, AnyRef] = Map.empty,
  authErrorRetrySchedule: Schedule[Any, Throwable, Any] = Schedule.recurs(5) && Schedule.spaced(500.millis)
) {
  def driverSettings: Map[String, AnyRef] = properties

  def withBootstrapServers(servers: List[String]): ProducerSettings =
    withProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers.mkString(","))

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

  def withSendBufferSize(sendBufferSize: Int): ProducerSettings =
    copy(sendBufferSize = sendBufferSize)

  /**
   * @param authErrorRetrySchedule
   *   The schedule at which the producer will retry sending records to the broker, even though a send fails with an
   *   [[org.apache.kafka.common.errors.AuthorizationException]] or
   *   [[org.apache.kafka.common.errors.AuthenticationException]]. The schedule is fed with the firs auth error in a
   *   batch of records, but it will decide for the entire batch. If there are any other type of errors in the batch no
   *   sends are retried for that batch.
   *
   * This setting helps with failed sends due to too slow authorization or authentication in the broker.
   *
   * Set to `Schedule.stop` to not retry on auth errors.
   *
   * The default is {{{Schedule.recurs(5) && Schedule.spaced(500.millis)}}} which is, to retry 5 times, spaced by 500ms.
   *
   * Warning: When sending a batch of records fails partially, the failed records are retries. This leads to a
   * re-ordering of records.
   */
  def withAuthErrorRetrySchedule(authErrorRetrySchedule: Schedule[Any, Throwable, Any]): ProducerSettings =
    copy(authErrorRetrySchedule = authErrorRetrySchedule)

}

object ProducerSettings {
  def apply(
    bootstrapServers: List[String]
  ): ProducerSettings =
    ProducerSettings().withBootstrapServers(bootstrapServers)
}
