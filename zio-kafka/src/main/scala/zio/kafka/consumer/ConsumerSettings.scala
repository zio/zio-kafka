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
 *
 * @param rebalanceSafeCommits
 *   Whether to hold up a rebalance until all offsets of consumed messages have been committed. The default is `false`,
 *   but the recommended value is `true` as it prevents duplicate messages.
 *
 * Use `false` _only_ when your streams does not do commits, or when it is okay to have messages processed twice
 * concurrently and you cannot afford the performance hit during a rebalance.
 *
 * When `true`, messages consumed from revoked partitions must be committed before we allow the rebalance to continue.
 *
 * When a partition is revoked, consuming the messages will be taken over by another consumer. The other consumer will
 * continue from the committed offset. It it therefore important that this consumer commits offsets of all consumed
 * messages. Therefore, by holding up the rebalance until these commits are done, we ensure that the new consumer will
 * start from the correct offset.
 *
 * During a rebalance no new messages can be received _for any stream_. Therefore, _all_ streams are deprived of new
 * messages until the revoked streams are ready committing.
 *
 * When `false`, streams for revoked partitions may continue to run even though the rebalance is not held up. Any offset
 * commits from these streams have a high chance of being delayed (commits are not possible during some phases of a
 * rebalance). The consumer that takes over the partition will likely not see these delayed commits and will start from
 * an earlier offset. The result is that some messages are processed twice and concurrently.
 *
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
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
  rebalanceListener: RebalanceListener = RebalanceListener.noop,
  restartStreamOnRebalancing: Boolean = false,
  rebalanceSafeCommits: Boolean = false,
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

  def withRebalanceSafeCommits(value: Boolean): ConsumerSettings =
    copy(rebalanceSafeCommits = value)

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
      offsetRetrieval = OffsetRetrieval.Auto(),
      runloopTimeout = defaultRunloopTimeout
    )
}
