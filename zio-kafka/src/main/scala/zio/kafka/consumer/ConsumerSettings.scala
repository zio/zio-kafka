package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer.fetch.{ FetchStrategy, QueueSizeBasedFetchStrategy }
import zio.kafka.security.KafkaCredentialStore

/**
 * Settings for the consumer.
 *
 * To stay source compatible with future releases, you are recommended to construct the settings as follows:
 * {{{
 *   ConsumerSettings(bootstrapServers)
 *     .withGroupId(groupId)
 *     .withProperties(properties)
 *     .... etc.
 * }}}
 *
 * @param bootstrapServers
 *   the Kafka bootstrap servers
 */
final case class ConsumerSettings(
  properties: Map[String, AnyRef] = Map.empty,
  closeTimeout: Duration = 30.seconds,
  pollTimeout: Duration = 50.millis,
  commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
  rebalanceListener: RebalanceListener = RebalanceListener.noop,
  restartStreamOnRebalancing: Boolean = false,
  fetchStrategy: FetchStrategy = QueueSizeBasedFetchStrategy()
) {
  private[this] def autoOffsetResetConfig: Map[String, String] = offsetRetrieval match {
    case OffsetRetrieval.Auto(reset) => Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> reset.toConfig)
    case OffsetRetrieval.Manual(_)   => Map.empty
  }

  /**
   * Tunes the consumer for high throughput.
   *
   * Sets poll timeout to 500 ms, `max.poll.records` to 2000 and `partitionPreFetchBufferLimit` to 4096.
   *
   * @see
   *   [[ConsumerSettings.withPollTimeout]]
   * @see
   *   [[ConsumerSettings.withMaxPollRecords]]
   * @see
   *   [[zio.kafka.consumer.fetch.QueueSizeBasedFetchStrategy]]
   */
  def tuneForHighThroughput: ConsumerSettings =
    this
      .withPollTimeout(500.millis)
      .withMaxPollRecords(2000)
      .withFetchStrategy(QueueSizeBasedFetchStrategy(partitionPreFetchBufferLimit = 4096))

  /**
   * Tunes the consumer for low latency.
   *
   * Sets poll timeout to 50 ms, `max.poll.records` to 100 and `partitionPreFetchBufferLimit` to 512.
   *
   * @see
   *   [[ConsumerSettings.withPollTimeout]]
   * @see
   *   [[ConsumerSettings.withMaxPollRecords]]
   * @see
   *   [[zio.kafka.consumer.fetch.QueueSizeBasedFetchStrategy]]
   */
  def tuneForLowLatency: ConsumerSettings =
    this
      .withPollTimeout(50.millis)
      .withMaxPollRecords(100)
      .withFetchStrategy(QueueSizeBasedFetchStrategy(partitionPreFetchBufferLimit = 512))

  def driverSettings: Map[String, AnyRef] =
    Map(
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false"
    ) ++ autoOffsetResetConfig ++ properties

  def withBootstrapServers(servers: List[String]): ConsumerSettings =
    withProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers.mkString(","))

  def withCloseTimeout(timeout: Duration): ConsumerSettings =
    copy(closeTimeout = timeout)

  def withCommitTimeout(timeout: Duration): ConsumerSettings =
    copy(commitTimeout = timeout)

  def withClientId(clientId: String): ConsumerSettings =
    withProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId)

  def withGroupId(groupId: String): ConsumerSettings =
    withProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)

  private[consumer] def hasGroupId: Boolean =
    properties.contains(ConsumerConfig.GROUP_ID_CONFIG)

  def withGroupInstanceId(groupInstanceId: String): ConsumerSettings =
    withProperty(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupInstanceId)

  def withOffsetRetrieval(retrieval: OffsetRetrieval): ConsumerSettings =
    copy(offsetRetrieval = retrieval)

  /**
   * The maximum time to block while polling the Kafka consumer. The Kafka consumer will return earlier when the maximum
   * number of record to poll (see https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records) is
   * collected.
   *
   * The default is `50ms` which is good for low latency applications. Set this higher, e.g. `500ms` for better
   * throughput.
   */
  def withPollTimeout(timeout: Duration): ConsumerSettings =
    copy(pollTimeout = timeout)

  /**
   * Set Kafka's `max.poll.interval.ms` configuration. See
   * https://kafka.apache.org/documentation/#consumerconfigs_max.poll.interval.ms for more information.
   *
   * Zio-kafka uses this value also to determine whether a stream stopped processing. If no chunks are pulled from a
   * stream for this interval (while data is available) we consider the stream to be halted. When this happens we
   * interrupt the stream with a failure. In addition the entire consumer is shutdown. In future versions of zio-kafka
   * we may (instead of a shutdown) stop only the affected subscription.
   *
   * The default is 5 minutes. Make sure that all records from a single poll can be processed in this interval. The
   * maximum number of records in a single poll is configured with the `max.poll.records` configuration (see
   * https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records and [[withMaxPollRecords]]).
   */
  def withMaxPollInterval(maxPollInterval: Duration): ConsumerSettings =
    withProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollInterval.toMillis.toString)

  /**
   * Set Kafka's `max.poll.records` configuration. See
   * https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records for more information.
   */
  def withMaxPollRecords(maxPollRecords: Int): ConsumerSettings =
    withProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords.toString)

  def withProperty(key: String, value: AnyRef): ConsumerSettings =
    copy(properties = properties + (key -> value))

  def withProperties(kvs: (String, AnyRef)*): ConsumerSettings =
    withProperties(kvs.toMap)

  def withProperties(kvs: Map[String, AnyRef]): ConsumerSettings =
    copy(properties = properties ++ kvs)

  def withRebalanceListener(listener: RebalanceListener): ConsumerSettings =
    copy(rebalanceListener = listener)

  /**
   * @param value
   *   When `true` _all_ streams are restarted during a rebalance, including those streams that are not revoked. The
   *   default is `false`.
   */
  def withRestartStreamOnRebalancing(value: Boolean): ConsumerSettings =
    copy(restartStreamOnRebalancing = value)

  def withCredentials(credentialsStore: KafkaCredentialStore): ConsumerSettings =
    withProperties(credentialsStore.properties)

  /**
   * @param partitionPreFetchBufferLimit
   *   The queue size at or below which more records are fetched and buffered (per partition). This buffer improves
   *   throughput and supports varying downstream message processing time, while maintaining some backpressure. Large
   *   values effectively disable backpressure at the cost of high memory usage, low values will effectively disable
   *   prefetching in favor of low memory consumption. The number of records that is fetched on every poll is controlled
   *   by the `max.poll.records` setting, the number of records fetched for every partition is somewhere between 0 and
   *   `max.poll.records`.
   *
   * The default value for this parameter is 1024. It is calculated by taking 2 * the default `max.poll.records` of 500,
   * rounded to the nearest power of 2.
   *
   * The value `0` disables pre-fetching.
   */
  def withPartitionPreFetchBufferLimit(partitionPreFetchBufferLimit: Int): ConsumerSettings =
    copy(fetchStrategy = QueueSizeBasedFetchStrategy(partitionPreFetchBufferLimit))

  /**
   * Disables partition record pre-fetching.
   */
  def withoutPartitionPreFetching: ConsumerSettings =
    withPartitionPreFetchBufferLimit(0)

  @deprecated("Use withPartitionPreFetchBufferLimit instead", "2.6.0")
  def withMaxPartitionQueueSize(partitionPreFetchBufferLimit: Int): ConsumerSettings =
    withPartitionPreFetchBufferLimit(partitionPreFetchBufferLimit)

  /**
   * WARNING: [[zio.kafka.consumer.fetch.FetchStrategy]] is an EXPERIMENTAL API and may change in an incompatible way
   * without notice in any zio-kafka version.
   *
   * @param fetchStrategy
   *   The fetch strategy which selects which partitions can fetch data in the next poll. The default is to use the
   *   [[zio.kafka.consumer.fetch.QueueSizeBasedFetchStrategy]] with a `partitionPreFetchBufferLimit` parameter of 1024,
   *   which is calculated by taking 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.
   */
  def withFetchStrategy(fetchStrategy: FetchStrategy): ConsumerSettings =
    copy(fetchStrategy = fetchStrategy)
}

object ConsumerSettings {
  val defaultCommitTimeout: Duration = 15.seconds

  def apply(bootstrapServers: List[String]) =
    new ConsumerSettings().withBootstrapServers(bootstrapServers)
}
