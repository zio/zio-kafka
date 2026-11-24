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
 * Kafka Admin client, can be used to create, list, and delete topics, get consumer groups, etc.
 *
 * This admin client is a wrapper around the java Admin client. See the API of the
 * [[org.apache.kafka.clients.admin.Admin java admin client]] for a full description of the provided operations.
 *
 * Typically constructed as:
 * {{{
 * import zio._
 * import zio.kafka.admin.AdminClient.NewTopic
 *
 * val topicNames: ZIO[Any, Throwable, Set[String]] =
 *   ZIO.scoped {
 *     for {
 *       admin  <- AdminClient.make(AdminClientSettings(List("localhost:9092")))
 *       _      <- admin.createTopic(NewTopic("orders", numPartitions = 6, replicationFactor = 3))
 *       topics <- admin.listTopics()
 *     } yield topics.keySet
 *   }
 * }}}
 *
 * Unlike the java Admin client, this client waits until a write operation (e.g. creating or deleting a topic)
 * completes. For most write operations this is done by polling the broker until we see the effect of the write
 * operation. If after 5s the effect is still not seen, we assume the write operation succeeded but was undone by
 * another write operation. For some operations where polling is difficult, we wait for `visibilitySleep` (1 second by
 * default), assuming the operation completes within that time.
 *
 * @see
 *   [[https://zio.dev/zio-kafka/admin-client/ Admin Client]]
 */
trait AdminClient {

  import AdminClient._

  /**
   * Create multiple topics.
   */
  def createTopics(
    newTopics: Iterable[NewTopic],
    options: Option[CreateTopicsOptions] = None
  ): Task[Unit]

  /**
   * Create a single topic.
   */
  def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): Task[Unit]

  /**
   * Delete consumer groups.
   */
  def deleteConsumerGroups(
    groupIds: Iterable[String],
    options: Option[DeleteConsumerGroupOptions] = None
  ): Task[Unit]

  /**
   * Delete multiple topics.
   */
  def deleteTopics(
    topics: Iterable[String],
    options: Option[DeleteTopicsOptions] = None
  ): Task[Unit]

  /**
   * Delete a single topic.
   */
  def deleteTopic(topic: String): Task[Unit]

  /**
   * Delete records.
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
   * List the topics in the cluster.
   */
  def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): Task[Map[String, TopicListing]]

  /**
   * Describe the specified topics.
   */
  def describeTopics(
    topicNames: Iterable[String],
    options: Option[DescribeTopicsOptions] = None
  ): Task[Map[String, TopicDescription]]

  /**
   * Get the configuration for the specified resources.
   */
  def describeConfigs(
    configResources: Iterable[ConfigResource],
    options: Option[DescribeConfigsOptions] = None
  ): Task[Map[ConfigResource, KafkaConfig]]

  /**
   * Get the configuration for the specified resources async.
   */
  def describeConfigsAsync(
    configResources: Iterable[ConfigResource],
    options: Option[DescribeConfigsOptions] = None
  ): Task[Map[ConfigResource, Task[KafkaConfig]]]

  /**
   * Get the cluster nodes.
   */
  def describeClusterNodes(options: Option[DescribeClusterOptions] = None): Task[List[Node]]

  /**
   * Get the cluster controller.
   */
  def describeClusterController(options: Option[DescribeClusterOptions] = None): Task[Option[Node]]

  /**
   * Get the cluster id.
   */
  def describeClusterId(options: Option[DescribeClusterOptions] = None): Task[String]

  /**
   * Get the cluster authorized operations.
   */
  def describeClusterAuthorizedOperations(
    options: Option[DescribeClusterOptions] = None
  ): Task[Set[AclOperation]]

  /**
   * Add new partitions to a topic.
   */
  def createPartitions(
    newPartitions: Map[String, NewPartitions],
    options: Option[CreatePartitionsOptions] = None
  ): Task[Unit]

  /**
   * List offset for the specified partitions.
   */
  def listOffsets(
    topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
    options: Option[ListOffsetsOptions] = None
  ): Task[Map[TopicPartition, ListOffsetsResultInfo]]

  /**
   * List offset for the specified partitions async: the outer task returns once the request is sent, the inner task of
   * a partition completes with its offset.
   */
  def listOffsetsAsync(
    topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
    options: Option[ListOffsetsOptions] = None
  ): Task[Map[TopicPartition, Task[ListOffsetsResultInfo]]]

  /**
   * List Consumer Group offsets for the specified partitions.
   */
  def listConsumerGroupOffsets(
    groupId: String,
    options: Option[ListConsumerGroupOffsetsOptions] = None
  ): Task[Map[TopicPartition, OffsetAndMetadata]]

  /**
   * Given a mapping of consumer group IDs and list of partitions, list the consumer group offsets available in the
   * cluster for the specified consumer groups and partitions.
   */
  def listConsumerGroupOffsets(
    groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]
  ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]

  /**
   * Given a mapping of consumer group IDs and list of partitions, list the consumer group offsets available in the
   * cluster for the specified consumer groups and partitions.
   */
  def listConsumerGroupOffsets(
    groupSpecs: Map[String, ListConsumerGroupOffsetsSpec],
    options: ListConsumerGroupOffsetsOptions
  ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]

  /**
   * Alter offsets for the specified partitions and consumer group.
   */
  def alterConsumerGroupOffsets(
    groupId: String,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    options: Option[AlterConsumerGroupOffsetsOptions] = None
  ): Task[Unit]

  /**
   * Retrieves metrics for the underlying AdminClient
   */
  def metrics: Task[Map[MetricName, Metric]]

  /**
   * List the consumer groups in the cluster.
   */
  def listConsumerGroups(options: Option[ListConsumerGroupsOptions] = None): Task[List[ConsumerGroupListing]]

  /**
   * List the groups in the cluster.
   */
  def listGroups(options: Option[ListGroupsOptions] = None): Task[List[GroupListing]]

  /**
   * Describe the specified consumer groups.
   */
  def describeConsumerGroups(groupIds: String*): Task[Map[String, ConsumerGroupDescription]]

  /**
   * Describe the specified consumer groups.
   */
  def describeConsumerGroups(
    groupIds: List[String],
    options: Option[DescribeConsumerGroupsOptions]
  ): Task[Map[String, ConsumerGroupDescription]]

  /**
   * Remove the specified members from a consumer group.
   *
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
   * Remove all members from a consumer group.
   *
   * @param visibilitySleep
   *   time to wait after getting a confirmation that the command was accepted by the kafka broker. This should be
   *   enough time for the operation to complete. Set to `Duration.Zero` if you don't care when the operation completes.
   */
  def removeAllMembersFromConsumerGroup(groupId: String, visibilitySleep: Duration = 1.second): Task[Unit]

  /**
   * Describe the log directories of the specified brokers
   */
  def describeLogDirs(
    brokersId: Iterable[Int]
  ): ZIO[Any, Throwable, Map[Int, Map[String, LogDirDescription]]]

  /**
   * Describe the log directories of the specified brokers async
   */
  def describeLogDirsAsync(
    brokersId: Iterable[Int]
  ): ZIO[Any, Throwable, Map[Int, Task[Map[String, LogDirDescription]]]]

  /**
   * Incrementally update the configuration for the specified resources. Only supported by brokers with version 2.3.0 or
   * higher.
   *
   * When `configs` contains an `AlterConfigOpType.Append` or an `AlterConfigOpType.Subtract`, `visibilitySleep` is
   * used. Otherwise, polling is used to wait until the operation completes. When `options` has `validateOnly` set, the
   * operation does not wait.
   *
   * @param visibilitySleep
   *   time to wait after getting a confirmation that the command was accepted by the kafka broker. This should be
   *   enough time for the operation to complete. Set to `Duration.Zero` if you don't care when the operation completes.
   */
  def incrementalAlterConfigs(
    configs: Map[ConfigResource, Iterable[AlterConfigOp]],
    options: AlterConfigsOptions,
    visibilitySleep: Duration = 1.second
  ): Task[Unit]

  /**
   * Incrementally update the configuration for the specified resources async. Only supported by brokers with version
   * 2.3.0 or higher.
   *
   * When `configs` contains an `AlterConfigOpType.Append` or an `AlterConfigOpType.Subtract`, `visibilitySleep` is
   * used. Otherwise, polling is used to wait until the operation completes. When `options` has `validateOnly` set, the
   * inner tasks do not wait. This method still returns immediately with the outer task, sleeping/polling happens in
   * the inner task.
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
   * Note: it may take some time for changes made by other clients, or by createAcls or deleteAcls when their wait ended
   * after 5 seconds, to be reflected in the output of describeAcls.
   */
  def describeAcls(filter: AclBindingFilter, options: Option[DescribeAclOptions] = None): Task[Set[AclBinding]]

  /**
   * Creates access control lists (ACLs) which are bound to specific resources.
   */
  def createAcls(acls: Set[AclBinding], options: Option[CreateAclOptions] = None): Task[Unit]

  /**
   * Creates access control lists (ACLs) which are bound to specific resources async.
   */
  def createAclsAsync(
    acls: Set[AclBinding],
    options: Option[CreateAclOptions] = None
  ): Task[Map[AclBinding, Task[Unit]]]

  /**
   * Deletes access control lists (ACLs) according to the supplied filters.
   */
  def deleteAcls(filters: Set[AclBindingFilter], options: Option[DeleteAclsOptions] = None): Task[Set[AclBinding]]

  /**
   * Deletes access control lists (ACLs) according to the supplied filters async.
   */
  def deleteAclsAsync(
    filters: Set[AclBindingFilter],
    options: Option[DeleteAclsOptions] = None
  ): Task[Map[AclBindingFilter, Task[Map[AclBinding, Option[Throwable]]]]]

}

/**
 * Constructors of an [[AdminClient]], and the case classes that it uses for its parameters and results. Most of these
 * mirror a type of the Java admin client; `asJava` and `fromJava` convert between the two.
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
   * A layer that makes an [[AdminClient]] from the [[AdminClientSettings]] in the environment, using
   * `AdminClient.make`. The client is closed when the layer's scope ends.
   */
  val live: ZLayer[AdminClientSettings, Throwable, AdminClient] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[AdminClientSettings]
        admin    <- make(settings)
      } yield admin
    }

  /**
   * Runs `kfv`, waits for the Kafka future it returns and returns its result. The effect fails when the future fails.
   */
  def fromKafkaFuture[R, T](kfv: RIO[R, KafkaFuture[T]]): RIO[R, T] =
    kfv.flatMap(f => ZIO.fromCompletionStage(f.toCompletionStage))

  /** Like `fromKafkaFuture`, for a future without a result. */
  def fromKafkaFutureVoid[R](kfv: RIO[R, KafkaFuture[Void]]): RIO[R, Unit] =
    fromKafkaFuture(kfv).unit

  /** Names a resource that has a configuration: its type, and its name (for a topic, the topic name). */
  final case class ConfigResource(`type`: ConfigResourceType, name: String) {
    /** Converts to the Java `ConfigResource`. */
    lazy val asJava: JConfigResource = new JConfigResource(`type`.asJava, name)
  }

  /** Conversion from the Java `ConfigResource`. */
  object ConfigResource {
    /** Converts a Java `ConfigResource`. */
    def fromJava(jcr: JConfigResource): ConfigResource =
      ConfigResource(`type` = ConfigResourceType.fromJava(jcrt = jcr.`type`()), name = jcr.name())
  }

  /** The type of a [[ConfigResource]]. Each case object in the companion maps to a Java `ConfigResource.Type`. */
  trait ConfigResourceType {
    /** Converts to the Java `ConfigResource.Type`. */
    def asJava: JConfigResource.Type
  }

  /** The types of a [[ConfigResource]], and the conversion from the Java `ConfigResource.Type`. */
  object ConfigResourceType {
    /** Broker logger configuration (Java `BROKER_LOGGER`). */
    case object BrokerLogger extends ConfigResourceType {
      override def asJava = JConfigResource.Type.BROKER_LOGGER
    }

    /** Broker configuration (Java `BROKER`). */
    case object Broker extends ConfigResourceType {
      override def asJava = JConfigResource.Type.BROKER
    }

    /** Topic configuration (Java `TOPIC`). */
    case object Topic extends ConfigResourceType {
      override def asJava = JConfigResource.Type.TOPIC
    }

    /** A resource type that is not known (Java `UNKNOWN`). */
    case object Unknown extends ConfigResourceType {
      override def asJava = JConfigResource.Type.UNKNOWN
    }

    /** Client metrics configuration (Java `CLIENT_METRICS`). */
    case object ClientMetrics extends ConfigResourceType {
      override def asJava = JConfigResource.Type.CLIENT_METRICS
    }

    /** Group configuration (Java `GROUP`). */
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

  /** The state of a group. Each case object in the companion maps to a Java `GroupState`. */
  sealed trait GroupState {
    /** Converts to the Java `GroupState`. */
    def asJava: JGroupState
  }

  /** The states of a group, and the conversion from the Java `GroupState`. */
  object GroupState {
    /** A state that is not known. */
    case object Unknown extends GroupState {
      override def asJava: JGroupState = JGroupState.UNKNOWN
    }

    /** The group is preparing to rebalance. */
    case object PreparingRebalance extends GroupState {
      override def asJava: JGroupState = JGroupState.PREPARING_REBALANCE
    }

    /** The group is completing a rebalance. */
    case object CompletingRebalance extends GroupState {
      override def asJava: JGroupState = JGroupState.COMPLETING_REBALANCE
    }

    /** The group is stable. */
    case object Stable extends GroupState {
      override def asJava: JGroupState = JGroupState.STABLE
    }

    /** The group has no members and its metadata is being removed. */
    case object Dead extends GroupState {
      override def asJava: JGroupState = JGroupState.DEAD
    }

    /** The group has no members. */
    case object Empty extends GroupState {
      override def asJava: JGroupState = JGroupState.EMPTY
    }

    /**
     * The group is assigning partitions. The Java client lists this state for groups of type `Consumer` and `Streams`.
     */
    case object Assigning extends GroupState {
      override def asJava: JGroupState = JGroupState.ASSIGNING
    }

    /** The group is reconciling. The Java client lists this state for groups of type `Consumer` and `Streams`. */
    case object Reconciling extends GroupState {
      override def asJava: JGroupState = JGroupState.RECONCILING
    }

    /** The group is not ready. The Java client lists this state for groups of type `Streams`. */
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

  /** The type of a group. Each case object in the companion maps to a Java `GroupType`. */
  sealed trait GroupType {
    /** Converts to the Java `GroupType`. */
    def asJava: JGroupType
  }

  /** The types of a group, and the conversion from the Java `GroupType`. */
  object GroupType {
    /** A group type that is not known. */
    case object Unknown extends GroupType {
      override def asJava = JGroupType.UNKNOWN
    }
    /** A group that uses the consumer group protocol. */
    case object Consumer extends GroupType {
      override def asJava = JGroupType.CONSUMER
    }
    /** A group that uses the classic group protocol. */
    case object Classic extends GroupType {
      override def asJava = JGroupType.CLASSIC
    }
    /** A share group. */
    case object Share extends GroupType {
      override def asJava = JGroupType.SHARE
    }
    /** A Kafka Streams group. */
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
   * A member of a consumer group. `groupInstanceId` is the static group instance id, when the member has one.
   * `assignment` is the set of partitions assigned to the member.
   */
  final case class MemberDescription(
    consumerId: String,
    groupInstanceId: Option[String],
    clientId: String,
    host: String,
    assignment: Set[TopicPartition]
  )

  /** Conversion from the Java `MemberDescription`. */
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
   * The description of a consumer group: its members, state and coordinator. `coordinator` is `None` when the Java
   * value is no node. `authorizedOperations` is empty when the Java description has none.
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

  /** Conversion from the Java `ConsumerGroupDescription`. */
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
   * Options for `createPartitions`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. With `validateOnly`, the broker only validates the request: nothing changes and `createPartitions`
   * does not wait. With `retryOnQuotaViolation`, the Java client retries when a quota is violated.
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
   * Options for `createTopics`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. With `validateOnly`, the broker only validates the request: no topic is created and `createTopics`
   * does not wait.
   */
  final case class CreateTopicsOptions(validateOnly: Boolean, timeout: Option[Duration]) {
    /** Converts to the Java `CreateTopicsOptions`. */
    def asJava: JCreateTopicsOptions = {
      val opts = new JCreateTopicsOptions().validateOnly(validateOnly)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `deleteConsumerGroups`. `timeout` is the request timeout; when it is `None`, the Java client's default
   * API timeout applies.
   */
  final case class DeleteConsumerGroupOptions(timeout: Option[Duration]) {
    /** Converts to the Java `DeleteConsumerGroupsOptions`. */
    def asJava: JDeleteConsumerGroupsOptions = {
      val opts = new JDeleteConsumerGroupsOptions()
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `deleteTopics`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. With `retryOnQuotaViolation`, the Java client retries when a quota is violated.
   */
  final case class DeleteTopicsOptions(retryOnQuotaViolation: Boolean = true, timeout: Option[Duration]) {
    /** Converts to the Java `DeleteTopicsOptions`. */
    def asJava: JDeleteTopicsOptions = {
      val opts = new JDeleteTopicsOptions().retryOnQuotaViolation(retryOnQuotaViolation)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `listTopics`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. With `listInternal`, internal topics are listed too.
   */
  final case class ListTopicsOptions(listInternal: Boolean = false, timeout: Option[Duration]) {
    /** Converts to the Java `ListTopicsOptions`. */
    def asJava: JListTopicsOptions = {
      val opts = new JListTopicsOptions().listInternal(listInternal)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeTopics`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. With `includeAuthorizedOperations`, the broker also returns the operations that the client is
   * authorized to do on each topic.
   */
  final case class DescribeTopicsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {
    /** Converts to the Java `DescribeTopicsOptions`. */
    def asJava: JDescribeTopicsOptions = {
      val opts = new JDescribeTopicsOptions().includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeConfigs`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. With `includeSynonyms`, the broker also returns the synonyms of each entry. With
   * `includeDocumentation`, it also returns the documentation of each entry.
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
   * Options for `describeClusterNodes` and the other `describeCluster*` operations. `timeout` is the request timeout;
   * when it is `None`, the Java client's default API timeout applies. With `includeAuthorizedOperations`, the broker
   * also returns the operations that the client is authorized to do on the cluster.
   */
  final case class DescribeClusterOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {
    /** Converts to the Java `DescribeClusterOptions`. */
    lazy val asJava: JDescribeClusterOptions = {
      val opts = new JDescribeClusterOptions().includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `describeConsumerGroups`. `timeout` is the request timeout; when it is `None`, the Java client's
   * default API timeout applies. With `includeAuthorizedOperations`, the broker also returns the operations that the
   * client is authorized to do on each group.
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
   * Options for `incrementalAlterConfigs`. `timeout` is the request timeout; when it is `None`, the Java client's
   * default API timeout applies. With `validateOnly`, the broker only validates the request: nothing changes and the
   * operation does not wait.
   */
  final case class AlterConfigsOptions(validateOnly: Boolean = false, timeout: Option[Duration] = None) {
    /** Converts to the Java `AlterConfigsOptions`. */
    lazy val asJava: JAlterConfigsOptions = {
      val jOpts = new JAlterConfigsOptions().validateOnly(validateOnly)
      timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /** A change to one configuration entry: applies `opType` to the Java `ConfigEntry` `configEntry`. */
  final case class AlterConfigOp(configEntry: ConfigEntry, opType: AlterConfigOpType) {
    /** Converts to the Java `AlterConfigOp`. */
    lazy val asJava: JAlterConfigOp = new JAlterConfigOp(configEntry, opType.asJava)
  }

  /**
   * The kind of change of an [[AlterConfigOp]]. Each case object in the companion maps to a Java
   * `AlterConfigOp.OpType`.
   */
  sealed trait AlterConfigOpType {
    /** Converts to the Java `AlterConfigOp.OpType`. */
    def asJava: JAlterConfigOp.OpType
  }

  /** The kinds of change of an [[AlterConfigOp]]. */
  object AlterConfigOpType {
    /** Sets the value of the entry. */
    case object Set extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.SET
    }

    /** Reverts the entry to its default value. */
    case object Delete extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.DELETE
    }

    /**
     * Adds the value to the current value of a list entry. When the entry has not been set, adds it to the default
     * value.
     */
    case object Append extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.APPEND
    }

    /**
     * Removes the value from the current value of a list entry. Removing all values leaves an empty list; it does not
     * revert to the default value.
     */
    case object Subtract extends AlterConfigOpType {
      override def asJava = JAlterConfigOp.OpType.SUBTRACT
    }

    /** Deprecated misspelling of `Subtract`. */
    @deprecated("Use AlterConfigOpType.Subtract", since = "3.6.0")
    val Substract: AlterConfigOpType.Subtract.type = AlterConfigOpType.Subtract
  }

  /** The name of a metric of the Java admin client, with its group, description and tags. */
  final case class MetricName(name: String, group: String, description: String, tags: Map[String, String])
  /** Conversion from the Java `MetricName`. */
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

  /** A metric of the Java admin client. `metricValue` is the value as the Java client reports it. */
  final case class Metric(name: MetricName, metricValue: AnyRef)
  /** Conversion from the Java `Metric`. */
  object Metric {
    /** Converts a Java `Metric`. */
    def fromJava(jm: JMetric): Metric =
      Metric(name = MetricName.fromJava(jmn = jm.metricName()), metricValue = jm.metricValue())
  }

  /** A topic to create: its name, number of partitions, replication factor and topic configuration. */
  final case class NewTopic(
    name: String,
    numPartitions: Int,
    replicationFactor: Short,
    configs: Map[String, String] = Map.empty
  ) {
    /** Converts to the Java `NewTopic`. The topic configuration is set only when `configs` is not empty. */
    def asJava: JNewTopic = {
      val jn = new JNewTopic(name, numPartitions, replicationFactor)

      if (configs.nonEmpty) {
        val _ = jn.configs(configs.asJava)
      }

      jn
    }
  }

  /**
   * Raises the partition count of a topic to `totalCount`. When `newAssignments` is empty, the broker decides where the
   * new replicas go. Otherwise it holds one list of broker ids per new partition.
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
   * A node (broker) of the cluster.
   *
   * The constructor does not check the fields. `Node.fromJava` returns `None` for a node with a negative id, and
   * returns `None` for the host when it is empty and for the port when it is negative.
   */
  final case class Node(id: Int, host: Option[String], port: Option[Int], rack: Option[String] = None) {
    /** The conversion to the Java `Node`: a missing host becomes an empty string, a missing port becomes `-1`. */
    lazy val asJava: JNode = new JNode(id, host.getOrElse(""), port.getOrElse(-1), rack.orNull)
  }
  /** Conversion from the Java `Node`. */
  object Node {
    /**
     * Converts a Java `Node`. The result is `None` for `null` and for a node with a negative id (the Java "no node"
     * placeholder). An empty host and a negative port become `None`.
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
   * The description of a topic and its partitions. `authorizedOperations` is `None` when the Java description has none.
   */
  final case class TopicDescription(
    name: String,
    internal: Boolean,
    partitions: List[TopicPartitionInfo],
    authorizedOperations: Option[Set[AclOperation]]
  )

  /** Conversion from the Java `TopicDescription`. */
  object TopicDescription {
    /**
     * Converts a Java `TopicDescription`. Fails when one of the partitions fails to convert, see
     * `TopicPartitionInfo.fromJava`.
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
   * The description of one partition of a topic: its leader (`None` when it has none), its replicas and its in-sync
   * replicas.
   */
  final case class TopicPartitionInfo(partition: Int, leader: Option[Node], replicas: List[Node], isr: List[Node]) {
    /** Converts to the Java `TopicPartitionInfo`. */
    lazy val asJava: JTopicPartitionInfo =
      new JTopicPartitionInfo(
        partition,
        leader.map(_.asJava).getOrElse(JNode.noNode()),
        replicas.map(_.asJava).asJava,
        isr.map(_.asJava).asJava
      )
  }

  /** Conversion from the Java `TopicPartitionInfo`. */
  object TopicPartitionInfo {
    /**
     * Converts a Java `TopicPartitionInfo`. Fails with a `RuntimeException` when a replica or an in-sync replica is the
     * Java "no node" placeholder.
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

  /** A topic in the output of `listTopics`: its name, its id and whether it is internal. */
  final case class TopicListing(name: String, topicId: Uuid, isInternal: Boolean) {
    /** Converts to the Java `TopicListing`. */
    def asJava: JTopicListing = new JTopicListing(name, topicId, isInternal)
  }

  /** Conversion from the Java `TopicListing`. */
  object TopicListing {
    /** Converts a Java `TopicListing`. */
    def fromJava(jtl: JTopicListing): TopicListing = TopicListing(jtl.name(), jtl.topicId(), jtl.isInternal)
  }

  /** A topic name and a partition number. The Java `TopicPartition` has the same fields. */
  final case class TopicPartition(
    name: String,
    partition: Int
  ) {
    /** Converts to the Java `TopicPartition`. */
    def asJava: JTopicPartition = new JTopicPartition(name, partition)
  }

  /** Conversion from the Java `TopicPartition`. */
  object TopicPartition {
    /** Converts a Java `TopicPartition`. */
    def fromJava(tp: JTopicPartition): TopicPartition =
      new TopicPartition(name = tp.topic(), partition = tp.partition())
  }

  /**
   * Selects which offset of a partition `listOffsets` returns: the earliest, the latest, or the one for a timestamp.
   */
  sealed abstract class OffsetSpec {
    /** Converts to the Java `OffsetSpec`. */
    def asJava: JOffsetSpec
  }

  /** The choices of an [[OffsetSpec]]. */
  object OffsetSpec {
    /** The earliest offset of the partition. */
    case object EarliestSpec extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.earliest()
    }

    /** The latest offset of the partition. */
    case object LatestSpec extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.latest()
    }

    /** The earliest offset whose timestamp is greater than or equal to `timestamp`, in milliseconds. */
    final case class TimestampSpec(timestamp: Long) extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.forTimestamp(timestamp)
    }
  }

  /** The isolation level of `listOffsets`. */
  sealed abstract class IsolationLevel {
    /** Converts to the Java `IsolationLevel`. */
    def asJava: JIsolationLevel
  }

  /** The isolation levels of `listOffsets`. */
  object IsolationLevel {
    /** Java `READ_UNCOMMITTED`. This is the default of [[ListOffsetsOptions]]. */
    case object ReadUncommitted extends IsolationLevel {
      override def asJava: JIsolationLevel = JIsolationLevel.READ_UNCOMMITTED
    }

    /** Java `READ_COMMITTED`. */
    case object ReadCommitted extends IsolationLevel {
      override def asJava: JIsolationLevel = JIsolationLevel.READ_COMMITTED
    }
  }

  /**
   * Options for `deleteRecords`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies.
   */
  final case class DeleteRecordsOptions(timeout: Option[Duration]) {
    /** Converts to the Java `DeleteRecordsOptions`. */
    def asJava: JDeleteRecordsOptions = {
      val deleteRecordsOpt = new JDeleteRecordsOptions()
      timeout.fold(deleteRecordsOpt)(timeout => deleteRecordsOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `listOffsets`. `timeout` is the request timeout; when it is `None`, the Java client's default API
   * timeout applies. `isolationLevel` is the isolation level of the lookup.
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

  /** The result of a `listOffsets` lookup: the `offset`, its `timestamp` and its `leaderEpoch`, when known. */
  final case class ListOffsetsResultInfo(
    offset: Long,
    timestamp: Long,
    leaderEpoch: Option[Int]
  )
  /** Conversion from the Java `ListOffsetsResult.ListOffsetsResultInfo`. */
  object ListOffsetsResultInfo {
    /** Converts a Java `ListOffsetsResult.ListOffsetsResultInfo`. */
    def fromJava(lo: JListOffsetsResultInfo): ListOffsetsResultInfo =
      ListOffsetsResultInfo(lo.offset(), lo.timestamp(), lo.leaderEpoch().toScala.map(_.toInt))
  }

  /** Options for `listConsumerGroupOffsets`. `requireStable` sets the `requireStable` flag of the Java options. */
  final case class ListConsumerGroupOffsetsOptions(requireStable: Boolean) {
    /** Converts to the Java `ListConsumerGroupOffsetsOptions`. */
    def asJava: JListConsumerGroupOffsetsOptions = {
      val opts = new JListConsumerGroupOffsetsOptions()
      opts.requireStable(requireStable)
    }
  }

  /** Constructor of [[ListConsumerGroupOffsetsOptions]]. */
  object ListConsumerGroupOffsetsOptions {
    /** Creates the options with the given `requireStable` flag. */
    def apply(requireStable: Boolean): ListConsumerGroupOffsetsOptions =
      new ListConsumerGroupOffsetsOptions(requireStable)
  }

  /** The partitions of one group to look up in `listConsumerGroupOffsets`. */
  final case class ListConsumerGroupOffsetsSpec(partitions: Chunk[TopicPartition]) {
    /** Converts to the Java `ListConsumerGroupOffsetsSpec`. */
    def asJava: JListConsumerGroupOffsetsSpec = {
      val opts = new JListConsumerGroupOffsetsSpec
      opts.topicPartitions(partitions.map(_.asJava).asJava)
      opts
    }
  }

  /**
   * A committed offset of a consumer group, with an optional leader epoch and optional metadata. `asJava` throws an
   * `IllegalArgumentException` ("Invalid negative offset") for a negative offset. `fromJava` gives `Some` metadata, an
   * empty string when the Java value has none.
   */
  final case class OffsetAndMetadata(
    offset: Long,
    leaderEpoch: Option[Int] = None,
    metadata: Option[String] = None
  ) {
    /** Converts to the Java `OffsetAndMetadata`. */
    def asJava: JOffsetAndMetadata = new JOffsetAndMetadata(offset, leaderEpoch.map(Int.box).toJava, metadata.orNull)
  }
  /** Conversion from the Java `OffsetAndMetadata`. */
  object OffsetAndMetadata {
    /** Converts a Java `OffsetAndMetadata`. */
    def fromJava(om: JOffsetAndMetadata): OffsetAndMetadata =
      OffsetAndMetadata(
        offset = om.offset(),
        leaderEpoch = om.leaderEpoch().toScala.map(_.toInt),
        metadata = Some(om.metadata())
      )
  }

  /**
   * Options for `alterConsumerGroupOffsets`. `timeout` is the request timeout; when it is `None`, the Java client's
   * default API timeout applies.
   */
  final case class AlterConsumerGroupOffsetsOptions(timeout: Option[Duration]) {
    /** Converts to the Java `AlterConsumerGroupOffsetsOptions`. */
    def asJava: JAlterConsumerGroupOffsetsOptions = {
      val options = new JAlterConsumerGroupOffsetsOptions()
      timeout.fold(options)(timeout => options.timeoutMs(timeout.toMillis.toInt))
    }
  }

  /**
   * Options for `listConsumerGroups`: only groups in one of `states` are listed. An empty set lists groups in every
   * state.
   */
  final case class ListConsumerGroupsOptions(states: Set[GroupState]) {
    /** Converts to the Java `ListGroupsOptions` for consumer groups, filtered by `states`. */
    def asJListGroupsOptions: JListGroupsOptions =
      JListGroupsOptions.forConsumerGroups().inGroupStates(states.map(_.asJava).asJava)
  }

  /**
   * A consumer group in the output of `listConsumerGroups`: its id, whether it is a simple consumer group, and its
   * state when it is known.
   */
  final case class ConsumerGroupListing(groupId: String, isSimple: Boolean, state: Option[GroupState])

  /** Conversion from the Java `GroupListing`. */
  object ConsumerGroupListing {
    /** Converts a Java `GroupListing`. */
    def fromJava(cg: JGroupListing): ConsumerGroupListing =
      ConsumerGroupListing(
        groupId = cg.groupId(),
        isSimple = cg.isSimpleConsumerGroup,
        state = cg.groupState().toScala.map(GroupState.fromJava)
      )
  }

  /** A group in the output of `listGroups`: its id, its type and state when known, and its protocol. */
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

  /** Conversion from the Java `GroupListing`. */
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
   * Options for `listGroups`: only groups with one of the given states, types and protocol types are listed. A set that
   * is empty does not filter on that property.
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

  /** Conversion from the Java `ListGroupsOptions`. */
  object ListGroupsOptions {
    /** Converts a Java `ListGroupsOptions`. */
    def fromJava(lgo: JListGroupsOptions): ListGroupsOptions =
      ListGroupsOptions(
        lgo.groupStates().asScala.map(GroupState.fromJava).toSet,
        lgo.types().asScala.map(GroupType.fromJava).toSet,
        lgo.protocolTypes().asScala.toSet
      )
  }

  /** The configuration of a resource: its entries by name. Each entry is a Java `ConfigEntry`. */
  final case class KafkaConfig(entries: Map[String, ConfigEntry]) {
    /** Converts to the Java `Config`. */
    def asJava: JConfig = new JConfig(entries.values.asJavaCollection)
  }
  /** Conversion from the Java `Config`. */
  object KafkaConfig {
    /** Converts a Java `Config`. */
    def fromJava(jConfig: JConfig): KafkaConfig =
      KafkaConfig(entries = jConfig.entries().asScala.map(e => e.name() -> e).toMap)
  }

  /**
   * A log directory of a broker: `replicaInfos` describes the replicas it holds. `error` is the error of the log
   * directory, and `null` when it has none.
   */
  final case class LogDirDescription(error: ApiException, replicaInfos: Map[TopicPartition, ReplicaInfo])
  /** Conversion from the Java `LogDirDescription`. */
  object LogDirDescription {
    /** Converts a Java `LogDirDescription`. */
    def fromJava(ld: JLogDirDescription): LogDirDescription =
      LogDirDescription(
        error = ld.error(),
        replicaInfos = ld.replicaInfos().asScala.bimap(TopicPartition.fromJava, ReplicaInfo.fromJava).toMap
      )
  }

  /**
   * A replica in a log directory: its `size` in bytes, its `offsetLag`, and whether it is a future replica
   * (`isFuture`): created by a request to move it to another log directory, and not yet replacing the current replica.
   */
  final case class ReplicaInfo(size: Long, offsetLag: Long, isFuture: Boolean)
  /** Conversion from the Java `ReplicaInfo`. */
  object ReplicaInfo {
    /** Converts a Java `ReplicaInfo`. */
    def fromJava(ri: JReplicaInfo): ReplicaInfo =
      ReplicaInfo(size = ri.size(), offsetLag = ri.offsetLag(), isFuture = ri.isFuture)
  }

  /**
   * Make a Kafka [[AdminClient]].
   *
   * @param settings
   *   the admin client settings
   */
  def make(settings: AdminClientSettings): ZIO[Scope, Throwable, AdminClient] =
    fromScopedJavaClient(javaClientFromSettings(settings))

  /**
   * Make a Kafka [[AdminClient]] by wrapping a provided Java admin client.
   *
   * @param javaClient
   *   the java admin client to wrap
   */
  def fromJavaClient(javaClient: JAdmin): URIO[Any, AdminClient] =
    ZIO.succeed(new LiveAdminClient(javaClient))

  /**
   * Make a Kafka [[AdminClient]] by wrapping a provided Java admin client. The client closes with the provided scope.
   */
  def fromScopedJavaClient[R, E](scopedJavaClient: ZIO[R & Scope, E, JAdmin]): ZIO[R & Scope, E, AdminClient] =
    for {
      javaClient  <- scopedJavaClient
      adminClient <- fromJavaClient(javaClient)
    } yield adminClient

  /**
   * Creates the Java admin client for `settings`. It first runs the SSL endpoint check, `SslHelper.validateEndpoint`,
   * which fails with a `org.apache.kafka.common.KafkaException` when the check finds a problem. When the scope closes,
   * it closes the client, waiting at most `closeTimeout` of the settings.
   */
  def javaClientFromSettings(settings: AdminClientSettings): ZIO[Scope, Throwable, JAdmin] =
    ZIO.acquireRelease {
      val endpointCheck = SslHelper
        .validateEndpoint(settings.driverSettings)

      endpointCheck *> ZIO.attempt(JAdmin.create(settings.driverSettings.asJava))
    }(client => ZIO.attempt(client.close(settings.closeTimeout)).orDie)

  /** Adds `bimap` to a `Map`. */
  implicit final class MapOps[K1, V1](private val v: Map[K1, V1]) extends AnyVal {
    /** Maps the keys with `fk` and the values with `fv`. */
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): Map[K2, V2] = v.map { case (k, v) => fk(k) -> fv(v) }
  }

  /** Adds `bimap` to a mutable `Map`. */
  implicit final class MutableMapOps[K1, V1](private val v: mutable.Map[K1, V1]) extends AnyVal {
    /** Maps the keys with `fk` and the values with `fv`. */
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): mutable.Map[K2, V2] = v.map { case (k, v) => fk(k) -> fv(v) }
  }

  /** Adds `toScala` to a Java `Optional`. */
  implicit final class OptionalOps[T](private val v: Optional[T]) extends AnyVal {
    /** Returns the value as a `Some`, or `None` when the `Optional` is empty. */
    def toScala: Option[T] = if (v.isPresent) Some(v.get()) else None
  }

  /** Adds `toJava` to an `Option`. */
  implicit final class OptionOps[T](private val v: Option[T]) extends AnyVal {
    /** Returns an `Optional` with the value, or an empty `Optional` for `None`. */
    def toJava: Optional[T] = v.fold(Optional.empty[T])(Optional.of)
  }

  /** Adds `forEach` to a `List`. */
  implicit final class ListOps[A](private val list: List[A]) extends AnyVal {
    /**
     * Applies `f` to the elements in order and collects the results. Stops at the first `Failure`, and returns it; the
     * remaining elements are not visited.
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
