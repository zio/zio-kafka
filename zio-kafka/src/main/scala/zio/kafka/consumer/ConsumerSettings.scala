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
 * @param perPartitionChunkPrefetch
 * @param offsetRetrieval
 * @param rebalanceListener
 * @param restartStreamOnRebalancing
 *   When `true` _all_ streams are restarted during a rebalance, including those streams that are not revoked. The
 *   default is `false`.
 *
 *   Set both `restartStreamOnRebalancing` and `endRevokedStreamsBeforeRebalance` to `true` for
 *   transactional producing.
 * @param endRevokedStreamsBeforeRebalance
 *   When `true` (the default) streams that need to end because the partition has been revoked, will be ended before the
 *   rebalance starts. The consumer that takes over this partition will continue from the committed offset. However, it
 *   is not possible to commit during a rebalance. So holding up the rebalance until the stream has ended (and done its
 *   commits) will prevent duplicate processing.
 *
 *   Set this to `false` when your streams does need commits, or when you need the extra performance and do not care
 *   for duplicate processing.
 *
 *   Set both `restartStreamOnRebalancing` and
 *   `endRevokedStreamsBeforeRebalance` to `true` for transactional producing.
 * @param runloopTimeout
 *   Internal timeout for each iteration of the command processing and polling loop, use to detect stalling. This should
 *   be much larger than the pollTimeout and the time it takes to process chunks of records. If your consumer is not
 *   subscribed for long periods during its lifetime, this timeout should take that into account as well. When the
 *   timeout expires, the plainStream/partitionedStream/etc will fail with a [[Consumer.RunloopTimeout]].
 */
case class ConsumerSettings(
  bootstrapServers: List[String],
  properties: Map[String, AnyRef],
  closeTimeout: Duration,
  pollTimeout: Duration,
  perPartitionChunkPrefetch: Int,
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
  rebalanceListener: RebalanceListener = RebalanceListener.noop,
  restartStreamOnRebalancing: Boolean = false,
  endRevokedStreamsBeforeRebalance: Boolean = true,
  runloopTimeout: Duration = ConsumerSettings.defaultRunloopTimeout
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

  def withPerPartitionChunkPrefetch(prefetch: Int): ConsumerSettings =
    copy(perPartitionChunkPrefetch = prefetch)

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

  def withEndRevokedStreamsBeforeRebalance(value: Boolean): ConsumerSettings =
    copy(endRevokedStreamsBeforeRebalance = value)

  def withCredentials(credentialsStore: KafkaCredentialStore): ConsumerSettings =
    withProperties(credentialsStore.properties)

  def withRunloopTimeout(timeout: Duration): ConsumerSettings =
    copy(runloopTimeout = timeout)
}

object ConsumerSettings {
  val defaultRunloopTimeout: Duration = 4.minutes

  def apply(bootstrapServers: List[String]): ConsumerSettings =
    new ConsumerSettings(
      bootstrapServers = bootstrapServers,
      properties = Map.empty,
      closeTimeout = 30.seconds,
      pollTimeout = 50.millis,
      perPartitionChunkPrefetch = 2,
      offsetRetrieval = OffsetRetrieval.Auto(),
      runloopTimeout = defaultRunloopTimeout
    )
}
