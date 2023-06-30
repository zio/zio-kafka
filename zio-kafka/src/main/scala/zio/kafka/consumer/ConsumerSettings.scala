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
  bootstrapServers: List[String],
  properties: Map[String, AnyRef] = Map.empty,
  closeTimeout: Duration = 30.seconds,
  pollTimeout: Duration = 50.millis,
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
  rebalanceListener: RebalanceListener = RebalanceListener.noop,
  restartStreamOnRebalancing: Boolean = false,
  runloopTimeout: Duration = ConsumerSettings.defaultRunloopTimeout,
  fetchStrategy: FetchStrategy = QueueSizeBasedFetchStrategy()
) {
  private[this] def autoOffsetResetConfig: Map[String, String] = offsetRetrieval match {
    case OffsetRetrieval.Auto(reset) => Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> reset.toConfig)
    case OffsetRetrieval.Manual(_)   => Map.empty
  }

  def driverSettings: Map[String, AnyRef] =
    Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG  -> bootstrapServers.mkString(","),
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false"
    ) ++ autoOffsetResetConfig ++ properties

  def withBootstrapServers(servers: List[String]): ConsumerSettings =
    copy(bootstrapServers = servers)

  def withCloseTimeout(timeout: Duration): ConsumerSettings =
    copy(closeTimeout = timeout)

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

  def withPollTimeout(timeout: Duration): ConsumerSettings =
    copy(pollTimeout = timeout)

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
   * @param timeout
   *   Internal timeout for each iteration of the command processing and polling loop, use to detect stalling. This
   *   should be much larger than the pollTimeout and the time it takes to process chunks of records. If your consumer
   *   is not subscribed for long periods during its lifetime, this timeout should take that into account as well. When
   *   the timeout expires, the plainStream/partitionedStream/etc will fail with a [[Consumer.RunloopTimeout]].
   */
  def withRunloopTimeout(timeout: Duration): ConsumerSettings =
    copy(runloopTimeout = timeout)

  /**
   * @param maxPartitionQueueSize
   *   Maximum number of records to be buffered per partition. This buffer improves throughput and supports varying
   *   downstream message processing time, while maintaining some backpressure. Large values effectively disable
   *   backpressure at the cost of high memory usage, low values will effectively disable prefetching in favour of low
   *   memory consumption. The number of records that is fetched on every poll is controlled by the `max.poll.records`
   *   setting, the number of records fetched for every partition is somewhere between 0 and `max.poll.records`. A value
   *   that is a power of 2 offers somewhat better queueing performance. The default value for this parameter is 1024,
   *   calculated by taking 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.
   */
  def withMaxPartitionQueueSize(maxPartitionQueueSize: Int): ConsumerSettings =
    copy(fetchStrategy = QueueSizeBasedFetchStrategy(maxPartitionQueueSize))

  /**
   * WARNING: [[FetchStrategy]] is an EXPERIMENTAL API and may change in an incompatible way without notice in any
   * zio-kafka version.
   *
   * @param fetchStrategy
   *   The fetch strategy which selects which partitions can fetch data in the next poll. The default is to use the
   *   [[QueueSizeBasedFetchStrategy]] with a `maxPartitionQueueSize` parameter of 1024, which is calculated by taking 2
   *   * the default `max.poll.records` of 500, rounded to the nearest power of 2.
   */
  def withFetchStrategy(fetchStrategy: FetchStrategy): ConsumerSettings =
    copy(fetchStrategy = fetchStrategy)
}

object ConsumerSettings {
  val defaultRunloopTimeout: Duration = 4.minutes
}
