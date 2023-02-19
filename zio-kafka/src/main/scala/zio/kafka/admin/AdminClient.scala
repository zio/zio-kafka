package zio.kafka.admin

import org.apache.kafka.clients.admin.ListOffsetsResult.{ ListOffsetsResultInfo => JListOffsetsResultInfo }
import org.apache.kafka.clients.admin.{
  Admin => JAdmin,
  AlterConfigOp => JAlterConfigOp,
  AlterConfigsOptions => JAlterConfigsOptions,
  AlterConsumerGroupOffsetsOptions => JAlterConsumerGroupOffsetsOptions,
  Config => JConfig,
  ConsumerGroupDescription => JConsumerGroupDescription,
  ConsumerGroupListing => JConsumerGroupListing,
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
  ListConsumerGroupOffsetsOptions => JListConsumerGroupOffsetsOptions,
  ListConsumerGroupOffsetsSpec => JListConsumerGroupOffsetsSpec,
  ListConsumerGroupsOptions => JListConsumerGroupsOptions,
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
  ConsumerGroupState => JConsumerGroupState,
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

import java.util.Optional
import scala.annotation.{ nowarn, tailrec }
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success, Try }

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
   */
  def deleteRecords(
    recordsToDelete: Map[TopicPartition, RecordsToDelete],
    deleteRecordsOptions: Option[DeleteRecordsOptions] = None
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
   * List offset for the specified partitions.
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
   * List the consumer group offsets available in the cluster for the specified consumer groups.
   */
  def listConsumerGroupOffsets(
    groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]
  ): Task[Map[TopicPartition, OffsetAndMetadata]]

  /**
   * List the consumer group offsets available in the cluster for the specified consumer groups.
   */
  def listConsumerGroupOffsets(
    groupSpecs: Map[String, ListConsumerGroupOffsetsSpec],
    options: ListConsumerGroupOffsetsOptions
  ): Task[Map[TopicPartition, OffsetAndMetadata]]

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
   */
  def removeMembersFromConsumerGroup(groupId: String, membersToRemove: Set[String]): Task[Unit]

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
   * higher. Use alterConfigs otherwise.
   */
  def incrementalAlterConfigs(
    configs: Map[ConfigResource, Iterable[AlterConfigOp]],
    options: AlterConfigsOptions
  ): Task[Unit]

  /**
   * Incrementally update the configuration for the specified resources async. Only supported by brokers with version
   * 2.3.0 or higher. Use alterConfigsAsync otherwise.
   */
  def incrementalAlterConfigsAsync(
    configs: Map[ConfigResource, Iterable[AlterConfigOp]],
    options: AlterConfigsOptions
  ): Task[Map[ConfigResource, Task[Unit]]]

  /**
   * Update the configuration for the specified resources.
   *
   * If you are using brokers with version 2.3.0 or higher, please use incrementalAlterConfigs instead.
   */
  def alterConfigs(configs: Map[ConfigResource, KafkaConfig], options: AlterConfigsOptions): Task[Unit]

  /**
   * Update the configuration for the specified resources async.
   *
   * If you are using brokers with version 2.3.0 or higher, please use incrementalAlterConfigs instead.
   */
  def alterConfigsAsync(
    configs: Map[ConfigResource, KafkaConfig],
    options: AlterConfigsOptions
  ): Task[Map[ConfigResource, Task[Unit]]]

  /*
   * Lists access control lists (ACLs) according to the supplied filter.
   *
   * Note: it may take some time for changes made by createAcls or deleteAcls to be reflected in the output of
   * describeAcls.
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

object AdminClient {

  /**
   * Thin wrapper around apache java AdminClient. See java api for descriptions
   *
   * @param adminClient
   */
  private final class LiveAdminClient(
    private val adminClient: JAdmin
  ) extends AdminClient {

    /**
     * Create multiple topics.
     */
    override def createTopics(
      newTopics: Iterable[NewTopic],
      options: Option[CreateTopicsOptions] = None
    ): Task[Unit] = {
      val asJava = newTopics.map(_.asJava).asJavaCollection

      fromKafkaFutureVoid {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts.asJava))
            .all()
        )
      }
    }

    /**
     * Create a single topic.
     */
    override def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): Task[Unit] =
      createTopics(List(newTopic), Some(CreateTopicsOptions(validateOnly = validateOnly, timeout = Option.empty)))

    /**
     * Delete consumer groups.
     */
    override def deleteConsumerGroups(
      groupIds: Iterable[String],
      options: Option[DeleteConsumerGroupOptions]
    ): Task[Unit] = {
      val asJava = groupIds.asJavaCollection
      fromKafkaFutureVoid {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.deleteConsumerGroups(asJava))(opts =>
              adminClient.deleteConsumerGroups(asJava, opts.asJava)
            )
            .all()
        )
      }
    }

    /**
     * Delete multiple topics.
     */
    override def deleteTopics(
      topics: Iterable[String],
      options: Option[DeleteTopicsOptions] = None
    ): Task[Unit] = {
      val asJava = topics.asJavaCollection
      fromKafkaFutureVoid {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.deleteTopics(asJava))(opts => adminClient.deleteTopics(asJava, opts.asJava))
            .all()
        )
      }
    }

    /**
     * Delete a single topic.
     */
    override def deleteTopic(topic: String): Task[Unit] =
      deleteTopics(List(topic))

    /**
     * Delete records.
     */
    override def deleteRecords(
      recordsToDelete: Map[TopicPartition, RecordsToDelete],
      deleteRecordsOptions: Option[DeleteRecordsOptions] = None
    ): Task[Unit] = {
      val records = recordsToDelete.map { case (k, v) => k.asJava -> v }.asJava
      fromKafkaFutureVoid {
        ZIO.attemptBlocking(
          deleteRecordsOptions
            .fold(adminClient.deleteRecords(records))(opts => adminClient.deleteRecords(records, opts.asJava))
            .all()
        )
      }
    }

    /**
     * List the topics in the cluster.
     */
    override def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): Task[Map[String, TopicListing]] =
      fromKafkaFuture {
        ZIO.attemptBlocking(
          listTopicsOptions
            .fold(adminClient.listTopics())(opts => adminClient.listTopics(opts.asJava))
            .namesToListings()
        )
      }.map(_.asScala.map { case (k, v) => k -> TopicListing(v) }.toMap)

    /**
     * Describe the specified topics.
     */
    override def describeTopics(
      topicNames: Iterable[String],
      options: Option[DescribeTopicsOptions] = None
    ): Task[Map[String, TopicDescription]] = {
      val asJava = topicNames.asJavaCollection
      fromKafkaFuture {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.describeTopics(asJava))(opts => adminClient.describeTopics(asJava, opts.asJava))
            .allTopicNames()
        )
      }.flatMap { jTopicDescriptions =>
        ZIO.fromTry {
          jTopicDescriptions.asScala.toList.forEach { case (k, v) => AdminClient.TopicDescription(v).map(k -> _) }
            .map(_.toMap)
        }
      }
    }

    /**
     * Get the configuration for the specified resources.
     */
    override def describeConfigs(
      configResources: Iterable[ConfigResource],
      options: Option[DescribeConfigsOptions] = None
    ): Task[Map[ConfigResource, KafkaConfig]] = {
      val asJava = configResources.map(_.asJava).asJavaCollection
      fromKafkaFuture {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
            .all()
        )
      }.map(
        _.asScala.view.map { case (configResource, config) =>
          (ConfigResource(configResource), KafkaConfig(config))
        }.toMap
      )
    }

    /**
     * Get the configuration for the specified resources async.
     */
    override def describeConfigsAsync(
      configResources: Iterable[ConfigResource],
      options: Option[DescribeConfigsOptions] = None
    ): Task[Map[ConfigResource, Task[KafkaConfig]]] = {
      val asJava = configResources.map(_.asJava).asJavaCollection
      ZIO
        .attemptBlocking(
          options
            .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
            .values()
        )
        .map(
          _.asScala.view.map { case (configResource, configFuture) =>
            (
              ConfigResource(configResource),
              ZIO
                .fromCompletionStage(configFuture.toCompletionStage)
                .map(config => KafkaConfig(config))
            )

          }.toMap
        )
    }

    private def describeCluster(options: Option[DescribeClusterOptions]): Task[DescribeClusterResult] =
      ZIO.attemptBlocking(
        options.fold(adminClient.describeCluster())(opts => adminClient.describeCluster(opts.asJava))
      )

    /**
     * Get the cluster nodes.
     */
    override def describeClusterNodes(options: Option[DescribeClusterOptions] = None): Task[List[Node]] =
      fromKafkaFuture(
        describeCluster(options).map(_.nodes())
      ).flatMap { nodes =>
        ZIO.foreach(nodes.asScala.toList) { jNode =>
          ZIO
            .getOrFailWith(new RuntimeException("NoNode not expected when listing cluster nodes"))(
              Node(jNode)
            )
        }
      }

    /**
     * Get the cluster controller.
     */
    override def describeClusterController(options: Option[DescribeClusterOptions] = None): Task[Option[Node]] =
      fromKafkaFuture(
        describeCluster(options).map(_.controller())
      ).map(Node(_))

    /**
     * Get the cluster id.
     */
    override def describeClusterId(options: Option[DescribeClusterOptions] = None): Task[String] =
      fromKafkaFuture(
        describeCluster(options).map(_.clusterId())
      )

    /**
     * Get the cluster authorized operations.
     */
    override def describeClusterAuthorizedOperations(
      options: Option[DescribeClusterOptions] = None
    ): Task[Set[AclOperation]] =
      for {
        res <- describeCluster(options)
        opt <- fromKafkaFuture(ZIO.attempt(res.authorizedOperations())).map(Option(_))
        lst <- ZIO.fromOption(opt.map(_.asScala.toSet)).orElseSucceed(Set.empty)
        aclOperations = lst.map(AclOperation.apply)
      } yield aclOperations

    /**
     * Add new partitions to a topic.
     */
    override def createPartitions(
      newPartitions: Map[String, NewPartitions],
      options: Option[CreatePartitionsOptions] = None
    ): Task[Unit] = {
      val asJava = newPartitions.map { case (k, v) => k -> v.asJava }.asJava
      fromKafkaFutureVoid {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts.asJava))
            .all()
        )
      }
    }

    /**
     * List offset for the specified partitions.
     */
    override def listOffsets(
      topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
      options: Option[ListOffsetsOptions] = None
    ): Task[Map[TopicPartition, ListOffsetsResultInfo]] = {
      val asJava = topicPartitionOffsets.bimap(_.asJava, _.asJava).asJava
      fromKafkaFuture {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
            .all()
        )
      }
    }.map(_.asScala.toMap.bimap(TopicPartition(_), ListOffsetsResultInfo(_)))

    /**
     * List offset for the specified partitions.
     */
    override def listOffsetsAsync(
      topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
      options: Option[ListOffsetsOptions]
    ): Task[Map[TopicPartition, Task[ListOffsetsResultInfo]]] = {
      val topicPartitionOffsetsAsJava = topicPartitionOffsets.bimap(_.asJava, _.asJava)
      val topicPartitionsAsJava       = topicPartitionOffsetsAsJava.keySet
      val asJava                      = topicPartitionOffsetsAsJava.asJava
      ZIO.attemptBlocking {
        val listOffsetsResult = options
          .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
        topicPartitionsAsJava.map(tp => tp -> listOffsetsResult.partitionResult(tp))
      }
    }.map(_.view.map { case (topicPartition, listOffsetResultInfoFuture) =>
      (
        TopicPartition(topicPartition),
        ZIO.fromCompletionStage(listOffsetResultInfoFuture.toCompletionStage).map(ListOffsetsResultInfo(_))
      )
    }.toMap)

    /**
     * List Consumer Group offsets for the specified partitions.
     */
    override def listConsumerGroupOffsets(
      groupId: String,
      options: Option[ListConsumerGroupOffsetsOptions] = None
    ): Task[Map[TopicPartition, OffsetAndMetadata]] =
      fromKafkaFuture {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.listConsumerGroupOffsets(groupId))(opts =>
              adminClient.listConsumerGroupOffsets(groupId, opts.asJava)
            )
            .partitionsToOffsetAndMetadata()
        )
      }
        .map(_.asScala.filterNot { case (_, om) => om eq null }.toMap.bimap(TopicPartition(_), OffsetAndMetadata(_)))

    /**
     * List the consumer group offsets available in the cluster for the specified consumer groups.
     */
    override def listConsumerGroupOffsets(
      groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]
    ): Task[Map[TopicPartition, OffsetAndMetadata]] =
      fromKafkaFuture {
        ZIO.attemptBlocking(
          adminClient
            .listConsumerGroupOffsets(groupSpecs.map { case (groupId, offsetsSpec) =>
              (groupId, offsetsSpec.asJava)
            }.asJava)
            .partitionsToOffsetAndMetadata()
        )
      }
        .map(_.asScala.filter { case (_, om) => om ne null }.toMap.bimap(TopicPartition(_), OffsetAndMetadata(_)))

    override def listConsumerGroupOffsets(
      groupSpecs: Map[String, ListConsumerGroupOffsetsSpec],
      options: ListConsumerGroupOffsetsOptions
    ): Task[Map[TopicPartition, OffsetAndMetadata]] =
      fromKafkaFuture {
        ZIO.attemptBlocking(
          adminClient
            .listConsumerGroupOffsets(
              groupSpecs.map { case (groupId, offsetsSpec) => (groupId, offsetsSpec.asJava) }.asJava,
              options.asJava
            )
            .partitionsToOffsetAndMetadata()
        )
      }
        .map(_.asScala.filter { case (_, om) => om ne null }.toMap.bimap(TopicPartition(_), OffsetAndMetadata(_)))

    /**
     * Alter offsets for the specified partitions and consumer group.
     */
    override def alterConsumerGroupOffsets(
      groupId: String,
      offsets: Map[TopicPartition, OffsetAndMetadata],
      options: Option[AlterConsumerGroupOffsetsOptions] = None
    ): Task[Unit] = {
      val asJava = offsets.bimap(_.asJava, _.asJava).asJava
      fromKafkaFutureVoid {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.alterConsumerGroupOffsets(groupId, asJava))(opts =>
              adminClient.alterConsumerGroupOffsets(groupId, asJava, opts.asJava)
            )
            .all()
        )
      }
    }

    /**
     * Retrieves metrics for the underlying AdminClient
     */
    override def metrics: Task[Map[MetricName, Metric]] =
      ZIO.attemptBlocking(
        adminClient.metrics().asScala.toMap.map { case (metricName, metric) =>
          (MetricName(metricName), Metric(metric))
        }
      )

    /**
     * List the consumer groups in the cluster.
     */
    override def listConsumerGroups(
      options: Option[ListConsumerGroupsOptions] = None
    ): Task[List[ConsumerGroupListing]] =
      fromKafkaFuture {
        ZIO.attemptBlocking(
          options
            .fold(adminClient.listConsumerGroups())(opts => adminClient.listConsumerGroups(opts.asJava))
            .all()
        )
      }.map(_.asScala.map(ConsumerGroupListing(_)).toList)

    /**
     * Describe the specified consumer groups.
     */
    override def describeConsumerGroups(groupIds: String*): Task[Map[String, ConsumerGroupDescription]] =
      describeConsumerGroups(groupIds.toList, options = None)

    /**
     * Describe the specified consumer groups.
     */
    override def describeConsumerGroups(
      groupIds: List[String],
      options: Option[DescribeConsumerGroupsOptions]
    ): Task[Map[String, ConsumerGroupDescription]] =
      fromKafkaFuture(
        ZIO.attemptBlocking(
          options
            .fold(adminClient.describeConsumerGroups(groupIds.asJavaCollection))(opts =>
              adminClient.describeConsumerGroups(groupIds.asJavaCollection, opts.asJava)
            )
            .all
        )
      ).map(_.asScala.map { case (k, v) => k -> ConsumerGroupDescription(v) }.toMap)

    /**
     * Remove the specified members from a consumer group.
     */
    override def removeMembersFromConsumerGroup(groupId: String, membersToRemove: Set[String]): Task[Unit] = {
      val options = new RemoveMembersFromConsumerGroupOptions(
        membersToRemove.map(new MemberToRemove(_)).asJavaCollection
      )
      fromKafkaFuture(
        ZIO.attemptBlocking(
          adminClient.removeMembersFromConsumerGroup(groupId, options).all()
        )
      ).unit
    }

    override def describeLogDirs(
      brokersId: Iterable[Int]
    ): ZIO[Any, Throwable, Map[Int, Map[String, LogDirDescription]]] =
      fromKafkaFuture(
        ZIO.attemptBlocking(
          adminClient.describeLogDirs(brokersId.map(Int.box).asJavaCollection).allDescriptions()
        )
      ).map(
        _.asScala.toMap.bimap(_.intValue, _.asScala.toMap.bimap(identity, LogDirDescription(_)))
      )

    /**
     * Describe the log directories of the specified brokers async
     */
    override def describeLogDirsAsync(
      brokersId: Iterable[Int]
    ): ZIO[Any, Throwable, Map[Int, Task[Map[String, LogDirDescription]]]] =
      ZIO
        .attemptBlocking(
          adminClient.describeLogDirs(brokersId.map(Int.box).asJavaCollection).descriptions()
        )
        .map(
          _.asScala.view.map { case (brokerId, descriptionsFuture) =>
            (
              brokerId.intValue(),
              ZIO
                .fromCompletionStage(descriptionsFuture.toCompletionStage)
                .map(_.asScala.toMap.map { case (k, v) => (k, LogDirDescription(v)) })
            )
          }.toMap
        )

    override def incrementalAlterConfigs(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      options: AlterConfigsOptions
    ): Task[Unit] =
      fromKafkaFutureVoid(
        ZIO
          .attemptBlocking(
            adminClient
              .incrementalAlterConfigs(
                configs.map { case (configResource, alterConfigOps) =>
                  (configResource.asJava, alterConfigOps.map(_.asJava).asJavaCollection)
                }.asJava,
                options.asJava
              )
              .all()
          )
      )

    override def incrementalAlterConfigsAsync(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      options: AlterConfigsOptions
    ): Task[Map[ConfigResource, Task[Unit]]] =
      ZIO
        .attemptBlocking(
          adminClient
            .incrementalAlterConfigs(
              configs.map { case (configResource, alterConfigOps) =>
                (configResource.asJava, alterConfigOps.map(_.asJava).asJavaCollection)
              }.asJava,
              options.asJava
            )
            .values()
        )
        .map(_.asScala.map { case (configResource, kf) =>
          (ConfigResource(configResource), ZIO.fromCompletionStage(kf.toCompletionStage).unit)
        }.toMap)

    @nowarn("msg=deprecated")
    override def alterConfigs(configs: Map[ConfigResource, KafkaConfig], options: AlterConfigsOptions): Task[Unit] =
      fromKafkaFutureVoid(
        ZIO
          .attemptBlocking(
            adminClient
              .alterConfigs(
                configs.map { case (configResource, kafkaConfig) =>
                  (configResource.asJava, kafkaConfig.asJava)
                }.asJava,
                options.asJava
              )
              .all()
          )
      )

    @nowarn("msg=deprecated")
    override def alterConfigsAsync(
      configs: Map[ConfigResource, KafkaConfig],
      options: AlterConfigsOptions
    ): Task[Map[ConfigResource, Task[Unit]]] =
      ZIO
        .attemptBlocking(
          adminClient
            .alterConfigs(
              configs.map { case (configResource, kafkaConfig) =>
                (configResource.asJava, kafkaConfig.asJava)
              }.asJava,
              options.asJava
            )
            .values()
        )
        .map(_.asScala.map { case (configResource, kf) =>
          (ConfigResource(configResource), ZIO.fromCompletionStage(kf.toCompletionStage).unit)
        }.toMap)

    override def describeAcls(
      filter: AclBindingFilter,
      options: Option[DescribeAclOptions]
    ): Task[Set[AclBinding]] =
      fromKafkaFuture(
        ZIO
          .attemptBlocking(
            options
              .fold(adminClient.describeAcls(filter.asJava))(opt => adminClient.describeAcls(filter.asJava, opt.asJava))
              .values()
          )
      ).map(_.asScala.view.map(AclBinding(_)).toSet)

    override def createAcls(acls: Set[AclBinding], options: Option[CreateAclOptions]): Task[Unit] =
      fromKafkaFutureVoid(
        ZIO
          .attemptBlocking(
            options
              .fold(adminClient.createAcls(acls.map(_.asJava).asJava))(opt =>
                adminClient.createAcls(acls.map(_.asJava).asJava, opt.asJava)
              )
              .all()
          )
      )

    override def createAclsAsync(
      acls: Set[AclBinding],
      options: Option[CreateAclOptions]
    ): Task[Map[AclBinding, Task[Unit]]] =
      ZIO
        .attemptBlocking(
          options
            .fold(adminClient.createAcls(acls.map(_.asJava).asJava))(opt =>
              adminClient.createAcls(acls.map(_.asJava).asJava, opt.asJava)
            )
            .values()
        )
        .map(_.asScala.view.map { case (k, v) =>
          (AclBinding(k), ZIO.fromCompletionStage(v.toCompletionStage).unit)
        }.toMap)

    override def deleteAcls(filters: Set[AclBindingFilter], options: Option[DeleteAclsOptions]): Task[Set[AclBinding]] =
      fromKafkaFuture(
        ZIO
          .attemptBlocking(
            options
              .fold(adminClient.deleteAcls(filters.map(_.asJava).asJava))(opt =>
                adminClient.deleteAcls(filters.map(_.asJava).asJava, opt.asJava)
              )
              .all()
          )
      ).map(_.asScala.view.map(AclBinding(_)).toSet)

    override def deleteAclsAsync(
      filters: Set[AclBindingFilter],
      options: Option[DeleteAclsOptions]
    ): Task[Map[AclBindingFilter, Task[Map[AclBinding, Option[Throwable]]]]] =
      ZIO
        .attemptBlocking(
          options
            .fold(adminClient.deleteAcls(filters.map(_.asJava).asJava))(opt =>
              adminClient.deleteAcls(filters.map(_.asJava).asJava, opt.asJava)
            )
            .values()
        )
        .map(_.asScala.view.map { case (k, v) =>
          (
            AclBindingFilter(k),
            ZIO
              .fromCompletionStage(v.toCompletionStage)
              .map(
                _.values().asScala.view.map { filterRes =>
                  // FilterResult.binding() is claimed to be nullable but in fact it is not: see DeleteAclsResponse.aclBinding
                  AclBinding(filterRes.binding()) -> Option(filterRes.exception())
                }.toMap
              )
          )
        }.toMap)

  }

  val live: ZLayer[AdminClientSettings, Throwable, AdminClient] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[AdminClientSettings]
        admin    <- make(settings)
      } yield admin
    }

  def fromKafkaFuture[R, T](kfv: RIO[R, KafkaFuture[T]]): RIO[R, T] =
    kfv.flatMap(f => ZIO.fromCompletionStage(f.toCompletionStage))

  def fromKafkaFutureVoid[R](kfv: RIO[R, KafkaFuture[Void]]): RIO[R, Unit] =
    fromKafkaFuture(kfv).unit

  final case class ConfigResource(`type`: ConfigResourceType, name: String) {
    lazy val asJava = new JConfigResource(`type`.asJava, name)
  }

  object ConfigResource {
    def apply(jcr: JConfigResource): ConfigResource =
      ConfigResource(`type` = ConfigResourceType(jcrt = jcr.`type`()), name = jcr.name())
  }

  trait ConfigResourceType {
    def asJava: JConfigResource.Type
  }

  object ConfigResourceType {
    case object BrokerLogger extends ConfigResourceType {
      lazy val asJava = JConfigResource.Type.BROKER_LOGGER
    }

    case object Broker extends ConfigResourceType {
      lazy val asJava = JConfigResource.Type.BROKER
    }

    case object Topic extends ConfigResourceType {
      lazy val asJava = JConfigResource.Type.TOPIC
    }

    case object Unknown extends ConfigResourceType {
      lazy val asJava = JConfigResource.Type.UNKNOWN
    }

    def apply(jcrt: JConfigResource.Type): ConfigResourceType = jcrt match {
      case JConfigResource.Type.BROKER_LOGGER => BrokerLogger
      case JConfigResource.Type.BROKER        => Broker
      case JConfigResource.Type.TOPIC         => Topic
      case JConfigResource.Type.UNKNOWN       => Unknown
    }
  }

  sealed trait ConsumerGroupState {
    def asJava: JConsumerGroupState
  }

  object ConsumerGroupState {
    case object Unknown extends ConsumerGroupState {
      override def asJava: JConsumerGroupState = JConsumerGroupState.UNKNOWN
    }

    case object PreparingRebalance extends ConsumerGroupState {
      override def asJava: JConsumerGroupState = JConsumerGroupState.PREPARING_REBALANCE
    }

    case object CompletingRebalance extends ConsumerGroupState {
      override def asJava: JConsumerGroupState = JConsumerGroupState.COMPLETING_REBALANCE
    }

    case object Stable extends ConsumerGroupState {
      override def asJava: JConsumerGroupState = JConsumerGroupState.STABLE
    }

    case object Dead extends ConsumerGroupState {
      override def asJava: JConsumerGroupState = JConsumerGroupState.DEAD
    }

    case object Empty extends ConsumerGroupState {
      override def asJava: JConsumerGroupState = JConsumerGroupState.EMPTY
    }

    def apply(state: JConsumerGroupState): ConsumerGroupState = state match {
      case JConsumerGroupState.UNKNOWN              => ConsumerGroupState.Unknown
      case JConsumerGroupState.PREPARING_REBALANCE  => ConsumerGroupState.PreparingRebalance
      case JConsumerGroupState.COMPLETING_REBALANCE => ConsumerGroupState.CompletingRebalance
      case JConsumerGroupState.STABLE               => ConsumerGroupState.Stable
      case JConsumerGroupState.DEAD                 => ConsumerGroupState.Dead
      case JConsumerGroupState.EMPTY                => ConsumerGroupState.Empty
    }
  }

  final case class MemberDescription(
    consumerId: String,
    groupInstanceId: Option[String],
    clientId: String,
    host: String,
    assignment: Set[TopicPartition]
  )

  object MemberDescription {
    def apply(desc: JMemberDescription): MemberDescription =
      MemberDescription(
        consumerId = desc.consumerId,
        groupInstanceId = desc.groupInstanceId.toScala,
        clientId = desc.clientId(),
        host = desc.host(),
        assignment = desc.assignment.topicPartitions().asScala.map(TopicPartition.apply).toSet
      )
  }

  final case class ConsumerGroupDescription(
    groupId: String,
    isSimpleConsumerGroup: Boolean,
    members: List[MemberDescription],
    partitionAssignor: String,
    state: ConsumerGroupState,
    coordinator: Option[Node],
    authorizedOperations: Set[AclOperation]
  )

  object ConsumerGroupDescription {

    def apply(description: JConsumerGroupDescription): ConsumerGroupDescription =
      ConsumerGroupDescription(
        groupId = description.groupId,
        isSimpleConsumerGroup = description.isSimpleConsumerGroup,
        members = description.members.asScala.map(MemberDescription.apply).toList,
        partitionAssignor = description.partitionAssignor,
        state = ConsumerGroupState(description.state),
        coordinator = Node(description.coordinator()),
        authorizedOperations = Option(description.authorizedOperations())
          .fold(Set.empty[AclOperation])(_.asScala.map(AclOperation.apply).toSet)
      )
  }

  final case class CreatePartitionsOptions(
    validateOnly: Boolean = false,
    retryOnQuotaViolation: Boolean = true,
    timeout: Option[Duration]
  ) {
    def asJava: JCreatePartitionsOptions = {
      val opts = new JCreatePartitionsOptions()
        .validateOnly(validateOnly)
        .retryOnQuotaViolation(retryOnQuotaViolation)

      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class CreateTopicsOptions(validateOnly: Boolean, timeout: Option[Duration]) {
    def asJava: JCreateTopicsOptions = {
      val opts = new JCreateTopicsOptions().validateOnly(validateOnly)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class DeleteConsumerGroupOptions(timeout: Option[Duration]) {
    def asJava: JDeleteConsumerGroupsOptions = {
      val opts = new JDeleteConsumerGroupsOptions()
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class DeleteTopicsOptions(retryOnQuotaViolation: Boolean = true, timeout: Option[Duration]) {
    def asJava: JDeleteTopicsOptions = {
      val opts = new JDeleteTopicsOptions().retryOnQuotaViolation(retryOnQuotaViolation)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class ListTopicsOptions(listInternal: Boolean = false, timeout: Option[Duration]) {
    def asJava: JListTopicsOptions = {
      val opts = new JListTopicsOptions().listInternal(listInternal)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class DescribeTopicsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {
    def asJava: JDescribeTopicsOptions = {
      val opts = new JDescribeTopicsOptions().includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class DescribeConfigsOptions(
    includeSynonyms: Boolean = false,
    includeDocumentation: Boolean = false,
    timeout: Option[Duration]
  ) {
    def asJava: JDescribeConfigsOptions = {
      val opts = new JDescribeConfigsOptions()
        .includeSynonyms(includeSynonyms)
        .includeDocumentation(includeDocumentation)

      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class DescribeClusterOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {
    lazy val asJava: JDescribeClusterOptions = {
      val opts = new JDescribeClusterOptions().includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class DescribeConsumerGroupsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration]) {
    lazy val asJava: JDescribeConsumerGroupsOptions = {
      val jOpts = new JDescribeConsumerGroupsOptions()
        .includeAuthorizedOperations(includeAuthorizedOperations)
      timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class AlterConfigsOptions(validateOnly: Boolean = false, timeout: Option[Duration] = None) {
    lazy val asJava: JAlterConfigsOptions = {
      val jOpts = new JAlterConfigsOptions().validateOnly(validateOnly)
      timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class AlterConfigOp(configEntry: ConfigEntry, opType: AlterConfigOpType) {
    lazy val asJava: JAlterConfigOp = new JAlterConfigOp(configEntry, opType.asJava)
  }

  sealed trait AlterConfigOpType {
    def asJava: JAlterConfigOp.OpType
  }

  object AlterConfigOpType {
    case object Set extends AlterConfigOpType {
      lazy val asJava = JAlterConfigOp.OpType.SET
    }

    case object Delete extends AlterConfigOpType {
      lazy val asJava = JAlterConfigOp.OpType.DELETE
    }

    case object Append extends AlterConfigOpType {
      lazy val asJava = JAlterConfigOp.OpType.APPEND
    }

    case object Substract extends AlterConfigOpType {
      lazy val asJava = JAlterConfigOp.OpType.SUBTRACT
    }
  }

  final case class MetricName(name: String, group: String, description: String, tags: Map[String, String])
  object MetricName {
    def apply(jmn: JMetricName): MetricName =
      MetricName(
        name = jmn.name(),
        group = jmn.group(),
        description = jmn.description(),
        tags = jmn.tags().asScala.toMap
      )
  }

  final case class Metric(name: MetricName, metricValue: AnyRef)
  object Metric {
    def apply(jm: JMetric): Metric = Metric(name = MetricName(jmn = jm.metricName()), metricValue = jm.metricValue())
  }

  final case class NewTopic(
    name: String,
    numPartitions: Int,
    replicationFactor: Short,
    configs: Map[String, String] = Map.empty
  ) {
    def asJava: JNewTopic = {
      val jn = new JNewTopic(name, numPartitions, replicationFactor)

      if (configs.nonEmpty) {
        jn.configs(configs.asJava)
      }

      jn
    }
  }

  final case class NewPartitions(
    totalCount: Int,
    newAssignments: List[List[Int]] = Nil
  ) {
    def asJava: JNewPartitions =
      if (newAssignments.nonEmpty)
        JNewPartitions.increaseTo(totalCount, newAssignments.map(_.map(Int.box).asJava).asJava)
      else JNewPartitions.increaseTo(totalCount)
  }

  /**
   * @param id
   *   >= 0
   * @param host
   *   can't be empty string if present
   * @param port
   *   can't be negative if present
   */
  final case class Node(id: Int, host: Option[String], port: Option[Int], rack: Option[String] = None) {
    lazy val asJava: JNode = new JNode(id, host.getOrElse(""), port.getOrElse(-1), rack.orNull)
  }
  object Node {
    def apply(jNode: JNode): Option[Node] =
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

  final case class TopicDescription(
    name: String,
    internal: Boolean,
    partitions: List[TopicPartitionInfo],
    authorizedOperations: Option[Set[AclOperation]]
  )

  object TopicDescription {
    def apply(jt: JTopicDescription): Try[TopicDescription] = {
      val authorizedOperations = Option(jt.authorizedOperations).map(_.asScala.toSet).map(_.map(AclOperation.apply))

      jt.partitions.asScala.toList.forEach(TopicPartitionInfo.apply).map { partitions =>
        TopicDescription(
          name = jt.name,
          internal = jt.isInternal,
          partitions = partitions,
          authorizedOperations = authorizedOperations
        )
      }
    }
  }

  final case class TopicPartitionInfo(partition: Int, leader: Option[Node], replicas: List[Node], isr: List[Node]) {
    lazy val asJava =
      new JTopicPartitionInfo(
        partition,
        leader.map(_.asJava).getOrElse(JNode.noNode()),
        replicas.map(_.asJava).asJava,
        isr.map(_.asJava).asJava
      )
  }

  object TopicPartitionInfo {
    def apply(jtpi: JTopicPartitionInfo): Try[TopicPartitionInfo] = {
      val replicas: Try[List[Node]] =
        jtpi
          .replicas()
          .asScala
          .toList
          .forEach { jNode =>
            Node(jNode) match {
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
            Node(jNode) match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode node not expected among topic in sync replicas"))
            }
          }

      for {
        replicas       <- replicas
        inSyncReplicas <- inSyncReplicas
      } yield TopicPartitionInfo(
        partition = jtpi.partition(),
        leader = Node(jtpi.leader()),
        replicas = replicas,
        isr = inSyncReplicas
      )
    }
  }

  final case class TopicListing(name: String, topicId: Uuid, isInternal: Boolean) {
    def asJava: JTopicListing = new JTopicListing(name, topicId, isInternal)
  }

  object TopicListing {
    def apply(jtl: JTopicListing): TopicListing = TopicListing(jtl.name(), jtl.topicId(), jtl.isInternal)
  }

  final case class TopicPartition(
    name: String,
    partition: Int
  ) {
    def asJava: JTopicPartition = new JTopicPartition(name, partition)
  }

  object TopicPartition {
    def apply(tp: JTopicPartition): TopicPartition = new TopicPartition(name = tp.topic(), partition = tp.partition())
  }

  sealed abstract class OffsetSpec {
    def asJava: JOffsetSpec
  }

  object OffsetSpec {
    case object EarliestSpec extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.earliest()
    }

    case object LatestSpec extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.latest()
    }

    final case class TimestampSpec(timestamp: Long) extends OffsetSpec {
      override def asJava: JOffsetSpec = JOffsetSpec.forTimestamp(timestamp)
    }
  }

  sealed abstract class IsolationLevel {
    def asJava: JIsolationLevel
  }

  object IsolationLevel {
    case object ReadUncommitted extends IsolationLevel {
      override def asJava: JIsolationLevel = JIsolationLevel.READ_UNCOMMITTED
    }

    case object ReadCommitted extends IsolationLevel {
      override def asJava: JIsolationLevel = JIsolationLevel.READ_COMMITTED
    }
  }

  final case class DeleteRecordsOptions(timeout: Option[Duration]) {
    def asJava: JDeleteRecordsOptions = {
      val deleteRecordsOpt = new JDeleteRecordsOptions()
      timeout.fold(deleteRecordsOpt)(timeout => deleteRecordsOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class ListOffsetsOptions(
    isolationLevel: IsolationLevel = IsolationLevel.ReadUncommitted,
    timeout: Option[Duration]
  ) {
    def asJava: JListOffsetsOptions = {
      val offsetOpt = new JListOffsetsOptions(isolationLevel.asJava)
      timeout.fold(offsetOpt)(timeout => offsetOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class ListOffsetsResultInfo(
    offset: Long,
    timestamp: Long,
    leaderEpoch: Option[Int]
  )
  object ListOffsetsResultInfo {
    def apply(lo: JListOffsetsResultInfo): ListOffsetsResultInfo =
      ListOffsetsResultInfo(lo.offset(), lo.timestamp(), lo.leaderEpoch().toScala.map(_.toInt))
  }

  @nowarn("msg=deprecated")
  final case class ListConsumerGroupOffsetsOptions(partitions: Chunk[TopicPartition], requireStable: Boolean) {
    def asJava: JListConsumerGroupOffsetsOptions = {
      val opts = new JListConsumerGroupOffsetsOptions()
      opts.requireStable(requireStable)
      if (partitions.isEmpty) opts else opts.topicPartitions(partitions.map(_.asJava).asJava)
    }
  }

  object ListConsumerGroupOffsetsOptions {
    @deprecated("Use the listConsumerGroupOffsets overload with ListConsumerGroupOffsetsSpec", since = "2.0.5")
    def apply(partitions: Chunk[TopicPartition], requireStable: Boolean): ListConsumerGroupOffsetsOptions =
      new ListConsumerGroupOffsetsOptions(partitions, requireStable)

    @deprecated("Use the listConsumerGroupOffsets overload with ListConsumerGroupOffsetsSpec", since = "2.0.5")
    def apply(partitions: Chunk[TopicPartition]): ListConsumerGroupOffsetsOptions =
      new ListConsumerGroupOffsetsOptions(partitions, requireStable = false)

    def apply(requireStable: Boolean): ListConsumerGroupOffsetsOptions =
      new ListConsumerGroupOffsetsOptions(Chunk.empty, requireStable)
  }

  final case class ListConsumerGroupOffsetsSpec(partitions: Chunk[TopicPartition]) {
    def asJava = {
      val opts = new JListConsumerGroupOffsetsSpec
      opts.topicPartitions(partitions.map(_.asJava).asJava)
      opts
    }
  }

  final case class OffsetAndMetadata(
    offset: Long,
    leaderEpoch: Option[Int] = None,
    metadata: Option[String] = None
  ) {
    def asJava: JOffsetAndMetadata = new JOffsetAndMetadata(offset, leaderEpoch.map(Int.box).toJava, metadata.orNull)
  }
  object OffsetAndMetadata {
    def apply(om: JOffsetAndMetadata): OffsetAndMetadata =
      OffsetAndMetadata(
        offset = om.offset(),
        leaderEpoch = om.leaderEpoch().toScala.map(_.toInt),
        metadata = Some(om.metadata())
      )
  }

  final case class AlterConsumerGroupOffsetsOptions(timeout: Option[Duration]) {
    def asJava: JAlterConsumerGroupOffsetsOptions = {
      val options = new JAlterConsumerGroupOffsetsOptions()
      timeout.fold(options)(timeout => options.timeoutMs(timeout.toMillis.toInt))
    }
  }

  final case class ListConsumerGroupsOptions(states: Set[ConsumerGroupState]) {
    def asJava: JListConsumerGroupsOptions = new JListConsumerGroupsOptions().inStates(states.map(_.asJava).asJava)
  }

  final case class ConsumerGroupListing(groupId: String, isSimple: Boolean, state: Option[ConsumerGroupState])

  object ConsumerGroupListing {
    def apply(cg: JConsumerGroupListing): ConsumerGroupListing =
      ConsumerGroupListing(
        groupId = cg.groupId(),
        isSimple = cg.isSimpleConsumerGroup,
        state = cg.state().toScala.map(ConsumerGroupState(_))
      )
  }

  final case class KafkaConfig(entries: Map[String, ConfigEntry]) {
    def asJava: JConfig = new JConfig(entries.values.asJavaCollection)
  }
  object KafkaConfig {
    def apply(jConfig: JConfig): KafkaConfig =
      KafkaConfig(entries = jConfig.entries().asScala.map(e => e.name() -> e).toMap)
  }

  final case class LogDirDescription(error: ApiException, replicaInfos: Map[TopicPartition, ReplicaInfo])
  object LogDirDescription {
    def apply(ld: JLogDirDescription): LogDirDescription =
      LogDirDescription(
        error = ld.error(),
        replicaInfos = ld.replicaInfos().asScala.toMap.bimap(TopicPartition(_), ReplicaInfo(_))
      )
  }

  final case class ReplicaInfo(size: Long, offsetLag: Long, isFuture: Boolean)
  object ReplicaInfo {
    def apply(ri: JReplicaInfo): ReplicaInfo =
      ReplicaInfo(size = ri.size(), offsetLag = ri.offsetLag(), isFuture = ri.isFuture)
  }

  def make(settings: AdminClientSettings): ZIO[Scope, Throwable, AdminClient] =
    fromManagedJavaClient(javaClientFromSettings(settings))

  def fromJavaClient(javaClient: JAdmin): URIO[Any, AdminClient] =
    ZIO.succeed(new LiveAdminClient(javaClient))

  def fromManagedJavaClient[R, E](
    managedJavaClient: ZIO[R & Scope, E, JAdmin]
  ): ZIO[R & Scope, E, AdminClient] =
    managedJavaClient.flatMap { javaClient =>
      fromJavaClient(javaClient)
    }

  def javaClientFromSettings(settings: AdminClientSettings): ZIO[Scope, Throwable, JAdmin] =
    ZIO.acquireRelease(ZIO.attempt(JAdmin.create(settings.driverSettings.asJava)))(client =>
      ZIO.succeed(client.close(settings.closeTimeout))
    )

  implicit final class MapOps[K1, V1](private val v: Map[K1, V1]) extends AnyVal {
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): Map[K2, V2] = v.map(kv => fk(kv._1) -> fv(kv._2))
  }

  implicit final class OptionalOps[T](private val v: Optional[T]) extends AnyVal {
    def toScala: Option[T] = if (v.isPresent) Some(v.get()) else None
  }

  implicit final class OptionOps[T](private val v: Option[T]) extends AnyVal {
    def toJava: Optional[T] = v.fold(Optional.empty[T])(Optional.of)
  }

  implicit final class ListOps[A](private val list: List[A]) extends AnyVal {
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
