package zio.kafka.admin

import org.apache.kafka.clients.admin.{
  AdminClient => JAdminClient,
  AlterConsumerGroupOffsetsOptions => JAlterConsumerGroupOffsetsOptions,
  Config => JConfig,
  ListOffsetsOptions => JListOffsetsOptions,
  NewPartitions => JNewPartitions,
  NewTopic => JNewTopic,
  OffsetSpec => JOffsetSpec,
  TopicDescription => JTopicDescription,
  _
}
import org.apache.kafka.clients.admin.ListOffsetsResult.{ ListOffsetsResultInfo => JListOffsetsResultInfo }
import org.apache.kafka.clients.consumer.{ OffsetAndMetadata => JOffsetAndMetadata }
import org.apache.kafka.common.acl.AclOperation
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.{
  IsolationLevel => JIsolationLevel,
  KafkaFuture,
  TopicPartition => JTopicPartition,
  TopicPartitionInfo
}
import zio._
import zio.blocking.Blocking

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

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
    createTopicOptions: Option[CreateTopicsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = newTopics.map(_.asJava).asJavaCollection
    fromKafkaFutureVoid {
      blocking
        .effectBlocking(
          createTopicOptions
            .fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts))
            .all()
        )
    }
  }

  /**
   * Create a single topic.
   */
  def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): RIO[Blocking, Unit] =
    createTopics(List(newTopic), Some(new CreateTopicsOptions().validateOnly(validateOnly)))

  /**
   * Delete multiple topics.
   */
  def deleteTopics(
    topics: Iterable[String],
    deleteTopicsOptions: Option[DeleteTopicsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = topics.asJavaCollection
    fromKafkaFutureVoid {
      blocking
        .effectBlocking(
          deleteTopicsOptions
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
    }.map(_.asScala.toMap)

  /**
   * Describe the specified topics.
   */
  def describeTopics(
    topicNames: Iterable[String],
    describeTopicsOptions: Option[DescribeTopicsOptions] = None
  ): RIO[Blocking, Map[String, TopicDescription]] = {
    val asJava = topicNames.asJavaCollection
    fromKafkaFuture {
      blocking.effectBlocking(
        describeTopicsOptions
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
    describeConfigsOptions: Option[DescribeConfigsOptions] = None
  ): RIO[Blocking, Map[ConfigResource, KafkaConfig]] = {
    val asJava = configResources.asJavaCollection
    fromKafkaFuture {
      blocking.effectBlocking(
        describeConfigsOptions
          .fold(adminClient.describeConfigs(asJava))(opts => adminClient.describeConfigs(asJava, opts))
          .all()
      )
    }.map(_.asScala.view.mapValues(AdminClient.KafkaConfig(_)).toMap)
  }

  /**
   * Add new partitions to a topic.
   */
  def createPartitions(
    newPartitions: Map[String, NewPartitions],
    createPartitionsOptions: Option[CreatePartitionsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = newPartitions.view.mapValues(_.asJava).toMap.asJava
    fromKafkaFutureVoid {
      blocking.effectBlocking(
        createPartitionsOptions
          .fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts))
          .all()
      )
    }
  }

  /**
   * List offset for the specified partitions.
   */
  def listOffsets(
    topicPartitionOffsets: Map[TopicPartition, OffsetSpec],
    listOffsetOptions: Option[ListOffsetsOptions] = None
  ): RIO[Blocking, Map[TopicPartition, ListOffsetsResultInfo]] = {
    val asJava = topicPartitionOffsets.bimap(_.asJava, _.asJava).asJava
    fromKafkaFuture {
      blocking.effectBlocking(
        listOffsetOptions
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
    alterConsumerGroupOffsetsOptions: Option[AlterConsumerGroupOffsetsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = offsets.bimap(_.asJava, _.asJava).asJava

    fromKafkaFutureVoid {
      blocking.effectBlocking(
        alterConsumerGroupOffsetsOptions
          .fold(adminClient.alterConsumerGroupOffsets(groupId, asJava))(opts =>
            adminClient.alterConsumerGroupOffsets(groupId, asJava, opts.asJava)
          )
          .all()
      )
    }
  }
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

  case class TopicDescription(
    name: String,
    internal: Boolean,
    partitions: List[TopicPartitionInfo],
    authorizedOperations: Option[Set[AclOperation]]
  )

  object TopicDescription {
    def apply(jt: JTopicDescription): TopicDescription = {
      val authorizedOperations = Option(jt.authorizedOperations).map(_.asScala.toSet)
      TopicDescription(jt.name, jt.isInternal, jt.partitions.asScala.toList, authorizedOperations)
    }
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
    timeoutMs: Option[Int]
  ) {
    def asJava = {
      val offsetOpt = new JListOffsetsOptions(isolationLevel.asJava)
      timeoutMs.fold(offsetOpt)(timeout => offsetOpt.timeoutMs(timeout))
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
    timeoutMs: Int
  ) {
    def asJava = new JAlterConsumerGroupOffsetsOptions().timeoutMs(timeoutMs)
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
}
