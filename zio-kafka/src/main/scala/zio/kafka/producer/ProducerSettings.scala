package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import zio._
import zio.kafka.security.KafkaCredentialStore

/**
 * Settings for the Producer.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings as follows:
 * {{{
 *   ProducerSettings(bootstrapServers)
 *     .withCloseTimeout(30.seconds)
 *     .... etc.
 * }}}
 */
final case class ProducerSettings(
  closeTimeout: Duration = 30.seconds,
  sendBufferSize: Int = 4096,
  authErrorRetrySchedule: Schedule[Any, Throwable, Any] = Schedule.stop,
  properties: Map[String, AnyRef] = Map.empty
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

  /**
   * @param sendBufferSize
   *   The maximum number of record chunks that can queue up while waiting for the underlying producer to become
   *   available.
   */
  def withSendBufferSize(sendBufferSize: Int): ProducerSettings =
    copy(sendBufferSize = sendBufferSize)

  /**
   * @param authErrorRetrySchedule
   *   The schedule at which the producer will retry producing, even when producing fails with an
   *   [[org.apache.kafka.common.errors.AuthorizationException]] or
   *   [[org.apache.kafka.common.errors.AuthenticationException]].
   *
   * This setting helps with failed producing due to too slow authorization or authentication in the broker.
   *
   * For example, to retry 5 times, spaced by 500ms, you can set this to
   * {{{Schedule.recurs(5) && Schedule.spaced(500.millis)}}}
   *
   * The default is `Schedule.stop` which is, to fail the producer on the first auth error.
   *
   * ⚠️ Retrying can cause records to be produced in a different order than the order in which they were given to
   * zio-kafka.
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
