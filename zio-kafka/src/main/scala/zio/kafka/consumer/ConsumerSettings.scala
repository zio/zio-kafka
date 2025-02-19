package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.IsolationLevel
import zio._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer.fetch.{ FetchStrategy, QueueSizeBasedFetchStrategy }
import zio.kafka.security.KafkaCredentialStore
import zio.metrics.MetricLabel

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
 */
final case class ConsumerSettings(
  properties: Map[String, AnyRef] = Map.empty,
  closeTimeout: Duration = 30.seconds,
  pollTimeout: Duration = 50.millis,
  commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
  rebalanceListener: RebalanceListener = RebalanceListener.noop,
  restartStreamOnRebalancing: Boolean = false,
  rebalanceSafeCommits: Boolean = false,
  maxRebalanceDuration: Option[Duration] = None,
  fetchStrategy: FetchStrategy = QueueSizeBasedFetchStrategy(),
  metricLabels: Set[MetricLabel] = Set.empty,
  runloopMetricsSchedule: Schedule[Any, Unit, Long] = Schedule.fixed(500.millis),
  authErrorRetrySchedule: Schedule[Any, Throwable, Any] = Schedule.recurs(5) && Schedule.spaced(500.millis),
  maxStreamPullIntervalOption: Option[Duration] = None
) {
  // Parse booleans in a way compatible with how Kafka does this in org.apache.kafka.common.config.ConfigDef.parseType:
  require(
    properties.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG).forall(_.toString.trim.equalsIgnoreCase("false")),
    "Because zio-kafka does pre-fetching, auto commit is not supported"
  )

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
    Map(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false") ++ properties

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

  /**
   * Which offset to start consuming from for new partitions.
   *
   * The options are:
   * {{{
   *   import zio.kafka.consumer.Consumer._
   *   OffsetRetrieval.Auto(AutoOffsetStrategy.Latest) // the default
   *   OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest)
   *   OffsetRetrieval.Auto(AutoOffsetStrategy.None)
   *   OffsetRetrieval.Manual(getOffsets, defaultStrategy)
   * }}}
   *
   * The `Auto` options make consuming start from the latest committed offset. When no committed offset is available,
   * the given offset strategy is used and consuming starts from the `Latest` offset (the default), the `Earliest`
   * offset, or results in an error for `None`.
   *
   * The `Manual` option allows fine grained control over which offset to consume from. The provided `getOffsets`
   * function should return an offset for each topic-partition that is being assigned. When the returned offset is
   * smaller than the log start offset or larger than the log end offset, the `defaultStrategy` is used and consuming
   * starts from the `Latest` offset (the default), the `Earliest` offset, or results in an error for `None`.
   *
   * When the returned map does ''not'' contain an entry for a topic-partition, the consumer will continue from the last
   * committed offset. When no committed offset is available, the `defaultStrategy` is used and consuming starts from
   * the `Latest` offset (the default), the `Earliest` offset, or results in an error for `None`.
   *
   * This configuration applies to both subscribed and assigned partitions.
   *
   * This method sets the `auto.offset.reset` Kafka configuration. See
   * https://kafka.apache.org/documentation/#consumerconfigs_auto.offset.reset for more information.
   */
  def withOffsetRetrieval(retrieval: OffsetRetrieval): ConsumerSettings = {
    val resetStrategy = retrieval match {
      case OffsetRetrieval.Auto(reset)                => reset
      case OffsetRetrieval.Manual(_, defaultStrategy) => defaultStrategy
    }
    copy(offsetRetrieval = retrieval)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, resetStrategy.toConfig)
  }

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
   * The default is 5 minutes. Make sure that all records from a single poll can be processed in this interval. See also
   * the [[withMaxPollRecords maxPollRecords]] configuration.
   */
  def withMaxPollInterval(maxPollInterval: Duration): ConsumerSettings =
    withProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollInterval.toMillis.toString)

  /**
   * The maximum time a stream may run without pulling a chunk of records.
   *
   * Zio-kafka uses this value to determine whether a stream stopped processing. This is to safeguard against alive
   * consumers in the consumer group which hold partition assignments but make no progress. If no chunks are pulled by
   * user code from a partition stream for this interval (while data is available) we consider the stream to be halted.
   * When this happens we interrupt the stream with a failure. In addition, the entire consumer is shutdown. In future
   * versions of zio-kafka we may (instead of a shutdown) stop only the affected subscription.
   *
   * Make sure that all records from a single poll (see [[withMaxPollRecords maxPollRecords]]) can be processed in this
   * interval, even when there is no concurrency because the records are all in the same partition.
   *
   * The default is equal to [[withMaxPollInterval maxPollInterval]]).
   */
  def withMaxStreamPullInterval(maxStreamPullInterval: Duration): ConsumerSettings =
    copy(maxStreamPullIntervalOption = Some(maxStreamPullInterval))

  /**
   * Set Kafka's `max.poll.records` configuration. See
   * https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records for more information.
   *
   * The default is 500.
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
   *
   * @deprecated
   *   starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
   *   contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
   *   that.
   */
  @deprecated("`restartStreamOnRebalancing` will be removed in zio-kafka 3.0", "2.10.0")
  def withRestartStreamOnRebalancing(value: Boolean): ConsumerSettings =
    copy(restartStreamOnRebalancing = value)

  /**
   * @param value
   *   Whether to hold up a rebalance until all offsets of consumed messages have been committed. The default is
   *   `false`, but the recommended value is `true` as it prevents duplicate messages.
   *
   * Use `false` when:
   *   - your streams do not commit, or
   *   - your streams require access to the consumer (the consumer is not available until the rebalance is done), or
   *   - when it is okay to process records twice (possibly concurrently), for example, because processing is
   *     idempotent.
   *
   * When `true`, messages consumed from revoked partitions must be committed before we allow the rebalance to continue.
   *
   * When a partition is revoked, consuming the messages will be taken over by another consumer. The other consumer will
   * continue from the committed offset. It is therefore important that this consumer commits offsets of all consumed
   * messages. Therefore, by holding up the rebalance until these commits are done, we ensure that the new consumer will
   * start from the correct offset.
   *
   * During a rebalance no new messages can be received _for any stream_. Therefore, _all_ streams are deprived of new
   * messages until the revoked streams are ready committing.
   *
   * Rebalances are held up for at most 3/5 of `maxPollInterval` (see [[withMaxPollInterval]]), by default this
   * calculates to 3 minutes. See [[#withMaxRebalanceDuration]] to change the default.
   *
   * External commits (that is, commits to an external system, e.g. a relational database) must be registered to the
   * consumer with [[Consumer.registerExternalCommits]].
   *
   * When this consumer is coupled to a TransactionalProducer, `rebalanceSafeCommits` must be enabled.
   *
   * When `false`, streams for revoked partitions may continue to run even though the rebalance is not held up. Any
   * offset commits from these streams have a high chance of being delayed (commits are not possible during some phases
   * of a rebalance). The consumer that takes over the partition will likely not see these delayed commits and will
   * start from an earlier offset. The result is that some messages are processed twice and concurrently.
   */
  def withRebalanceSafeCommits(value: Boolean): ConsumerSettings =
    copy(rebalanceSafeCommits = value)

  /**
   * @param value
   *   Maximum time spent in the rebalance callback when `rebalanceSafeCommits` is enabled.
   *
   * In this time zio-kafka awaits processing of records and the completion of commits.
   *
   * By default this value is set to 3/5 of `maxPollInterval` which by default calculates to 3 minutes. Only values
   * between `commitTimeout` and `maxPollInterval` are useful. Lower values will make the rebalance callback be done
   * immediately, higher values lead to lost partitions.
   *
   * See [[#withRebalanceSafeCommits]] for more information.
   */
  def withMaxRebalanceDuration(value: Duration): ConsumerSettings =
    copy(maxRebalanceDuration = Some(value))

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
   * @param fetchStrategy
   *   The fetch strategy which selects which partitions can fetch data in the next poll. The default is to use the
   *   [[zio.kafka.consumer.fetch.QueueSizeBasedFetchStrategy]] with a `partitionPreFetchBufferLimit` parameter of 1024,
   *   which is calculated by taking 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.
   */
  def withFetchStrategy(fetchStrategy: FetchStrategy): ConsumerSettings =
    copy(fetchStrategy = fetchStrategy)

  /**
   * @param metricLabels
   *   The labels given to all metrics collected by zio-kafka. By default no labels are set.
   *
   * For applications with multiple consumers it is recommended to set some metric labels. For example, if one is used,
   * the consumer group id could be used as a label:
   *
   * {{{
   *   consumerSettings.withMetricLabels(Set(MetricLabel("group-id", groupId)))
   * }}}
   */
  def withMetricsLabels(metricLabels: Set[MetricLabel]): ConsumerSettings =
    copy(metricLabels = metricLabels)

  /**
   * @param runloopMetricsSchedule
   *   The schedule at which the runloop metrics are measured. Example runloop metrics are queue sizes and number of
   *   outstanding commits. The default is to measure every 500ms.
   */
  def withRunloopMetricsSchedule(runloopMetricsSchedule: Schedule[Any, Unit, Long]): ConsumerSettings =
    copy(runloopMetricsSchedule = runloopMetricsSchedule)

  /**
   * @param authErrorRetrySchedule
   *   The schedule at which the consumer will retry polling the broker for more records, even though a poll fails with
   *   an [[org.apache.kafka.common.errors.AuthorizationException]] or
   *   [[org.apache.kafka.common.errors.AuthenticationException]].
   *
   * This setting helps with failed polls due to too slow authorization or authentication in the broker. You may also
   * consider increasing `pollTimeout` to reduce auth-work on the broker.
   *
   * Set to `Schedule.stop` to fail the consumer on the first auth error.
   *
   * The default is {{{Schedule.recurs(5) && Schedule.spaced(500.millis)}}} which is, to retry 5 times, spaced by 500ms.
   */
  def withAuthErrorRetrySchedule(authErrorRetrySchedule: Schedule[Any, Throwable, Any]): ConsumerSettings =
    copy(authErrorRetrySchedule = authErrorRetrySchedule)

  /**
   * Controls how to consume records produced transactionally.
   *
   * @param readCommitted
   *   when `true`, only consume records which have been committed, when `false`, consume all records, even records
   *   which are part of an aborted transaction. Non-transactional records will be consumed unconditionally in either
   *   mode.
   *
   * Note that Kafka's default is to read all records (`readCommitted = false`).
   */
  def withReadCommitted(readCommitted: Boolean = true): ConsumerSettings = {
    val isolationLevel = if (readCommitted) IsolationLevel.READ_COMMITTED else IsolationLevel.READ_UNCOMMITTED
    withProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, isolationLevel.toString)
  }

}

object ConsumerSettings {
  val defaultCommitTimeout: Duration = 15.seconds

  def apply(bootstrapServers: List[String]): ConsumerSettings =
    new ConsumerSettings().withBootstrapServers(bootstrapServers)
}
