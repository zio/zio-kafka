package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.security.KafkaCredentialStore

/**
 * @param bootstrapServers
 * @param properties
 * @param closeTimeout
 * @param pollTimeout
 * @param offsetRetrieval
 * @param rebalanceListener
 * @param restartStreamOnRebalancing
 *   When `true` _all_ streams are restarted during a rebalance, including those streams that are not revoked. The
 *   default is `false`.
 * @param runloopTimeout
 *   Internal timeout for each iteration of the command processing and polling loop, use to detect stalling. This should
 *   be much larger than the pollTimeout and the time it takes to process chunks of records. If your consumer is not
 *   subscribed for long periods during its lifetime, this timeout should take that into account as well. When the
 *   timeout expires, the plainStream/partitionedStream/etc will fail with a [[Consumer.RunloopTimeout]].
 * @param maxPartitionQueueSize
 *   Maximum number of records that can be enqueued in a partition stream's buffer before pausing the partition. This
 *   facilitates backpressure and throughput.
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
  maxPartitionQueueSize: Int = 1024
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

  def withRestartStreamOnRebalancing(value: Boolean): ConsumerSettings =
    copy(restartStreamOnRebalancing = value)

  def withCredentials(credentialsStore: KafkaCredentialStore): ConsumerSettings =
    withProperties(credentialsStore.properties)

  def withRunloopTimeout(timeout: Duration): ConsumerSettings =
    copy(runloopTimeout = timeout)

  def withMaxPartitionQueueSize(maxPartitionQueueSize: Int): ConsumerSettings =
    copy(maxPartitionQueueSize = maxPartitionQueueSize)
}

object ConsumerSettings {
  val defaultRunloopTimeout: Duration = 4.minutes
}
