package zio.kafka.admin

import java.util.Optional
import org.apache.kafka.clients.admin.{
  AdminClient => JAdminClient,
  AlterConsumerGroupOffsetsOptions => JAlterConsumerGroupOffsetsOptions,
  Config => JConfig,
  ListOffsetsOptions => JListOffsetsOptions,
  NewPartitions => JNewPartitions,
  NewTopic => JNewTopic,
  OffsetSpec => JOffsetSpec,
  TopicDescription => JTopicDescription,
  TopicListing => JTopicListing,
  CreatePartitionsOptions => JCreatePartitionsOptions,
  DescribeClusterOptions => JDescribeClusterOptions,
  DescribeConfigsOptions => JDescribeConfigsOptions,
  CreateTopicsOptions => JCreateTopicsOptions,
  _
}
import org.apache.kafka.clients.admin.ListOffsetsResult.{ ListOffsetsResultInfo => JListOffsetsResultInfo }
import org.apache.kafka.clients.consumer.{ OffsetAndMetadata => JOffsetAndMetadata }
import org.apache.kafka.common.config.{ ConfigResource => JConfigResource }
import org.apache.kafka.common.{
  KafkaFuture,
  IsolationLevel => JIsolationLevel,
  Metric => JMetric,
  MetricName => JMetricName,
  Node => JNode,
  TopicPartition => JTopicPartition,
  TopicPartitionInfo => JTopicPartitionInfo
}
import zio._
import zio.blocking.Blocking
import zio.duration.Duration

import scala.collection.compat._
import scala.jdk.CollectionConverters._

/**
 * Thin wrapper around apache java AdminClient. See java api for descriptions
 * @param adminClient
 */
case class AdminClient(private val adminClient: JAdminClient) {
  import AdminClient._

  /**
   * Create multiple topics.
   */
  def createTopics(
    newTopics: Iterable[NewTopic],
    @deprecatedName(Symbol("createTopicOptions")) options: Option[CreateTopicsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = newTopics.map(_.asJava).asJavaCollection

    fromKafkaFutureVoid {
      blocking
        .effectBlocking(
          options
            .fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts.asJava))
            .all()
        )
    }
  }

  /**
   * Create a single topic.
   */
  def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): RIO[Blocking, Unit] =
    createTopics(List(newTopic), Some(CreateTopicsOptions(validateOnly)))

  /**
   * Delete multiple topics.
   */
  def deleteTopics(
    topics: Iterable[String],
    @deprecatedName(Symbol("deleteTopicsOptions")) options: Option[DeleteTopicsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = topics.asJavaCollection
    fromKafkaFutureVoid {
      blocking
        .effectBlocking(
          options
            .fold(adminClient.deleteTopics(asJava))(opts => adminClient.deleteTopics(asJava, opts))
            .all()
        )
    }
  }

  /**
   * Delete a single topic.
   */
  def deleteTopic(topic: String): RIO[Blocking, Unit] =
    deleteTopics(List(topic))

  /**
   * List the topics in the cluster.
   */
  def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): RIO[Blocking, Map[String, TopicListing]] =
    fromKafkaFuture {
      blocking.effectBlocking(
        listTopicsOptions.fold(adminClient.listTopics())(opts => adminClient.listTopics(opts)).namesToListings()
      )
    }.map(_.asScala.toMap.view.mapValues(TopicListing.apply).toMap)

  /**
   * Describe the specified topics.
   */
  def describeTopics(
    topicNames: Iterable[String],
    @deprecatedName(Symbol("describeTopicsOptions")) options: Option[DescribeTopicsOptions] = None
  ): RIO[Blocking, Map[String, TopicDescription]] = {
    val asJava = topicNames.asJavaCollection
    fromKafkaFuture {
      blocking.effectBlocking(
        options
          .fold(adminClient.describeTopics(asJava))(opts => adminClient.describeTopics(asJava, opts))
          .all()
      )
    }.map(_.asScala.view.mapValues(AdminClient.TopicDescription(_)).toMap)
  }

  /**
   * Get the configuration for the specified resources.
   */
  def describeConfigs(
    configResources: Iterable[ConfigResource],
    @deprecatedName(Symbol("describeConfigsOptions")) options: Option[DescribeConfigsOptions] = None
  ): RIO[Blocking, Map[ConfigResource, KafkaConfig]] = {
    val asJava = configResources.map(_.asJava).asJavaCollection
    fromKafkaFuture {
      blocking.effectBlocking(
        options
          .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts.asJava))
          .all()
      )
    }.map(
      _.asScala.view.map { case (configResource, config) => (ConfigResource(configResource), KafkaConfig(config)) }.toMap
    )
  }

  private def describeCluster(options: Option[DescribeClusterOptions]): RIO[Blocking, DescribeClusterResult] =
    blocking.effectBlocking(
      options.fold(adminClient.describeCluster())(opts => adminClient.describeCluster(opts.asJava))
    )

  /**
   * Get the cluster nodes.
   */
  def describeClusterNodes(options: Option[DescribeClusterOptions] = None): RIO[Blocking, List[Node]] =
    fromKafkaFuture(
      describeCluster(options).map(_.nodes())
    ).map(_.asScala.toList.map(Node.apply))

  /**
   * Get the cluster controller.
   */
  def describeClusterController(options: Option[DescribeClusterOptions] = None): RIO[Blocking, Node] =
    fromKafkaFuture(
      describeCluster(options).map(_.controller())
    ).map(Node.apply)

  /**
   * Get the cluster id.
   */
  def describeClusterId(options: Option[DescribeClusterOptions] = None): RIO[Blocking, String] =
    fromKafkaFuture(
      describeCluster(options).map(_.clusterId())
    )

  /**
   * Get the cluster authorized operations.
   */
  def describeClusterAuthorizedOperations(
    options: Option[DescribeClusterOptions] = None
  ): RIO[Blocking, Set[AclOperation]] =
    for {
      res           <- describeCluster(options)
      opt           <- fromKafkaFuture(Task(res.authorizedOperations())).map(Option(_))
      lst           <- ZIO.fromOption(opt.map(_.asScala.toSet)).orElseSucceed(Set.empty)
      aclOperations = lst.map(AclOperation.apply)
    } yield aclOperations

  /**
   * Add new partitions to a topic.
   */
  def createPartitions(
    newPartitions: Map[String, NewPartitions],
    @deprecatedName(Symbol("createPartitionsOptions")) options: Option[CreatePartitionsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = newPartitions.view.mapValues(_.asJava).toMap.asJava
    fromKafkaFutureVoid {
      blocking.effectBlocking(
        options
          .fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts.asJava))
          .all()
      )
    }
  }

  /**
   * List offset for the specified partitions.
   */
  def listOffsets(
    topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
    @deprecatedName(Symbol("listOffsetOptions")) options: Option[ListOffsetsOptions] = None
  ): RIO[Blocking, Map[TopicPartition, ListOffsetsResultInfo]] = {
    val asJava = topicPartitionOffsets.bimap(_.asJava, _.asJava).asJava
    fromKafkaFuture {
      blocking.effectBlocking(
        options
          .fold(adminClient.listOffsets(asJava))(opts => adminClient.listOffsets(asJava, opts.asJava))
          .all()
      )
    }
  }.map(_.asScala.toMap.bimap(TopicPartition(_), ListOffsetsResultInfo(_)))

  /**
   * Alter offsets for the specified partitions and consumer group.
   */
  def alterConsumerGroupOffsets(
    groupId: String,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    @deprecatedName(Symbol("alterConsumerGroupOffsetsOptions")) options: Option[
      AlterConsumerGroupOffsetsOptions
    ] = None
  ): RIO[Blocking, Unit] = {
    val asJava = offsets.bimap(_.asJava, _.asJava).asJava
    fromKafkaFutureVoid {
      blocking.effectBlocking(
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
   *
   */
  def metrics: RIO[Blocking, Map[MetricName, Metric]] =
    blocking.effectBlocking(
      adminClient.metrics().asScala.toMap.map {
        case (metricName, metric) =>
          (MetricName(metricName), Metric(metric))
      }
    )
}

object AdminClient {
  def fromKafkaFuture[R, T](kfv: RIO[R, KafkaFuture[T]]): RIO[R, T] =
    kfv.flatMap { f =>
      Task.effectAsyncInterrupt[T] { cb =>
        f.whenComplete {
          new KafkaFuture.BiConsumer[T, Throwable] {
            def accept(t: T, e: Throwable): Unit =
              if (f.isCancelled) cb(ZIO.fiberId.flatMap(id => Task.halt(Cause.interrupt(id))))
              else if (e ne null) cb(Task.fail(e))
              else cb(Task.succeed(t))
          }
        }
        Left(ZIO.effectTotal(f.cancel(true)))
      }
    }

  def fromKafkaFutureVoid[R](kfv: RIO[R, KafkaFuture[Void]]): RIO[R, Unit] =
    fromKafkaFuture(kfv).unit

  case class ConfigResource(`type`: ConfigResourceType, name: String) {
    lazy val asJava = new JConfigResource(`type`.asJava, name)
  }

  object ConfigResource {
    def apply(jcr: JConfigResource): ConfigResource = ConfigResource(ConfigResourceType(jcr.`type`()), jcr.name())
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

  case class CreatePartitionsOptions(validateOnly: Boolean) {
    lazy val asJava: JCreatePartitionsOptions = new JCreatePartitionsOptions().validateOnly(validateOnly)
  }

  case class CreateTopicsOptions(validateOnly: Boolean) {
    lazy val asJava: JCreateTopicsOptions = new JCreateTopicsOptions().validateOnly(validateOnly)
  }

  case class DescribeConfigsOptions(includeSynonyms: Boolean, includeDocumentation: Boolean) {
    lazy val asJava: JDescribeConfigsOptions =
      new JDescribeConfigsOptions().includeSynonyms(includeSynonyms).includeDocumentation(includeDocumentation)
  }

  case class DescribeClusterOptions(includeAuthorizedOperations: Boolean) {
    lazy val asJava: JDescribeClusterOptions =
      new JDescribeClusterOptions().includeAuthorizedOperations(includeAuthorizedOperations)
  }

  case class MetricName(name: String, group: String, description: String, tags: Map[String, String])

  object MetricName {
    def apply(jmn: JMetricName): MetricName =
      MetricName(jmn.name(), jmn.group(), jmn.description(), jmn.tags().asScala.toMap)
  }

  case class Metric(name: MetricName, metricValue: AnyRef)

  object Metric {
    def apply(jm: JMetric): Metric = Metric(MetricName(jm.metricName()), jm.metricValue())
  }

  case class NewTopic(
    name: String,
    numPartitions: Int,
    replicationFactor: Short,
    configs: Map[String, String] = Map()
  ) {
    def asJava: JNewTopic = {
      val jn = new JNewTopic(name, numPartitions, replicationFactor)

      if (configs.nonEmpty)
        jn.configs(configs.asJava)

      jn
    }
  }

  case class NewPartitions(
    totalCount: Int,
    newAssignments: List[List[Int]] = Nil
  ) {
    def asJava =
      if (newAssignments.nonEmpty)
        JNewPartitions.increaseTo(totalCount, newAssignments.map(_.map(Int.box).asJava).asJava)
      else JNewPartitions.increaseTo(totalCount)
  }

  case class Node(id: Int, host: String, port: Int, rack: Option[String] = None) {
    lazy val asJava = rack.fold(new JNode(id, host, port))(rack => new JNode(id, host, port, rack))
  }

  object Node {
    def apply(jNode: JNode): Node = Node(jNode.id(), jNode.host(), jNode.port, Option(jNode.rack()))
  }

  case class TopicDescription(
    name: String,
    internal: Boolean,
    partitions: List[TopicPartitionInfo],
    authorizedOperations: Option[Set[AclOperation]]
  )

  object TopicDescription {
    def apply(jt: JTopicDescription): TopicDescription = {
      val authorizedOperations = Option(jt.authorizedOperations).map(_.asScala.toSet)
      TopicDescription(
        jt.name,
        jt.isInternal,
        jt.partitions.asScala.toList.map(TopicPartitionInfo.apply),
        authorizedOperations.map(_.map(AclOperation.apply))
      )
    }
  }

  case class TopicPartitionInfo(partition: Int, leader: Node, replicas: List[Node], isr: List[Node]) {
    lazy val asJava =
      new JTopicPartitionInfo(partition, leader.asJava, replicas.map(_.asJava).asJava, isr.map(_.asJava).asJava)
  }

  object TopicPartitionInfo {
    def apply(jtpi: JTopicPartitionInfo): TopicPartitionInfo =
      TopicPartitionInfo(
        jtpi.partition(),
        Node(jtpi.leader()),
        jtpi.replicas().asScala.map(Node.apply).toList,
        jtpi.isr().asScala.map(Node.apply).toList
      )
  }

  case class TopicListing(name: String, isInternal: Boolean) {
    def asJava = new JTopicListing(name, isInternal)
  }

  object TopicListing {
    def apply(jtl: JTopicListing): TopicListing = TopicListing(jtl.name(), jtl.isInternal)
  }

  case class TopicPartition(
    name: String,
    partition: Int
  ) {
    def asJava = new JTopicPartition(name, partition)
  }

  object TopicPartition {
    def apply(tp: JTopicPartition): TopicPartition = new TopicPartition(tp.topic(), tp.partition())
  }

  sealed abstract class OffsetSpec { def asJava: JOffsetSpec }

  object OffsetSpec {
    final case object EarliestSpec extends OffsetSpec {
      override def asJava = JOffsetSpec.earliest()
    }

    final case object LatestSpec extends OffsetSpec {
      override def asJava = JOffsetSpec.latest()
    }

    final case class TimestampSpec(timestamp: Long) extends OffsetSpec {
      override def asJava = JOffsetSpec.forTimestamp(timestamp)
    }
  }

  sealed abstract class IsolationLevel { def asJava: JIsolationLevel }

  object IsolationLevel {
    final case object ReadUncommitted extends IsolationLevel {
      override def asJava = JIsolationLevel.READ_UNCOMMITTED
    }

    final case object ReadCommitted extends IsolationLevel {
      override def asJava = JIsolationLevel.READ_COMMITTED
    }
  }

  case class ListOffsetsOptions(
    isolationLevel: IsolationLevel = IsolationLevel.ReadUncommitted,
    timeout: Option[Duration]
  ) {
    def asJava = {
      val offsetOpt = new JListOffsetsOptions(isolationLevel.asJava)
      timeout.fold(offsetOpt)(timeout => offsetOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  case class ListOffsetsResultInfo(
    offset: Long,
    timestamp: Long,
    leaderEpoch: Option[Int]
  )

  object ListOffsetsResultInfo {
    def apply(lo: JListOffsetsResultInfo): ListOffsetsResultInfo =
      ListOffsetsResultInfo(lo.offset(), lo.timestamp(), lo.leaderEpoch().toScala.map(_.toInt))
  }

  case class OffsetAndMetadata(
    offset: Long,
    leaderEpoch: Option[Int] = None,
    metadata: Option[String] = None
  ) {
    def asJava = new JOffsetAndMetadata(offset, leaderEpoch.map(Int.box).toJava, metadata.orNull)
  }

  case class AlterConsumerGroupOffsetsOptions(
    timeout: Duration
  ) {
    def asJava = new JAlterConsumerGroupOffsetsOptions().timeoutMs(timeout.toMillis.toInt)
  }

  case class KafkaConfig(entries: Map[String, ConfigEntry])

  object KafkaConfig {
    def apply(jConfig: JConfig): KafkaConfig =
      KafkaConfig(jConfig.entries().asScala.map(e => e.name() -> e).toMap)
  }

  def make(settings: AdminClientSettings) =
    ZManaged.make(
      ZIO(JAdminClient.create(settings.driverSettings.asJava)).map(ac => AdminClient(ac))
    )(client => ZIO.effectTotal(client.adminClient.close(settings.closeTimeout)))

  implicit class MapOps[K1, V1](val v: Map[K1, V1]) extends AnyVal {
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2) = v.map(kv => fk(kv._1) -> fv(kv._2))
  }

  implicit class OptionalOps[T](val v: Optional[T]) extends AnyVal {
    def toScala = if (v.isPresent) Some(v.get()) else None
  }

  implicit class OptionOps[T](val v: Option[T]) extends AnyVal {
    def toJava = v.fold(Optional.empty[T])(Optional.of)
  }
}
