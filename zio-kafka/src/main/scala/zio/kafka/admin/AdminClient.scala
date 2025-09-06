package zio.kafka.admin

import org.apache.kafka.clients.admin.{
  Admin => JAdmin,
  ConfigEntry,
  DeleteAclsOptions => _,
  DescribeClusterResult,
  ListGroupsOptions => JListGroupsOptions,
  MemberToRemove,
  RecordsToDelete,
  RemoveMembersFromConsumerGroupOptions
}
import org.apache.kafka.common.errors.ApiException
import org.apache.kafka.common.{ KafkaFuture, Uuid }
import zio._
import zio.kafka.admin.acl._
import zio.kafka.admin.internal.JavaConverters._
import zio.kafka.utils.SslHelper

import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success }

/**
 * Admin client, can be used to create, list, delete topics, consumer groups, etc.
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
    options: Option[DeleteConsumerGroupsOptions] = None
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
   */
  def removeMembersFromConsumerGroup(groupId: String, membersToRemove: Set[String]): Task[Unit]

  /**
   * Remove all members from a consumer group.
   */
  def removeMembersFromConsumerGroup(groupId: String): Task[Unit]

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

    // workaround for https://issues.apache.org/jira/browse/KAFKA-18818
    private val kafka18818Workaround: ZIO[Any, Nothing, Unit] =
      ZIO.sleep(550.millis).unit

    /**
     * Create multiple topics.
     */
    override def createTopics(
      newTopics: Iterable[NewTopic],
      options: Option[CreateTopicsOptions] = None
    ): Task[Unit] = {
      val asJava = newTopics.map(_.asJava).asJavaCollection

      fromKafkaFutureVoid {
        ZIO.attempt(
          options
            .fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts.asJava))
            .all()
        ) <* kafka18818Workaround
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
      options: Option[DeleteConsumerGroupsOptions]
    ): Task[Unit] = {
      val asJava = groupIds.asJavaCollection
      fromKafkaFutureVoid {
        ZIO.attempt(
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
        ZIO.attempt(
          options
            .fold(adminClient.deleteTopics(asJava))(opts => adminClient.deleteTopics(asJava, opts.asJava))
            .all()
        ) <* kafka18818Workaround
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
        ZIO.attempt(
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
        ZIO.attempt(
          listTopicsOptions
            .fold(adminClient.listTopics())(opts => adminClient.listTopics(opts.asJava))
            .namesToListings()
        )
      }.map(_.asScala.map { case (k, v) => k -> v.asScala }.toMap)

    /**
     * Describe the specified topics.
     */
    override def describeTopics(
      topicNames: Iterable[String],
      options: Option[DescribeTopicsOptions] = None
    ): Task[Map[String, TopicDescription]] = {
      val asJava = topicNames.asJavaCollection
      fromKafkaFuture {
        ZIO.attempt(
          options
            .fold(adminClient.describeTopics(asJava))(opts => adminClient.describeTopics(asJava, opts.asJava))
            .allTopicNames()
        )
      }.flatMap { jTopicDescriptions =>
        ZIO.fromTry {
          jTopicDescriptions.asScala.toList.forEach { case (k, v) =>
            JTopicDescriptionAsScala(v).asScala.map(k -> _)
          }
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
        ZIO.attempt(
          options
            .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
            .all()
        )
      }.map {
        _.asScala.view.map { case (configResource, config) =>
          (configResource.asScala, config.asScala)
        }.toMap
      }
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
        .attempt(
          options
            .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
            .values()
        )
        .map {
          _.asScala.view.map { case (configResource, configFuture) =>
            (
              configResource.asScala,
              ZIO
                .fromCompletionStage(configFuture.toCompletionStage)
                .map(config => config.asScala)
            )

          }.toMap
        }
    }

    private def describeCluster(options: Option[DescribeClusterOptions]): Task[DescribeClusterResult] =
      ZIO.attempt(
        options.fold(adminClient.describeCluster())(opts => adminClient.describeCluster(opts.asJava))
      )

    /**
     * Get the cluster nodes.
     */
    override def describeClusterNodes(options: Option[DescribeClusterOptions] = None): Task[List[Node]] =
      fromKafkaFuture(
        describeCluster(options).map(_.nodes())
      ).flatMap { nodes =>
        ZIO.fromTry {
          nodes.asScala.toList.forEach { jNode =>
            jNode.asScala match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode not expected when listing cluster nodes"))
            }
          }
        }
      }

    /**
     * Get the cluster controller.
     */
    override def describeClusterController(options: Option[DescribeClusterOptions] = None): Task[Option[Node]] =
      fromKafkaFuture(describeCluster(options).map(_.controller())).map(_.asScala)

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
        aclOperations = lst.map(_.asScala)
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
        ZIO.attempt(
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
        ZIO.attempt(
          options
            .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
            .all()
        )
      }
    }.map(_.asScala.bimap(_.asScala, _.asScala).toMap)

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
      ZIO.attempt {
        val listOffsetsResult = options
          .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
        topicPartitionsAsJava.map(tp => tp -> listOffsetsResult.partitionResult(tp))
      }
    }.map {
      _.view.map { case (topicPartition, listOffsetResultInfoFuture) =>
        (
          topicPartition.asScala,
          ZIO.fromCompletionStage(listOffsetResultInfoFuture.toCompletionStage).map(_.asScala)
        )
      }.toMap
    }

    /**
     * List Consumer Group offsets for the specified partitions.
     */
    override def listConsumerGroupOffsets(
      groupId: String,
      options: Option[ListConsumerGroupOffsetsOptions] = None
    ): Task[Map[TopicPartition, OffsetAndMetadata]] =
      fromKafkaFuture {
        ZIO.attempt(
          options
            .fold(adminClient.listConsumerGroupOffsets(groupId))(opts =>
              adminClient.listConsumerGroupOffsets(groupId, opts.asJava)
            )
            .partitionsToOffsetAndMetadata()
        )
      }.map {
        _.asScala.filter { case (_, om) => om ne null }
          .bimap(_.asScala, _.asScala)
          .toMap
      }

    /**
     * List the consumer group offsets available in the cluster for the specified consumer groups.
     */
    override def listConsumerGroupOffsets(
      groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]
    ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]] =
      fromKafkaFuture {
        ZIO.attempt(
          adminClient
            .listConsumerGroupOffsets(groupSpecs.map { case (groupId, offsetsSpec) =>
              (groupId, offsetsSpec.asJava)
            }.asJava)
            .all()
        )
      }.map {
        _.asScala.map { case (groupId, offsets) =>
          groupId ->
            offsets.asScala.filter { case (_, om) => om ne null }
              .bimap(_.asScala, _.asScala)
              .toMap
        }.toMap
      }

    override def listConsumerGroupOffsets(
      groupSpecs: Map[String, ListConsumerGroupOffsetsSpec],
      options: ListConsumerGroupOffsetsOptions
    ): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]] =
      fromKafkaFuture {
        ZIO.attempt(
          adminClient
            .listConsumerGroupOffsets(
              groupSpecs.map { case (groupId, offsetsSpec) => (groupId, offsetsSpec.asJava) }.asJava,
              options.asJava
            )
            .all()
        )
      }.map {
        _.asScala.map { case (groupId, offsets) =>
          groupId ->
            offsets.asScala.filter { case (_, om) => om ne null }
              .bimap(_.asScala, _.asScala)
              .toMap
        }.toMap
      }

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
        ZIO.attempt(
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
      ZIO.attempt(
        adminClient.metrics().asScala.toMap.map { case (metricName, metric) =>
          (metricName.asScala, metric.asScala)
        }
      )

    /**
     * List the consumer groups in the cluster.
     */
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
      }.map(_.asScala.map(_.asConsumerGroupListing).toList)

    /**
     * List the groups in the cluster.
     */
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
      }.map(_.asScala.map(_.asScala).toList)

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
        ZIO.attempt(
          options
            .fold(adminClient.describeConsumerGroups(groupIds.asJavaCollection))(opts =>
              adminClient.describeConsumerGroups(groupIds.asJavaCollection, opts.asJava)
            )
            .all
        )
      ).map(_.asScala.map { case (k, v) => k -> v.asScala }.toMap)

    /**
     * Remove the specified members from a consumer group.
     */
    override def removeMembersFromConsumerGroup(groupId: String, membersToRemove: Set[String]): Task[Unit] = {
      val options = new RemoveMembersFromConsumerGroupOptions(
        membersToRemove.map(new MemberToRemove(_)).asJavaCollection
      )
      fromKafkaFuture(
        ZIO.attempt(
          adminClient.removeMembersFromConsumerGroup(groupId, options).all()
        )
      ).unit
    }

    /**
     * Remove all members from a consumer group.
     */
    override def removeMembersFromConsumerGroup(groupId: String): Task[Unit] = {
      val options = new RemoveMembersFromConsumerGroupOptions()
      fromKafkaFuture(
        ZIO.attempt(
          adminClient.removeMembersFromConsumerGroup(groupId, options).all()
        )
      ).unit
    }

    override def describeLogDirs(
      brokersId: Iterable[Int]
    ): ZIO[Any, Throwable, Map[Int, Map[String, LogDirDescription]]] =
      fromKafkaFuture(
        ZIO.attempt(
          adminClient.describeLogDirs(brokersId.map(Int.box).asJavaCollection).allDescriptions()
        )
      ).map {
        _.asScala.bimap(_.intValue, _.asScala.bimap(identity, _.asScala).toMap).toMap
      }

    /**
     * Describe the log directories of the specified brokers async
     */
    override def describeLogDirsAsync(
      brokersId: Iterable[Int]
    ): ZIO[Any, Throwable, Map[Int, Task[Map[String, LogDirDescription]]]] =
      ZIO
        .attempt(
          adminClient.describeLogDirs(brokersId.map(Int.box).asJavaCollection).descriptions()
        )
        .map {
          _.asScala.view.map { case (brokerId, descriptionsFuture) =>
            (
              brokerId.intValue(),
              ZIO
                .fromCompletionStage(descriptionsFuture.toCompletionStage)
                .map(_.asScala.toMap.map { case (k, v) => (k, v.asScala) })
            )
          }.toMap
        }

    override def incrementalAlterConfigs(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      options: AlterConfigsOptions
    ): Task[Unit] =
      fromKafkaFutureVoid(
        ZIO
          .attempt(
            adminClient
              .incrementalAlterConfigs(
                configs.map { case (configResource, alterConfigOps) =>
                  (configResource.asJava, alterConfigOps.map(_.asJava).asJavaCollection)
                }.asJava,
                options.asJava
              )
              .all()
          ) <* kafka18818Workaround
      )

    override def incrementalAlterConfigsAsync(
      configs: Map[ConfigResource, Iterable[AlterConfigOp]],
      options: AlterConfigsOptions
    ): Task[Map[ConfigResource, Task[Unit]]] =
      ZIO
        .attempt(
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
          (configResource.asScala, ZIO.fromCompletionStage(kf.toCompletionStage).unit)
        }.toMap)

    override def describeAcls(
      filter: AclBindingFilter,
      options: Option[DescribeAclOptions]
    ): Task[Set[AclBinding]] =
      fromKafkaFuture(
        ZIO
          .attempt(
            options
              .fold(adminClient.describeAcls(filter.asJava))(opt => adminClient.describeAcls(filter.asJava, opt.asJava))
              .values()
          )
      ).map(_.asScala.view.map(AclBinding(_)).toSet)

    override def createAcls(acls: Set[AclBinding], options: Option[CreateAclOptions]): Task[Unit] =
      fromKafkaFutureVoid(
        ZIO
          .attempt(
            options
              .fold(adminClient.createAcls(acls.map(_.asJava).asJava))(opt =>
                adminClient.createAcls(acls.map(_.asJava).asJava, opt.asJava)
              )
              .all()
          ) <* kafka18818Workaround
      )

    override def createAclsAsync(
      acls: Set[AclBinding],
      options: Option[CreateAclOptions]
    ): Task[Map[AclBinding, Task[Unit]]] =
      ZIO
        .attempt(
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
          .attempt(
            options
              .fold(adminClient.deleteAcls(filters.map(_.asJava).asJava))(opt =>
                adminClient.deleteAcls(filters.map(_.asJava).asJava, opt.asJava)
              )
              .all()
          ) <* kafka18818Workaround
      ).map(_.asScala.view.map(AclBinding(_)).toSet)

    override def deleteAclsAsync(
      filters: Set[AclBindingFilter],
      options: Option[DeleteAclsOptions]
    ): Task[Map[AclBindingFilter, Task[Map[AclBinding, Option[Throwable]]]]] =
      ZIO
        .attempt(
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

  final case class ConfigResource(`type`: ConfigResourceType, name: String)

  sealed trait ConfigResourceType
  object ConfigResourceType {
    case object BrokerLogger  extends ConfigResourceType
    case object Broker        extends ConfigResourceType
    case object Topic         extends ConfigResourceType
    case object Unknown       extends ConfigResourceType
    case object ClientMetrics extends ConfigResourceType
    case object Group         extends ConfigResourceType
  }

  sealed trait GroupState
  object GroupState {
    case object Unknown             extends GroupState
    case object PreparingRebalance  extends GroupState
    case object CompletingRebalance extends GroupState
    case object Stable              extends GroupState
    case object Dead                extends GroupState
    case object Empty               extends GroupState
    case object Assigning           extends GroupState
    case object Reconciling         extends GroupState
    case object NotReady            extends GroupState
  }

  sealed trait GroupType
  object GroupType {
    case object Unknown  extends GroupType
    case object Consumer extends GroupType
    case object Classic  extends GroupType
    case object Share    extends GroupType
    case object Streams  extends GroupType
  }

  final case class MemberDescription(
    consumerId: String,
    groupInstanceId: Option[String],
    clientId: String,
    host: String,
    assignment: Set[TopicPartition]
  )

  final case class ConsumerGroupDescription(
    groupId: String,
    isSimpleConsumerGroup: Boolean,
    members: List[MemberDescription],
    partitionAssignor: String,
    state: GroupState,
    coordinator: Option[Node],
    authorizedOperations: Set[AclOperation]
  )

  final case class CreatePartitionsOptions(
    validateOnly: Boolean = false,
    retryOnQuotaViolation: Boolean = true,
    timeout: Option[Duration]
  )

  final case class CreateTopicsOptions(validateOnly: Boolean, timeout: Option[Duration])
  @deprecated(since = "3.1.0", message = "Use DeleteConsumerGroupsOptions")
  type DeleteConsumerGroupOptions = DeleteConsumerGroupsOptions
  final case class DeleteConsumerGroupsOptions(timeout: Option[Duration])
  final case class DeleteTopicsOptions(retryOnQuotaViolation: Boolean = true, timeout: Option[Duration])
  final case class ListTopicsOptions(listInternal: Boolean = false, timeout: Option[Duration])
  final case class DescribeTopicsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration])
  final case class DescribeConfigsOptions(
    includeSynonyms: Boolean = false,
    includeDocumentation: Boolean = false,
    timeout: Option[Duration]
  )
  final case class DescribeClusterOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration])
  final case class DescribeConsumerGroupsOptions(includeAuthorizedOperations: Boolean, timeout: Option[Duration])
  final case class AlterConfigsOptions(validateOnly: Boolean = false, timeout: Option[Duration] = None)
  final case class AlterConfigOp(configEntry: ConfigEntry, opType: AlterConfigOpType)

  sealed trait AlterConfigOpType
  object AlterConfigOpType {
    case object Set      extends AlterConfigOpType
    case object Delete   extends AlterConfigOpType
    case object Append   extends AlterConfigOpType
    case object Subtract extends AlterConfigOpType
    // noinspection SpellCheckingInspection
    @deprecated(since = "3.1.0", message = "Use Subtract")
    val Substract: Subtract.type = Subtract
  }

  final case class MetricName(name: String, group: String, description: String, tags: Map[String, String])
  final case class Metric(name: MetricName, metricValue: AnyRef)
  final case class NewTopic(
    name: String,
    numPartitions: Int,
    replicationFactor: Short,
    configs: Map[String, String] = Map.empty
  )
  final case class NewPartitions(totalCount: Int, newAssignments: List[List[Int]] = List.empty)

  /**
   * @param id
   *   >= 0
   * @param host
   *   can't be empty string if present
   * @param port
   *   can't be negative if present
   */
  final case class Node(id: Int, host: Option[String], port: Option[Int], rack: Option[String] = None)

  final case class TopicDescription(
    name: String,
    internal: Boolean,
    partitions: List[TopicPartitionInfo],
    authorizedOperations: Option[Set[AclOperation]]
  )

  final case class TopicPartitionInfo(partition: Int, leader: Option[Node], replicas: List[Node], isr: List[Node])
  final case class TopicListing(name: String, topicId: Uuid, isInternal: Boolean)
  final case class TopicPartition(name: String, partition: Int)

  sealed abstract class OffsetSpec
  object OffsetSpec {
    case object EarliestSpec                        extends OffsetSpec
    case object LatestSpec                          extends OffsetSpec
    final case class TimestampSpec(timestamp: Long) extends OffsetSpec
  }

  sealed abstract class IsolationLevel
  object IsolationLevel {
    case object ReadUncommitted extends IsolationLevel
    case object ReadCommitted   extends IsolationLevel
  }

  final case class DeleteRecordsOptions(timeout: Option[Duration])

  final case class ListOffsetsOptions(
    isolationLevel: IsolationLevel = IsolationLevel.ReadUncommitted,
    timeout: Option[Duration]
  )
  final case class ListOffsetsResultInfo(
    offset: Long,
    timestamp: Long,
    leaderEpoch: Option[Int]
  )
  final case class ListConsumerGroupOffsetsOptions(requireStable: Boolean)
  final case class ListConsumerGroupOffsetsSpec(partitions: Chunk[TopicPartition])

  final case class OffsetAndMetadata(
    offset: Long,
    leaderEpoch: Option[Int] = None,
    metadata: Option[String] = None
  )

  final case class AlterConsumerGroupOffsetsOptions(timeout: Option[Duration])
  final case class ListConsumerGroupsOptions(states: Set[GroupState])

  final case class ConsumerGroupListing(groupId: String, isSimple: Boolean, state: Option[GroupState])

  final case class GroupListing(
    groupId: String,
    groupType: Option[GroupType],
    protocol: String,
    groupState: Option[GroupState]
  )

  final case class ListGroupsOptions(
    groupStates: Set[GroupState] = Set.empty,
    groupTypes: Set[GroupType] = Set.empty,
    protocolTypes: Set[String] = Set.empty
  )

  final case class KafkaConfig(entries: Map[String, ConfigEntry])

  final case class LogDirDescription(error: ApiException, replicaInfos: Map[TopicPartition, ReplicaInfo])

  final case class ReplicaInfo(size: Long, offsetLag: Long, isFuture: Boolean)

  def make(settings: AdminClientSettings): ZIO[Scope, Throwable, AdminClient] =
    fromScopedJavaClient(javaClientFromSettings(settings))

  def fromJavaClient(javaClient: JAdmin): URIO[Any, AdminClient] =
    ZIO.succeed(new LiveAdminClient(javaClient))

  def fromScopedJavaClient[R, E](
    scopedJavaClient: ZIO[R & Scope, E, JAdmin]
  ): ZIO[R & Scope, E, AdminClient] =
    scopedJavaClient.flatMap { javaClient =>
      fromJavaClient(javaClient)
    }

  def javaClientFromSettings(settings: AdminClientSettings): ZIO[Scope, Throwable, JAdmin] =
    ZIO.acquireRelease {
      val endpointCheck = SslHelper
        .validateEndpoint(settings.driverSettings)

      endpointCheck *> ZIO.attempt(JAdmin.create(settings.driverSettings.asJava))
    }(client => ZIO.attempt(client.close(settings.closeTimeout)).orDie)

}
