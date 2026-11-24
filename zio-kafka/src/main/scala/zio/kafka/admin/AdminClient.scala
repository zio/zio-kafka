package zio.kafka.admin

import org.apache.kafka.clients.admin.ListOffsetsResult.{ ListOffsetsResultInfo => JListOffsetsResultInfo }
import org.apache.kafka.clients.admin.{
  Admin => JAdmin,
  AlterConfigOp => JAlterConfigOp,
  AlterConfigsOptions => JAlterConfigsOptions,
  AlterConsumerGroupOffsetsOptions => JAlterConsumerGroupOffsetsOptions,
  Config => JConfig,
  ConsumerGroupDescription => JConsumerGroupDescription,
  CreatePartitionsOptions => JCreatePartitionsOptions,
  CreateTopicsOptions => JCreateTopicsOptions,
  DeleteAclsOptions => _,
  DeleteConsumerGroupsOptions => JDeleteConsumerGroupsOptions,
  DeleteRecordsOptions => JDeleteRecordsOptions,
  DeleteTopicsOptions => JDeleteTopicsOptions,
  DescribeClusterOptions => JDescribeClusterOptions,
  DescribeConfigsOptions => JDescribeConfigsOptions,
  DescribeConsumerGroupsOptions => JDescribeConsumerGroupsOptions,
  DescribeTopicsOptions => JDescribeTopicsOptions,
  GroupListing => JGroupListing,
  ListConsumerGroupOffsetsOptions => JListConsumerGroupOffsetsOptions,
  ListConsumerGroupOffsetsSpec => JListConsumerGroupOffsetsSpec,
  ListGroupsOptions => JListGroupsOptions,
  ListOffsetsOptions => JListOffsetsOptions,
  ListTopicsOptions => JListTopicsOptions,
  LogDirDescription => JLogDirDescription,
  MemberDescription => JMemberDescription,
  NewPartitions => JNewPartitions,
  NewTopic => JNewTopic,
  OffsetSpec => JOffsetSpec,
  ReplicaInfo => JReplicaInfo,
  TopicDescription => JTopicDescription,
  TopicListing => JTopicListing,
  _
}
import org.apache.kafka.clients.consumer.{ OffsetAndMetadata => JOffsetAndMetadata }
import org.apache.kafka.common.config.{ ConfigResource => JConfigResource }
import org.apache.kafka.common.errors.ApiException
import org.apache.kafka.common.{
  GroupState => JGroupState,
  GroupType => JGroupType,
  IsolationLevel => JIsolationLevel,
  KafkaFuture,
  Metric => JMetric,
  MetricName => JMetricName,
  Node => JNode,
  TopicPartition => JTopicPartition,
  TopicPartitionInfo => JTopicPartitionInfo,
  Uuid
}
import zio._
import zio.kafka.admin.acl._
import zio.kafka.admin.resource.ResourcePatternFilter
import zio.kafka.utils.SslHelper

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success, Try }

/**
 * A Kafka admin client: it creates, lists and deletes topics, reads and changes configuration, inspects and resets
 * consumer groups, lists offsets, describes the cluster, and manages access control lists (ACLs).
 *
 * This admin client is a wrapper around the java Admin client. See the API of the
 * [[org.apache.kafka.clients.admin.Admin java admin client]] for a full description of the provided operations. Each
 * operation returns a `Task`; a Kafka error, such as `org.apache.kafka.common.errors.TopicExistsException`, becomes the
 * failure of that task. Results use Scala collections, `Option`, and the case classes of the `AdminClient` companion
 * object.
 *
 * Create one with `AdminClient.make`, which closes it when the scope ends, or provide one with the `AdminClient.live`
 * layer. To create a topic and then list the topic names:
 * {{{
 * import zio._
 * import zio.kafka.admin.AdminClient.NewTopic
 *
 * val topicNames: ZIO[Any, Throwable, Set[String]] =
 *   ZIO.scoped {
 *     for {
 *       admin  <- AdminClient.make(AdminClientSettings(List("localhost:9092")))
 *       _      <- admin.createTopic(NewTopic("payments", numPartitions = 3, replicationFactor = 1))
 *       topics <- admin.listTopics()
 *     } yield topics.keySet
 *   }
 * // On a cluster without other topics: Set(payments)
 * }}}
 *
 * Unlike the java Admin client, this client waits until a write operation (for example creating or deleting a topic) is
 * visible to the next read, in one of two ways:
 *
 *   - Polling, used by `createTopic`, `createTopics`, `deleteTopic`, `deleteTopics`, `createPartitions`,
 *     `deleteConsumerGroups`, `alterConsumerGroupOffsets`, `createAcls`, `deleteAcls`, and by `incrementalAlterConfigs`
 *     when it has only `Set` and `Delete` ops. After the broker accepts the request, the client waits 50 ms and then
 *     reads the state back, with a backoff that doubles each time, until the change shows. It stops reading once 5
 *     seconds have passed (the last read can come about 6.5 seconds after the request), and then succeeds even though
 *     the change did not show, for example because another write undid it. When one of these reads fails, the operation
 *     fails with that error.
 *   - A fixed sleep of `visibilitySleep` (default 1 second) after the broker accepts the request, used by
 *     `deleteRecords`, `removeMembersFromConsumerGroup`, `removeAllMembersFromConsumerGroup`, and by
 *     `incrementalAlterConfigs` when it has an `Append` or `Subtract` op.
 *
 * With `validateOnly = true`, `createTopic`, `createTopics`, `createPartitions`, `incrementalAlterConfigs` and
 * `incrementalAlterConfigsAsync` do not wait. `createAclsAsync`, `deleteAclsAsync` and `incrementalAlterConfigsAsync`
 * wait in the inner task of each item. The waiting uses the ZIO `Clock`, so a test that runs with `TestClock` needs a
 * live clock, for example `TestAspect.withLiveClock`.
 *
 * @see
 *   [[https://zio.dev/zio-kafka/admin-client Admin Client]]
 */
trait AdminClient {

  import AdminClient._

  /**
   * Creates the given topics, and polls `listTopics` until every new topic is listed. When `options` sets
   * `validateOnly = true`, the broker only validates the request, nothing is created, and the call does not poll.
   *
   * Fails with a Kafka error, for example `org.apache.kafka.common.errors.TopicExistsException` when a topic already
   * exists.
   */
  def createTopics(
    newTopics: Iterable[NewTopic],
    options: Option[CreateTopicsOptions] = None
  ): Task[Unit]

  /**
   * Creates one topic, and polls `listTopics` until it is listed. With `validateOnly = true`, the broker only validates
   * the request, nothing is created, and the call does not poll. The request uses the client's default timeout.
   *
   * Fails with `org.apache.kafka.common.errors.TopicExistsException` when the topic already exists.
   */
  def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): Task[Unit]

  /**
   * Deletes the given consumer groups, and polls `listConsumerGroups` until none of them is listed.
   *
   * Fails with a Kafka error, for example `org.apache.kafka.common.errors.GroupIdNotFoundException` when a group does
   * not exist.
   */
  def deleteConsumerGroups(
    groupIds: Iterable[String],
    options: Option[DeleteConsumerGroupOptions] = None
  ): Task[Unit]

  /**
   * Deletes the given topics, and polls `listTopics` until none of them is listed.
   *
   * Fails with a Kafka error, for example `org.apache.kafka.common.errors.UnknownTopicOrPartitionException` when a
   * topic does not exist.
   */
  def deleteTopics(
    topics: Iterable[String],
    options: Option[DeleteTopicsOptions] = None
  ): Task[Unit]

  /**
   * Deletes one topic, and polls `listTopics` until it is no longer listed.
   *
   * Fails with `org.apache.kafka.common.errors.UnknownTopicOrPartitionException` when the topic does not exist.
   */
  def deleteTopic(topic: String): Task[Unit]

  /**
   * Deletes the records of each given partition that come before the offset in its `RecordsToDelete` (for example
   * `RecordsToDelete.beforeOffset(offset)`), so that the partition's earliest offset becomes that offset. Then sleeps
   * for `visibilitySleep`.
   *
   * @param visibilitySleep
   *   time to wait after getting a confirmation that the command was accepted by the kafka broker. This should be
   *   enough time for the operation to complete. Set to `Duration.Zero` if you don't care when the operation completes.
   */
  def deleteRecords(
    recordsToDelete: Map[TopicPartition, RecordsToDelete],
    deleteRecordsOptions: Option[DeleteRecordsOptions] = None,
    visibilitySleep: Duration = 1.second
  ): Task[Unit]

  /**
   * Returns the topics in the cluster, keyed by topic name. Internal topics, such as `__consumer_offsets`, are left out
   * unless `listTopicsOptions` sets `listInternal = true`.
   */
  def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): Task[Map[String, TopicListing]]

  /**
   * Returns the description of each given topic (its partitions, with their leader, replicas and in-sync replicas),
   * keyed by topic name. `TopicDescription.authorizedOperations` is `None` unless `options` sets
   * `includeAuthorizedOperations = true`.
   *
   * Fails with `org.apache.kafka.common.errors.UnknownTopicOrPartitionException` when a topic does not exist.
   */
  def describeTopics(
    topicNames: Iterable[String],
    options: Option[DescribeTopicsOptions] = None
  ): Task[Map[String, TopicDescription]]

  /**
   * Returns the configuration of each given resource (a topic, a broker, ...), keyed by resource. For a broker, the
   * resource name is the broker id, for example `"0"`.
   *
   * Fails when one resource fails, for example with `org.apache.kafka.common.errors.UnknownTopicOrPartitionException`
   * for a topic that does not exist. Use `describeConfigsAsync` to get a result per resource.
   */
  def describeConfigs(
    configResources: Iterable[ConfigResource],
    options: Option[DescribeConfigsOptions] = None
  ): Task[Map[ConfigResource, KafkaConfig]]

  /**
   * Like `describeConfigs`, but returns one task per resource. The outer task sends the request and completes without
   * waiting for the broker's answer; each inner task waits for the configuration of one resource, and fails on its own,
   * so a missing topic does not fail the other resources.
   */
  def describeConfigsAsync(
    configResources: Iterable[ConfigResource],
    options: Option[DescribeConfigsOptions] = None
  ): Task[Map[ConfigResource, Task[KafkaConfig]]]

  /**
   * Returns the brokers of the cluster.
   *
   * Fails with a `java.lang.RuntimeException` when the cluster reports a node with a negative id.
   */
  def describeClusterNodes(options: Option[DescribeClusterOptions] = None): Task[List[Node]]

  /**
   * Returns the node that the cluster reports as its controller, or `None` when it reports none.
   */
  def describeClusterController(options: Option[DescribeClusterOptions] = None): Task[Option[Node]]

  /**
   * Returns the id of the cluster.
   */
  def describeClusterId(options: Option[DescribeClusterOptions] = None): Task[String]

  /**
   * Returns the operations this client is allowed to perform on the cluster. Returns an empty set unless `options` sets
   * `includeAuthorizedOperations = true`.
   */
  def describeClusterAuthorizedOperations(
    options: Option[DescribeClusterOptions] = None
  ): Task[Set[AclOperation]]

  /**
   * Raises the partition count of each given topic to its `NewPartitions.totalCount`, and polls `describeTopics` until
   * each topic has at least that many partitions. When `options` sets `validateOnly = true`, nothing changes and the
   * call does not poll.
   *
   * Fails with `org.apache.kafka.common.errors.InvalidPartitionsException` when `totalCount` is not higher than the
   * topic's current partition count.
   */
  def createPartitions(
    newPartitions: Map[String, NewPartitions],
    options: Option[CreatePartitionsOptions] = None
  ): Task[Unit]

  /**
   * Returns an offset for each given partition, chosen by its `OffsetSpec`: the earliest offset still in the log, the
   * latest offset (the offset the next record will get), or the first offset whose timestamp is at or after a given
   * time. The offsets are read from the partitions themselves, not from a consumer group.
   */
  def listOffsets(
    topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
    options: Option[ListOffsetsOptions] = None
  ): Task[Map[TopicPartition, ListOffsetsResultInfo]]

  /**
   * Like `listOffsets`, but returns one task per partition. The outer task sends the request and completes without
   * waiting for the broker's answer; each inner task waits for the offset of one partition.
   */
  def listOffsetsAsync(
    topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
    options: Option[ListOffsetsOptions] = None
  ): Task[Map[TopicPartition, Task[ListOffsetsResultInfo]]]

  /**
   * Returns the committed offsets of one consumer group, keyed by partition. Partitions without a committed offset are
   * left out, and a group that does not exist gives an empty map.
   */
  def listConsumerGroupOffsets(
    groupId: String,
    options: Option[ListConsumerGroupOffsetsOptions] = None
  ): Task[Map[TopicPartition, OffsetAndMetadata]]

  /**
   * Given a mapping of consumer group IDs and list of partitions, list the consumer group offsets available in the
   * cluster for the specified consumer groups and partitions.
   *
   * The result is keyed by group id, then by partition. Partitions without a committed offset are left out, so a group
   * or partition that is not known gives an empty map for that group, and so does a spec with no partitions.
   */
  def listConsumerGroupOffsets(
    groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]
  ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]

  /**
   * Given a mapping of consumer group IDs and list of partitions, list the consumer group offsets available in the
   * cluster for the specified consumer groups and partitions.
   *
   * Same as the overload without `options`, with the request flags of `options` (for example `requireStable`).
   */
  def listConsumerGroupOffsets(
    groupSpecs: Map[String, ListConsumerGroupOffsetsSpec],
    options: ListConsumerGroupOffsetsOptions
  ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]

  /**
   * Sets the committed offsets of a consumer group for the given partitions, and polls `listConsumerGroupOffsets` until
   * every partition shows its new offset. For a group id that does not exist yet, this creates the group, in state
   * `GroupState.Empty`.
   */
  def alterConsumerGroupOffsets(
    groupId: String,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    options: Option[AlterConsumerGroupOffsetsOptions] = None
  ): Task[Unit]

  /**
   * Retrieves metrics for the underlying AdminClient: the Java admin client's own metrics, keyed by metric name. They
   * are read from the client; no request goes to the broker.
   */
  def metrics: Task[Map[MetricName, Metric]]

  /**
   * Returns the consumer groups in the cluster: the groups of type `Classic` or `Consumer` whose protocol type is
   * `consumer` or empty. With `options`, only the groups in one of `ListConsumerGroupsOptions.states` are returned; an
   * empty set returns groups in every state.
   */
  def listConsumerGroups(options: Option[ListConsumerGroupsOptions] = None): Task[List[ConsumerGroupListing]]

  /**
   * Returns the groups in the cluster, whatever their `GroupType`. `options` can filter them by state, type and
   * protocol type; without `options`, no filter is applied.
   */
  def listGroups(options: Option[ListGroupsOptions] = None): Task[List[GroupListing]]

  /**
   * Returns the description of each given consumer group (members, state, coordinator), keyed by group id.
   *
   * Fails with `org.apache.kafka.common.errors.GroupIdNotFoundException` when a group does not exist.
   */
  def describeConsumerGroups(groupIds: String*): Task[Map[String, ConsumerGroupDescription]]

  /**
   * Returns the description of each given consumer group (members, state, coordinator), keyed by group id.
   * `ConsumerGroupDescription.authorizedOperations` is empty unless `options` sets
   * `includeAuthorizedOperations = true`.
   *
   * Fails with `org.apache.kafka.common.errors.GroupIdNotFoundException` when a group does not exist.
   */
  def describeConsumerGroups(
    groupIds: List[String],
    options: Option[DescribeConsumerGroupsOptions]
  ): Task[Map[String, ConsumerGroupDescription]]

  /**
   * Removes the specified members from a consumer group, then sleeps for `visibilitySleep`.
   *
   * Fails with `org.apache.kafka.common.errors.UnknownMemberIdException` when the group has no member with one of the
   * given ids.
   *
   * @param membersToRemove
   *   the group instance ids (`group.instance.id`) of the static members to remove
   * @param visibilitySleep
   *   time to wait after getting a confirmation that the command was accepted by the kafka broker. This should be
   *   enough time for the operation to complete. Set to `Duration.Zero` if you don't care when the operation completes.
   */
  def removeMembersFromConsumerGroup(
    groupId: String,
    membersToRemove: Set[String],
    visibilitySleep: Duration = 1.second
  ): Task[Unit]

  /**
   * Remove all members from a consumer group.
   *
   * Alias for `removeAllMembersFromConsumerGroup` with the default `visibilitySleep`.
   */
  def removeMembersFromConsumerGroup(groupId: String): Task[Unit] =
    removeAllMembersFromConsumerGroup(groupId)

  /**
   * Removes all members from a consumer group, then sleeps for `visibilitySleep`.
   *
   * Fails with a `java.lang.IllegalArgumentException` ("leaving members should not be empty") when the group has no
   * members.
   *
   * @param visibilitySleep
   *   time to wait after getting a confirmation that the command was accepted by the kafka broker. This should be
   *   enough time for the operation to complete. Set to `Duration.Zero` if you don't care when the operation completes.
   */
  def removeAllMembersFromConsumerGroup(groupId: String, visibilitySleep: Duration = 1.second): Task[Unit]

  /**
   * Returns the log directories of each given broker, keyed by broker id and then by log directory path.
   *
   * Fails with a Kafka error, for example `org.apache.kafka.common.errors.TimeoutException` when a broker id is not
   * part of the cluster.
   */
  def describeLogDirs(
    brokersId: Iterable[Int]
  ): ZIO[Any, Throwable, Map[Int, Map[String, LogDirDescription]]]

  /**
   * Like `describeLogDirs`, but returns one task per broker. The outer task sends the request and completes without
   * waiting for the brokers' answers; each inner task waits for the log directories of one broker.
   */
  def describeLogDirsAsync(
    brokersId: Iterable[Int]
  ): ZIO[Any, Throwable, Map[Int, Task[Map[String, LogDirDescription]]]]

  /**
   * Incrementally update the configuration for the specified resources. Only supported by brokers with version 2.3.0 or
   * higher. Zio-kafka does not wrap the Java client's older `alterConfigs`.
   *
   * When `configs` contains an `AlterConfigOpType.Append` or an `AlterConfigOpType.Subtract`, `visibilitySleep` is
   * used. Otherwise, polling is used to wait until the operation completes: `describeConfigs` is read until each `Set`
   * value and each `Delete` shows. When `options` sets `validateOnly = true`, nothing changes and the call does not
   * wait.
   *
   * @param visibilitySleep
   *   time to wait after getting a confirmation that the command was accepted by the kafka broker, when `configs`
   *   contains an `Append` or `Subtract` op. This should be enough time for the operation to complete. Set to
   *   `Duration.Zero` if you don't care when the operation completes.
   */
  def incrementalAlterConfigs(
    configs: Map[ConfigResource, Iterable[AlterConfigOp]],
    options: AlterConfigsOptions,
    visibilitySleep: Duration = 1.second
  ): Task[Unit]

  /**
   * Incrementally update the configuration for the specified resources async. Only supported by brokers with version
   * 2.3.0 or higher. Zio-kafka does not wrap the Java client's older `alterConfigs`.
   *
   * When `configs` contains an `AlterConfigOpType.Append` or an `AlterConfigOpType.Subtract`, `visibilitySleep` is
   * used. Otherwise, polling is used to wait until the operation completes. This method still returns immediately with
   * the outer task, sleeping/polling happens in the inner task. The inner task of each resource waits for that
   * resource's result only. When `options` sets `validateOnly = true`, the inner tasks do not poll or sleep.
   *
   * @param visibilitySleep
   *   time to wait (in the inner task) after getting a confirmation that the command was accepted by the kafka broker.
   *   This should be enough time for the operation to complete. Set to `Duration.Zero` if you don't care when the
   *   operation completes.
   */
  def incrementalAlterConfigsAsync(
    configs: Map[ConfigResource, Iterable[AlterConfigOp]],
    options: AlterConfigsOptions,
    visibilitySleep: Duration = 1.second
  ): Task[Map[ConfigResource, Task[Unit]]]

  /**
   * Lists access control lists (ACLs) according to the supplied filter.
   *
   * `createAcls` and `deleteAcls` of this client complete once their change shows in `describeAcls`, unless their
   * polling gave up after about 5 seconds. Changes made in other ways can take some time to show.
   */
  def describeAcls(filter: AclBindingFilter, options: Option[DescribeAclOptions] = None): Task[Set[AclBinding]]

  /**
   * Creates access control lists (ACLs) which are bound to specific resources, and polls `describeAcls` until every new
   * binding is listed.
   */
  def createAcls(acls: Set[AclBinding], options: Option[CreateAclOptions] = None): Task[Unit]

  /**
   * Creates access control lists (ACLs) which are bound to specific resources async: like `createAcls`, but returns one
   * task per binding. The outer task sends the request and completes without waiting for the broker's answer; each
   * inner task waits for the result of one binding, then polls `describeAcls` until that binding is listed.
   */
  def createAclsAsync(
    acls: Set[AclBinding],
    options: Option[CreateAclOptions] = None
  ): Task[Map[AclBinding, Task[Unit]]]

  /**
   * Deletes access control lists (ACLs) according to the supplied filters, and returns the bindings that were deleted.
   * Then polls `describeAcls` with each filter until it matches nothing.
   */
  def deleteAcls(filters: Set[AclBindingFilter], options: Option[DeleteAclsOptions] = None): Task[Set[AclBinding]]

  /**
   * Deletes access control lists (ACLs) according to the supplied filters async: like `deleteAcls`, but returns one
   * task per filter. The outer task sends the request and completes without waiting for the broker's answer. Each inner
   * task returns, for each binding its filter matched, `None` when the binding was deleted or `Some(error)` when its
   * deletion failed; then it polls `describeAcls` with its filter until it matches nothing.
   */
  def deleteAclsAsync(
    filters: Set[AclBindingFilter],
    options: Option[DeleteAclsOptions] = None
  ): Task[Map[AclBindingFilter, Task[Map[AclBinding, Option[Throwable]]]]]

}

/**
 * Constructors for [[AdminClient]] (`make`, `live`, `fromJavaClient`, `fromScopedJavaClient` and
 * `javaClientFromSettings`), the model and options case classes that its operations take and return, and helpers that
 * convert Kafka futures and Java types.
 */
object AdminClient {

  /**
   * Wrapper around the apache java AdminClient.
   *
   * @param adminClient
   *   wrapped java admin client
   */
  private final class LiveAdminClient(
    private val adminClient: JAdmin
  ) extends AdminClient {

    override def createTopics(
      newTopics: Iterable[NewTopic],
      options: Option[CreateTopicsOptions] = None
    ): Task[Unit] = {
      val asJava     = newTopics.map(_.asJava).asJavaCollection
      val topicNames = asJava.asScala.map(_.name()).toIndexedSeq

      fromKafkaFutureVoid {
        ZIO.attempt {
          options
            .fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts.asJava))
            .all()
        }
      } <*
        awaitVisible(
          listTopics(),
          (topics: Map[String, TopicListing]) => topicNames.forall(topics.contains)
        ).unless(options.exists(_.validateOnly))
    }

    override def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): Task[Unit] =
      createTopics(List(newTopic), Some(CreateTopicsOptions(validateOnly = validateOnly, timeout = Option.empty)))

    override def deleteConsumerGroups(
      groupIds: Iterable[String],
      options: Option[DeleteConsumerGroupOptions]
    ): Task[Unit] = {
      val asJava      = groupIds.asJavaCollection
      val groupIdsSet = groupIds.toSet
      fromKafkaFutureVoid {
        ZIO.attempt {
          options
            .fold(adminClient.deleteConsumerGroups(asJava)) { opts =>
              adminClient.deleteConsumerGroups(asJava, opts.asJava)
            }
            .all()
        }
      } <*
        awaitVisible(
          listConsumerGroups(),
          (current: List[ConsumerGroupListing]) => current.forall(g => !groupIdsSet.contains(g.groupId))
        )
    }

    override def deleteTopics(
      topics: Iterable[String],
      options: Option[DeleteTopicsOptions] = None
    ): Task[Unit] = {
      val asJava     = topics.asJavaCollection
      val topicNames = asJava.asScala.toIndexedSeq
      fromKafkaFutureVoid {
        ZIO.attempt {
          options
            .fold(adminClient.deleteTopics(asJava))(opts => adminClient.deleteTopics(asJava, opts.asJava))
            .all()
        }
      } <*
        awaitVisible(
          listTopics(),
          (current: Map[String, TopicListing]) => topicNames.forall(name => !current.contains(name))
        )
    }

    override def deleteTopic(topic: String): Task[Unit] =
      deleteTopics(List(topic))

    override def deleteRecords(
      recordsToDelete: Map[TopicPartition, RecordsToDelete],
      deleteRecordsOptions: Option[DeleteRecordsOptions] = None,
      visibilitySleep: Duration = 1.second
    ): Task[Unit] = {
      val records = recordsToDelete.map { case (k, v) => k.asJava -> v }.asJava
      fromKafkaFutureVoid {
        ZIO.attempt {
          deleteRecordsOptions
            .fold(adminClient.deleteRecords(records))(opts => adminClient.deleteRecords(records, opts.asJava))
            .all()
        }
      } <* ZIO.sleep(visibilitySleep)
    }

    override def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): Task[Map[String, TopicListing]] =
      fromKafkaFuture {
        ZIO.attempt {
          listTopicsOptions
            .fold(adminClient.listTopics())(opts => adminClient.listTopics(opts.asJava))
            .namesToListings()
        }
      }.map(_.asScala.map { case (k, v) => k -> TopicListing.fromJava(v) }.toMap)

    override def describeTopics(
      topicNames: Iterable[String],
      options: Option[DescribeTopicsOptions] = None
    ): Task[Map[String, TopicDescription]] = {
      val asJava = topicNames.asJavaCollection
      fromKafkaFuture {
        ZIO.attempt {
          options
            .fold(adminClient.describeTopics(asJava))(opts => adminClient.describeTopics(asJava, opts.asJava))
            .allTopicNames()
        }
      }.flatMap { jTopicDescriptions =>
        ZIO.fromTry {
          jTopicDescriptions.asScala.toList.forEach { case (k, v) =>
            AdminClient.TopicDescription.fromJava(v).map(k -> _)
          }
            .map(_.toMap)
        }
      }
    }

    override def describeConfigs(
      configResources: Iterable[ConfigResource],
      options: Option[DescribeConfigsOptions] = None
    ): Task[Map[ConfigResource, KafkaConfig]] = {
      val asJava = configResources.map(_.asJava).asJavaCollection
      fromKafkaFuture {
        ZIO.attempt {
          options
            .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
            .all()
        }
      }.map {
        _.asScala.view.map { case (configResource, config) =>
          (ConfigResource.fromJava(configResource), KafkaConfig.fromJava(config))
        }.toMap
      }
    }

    override def describeConfigsAsync(
      configResources: Iterable[ConfigResource],
      options: Option[DescribeConfigsOptions] = None
    ): Task[Map[ConfigResource, Task[KafkaConfig]]] = {
      val asJava = configResources.map(_.asJava).asJavaCollection
      ZIO.attempt {
        options
          .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
          .values()
      }.map {
        _.asScala.view.map { case (configResource, configFuture) =>
          (
            ConfigResource.fromJava(configResource),
            ZIO
              .fromCompletionStage(configFuture.toCompletionStage)
              .map(config => KafkaConfig.fromJava(config))
          )

        }.toMap
      }
    }

    private def describeCluster(options: Option[DescribeClusterOptions]): Task[DescribeClusterResult] =
      ZIO.attempt {
        options.fold(adminClient.describeCluster())(opts => adminClient.describeCluster(opts.asJava))
      }

    override def describeClusterNodes(options: Option[DescribeClusterOptions] = None): Task[List[Node]] =
      fromKafkaFuture(
        describeCluster(options).map(_.nodes())
      ).flatMap { nodes =>
        ZIO.fromTry {
          nodes.asScala.toList.forEach { jNode =>
            Node.fromJava(jNode) match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode not expected when listing cluster nodes"))
            }
          }
        }
      }

    override def describeClusterController(options: Option[DescribeClusterOptions] = None): Task[Option[Node]] =
      fromKafkaFuture(
        describeCluster(options).map(_.controller())
      ).map(Node.fromJava)

    override def describeClusterId(options: Option[DescribeClusterOptions] = None): Task[String] =
      fromKafkaFuture(
        describeCluster(options).map(_.clusterId())
      )

    override def describeClusterAuthorizedOperations(
      options: Option[DescribeClusterOptions] = None
    ): Task[Set[AclOperation]] =
      for {
        res <- describeCluster(options)
        opt <- fromKafkaFuture(ZIO.attempt(res.authorizedOperations())).map(Option(_))
        lst <- ZIO.fromOption(opt.map(_.asScala.toSet)).orElseSucceed(Set.empty)
        aclOperations = lst.map(AclOperation.apply)
      } yield aclOperations

    override def createPartitions(
      newPartitions: Map[String, NewPartitions],
      options: Option[CreatePartitionsOptions] = None
    ): Task[Unit] = {
      val asJava        = newPartitions.map { case (k, v) => k -> v.asJava }.asJava
      val topicNames    = Chunk.fromIterable(newPartitions.keys)
      val expectedSizes = newPartitions.map { case (name, np) => name -> np.totalCount }
      fromKafkaFutureVoid {
        ZIO.attempt {
          options
            .fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts.asJava))
            .all()
        }
      } <*
        awaitVisible(
          describeTopics(topicNames),
          (described: Map[String, TopicDescription]) =>
            expectedSizes.forall { case (name, expected) =>
              described.get(name).exists(_.partitions.size >= expected)
            }
        ).unless(options.exists(_.validateOnly))
    }

    override def listOffsets(
      topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
      options: Option[ListOffsetsOptions] = None
    ): Task[Map[TopicPartition, ListOffsetsResultInfo]] = {
      val asJava = topicPartitionOffsets.bimap(_.asJava, _.asJava).asJava
      fromKafkaFuture {
        ZIO.attempt {
          options
            .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
            .all()
        }
      }
    }.map(_.asScala.bimap(TopicPartition.fromJava, ListOffsetsResultInfo.fromJava).toMap)

    override def listOffsetsAsync(
      topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
      options: Option[ListOffsetsOptions]
    ): Task[Map[TopicPartition, Task[ListOffsetsResultInfo]]] = {
      val topicPartitionOffsetsAsJava = topicPartitionOffsets.bimap(_.asJava, _.asJava)
      val topicPartitionsAsJava       = topicPartitionOffsetsAsJava.keySet
      val asJava                      = topicPartitionOffsetsAsJava.asJava
      ZIO.attempt {
        val listOffsetsResult = options
          .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
        topicPartitionsAsJava.map(tp => tp -> listOffsetsResult.partitionResult(tp))
      }
    }.map {
      _.view.map { case (topicPartition, listOffsetResultInfoFuture) =>
        (
          TopicPartition.fromJava(topicPartition),
          ZIO.fromCompletionStage(listOffsetResultInfoFuture.toCompletionStage).map(ListOffsetsResultInfo.fromJava)
        )
      }.toMap
    }

    override def listConsumerGroupOffsets(
      groupId: String,
      options: Option[ListConsumerGroupOffsetsOptions] = None
    ): Task[Map[TopicPartition, OffsetAndMetadata]] =
      fromKafkaFuture {
        ZIO.attempt {
          options
            .fold(adminClient.listConsumerGroupOffsets(groupId))(opts =>
              adminClient.listConsumerGroupOffsets(groupId, opts.asJava)
            )
            .partitionsToOffsetAndMetadata()
        }
      }.map {
        _.asScala.filter { case (_, om) => om ne null }
          .bimap(TopicPartition.fromJava, OffsetAndMetadata.fromJava)
          .toMap
      }

    override def listConsumerGroupOffsets(
      groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]
    ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]] =
      fromKafkaFuture {
        ZIO.attempt {
          adminClient
            .listConsumerGroupOffsets(groupSpecs.map { case (groupId, offsetsSpec) =>
              (groupId, offsetsSpec.asJava)
            }.asJava)
            .all()
        }
      }.map {
        _.asScala.map { case (groupId, offsets) =>
          groupId ->
            offsets.asScala.filter { case (_, om) => om ne null }
              .bimap(TopicPartition.fromJava, OffsetAndMetadata.fromJava)
              .toMap
        }.toMap
      }

    override def listConsumerGroupOffsets(
      groupSpecs: Map[String, ListConsumerGroupOffsetsSpec],
      options: ListConsumerGroupOffsetsOptions
    ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]] =
      fromKafkaFuture {
        ZIO.attempt {
          adminClient
            .listConsumerGroupOffsets(
              groupSpecs.map { case (groupId, offsetsSpec) => (groupId, offsetsSpec.asJava) }.asJava,
              options.asJava
            )
            .all()
        }
      }.map {
        _.asScala.map { case (groupId, offsets) =>
          groupId ->
            offsets.asScala.filter { case (_, om) => om ne null }
              .bimap(TopicPartition.fromJava, OffsetAndMetadata.fromJava)
              .toMap
        }.toMap
      }

    override def alterConsumerGroupOffsets(
      groupId: String,
      offsets: Map[TopicPartition, OffsetAndMetadata],
      options: Option[AlterConsumerGroupOffsetsOptions] = None
    ): Task[Unit] = {
      val asJava     = offsets.bimap(_.asJava, _.asJava).asJava
      val groupSpecs = Map(groupId -> ListConsumerGroupOffsetsSpec(Chunk.fromIterable(offsets.keySet)))
      val expected   = offsets.view.mapValues(_.offset).toMap
      fromKafkaFutureVoid {
        ZIO.attempt {
          options
            .fold(adminClient.alterConsumerGroupOffsets(groupId, asJava)) { opts =>
              adminClient.alterConsumerGroupOffsets(groupId, asJava, opts.asJava)
            }
            .all()
        }
      } <*
        awaitVisible(
          listConsumerGroupOffsets(groupSpecs),
          (current: Map[String, Map[TopicPartition, OffsetAndMetadata]]) =>
            expected.forall { case (tp, expectedOffset) =>
              current.get(groupId).flatMap(_.get(tp)).exists(_.offset == expectedOffset)
            }
        )
    }

    override def metrics: Task[Map[MetricName, Metric]] =
      ZIO.attempt {
        adminClient.metrics().asScala.toMap.map { case (metricName, metric) =>
          (MetricName.fromJava(metricName), Metric.fromJava(metric))
        }
      }

    override def listConsumerGroups(
      options: Option[ListConsumerGroupsOptions] = None
    ): Task[List[ConsumerGroupListing]] =
      fromKafkaFuture {
        ZIO.attempt {
          val result = options match {
            case None =>
              adminClient.listGroups(JListGroupsOptions.forConsumerGroups())
            case Some(opt) =>
              adminClient.listGroups(opt.asJListGroupsOptions)
          }
          result.all()
        }
      }.map(_.asScala.map(ConsumerGroupListing.fromJava).toList)

    override def listGroups(options: Option[ListGroupsOptions] = None): Task[List[GroupListing]] =
      fromKafkaFuture {
        ZIO.attempt {
          val result = options match {
            case None =>
              adminClient.listGroups()
            case Some(opts) =>
              adminClient.listGroups(opts.asJava)
          }
          result.all()
        }
      }.map(_.asScala.map(GroupListing.fromJava).toList)

    override def describeConsumerGroups(groupIds: String*): Task[Map[String, ConsumerGroupDescription]] =
      describeConsumerGroups(groupIds.toList, options = None)

    override def describeConsumerGroups(
      groupIds: List[String],
      options: Option[DescribeConsumerGroupsOptions]
    ): Task[Map[String, ConsumerGroupDescription]] =
      fromKafkaFuture(
        ZIO.attempt {
          options
            .fold(adminClient.describeConsumerGroups(groupIds.asJavaCollection))(opts =>
              adminClient.describeConsumerGroups(groupIds.asJavaCollection, opts.asJava)
            )
            .all
        }
      ).map(_.asScala.map { case (k, v) => k -> ConsumerGroupDescription.fromJava(v) }.toMap)

    override def removeMembersFromConsumerGroup(
      groupId: String,
      membersToRemove: Set[String],
      visibilitySleep: Duration = 1.second
    ): Task[Unit] = {
      val options = new RemoveMembersFromConsumerGroupOptions(
        membersToRemove.map(new MemberToRemove(_)).asJavaCollection
      )
      fromKafkaFutureVoid {
        ZIO.attempt {
          adminClient.removeMembersFromConsumerGroup(groupId, options).all()
        }
      } <* ZIO.sleep(visibilitySleep)
    }

    override def removeAllMembersFromConsumerGroup(
      groupId: String,
      visibilitySleep: Duration = 1.second
    ): Task[Unit] = {
      val options = new RemoveMembersFromConsumerGroupOptions()
      fromKafkaFutureVoid {
        ZIO.attempt {
          adminClient.removeMembersFromConsumerGroup(groupId, options).all()
        }
      } <* ZIO.sleep(visibilitySleep)
    }

    override def describeLogDirs(
      brokersId: Iterable[Int]
    ): ZIO[Any, Throwable, Map[Int, Map[String, LogDirDescription]]] =
      fromKafkaFuture {
        ZIO.attempt {
          adminClient.describeLogDirs(brokersId.map(Int.box).asJavaCollection).allDescriptions()
        }
      }.map {
        _.asScala.bimap(_.intValue, _.asScala.bimap(identity, LogDirDescription.fromJava).toMap).toMap
      }

    override def describeLogDirsAsync(
      brokersId: Iterable[Int]
    ): ZIO[Any, Throwable, Map[Int, Task[Map[String, LogDirDescription]]]] =
      ZIO.attempt {
        adminClient.describeLogDirs(brokersId.map(Int.box).asJavaCollection).descriptions()
      }.map {
        _.asScala.view.map { case (brokerId, descriptionsFuture) =>
          (
            brokerId.intValue(),
            ZIO
              .fromCompletionStage(descriptionsFuture.toCompletionStage)
              .map(_.asScala.toMap.map { case (k, v) => (k, LogDirDescription.fromJava(v)) })
          )
        }.toMap
      }

    override def incrementalAlterConfigs(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      options: AlterConfigsOptions,
      visibilitySleep: Duration = 1.second
    ): Task[Unit] =
      fromKafkaFutureVoid {
        ZIO.attempt {
          adminClient
            .incrementalAlterConfigs(
              configs.map { case (configResource, alterConfigOps) =>
                (configResource.asJava, alterConfigOps.map(_.asJava).asJavaCollection)
              }.asJava,
              options.asJava
            )
            .all()
        }
      } <* awaitConfigsVisible(configs, visibilitySleep).unless(options.validateOnly)

    override def incrementalAlterConfigsAsync(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      options: AlterConfigsOptions,
      visibilitySleep: Duration = 1.second
    ): Task[Map[ConfigResource, Task[Unit]]] =
      ZIO.attempt {
        adminClient
          .incrementalAlterConfigs(
            configs.map { case (configResource, alterConfigOps) =>
              (configResource.asJava, alterConfigOps.map(_.asJava).asJavaCollection)
            }.asJava,
            options.asJava
          )
          .values()
      }.map {
        _.asScala.map { case (jConfigResource, kf) =>
          val configResource = ConfigResource.fromJava(jConfigResource)
          val ops            = configs.getOrElse(configResource, Iterable.empty)
          (
            configResource,
            ZIO.fromCompletionStage(kf.toCompletionStage).unit <*
              awaitConfigsVisible(Map(configResource -> ops), visibilitySleep).unless(options.validateOnly)
          )
        }.toMap
      }

    override def describeAcls(
      filter: AclBindingFilter,
      options: Option[DescribeAclOptions]
    ): Task[Set[AclBinding]] =
      fromKafkaFuture {
        ZIO.attempt {
          options
            .fold(adminClient.describeAcls(filter.asJava))(opt => adminClient.describeAcls(filter.asJava, opt.asJava))
            .values()
        }
      }.map(_.asScala.view.map(AclBinding(_)).toSet)

    override def createAcls(acls: Set[AclBinding], options: Option[CreateAclOptions]): Task[Unit] =
      fromKafkaFutureVoid {
        ZIO.attempt {
          options
            .fold(adminClient.createAcls(acls.map(_.asJava).asJava)) { opt =>
              adminClient.createAcls(acls.map(_.asJava).asJava, opt.asJava)
            }
            .all()
        }
      } <*
        awaitVisible(
          describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any), None),
          (current: Set[AclBinding]) => acls.forall(current.contains)
        )

    override def createAclsAsync(
      acls: Set[AclBinding],
      options: Option[CreateAclOptions]
    ): Task[Map[AclBinding, Task[Unit]]] =
      ZIO.attempt {
        options
          .fold(adminClient.createAcls(acls.map(_.asJava).asJava)) { opt =>
            adminClient.createAcls(acls.map(_.asJava).asJava, opt.asJava)
          }
          .values()
      }.map {
        _.asScala.view.map { case (k, v) =>
          val binding = AclBinding(k)
          val await =
            awaitVisible(
              describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any), None),
              (current: Set[AclBinding]) => current.contains(binding)
            )
          (binding, ZIO.fromCompletionStage(v.toCompletionStage).unit <* await)
        }.toMap
      }

    override def deleteAcls(filters: Set[AclBindingFilter], options: Option[DeleteAclsOptions]): Task[Set[AclBinding]] =
      fromKafkaFuture {
        ZIO.attempt {
          options
            .fold(adminClient.deleteAcls(filters.map(_.asJava).asJava)) { opt =>
              adminClient.deleteAcls(filters.map(_.asJava).asJava, opt.asJava)
            }
            .all()
        }
      }
        .map(_.asScala.view.map(AclBinding(_)).toSet) <*
        ZIO.foreachDiscard(filters) { filter =>
          awaitVisible(describeAcls(filter, None), (current: Set[AclBinding]) => current.isEmpty)
        }

    override def deleteAclsAsync(
      filters: Set[AclBindingFilter],
      options: Option[DeleteAclsOptions]
    ): Task[Map[AclBindingFilter, Task[Map[AclBinding, Option[Throwable]]]]] =
      ZIO.attempt {
        options
          .fold(adminClient.deleteAcls(filters.map(_.asJava).asJava)) { opt =>
            adminClient.deleteAcls(filters.map(_.asJava).asJava, opt.asJava)
          }
          .values()
      }.map {
        _.asScala.view.map { case (k, v) =>
          (
            AclBindingFilter(k),
            ZIO
              .fromCompletionStage(v.toCompletionStage)
              .map(
                _.values().asScala.view.map { filterRes =>
                  // FilterResult.binding() is claimed to be nullable but in fact it is not: see DeleteAclsResponse.aclBinding
                  AclBinding(filterRes.binding()) -> Option(filterRes.exception())
                }.toMap
              ) <* awaitVisible(describeAcls(AclBindingFilter(k), None), (current: Set[AclBinding]) => current.isEmpty)
          )
        }.toMap
      }

    /**
     * Polls `read` (exponential backoff starting at 50ms) until `ready` reports the change is visible, or 5 seconds
     * elapses, with a silent success on timeout.
     */
    // noinspection SimplifySleepInspection
    private def awaitVisible[A](read: => Task[A], ready: A => Boolean): Task[Unit] =
      ZIO.sleep(50.millis) *>
        read
          .repeat(
            Schedule.exponential(50.millis) &&
              Schedule.upTo(5.seconds) &&
              Schedule.recurUntil(ready)
          )
          .unit

    /**
     * Visibility check for `incrementalAlterConfigs`. For requests containing only Set and Delete ops it polls
     * `describeConfigs` until each op's effect is observable. Requests containing Append or Subtract fall back to
     * `visibilitySleep` because verifying them would require comparing against pre-op state.
     */
    private def awaitConfigsVisible(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      visibilitySleep: Duration
    ): Task[Unit] = {
      val hasAppendOrSubtract = configs.valuesIterator.flatten.exists { op =>
        op.opType == AlterConfigOpType.Append || op.opType == AlterConfigOpType.Subtract
      }
      if (hasAppendOrSubtract) ZIO.sleep(visibilitySleep)
      else {
        awaitVisible(
          describeConfigs(configs.keys.toList),
          (current: Map[ConfigResource, KafkaConfig]) =>
            configs.forall { case (resource, ops) =>
              current.get(resource).exists(config => ops.forall(op => isConfigOpVisible(config, op)))
            }
        )
      }
    }

    private def isConfigOpVisible(config: KafkaConfig, op: AlterConfigOp): Boolean = {
      val key = op.configEntry.name()
      op.opType match {
        case AlterConfigOpType.Set =>
          config.entries.get(key).exists { entry =>
            entry.value() == op.configEntry.value() &&
            entry.source() != ConfigEntry.ConfigSource.DEFAULT_CONFIG
          }
        case AlterConfigOpType.Delete =>
          config.entries.get(key).forall(_.source() == ConfigEntry.ConfigSource.DEFAULT_CONFIG)
        case _ => true
      }
    }

  }

  /**
   * A layer that creates an [[AdminClient]] from the [[AdminClientSettings]] in the environment, as `make` does, and
   * closes it when the layer is released.
   */
  val live: ZLayer[AdminClientSettings, Throwable, AdminClient] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[AdminClientSettings]
        admin    <- make(settings)
      } yield admin
    }

  /**
   * Runs `kfv`, then waits for the `KafkaFuture` it returns: succeeds with the future's value, or fails with the
   * future's error (for example a `org.apache.kafka.common.errors.TopicExistsException`). Use it to call a Java admin
   * operation that this client does not wrap.
   */
  def fromKafkaFuture[R, T](kfv: RIO[R, KafkaFuture[T]]): RIO[R, T] =
    kfv.flatMap(f => ZIO.fromCompletionStage(f.toCompletionStage))

  /** Like `fromKafkaFuture`, for a `KafkaFuture[Void]`: succeeds with `Unit`. */
  def fromKafkaFutureVoid[R](kfv: RIO[R, KafkaFuture[Void]]): RIO[R, Unit] =
    fromKafkaFuture(kfv).unit

  /**
   * A resource that has a configuration, for `describeConfigs` and `incrementalAlterConfigs`.
   *
   * @param name
   *   the topic name for `ConfigResourceType.Topic`, and the broker id (for example `"0"`) for
   *   `ConfigResourceType.Broker` and `ConfigResourceType.BrokerLogger`. An empty name stands for the default resource
   *   of the type.
   */
  final case class ConfigResource(`type`: ConfigResourceType, name: String) {

    /** Converts to the Java `ConfigResource`. */
    lazy val asJava: JConfigResource = new JConfigResource(`type`.asJava, name)
  }

  /** Conversions for [[ConfigResource]]. */
  object ConfigResource {

    /** Converts a Java `ConfigResource`. */
    def fromJava(jcr: JConfigResource): ConfigResource =
      ConfigResource(`type` = ConfigResourceType.fromJava(jcrt = jcr.`type`()), name = jcr.name())
  }

  /** The type of a [[ConfigResource]]. */
  trait ConfigResourceType {

    /** Converts to the Java `ConfigResource.Type`. */
    def asJava: JConfigResource.Type
  }

  /** The values of [[ConfigResourceType]], one per Java `ConfigResource.Type`. */
  object ConfigResourceType {

    /** The logger levels of a broker; the resource name is the broker id. */
    case object BrokerLogger extends ConfigResourceType {
      override def asJava = JConfigResource.Type.BROKER_LOGGER
    }

    /** The configuration of a broker; the resource name is the broker id. */
    case object Broker extends ConfigResourceType {
      override def asJava = JConfigResource.Type.BROKER
    }

    /** The configuration of a topic; the resource name is the topic name. */
    case object Topic extends ConfigResourceType {
      override def asJava = JConfigResource.Type.TOPIC
    }

    /** A resource type that this client does not know. */
    case object Unknown extends ConfigResourceType {
      override def asJava = JConfigResource.Type.UNKNOWN
    }

    /** A client metrics subscription; the resource name is the subscription name. */
    case object ClientMetrics extends ConfigResourceType {
      override def asJava = JConfigResource.Type.CLIENT_METRICS
    }

    /** The configuration of a group; the resource name is the group id. */
    case object Group extends ConfigResourceType {
      override def asJava = JConfigResource.Type.GROUP
    }

    /** Converts a Java `ConfigResource.Type`. */
    def fromJava(jcrt: JConfigResource.Type): ConfigResourceType =
      jcrt match {
        case JConfigResource.Type.BROKER_LOGGER  => BrokerLogger
        case JConfigResource.Type.BROKER         => Broker
        case JConfigResource.Type.TOPIC          => Topic
        case JConfigResource.Type.UNKNOWN        => Unknown
        case JConfigResource.Type.CLIENT_METRICS => ClientMetrics
        case JConfigResource.Type.GROUP          => Group
      }
  }

  /**
   * The state of a group, as reported by the broker. Which states a group can be in depends on its [[GroupType]].
   */
  sealed trait GroupState {

    /** Converts to the Java `GroupState`. */
    def asJava: JGroupState
  }

  /** The values of [[GroupState]], one per Java `GroupState`. */
  object GroupState {

    /** A state that this client cannot parse, for example one reported by a newer broker. */
    case object Unknown extends GroupState {
      override def asJava: JGroupState = JGroupState.UNKNOWN
    }

    /** A rebalance has started and the group waits for its members to join. Used by classic and consumer groups. */
    case object PreparingRebalance extends GroupState {
      override def asJava: JGroupState = JGroupState.PREPARING_REBALANCE
    }

    /** The members have joined and wait for their assignment. Used by classic and consumer groups. */
    case object CompletingRebalance extends GroupState {
      override def asJava: JGroupState = JGroupState.COMPLETING_REBALANCE
    }

    /** The group has members and a settled assignment. */
    case object Stable extends GroupState {
      override def asJava: JGroupState = JGroupState.STABLE
    }

    /** The group has no members left and its metadata is being removed. */
    case object Dead extends GroupState {
      override def asJava: JGroupState = JGroupState.DEAD
    }

    /**
     * The group has no members, but can still have committed offsets. A group that `alterConsumerGroupOffsets` creates
     * is in this state.
     */
    case object Empty extends GroupState {
      override def asJava: JGroupState = JGroupState.EMPTY
    }

    /** The group computes a new target assignment. Used by consumer and streams groups. */
    case object Assigning extends GroupState {
      override def asJava: JGroupState = JGroupState.ASSIGNING
    }

    /** The members move to their new target assignment. Used by consumer and streams groups. */
    case object Reconciling extends GroupState {
      override def asJava: JGroupState = JGroupState.RECONCILING
    }

    /** The group is not ready yet. Used by streams groups only. */
    case object NotReady extends GroupState {
      override def asJava: JGroupState = JGroupState.NOT_READY
    }

    /** Converts a Java `GroupState`. */
    def fromJava(state: JGroupState): GroupState =
      state match {
        case JGroupState.UNKNOWN              => GroupState.Unknown
        case JGroupState.PREPARING_REBALANCE  => GroupState.PreparingRebalance
        case JGroupState.COMPLETING_REBALANCE => GroupState.CompletingRebalance
        case JGroupState.STABLE               => GroupState.Stable
        case JGroupState.DEAD                 => GroupState.Dead
        case JGroupState.EMPTY                => GroupState.Empty
        case JGroupState.ASSIGNING            => GroupState.Assigning
        case JGroupState.RECONCILING          => GroupState.Reconciling
        case JGroupState.NOT_READY            => GroupState.NotReady
      }
  }

  /** The type of a group, which is also the rebalance protocol it uses. */
  sealed trait GroupType {

    /** Converts to the Java `GroupType`. */
    def asJava: JGroupType
  }

  /** The values of [[GroupType]], one per Java `GroupType`. */
  object GroupType {

    /** A type that this client cannot parse, for example one reported by a newer broker. */
    case object Unknown extends GroupType {
      override def asJava = JGroupType.UNKNOWN
    }

    /** A consumer group that uses the consumer rebalance protocol (`group.protocol=consumer`). */
    case object Consumer extends GroupType {
      override def asJava = JGroupType.CONSUMER
    }

    /** A group that uses the classic rebalance protocol, for example a consumer with `group.protocol=classic`. */
    case object Classic extends GroupType {
      override def asJava = JGroupType.CLASSIC
    }

    /** A share group. */
    case object Share extends GroupType {
      override def asJava = JGroupType.SHARE
    }

    /** A Kafka Streams group that uses the streams rebalance protocol. */
    case object Streams extends GroupType {
      override def asJava = JGroupType.STREAMS
    }

    /** Converts a Java `GroupType`. */
    def fromJava(groupType: JGroupType): GroupType =
      groupType match {
        case JGroupType.UNKNOWN  => GroupType.Unknown
        case JGroupType.CONSUMER => GroupType.Consumer
        case JGroupType.CLASSIC  => GroupType.Classic
        case JGroupType.SHARE    => GroupType.Share
        case JGroupType.STREAMS  => GroupType.Streams
      }
  }

  /**
   * A member of a consumer group, as part of a [[ConsumerGroupDescription]].
   *
   * @param consumerId
   *   the member id that the group coordinator gave the member
   * @param groupInstanceId
   *   the `group.instance.id` of a static member; `None` for a dynamic member
   * @param clientId
   *   the `client.id` of the member
   * @param host
   *   the host the member runs on
   * @param assignment
   *   the partitions assigned to the member
   */
  final case class MemberDescription(
    consumerId: String,
    groupInstanceId: Option[String],
    clientId: String,
    host: String,
    assignment: Set[TopicPartition]
  )

  /** Conversions for [[MemberDescription]]. */
  object MemberDescription {

    /** Converts a Java `MemberDescription`. */
    def fromJava(desc: JMemberDescription): MemberDescription =
      MemberDescription(
        consumerId = desc.consumerId,
        groupInstanceId = desc.groupInstanceId.toScala,
        clientId = desc.clientId(),
        host = desc.host(),
        assignment = desc.assignment.topicPartitions().asScala.map(TopicPartition.fromJava).toSet
      )
  }

  /**
   * A consumer group, as returned by `describeConsumerGroups`.
   *
   * @param isSimpleConsumerGroup
   *   `true` when the group has no protocol type, as for a group that `alterConsumerGroupOffsets` created
   * @param members
   *   the current members; empty for a group in state `GroupState.Empty`
   * @param partitionAssignor
   *   the name of the partition assignor; empty when the group has none
   * @param coordinator
   *   the broker that coordinates the group
   * @param authorizedOperations
   *   the operations this client may perform on the group; empty unless the request set `includeAuthorizedOperations`
   */
  final case class ConsumerGroupDescription(
    groupId: String,
    isSimpleConsumerGroup: Boolean,
    members: List[MemberDescription],
    partitionAssignor: String,
    state: GroupState,
    coordinator: Option[Node],
    authorizedOperations: Set[AclOperation]
  )

  /** Conversions for [[ConsumerGroupDescription]]. */
  object ConsumerGroupDescription {

    /** Converts a Java `ConsumerGroupDescription`. */
    def fromJava(description: JConsumerGroupDescription): ConsumerGroupDescription =
      ConsumerGroupDescription(
        groupId = description.groupId,
        isSimpleConsumerGroup = description.isSimpleConsumerGroup,
        members = description.members.asScala.map(MemberDescription.fromJava).toList,
        partitionAssignor = description.partitionAssignor,
        state = GroupState.fromJava(description.groupState),
        coordinator = Node.fromJava(description.coordinator()),
        authorizedOperations = Option(description.authorizedOperations())
          .fold(Set.empty[AclOperation])(_.asScala.map(AclOperation.apply).toSet)
      )
  }

  /**
   * Options for `createPartitions`.
   *
   * @param validateOnly
   *   when `true`, the broker only validates the request and nothing changes
   * @param retryOnQuotaViolation
   *   when `true`, the Java client retries the request when the broker rejects it for violating a quota
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class CreatePartitionsOptions(
    validateOnly: Boolean = false,
    retryOnQuotaViolation: Boolean = true,
    timeout: Option[Duration]
  ) {

    /** Converts to the Java `CreatePartitionsOptions`. */
    def asJava: JCreatePartitionsOptions = {
      val opts = new JCreatePartitionsOptions()
        .validateOnly(validateOnly)
        .retryOnQuotaViolation(retryOnQuotaViolation)

      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `createTopics`.
   *
   * @param validateOnly
   *   when `true`, the broker only validates the request and creates nothing
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class CreateTopicsOptions(validateOnly: Boolean, timeout: Option[Duration]) {

    /** Converts to the Java `CreateTopicsOptions`. */
    def asJava: JCreateTopicsOptions = {
      val opts = new JCreateTopicsOptions().validateOnly(validateOnly)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `deleteConsumerGroups`.
   *
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DeleteConsumerGroupOptions(timeout: Option[Duration]) {

    /** Converts to the Java `DeleteConsumerGroupsOptions`. */
    def asJava: JDeleteConsumerGroupsOptions = {
      val opts = new JDeleteConsumerGroupsOptions()
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `deleteTopics`.
   *
   * @param retryOnQuotaViolation
   *   when `true`, the Java client retries the request when the broker rejects it for violating a quota
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DeleteTopicsOptions(retryOnQuotaViolation: Boolean = true, timeout: Option[Duration]) {

    /** Converts to the Java `DeleteTopicsOptions`. */
    def asJava: JDeleteTopicsOptions = {
      val opts = new JDeleteTopicsOptions().retryOnQuotaViolation(retryOnQuotaViolation)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `listTopics`.
   *
   * @param listInternal
   *   when `true`, internal topics such as `__consumer_offsets` are listed too
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class ListTopicsOptions(listInternal: Boolean = false, timeout: Option[Duration]) {

    /** Converts to the Java `ListTopicsOptions`. */
    def asJava: JListTopicsOptions = {
      val opts = new JListTopicsOptions().listInternal(listInternal)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeTopics`.
   *
   * @param includeAuthorizedOperations
   *   when `true`, `TopicDescription.authorizedOperations` holds the operations this client may perform on the topic
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DescribeTopicsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {

    /** Converts to the Java `DescribeTopicsOptions`. */
    def asJava: JDescribeTopicsOptions = {
      val opts = new JDescribeTopicsOptions().includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeConfigs` and `describeConfigsAsync`.
   *
   * @param includeSynonyms
   *   when `true`, each `ConfigEntry` also lists its synonyms: the values of the same setting from other sources
   * @param includeDocumentation
   *   when `true`, each `ConfigEntry` also carries its documentation
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DescribeConfigsOptions(
    includeSynonyms: Boolean = false,
    includeDocumentation: Boolean = false,
    timeout: Option[Duration]
  ) {

    /** Converts to the Java `DescribeConfigsOptions`. */
    def asJava: JDescribeConfigsOptions = {
      val opts = new JDescribeConfigsOptions()
        .includeSynonyms(includeSynonyms)
        .includeDocumentation(includeDocumentation)

      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeClusterNodes`, `describeClusterController`, `describeClusterId` and
   * `describeClusterAuthorizedOperations`.
   *
   * @param includeAuthorizedOperations
   *   when `true`, `describeClusterAuthorizedOperations` returns the operations this client may perform on the cluster;
   *   otherwise it returns an empty set
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DescribeClusterOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {

    /** Converts to the Java `DescribeClusterOptions`. */
    lazy val asJava: JDescribeClusterOptions = {
      val opts = new JDescribeClusterOptions().includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeConsumerGroups`.
   *
   * @param includeAuthorizedOperations
   *   when `true`, `ConsumerGroupDescription.authorizedOperations` holds the operations this client may perform on the
   *   group
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DescribeConsumerGroupsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {

    /** Converts to the Java `DescribeConsumerGroupsOptions`. */
    lazy val asJava: JDescribeConsumerGroupsOptions = {
      val jOpts = new JDescribeConsumerGroupsOptions()
        .includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `incrementalAlterConfigs` and `incrementalAlterConfigsAsync`.
   *
   * @param validateOnly
   *   when `true`, the broker only validates the request, nothing changes, and the operation does not wait for the
   *   change to show
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class AlterConfigsOptions(validateOnly: Boolean = false, timeout: Option[Duration] = None) {

    /** Converts to the Java `AlterConfigsOptions`. */
    lazy val asJava: JAlterConfigsOptions = {
      val jOpts = new JAlterConfigsOptions().validateOnly(validateOnly)
      timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * One change to one configuration entry, for `incrementalAlterConfigs`.
   *
   * @param configEntry
   *   the name of the entry and, for `Set`, `Append` and `Subtract`, the value
   * @param opType
   *   what to do with the entry
   */
  final case class AlterConfigOp(configEntry: ConfigEntry, opType: AlterConfigOpType) {

    /** Converts to the Java `AlterConfigOp`. */
    lazy val asJava: JAlterConfigOp = new JAlterConfigOp(configEntry, opType.asJava)
  }

  /** What an [[AlterConfigOp]] does with its configuration entry. */
  sealed trait AlterConfigOpType {

    /** Converts to the Java `AlterConfigOp.OpType`. */
    def asJava: JAlterConfigOp.OpType
  }

  /** The values of [[AlterConfigOpType]]. */
  object AlterConfigOpType {

    /** Sets the entry to the given value. */
    case object Set extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.SET
    }

    /** Removes the value set for the entry, so that its default applies again. */
    case object Delete extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.DELETE
    }

    /** Adds the given value to a list-valued entry, for example `compact` to `cleanup.policy`. */
    case object Append extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.APPEND
    }

    /** Removes the given value from a list-valued entry. */
    case object Subtract extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.SUBTRACT
    }

    /** The old, misspelled name of `Subtract`: the same object. */
    @deprecated("Use AlterConfigOpType.Subtract", since = "3.6.0")
    val Substract: AlterConfigOpType.Subtract.type = AlterConfigOpType.Subtract
  }

  /**
   * The name of a Java admin client metric.
   *
   * @param group
   *   the group the metric belongs to
   * @param tags
   *   the tags that tell metrics with the same name and group apart
   */
  final case class MetricName(name: String, group: String, description: String, tags: Map[String, String])

  /** Conversions for [[MetricName]]. */
  object MetricName {

    /** Converts a Java `MetricName`. */
    def fromJava(jmn: JMetricName): MetricName =
      MetricName(
        name = jmn.name(),
        group = jmn.group(),
        description = jmn.description(),
        tags = jmn.tags().asScala.toMap
      )
  }

  /**
   * A Java admin client metric and its value.
   *
   * @param metricValue
   *   the value when the metric was read, as returned by the Java `Metric.metricValue`
   */
  final case class Metric(name: MetricName, metricValue: AnyRef)

  /** Conversions for [[Metric]]. */
  object Metric {

    /** Converts a Java `Metric`, reading its current value. */
    def fromJava(jm: JMetric): Metric =
      Metric(name = MetricName.fromJava(jmn = jm.metricName()), metricValue = jm.metricValue())
  }

  /**
   * A topic to create with `createTopic` or `createTopics`.
   *
   * @param replicationFactor
   *   the number of replicas of each partition
   * @param configs
   *   topic configuration entries, for example `"cleanup.policy" -> "compact"`; empty uses the broker defaults
   */
  final case class NewTopic(
    name: String,
    numPartitions: Int,
    replicationFactor: Short,
    configs: Map[String, String] = Map.empty
  ) {

    /** Converts to the Java `NewTopic`. */
    def asJava: JNewTopic = {
      val jn = new JNewTopic(name, numPartitions, replicationFactor)

      if (configs.nonEmpty) {
        val _ = jn.configs(configs.asJava)
      }

      jn
    }
  }

  /**
   * The new partition count of a topic, for `createPartitions`.
   *
   * @param totalCount
   *   the number of partitions after the change, not the number of partitions to add
   * @param newAssignments
   *   for each new partition, the ids of the brokers that hold its replicas, the preferred leader first. Empty lets the
   *   broker choose.
   */
  final case class NewPartitions(
    totalCount: Int,
    newAssignments: List[List[Int]] = List.empty
  ) {

    /** Converts to the Java `NewPartitions`. */
    def asJava: JNewPartitions =
      if (newAssignments.nonEmpty)
        JNewPartitions.increaseTo(totalCount, newAssignments.map(_.map(Int.box).asJava).asJava)
      else JNewPartitions.increaseTo(totalCount)
  }

  /**
   * A Kafka broker. The constructor does not check its arguments; `Node.fromJava` gives the values described below.
   *
   * @param id
   *   >= 0 for a real broker; `Node.fromJava` returns `None` for a negative id
   * @param host
   *   can't be empty string if present: `Node.fromJava` turns an empty host into `None`
   * @param port
   *   can't be negative if present: `Node.fromJava` turns a negative port into `None`
   * @param rack
   *   the rack of the broker, when it has one
   */
  final case class Node(id: Int, host: Option[String], port: Option[Int], rack: Option[String] = None) {

    /** Converts to the Java `Node`; a `None` host becomes the empty string and a `None` port becomes -1. */
    lazy val asJava: JNode = new JNode(id, host.getOrElse(""), port.getOrElse(-1), rack.orNull)
  }

  /** Conversions for [[Node]]. */
  object Node {

    /**
     * Converts a Java `Node`. Returns `None` for `null` and for a node with a negative id, such as `Node.noNode()`; an
     * empty host and a negative port become `None`.
     */
    def fromJava(jNode: JNode): Option[Node] =
      Option(jNode)
        .filter(_.id() >= 0)
        .map { jNode =>
          Node(
            id = jNode.id(),
            host = Option(jNode.host()).filterNot(_.isEmpty),
            port = Option(jNode.port()).filter(_ >= 0),
            rack = Option(jNode.rack())
          )
        }
  }

  /**
   * A topic, as returned by `describeTopics`.
   *
   * @param internal
   *   `true` for an internal topic such as `__consumer_offsets`
   * @param partitions
   *   the partitions of the topic
   * @param authorizedOperations
   *   the operations this client may perform on the topic; `None` unless the request set `includeAuthorizedOperations`
   */
  final case class TopicDescription(
    name: String,
    internal: Boolean,
    partitions: List[TopicPartitionInfo],
    authorizedOperations: Option[Set[AclOperation]]
  )

  /** Conversions for [[TopicDescription]]. */
  object TopicDescription {

    /**
     * Converts a Java `TopicDescription`. Fails with a `java.lang.RuntimeException` when a replica or an in-sync
     * replica is a node with a negative id.
     */
    def fromJava(jt: JTopicDescription): Try[TopicDescription] = {
      val authorizedOperations = Option(jt.authorizedOperations).map(_.asScala.toSet).map(_.map(AclOperation.apply))

      jt.partitions.asScala.toList.forEach(TopicPartitionInfo.fromJava).map { partitions =>
        TopicDescription(
          name = jt.name,
          internal = jt.isInternal,
          partitions = partitions,
          authorizedOperations = authorizedOperations
        )
      }
    }
  }

  /**
   * A partition of a topic, as part of a [[TopicDescription]].
   *
   * @param leader
   *   the broker that leads the partition; `None` when it has no leader
   * @param replicas
   *   the brokers that hold a replica of the partition
   * @param isr
   *   the in-sync replicas
   */
  final case class TopicPartitionInfo(partition: Int, leader: Option[Node], replicas: List[Node], isr: List[Node]) {

    /** Converts to the Java `TopicPartitionInfo`; a `None` leader becomes `Node.noNode()`. */
    lazy val asJava: JTopicPartitionInfo =
      new JTopicPartitionInfo(
        partition,
        leader.map(_.asJava).getOrElse(JNode.noNode()),
        replicas.map(_.asJava).asJava,
        isr.map(_.asJava).asJava
      )
  }

  /** Conversions for [[TopicPartitionInfo]]. */
  object TopicPartitionInfo {

    /**
     * Converts a Java `TopicPartitionInfo`. Fails with a `java.lang.RuntimeException` when a replica or an in-sync
     * replica is a node with a negative id.
     */
    def fromJava(jtpi: JTopicPartitionInfo): Try[TopicPartitionInfo] = {
      val replicas: Try[List[Node]] =
        jtpi
          .replicas()
          .asScala
          .toList
          .forEach { jNode =>
            Node.fromJava(jNode) match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode node not expected among topic replicas"))
            }
          }

      val inSyncReplicas: Try[List[Node]] =
        jtpi
          .isr()
          .asScala
          .toList
          .forEach { jNode =>
            Node.fromJava(jNode) match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode node not expected among topic in sync replicas"))
            }
          }

      for {
        replicas       <- replicas
        inSyncReplicas <- inSyncReplicas
      } yield TopicPartitionInfo(
        partition = jtpi.partition(),
        leader = Node.fromJava(jtpi.leader()),
        replicas = replicas,
        isr = inSyncReplicas
      )
    }
  }

  /**
   * A topic, as listed by `listTopics`.
   *
   * @param isInternal
   *   `true` for an internal topic such as `__consumer_offsets`
   */
  final case class TopicListing(name: String, topicId: Uuid, isInternal: Boolean) {

    /** Converts to the Java `TopicListing`. */
    def asJava: JTopicListing = new JTopicListing(name, topicId, isInternal)
  }

  /** Conversions for [[TopicListing]]. */
  object TopicListing {

    /** Converts a Java `TopicListing`. */
    def fromJava(jtl: JTopicListing): TopicListing = TopicListing(jtl.name(), jtl.topicId(), jtl.isInternal)
  }

  /**
   * A partition of a topic, as used by the admin client. It is not the Java `org.apache.kafka.common.TopicPartition`.
   *
   * @param name
   *   the topic name
   * @param partition
   *   the partition number, from 0
   */
  final case class TopicPartition(
    name: String,
    partition: Int
  ) {

    /** Converts to the Java `TopicPartition`. */
    def asJava: JTopicPartition = new JTopicPartition(name, partition)
  }

  /** Conversions for [[TopicPartition]]. */
  object TopicPartition {

    /** Converts a Java `TopicPartition`. */
    def fromJava(tp: JTopicPartition): TopicPartition =
      new TopicPartition(name = tp.topic(), partition = tp.partition())
  }

  /** Which offset `listOffsets` looks up in a partition. */
  sealed abstract class OffsetSpec {

    /** Converts to the Java `OffsetSpec`. */
    def asJava: JOffsetSpec
  }

  /** The values of [[OffsetSpec]]. */
  object OffsetSpec {

    /** The earliest offset still in the log. */
    case object EarliestSpec extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.earliest()
    }

    /** The offset that the next record written to the partition will get. */
    case object LatestSpec extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.latest()
    }

    /**
     * The earliest offset whose record timestamp is at or after `timestamp`, in epoch milliseconds. When no record is
     * that recent, the result has offset -1.
     */
    final case class TimestampSpec(timestamp: Long) extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.forTimestamp(timestamp)
    }
  }

  /** Which records `listOffsets` takes into account, set in [[ListOffsetsOptions]]. */
  sealed abstract class IsolationLevel {

    /** Converts to the Java `IsolationLevel`. */
    def asJava: JIsolationLevel
  }

  /** The values of [[IsolationLevel]]. */
  object IsolationLevel {

    /** Counts all records, including those of open transactions. */
    case object ReadUncommitted extends IsolationLevel {
      override def asJava: JIsolationLevel = JIsolationLevel.READ_UNCOMMITTED
    }

    /** Leaves out records of open transactions: `LatestSpec` gives the last stable offset. */
    case object ReadCommitted extends IsolationLevel {
      override def asJava: JIsolationLevel = JIsolationLevel.READ_COMMITTED
    }
  }

  /**
   * Options for `deleteRecords`.
   *
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class DeleteRecordsOptions(timeout: Option[Duration]) {

    /** Converts to the Java `DeleteRecordsOptions`. */
    def asJava: JDeleteRecordsOptions = {
      val deleteRecordsOpt = new JDeleteRecordsOptions()
      timeout.fold(deleteRecordsOpt)(timeout => deleteRecordsOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `listOffsets` and `listOffsetsAsync`.
   *
   * @param isolationLevel
   *   which records to take into account; `IsolationLevel.ReadUncommitted` by default
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class ListOffsetsOptions(
    isolationLevel: IsolationLevel = IsolationLevel.ReadUncommitted,
    timeout: Option[Duration]
  ) {

    /** Converts to the Java `ListOffsetsOptions`. */
    def asJava: JListOffsetsOptions = {
      val offsetOpt = new JListOffsetsOptions(isolationLevel.asJava)
      timeout.fold(offsetOpt)(timeout => offsetOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * The offset that `listOffsets` found for a partition.
   *
   * @param offset
   *   the offset; -1 when a `TimestampSpec` found no record that recent
   * @param timestamp
   *   the timestamp of the record at `offset`, or -1 when there is none, as for `EarliestSpec` and `LatestSpec`
   * @param leaderEpoch
   *   the leader epoch of the record at `offset`, when known
   */
  final case class ListOffsetsResultInfo(
    offset: Long,
    timestamp: Long,
    leaderEpoch: Option[Int]
  )

  /** Conversions for [[ListOffsetsResultInfo]]. */
  object ListOffsetsResultInfo {

    /** Converts a Java `ListOffsetsResult.ListOffsetsResultInfo`. */
    def fromJava(lo: JListOffsetsResultInfo): ListOffsetsResultInfo =
      ListOffsetsResultInfo(lo.offset(), lo.timestamp(), lo.leaderEpoch().toScala.map(_.toInt))
  }

  /**
   * Options for `listConsumerGroupOffsets`.
   *
   * @param requireStable
   *   when `true`, only stable offsets are returned: offsets that a still-open transaction commits are not returned
   *   until that transaction completes
   */
  final case class ListConsumerGroupOffsetsOptions(requireStable: Boolean) {

    /** Converts to the Java `ListConsumerGroupOffsetsOptions`. */
    def asJava: JListConsumerGroupOffsetsOptions = {
      val opts = new JListConsumerGroupOffsetsOptions()
      opts.requireStable(requireStable)
    }
  }

  /** Constructors for [[ListConsumerGroupOffsetsOptions]]. */
  object ListConsumerGroupOffsetsOptions {

    /** Creates the options with the given `requireStable` flag. */
    def apply(requireStable: Boolean): ListConsumerGroupOffsetsOptions =
      new ListConsumerGroupOffsetsOptions(requireStable)
  }

  /**
   * The partitions to look up for one group in `listConsumerGroupOffsets`. With an empty `partitions`, no partition is
   * looked up, and the result for the group is an empty map.
   */
  final case class ListConsumerGroupOffsetsSpec(partitions: Chunk[TopicPartition]) {

    /** Converts to the Java `ListConsumerGroupOffsetsSpec`. */
    def asJava: JListConsumerGroupOffsetsSpec = {
      val opts = new JListConsumerGroupOffsetsSpec
      opts.topicPartitions(partitions.map(_.asJava).asJava)
      opts
    }
  }

  /**
   * A committed offset of a consumer group.
   *
   * @param offset
   *   the offset of the next record the group will read
   * @param leaderEpoch
   *   the leader epoch of the last record the group read, when known
   * @param metadata
   *   a string stored with the commit. Offsets read from Kafka always have `Some`: a commit without metadata gives
   *   `Some("")`.
   */
  final case class OffsetAndMetadata(
    offset: Long,
    leaderEpoch: Option[Int] = None,
    metadata: Option[String] = None
  ) {

    /** Converts to the Java `OffsetAndMetadata`; `None` metadata becomes the empty string. */
    def asJava: JOffsetAndMetadata = new JOffsetAndMetadata(offset, leaderEpoch.map(Int.box).toJava, metadata.orNull)
  }

  /** Conversions for [[OffsetAndMetadata]]. */
  object OffsetAndMetadata {

    /**
     * Converts a Java `OffsetAndMetadata`. The result's `metadata` is always `Some`, and is `Some("")` when no metadata
     * was stored, so `OffsetAndMetadata.fromJava(OffsetAndMetadata(42L).asJava)` is
     * `OffsetAndMetadata(42, None, Some(""))`.
     */
    def fromJava(om: JOffsetAndMetadata): OffsetAndMetadata =
      OffsetAndMetadata(
        offset = om.offset(),
        leaderEpoch = om.leaderEpoch().toScala.map(_.toInt),
        metadata = Some(om.metadata())
      )
  }

  /**
   * Options for `alterConsumerGroupOffsets`.
   *
   * @param timeout
   *   the request timeout; `None` uses the client's `default.api.timeout.ms`. It is passed to Kafka as an `Int` of
   *   milliseconds, so a timeout longer than `Int.MaxValue` milliseconds (about 24.8 days) overflows: with 30 days, the
   *   request fails at once with an `org.apache.kafka.common.errors.TimeoutException`.
   */
  final case class AlterConsumerGroupOffsetsOptions(timeout: Option[Duration]) {

    /** Converts to the Java `AlterConsumerGroupOffsetsOptions`. */
    def asJava: JAlterConsumerGroupOffsetsOptions = {
      val options = new JAlterConsumerGroupOffsetsOptions()
      timeout.fold(options)(timeout => options.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `listConsumerGroups`.
   *
   * @param states
   *   only the groups in one of these states are listed; an empty set lists groups in every state
   */
  final case class ListConsumerGroupsOptions(states: Set[GroupState]) {

    /**
     * Converts to a Java `ListGroupsOptions` that selects consumer groups (types `Classic` and `Consumer`, protocol
     * type `consumer` or empty) in `states`.
     */
    def asJListGroupsOptions: JListGroupsOptions =
      JListGroupsOptions.forConsumerGroups().inGroupStates(states.map(_.asJava).asJava)
  }

  /**
   * A consumer group, as listed by `listConsumerGroups`.
   *
   * @param isSimple
   *   `true` when the group has no protocol type, as for a group that `alterConsumerGroupOffsets` created
   * @param state
   *   the state of the group; `None` when the broker does not report it
   */
  final case class ConsumerGroupListing(groupId: String, isSimple: Boolean, state: Option[GroupState])

  /** Conversions for [[ConsumerGroupListing]]. */
  object ConsumerGroupListing {

    /** Converts a Java `GroupListing`. */
    def fromJava(cg: JGroupListing): ConsumerGroupListing =
      ConsumerGroupListing(
        groupId = cg.groupId(),
        isSimple = cg.isSimpleConsumerGroup,
        state = cg.groupState().toScala.map(GroupState.fromJava)
      )
  }

  /**
   * A group, as listed by `listGroups`.
   *
   * @param groupType
   *   the type of the group; `None` when the broker does not report it
   * @param protocol
   *   the protocol type of the group, for example `consumer`; empty for a group without one
   * @param groupState
   *   the state of the group; `None` when the broker does not report it
   */
  final case class GroupListing(
    groupId: String,
    groupType: Option[GroupType],
    protocol: String,
    groupState: Option[GroupState]
  ) {

    /** Converts to the Java `GroupListing`. */
    def asJava: JGroupListing =
      new JGroupListing(
        groupId,
        groupType.map(_.asJava).toJava,
        protocol,
        groupState.map(_.asJava).toJava
      )
  }

  /** Conversions for [[GroupListing]]. */
  object GroupListing {

    /** Converts a Java `GroupListing`. */
    def fromJava(jgl: JGroupListing): GroupListing =
      GroupListing(
        jgl.groupId(),
        jgl.`type`().toScala.map(GroupType.fromJava),
        jgl.protocol(),
        jgl.groupState().toScala.map(GroupState.fromJava)
      )
  }

  /**
   * Options for `listGroups`. Each filter applies only when its set is not empty.
   *
   * @param groupStates
   *   only the groups in one of these states are listed
   * @param groupTypes
   *   only the groups of one of these types are listed
   * @param protocolTypes
   *   only the groups with one of these protocol types (for example `consumer`) are listed
   */
  final case class ListGroupsOptions(
    groupStates: Set[GroupState] = Set.empty,
    groupTypes: Set[GroupType] = Set.empty,
    protocolTypes: Set[String] = Set.empty
  ) {

    /** Converts to the Java `ListGroupsOptions`. */
    def asJava: JListGroupsOptions =
      new JListGroupsOptions()
        .inGroupStates(groupStates.map(_.asJava).asJava)
        .withTypes(groupTypes.map(_.asJava).asJava)
        .withProtocolTypes(protocolTypes.asJava)
  }

  /** Conversions for [[ListGroupsOptions]]. */
  object ListGroupsOptions {

    /** Converts a Java `ListGroupsOptions`. */
    def fromJava(lgo: JListGroupsOptions): ListGroupsOptions =
      ListGroupsOptions(
        lgo.groupStates().asScala.map(GroupState.fromJava).toSet,
        lgo.types().asScala.map(GroupType.fromJava).toSet,
        lgo.protocolTypes().asScala.toSet
      )
  }

  /**
   * The configuration of one resource, as returned by `describeConfigs`.
   *
   * @param entries
   *   the Java `ConfigEntry` of each setting, keyed by setting name
   */
  final case class KafkaConfig(entries: Map[String, ConfigEntry]) {

    /** Converts to the Java `Config`. */
    def asJava: JConfig = new JConfig(entries.values.asJavaCollection)
  }

  /** Conversions for [[KafkaConfig]]. */
  object KafkaConfig {

    /** Converts a Java `Config`. */
    def fromJava(jConfig: JConfig): KafkaConfig =
      KafkaConfig(entries = jConfig.entries().asScala.map(e => e.name() -> e).toMap)
  }

  /**
   * A log directory of a broker, as returned by `describeLogDirs`.
   *
   * @param error
   *   the error of the directory, for example a `org.apache.kafka.common.errors.KafkaStorageException` when it is
   *   offline; `null` when there is none
   * @param replicaInfos
   *   the replicas stored in the directory, keyed by partition
   */
  final case class LogDirDescription(error: ApiException, replicaInfos: Map[TopicPartition, ReplicaInfo])

  /** Conversions for [[LogDirDescription]]. */
  object LogDirDescription {

    /** Converts a Java `LogDirDescription`. */
    def fromJava(ld: JLogDirDescription): LogDirDescription =
      LogDirDescription(
        error = ld.error(),
        replicaInfos = ld.replicaInfos().asScala.bimap(TopicPartition.fromJava, ReplicaInfo.fromJava).toMap
      )
  }

  /**
   * A replica in a log directory, as part of a [[LogDirDescription]].
   *
   * @param size
   *   the size of the replica's log segments in bytes, not counting data in remote storage
   * @param offsetLag
   *   how far the replica's log end offset is behind the partition's high watermark (or, for a future replica, behind
   *   the current replica)
   * @param isFuture
   *   `true` for a replica that is being moved to this directory and has not yet replaced the current one
   */
  final case class ReplicaInfo(size: Long, offsetLag: Long, isFuture: Boolean)

  /** Conversions for [[ReplicaInfo]]. */
  object ReplicaInfo {

    /** Converts a Java `ReplicaInfo`. */
    def fromJava(ri: JReplicaInfo): ReplicaInfo =
      ReplicaInfo(size = ri.size(), offsetLag = ri.offsetLag(), isFuture = ri.isFuture)
  }

  /**
   * Make a Kafka [[AdminClient]].
   *
   * It first runs zio-kafka's SSL endpoint check (see `zio.kafka.utils.SslHelper`): unless the `security.protocol`
   * property contains `SSL`, it connects to the bootstrap servers, and fails with an
   * `org.apache.kafka.common.KafkaException` ("Failed to connect to Kafka") when none of them can be reached. Then it
   * creates the Java admin client, and closes it, waiting at most `settings.closeTimeout`, when the scope ends.
   *
   * @param settings
   *   the admin client settings
   */
  def make(settings: AdminClientSettings): ZIO[Scope, Throwable, AdminClient] =
    fromScopedJavaClient(javaClientFromSettings(settings))

  /**
   * Make a Kafka [[AdminClient]] by wrapping a provided Java admin client. It does not run the SSL endpoint check of
   * `make`, and it does not close the Java client: close it yourself.
   *
   * @param javaClient
   *   the java admin client to wrap
   */
  def fromJavaClient(javaClient: JAdmin): URIO[Any, AdminClient] =
    ZIO.succeed(new LiveAdminClient(javaClient))

  /**
   * Make a Kafka [[AdminClient]] by wrapping the Java admin client that `scopedJavaClient` creates. It does not run the
   * SSL endpoint check of `make`, and it does not close the Java client itself: `scopedJavaClient` must close it when
   * the scope ends, for example by creating it with `ZIO.acquireRelease`.
   */
  def fromScopedJavaClient[R, E](scopedJavaClient: ZIO[R & Scope, E, JAdmin]): ZIO[R & Scope, E, AdminClient] =
    for {
      javaClient  <- scopedJavaClient
      adminClient <- fromJavaClient(javaClient)
    } yield adminClient

  /**
   * Creates a Java admin client from `settings`, after the same SSL endpoint check as `make`, and closes it, waiting at
   * most `settings.closeTimeout`, when the scope ends. Use it to call a Java admin operation that this client does not
   * wrap; see `fromKafkaFuture`. An error while closing becomes a defect.
   */
  def javaClientFromSettings(settings: AdminClientSettings): ZIO[Scope, Throwable, JAdmin] =
    ZIO.acquireRelease {
      val endpointCheck = SslHelper
        .validateEndpoint(settings.driverSettings)

      endpointCheck *> ZIO.attempt(JAdmin.create(settings.driverSettings.asJava))
    }(client => ZIO.attempt(client.close(settings.closeTimeout)).orDie)

  /** Adds `bimap` to an immutable `Map`; used by the conversions. */
  implicit final class MapOps[K1, V1](private val v: Map[K1, V1]) extends AnyVal {

    /** Maps the keys with `fk` and the values with `fv`. */
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): Map[K2, V2] = v.map { case (k, v) => fk(k) -> fv(v) }
  }

  /** Adds `bimap` to a mutable `Map`; used by the conversions. */
  implicit final class MutableMapOps[K1, V1](private val v: mutable.Map[K1, V1]) extends AnyVal {

    /** Maps the keys with `fk` and the values with `fv`, into a new mutable map. */
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): mutable.Map[K2, V2] = v.map { case (k, v) => fk(k) -> fv(v) }
  }

  /** Adds `toScala` to a Java `Optional`. */
  implicit final class OptionalOps[T](private val v: Optional[T]) extends AnyVal {

    /** Converts to a Scala `Option`. */
    def toScala: Option[T] = if (v.isPresent) Some(v.get()) else None
  }

  /** Adds `toJava` to a Scala `Option`. */
  implicit final class OptionOps[T](private val v: Option[T]) extends AnyVal {

    /** Converts to a Java `Optional`. Throws a `java.lang.NullPointerException` for `Some(null)`. */
    def toJava: Optional[T] = v.fold(Optional.empty[T])(Optional.of)
  }

  /** Adds `forEach` to a `List`. */
  implicit final class ListOps[A](private val list: List[A]) extends AnyVal {

    /**
     * Applies `f` to each element in order, and returns the results, or the first `Failure`; elements after it are not
     * visited.
     */
    def forEach[B](f: A => Try[B]): Try[List[B]] = {
      @tailrec
      def loop(acc: ListBuffer[B], rest: List[A]): Try[List[B]] =
        rest match {
          case Nil => Success(acc.toList)
          case h :: t =>
            f(h) match {
              case Success(b)        => loop(acc += b, t)
              case fail @ Failure(_) => fail.asInstanceOf[Try[List[B]]]
            }
        }

      loop(ListBuffer.empty, list)
    }
  }
}
