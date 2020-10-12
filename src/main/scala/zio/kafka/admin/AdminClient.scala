package zio.kafka.admin

import org.apache.kafka.clients.admin.{
  AdminClient => JAdminClient,
  Config => JConfig,
  NewPartitions => JNewPartitions,
  NewTopic => JNewTopic,
  TopicDescription => JTopicDescription,
  _
}
import org.apache.kafka.common.acl.AclOperation
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.{ KafkaFuture, TopicPartitionInfo }
import zio._
import zio.blocking.Blocking

import scala.jdk.CollectionConverters._, scala.collection.compat._

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
    createTopicOptions: Option[CreateTopicsOptions] = None,
    options: Option[CreateTopicsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = newTopics.map(_.asJava).asJavaCollection
    fromKafkaFutureVoid {
      blocking
        .effectBlocking(
          options
            .orElse(createTopicOptions)
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
    deleteTopicsOptions: Option[DeleteTopicsOptions] = None,
    options: Option[DeleteTopicsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = topics.asJavaCollection
    fromKafkaFutureVoid {
      blocking
        .effectBlocking(
          options
            .orElse(deleteTopicsOptions)
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
    describeTopicsOptions: Option[DescribeTopicsOptions] = None,
    options: Option[DescribeTopicsOptions] = None
  ): RIO[Blocking, Map[String, TopicDescription]] = {
    val asJava = topicNames.asJavaCollection
    fromKafkaFuture {
      blocking.effectBlocking(
        options
          .orElse(describeTopicsOptions)
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
    describeConfigsOptions: Option[DescribeConfigsOptions] = None,
    options: Option[DescribeConfigsOptions] = None
  ): RIO[Blocking, Map[ConfigResource, KafkaConfig]] = {
    val asJava = configResources.asJavaCollection
    fromKafkaFuture {
      blocking.effectBlocking(
        options
          .orElse(describeConfigsOptions)
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
    createPartitionsOptions: Option[CreatePartitionsOptions] = None,
    options: Option[CreatePartitionsOptions] = None
  ): RIO[Blocking, Unit] = {
    val asJava = newPartitions.view.mapValues(_.asJava).toMap.asJava
    fromKafkaFutureVoid {
      blocking.effectBlocking(
        options
          .orElse(createPartitionsOptions)
          .fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts))
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

  case class KafkaConfig(entries: Map[String, ConfigEntry])

  object KafkaConfig {
    def apply(jConfig: JConfig): KafkaConfig =
      KafkaConfig(jConfig.entries().asScala.map(e => e.name() -> e).toMap)
  }

  def make(settings: AdminClientSettings) =
    ZManaged.make(
      ZIO(JAdminClient.create(settings.driverSettings.asJava)).map(ac => AdminClient(ac))
    )(client => ZIO.effectTotal(client.adminClient.close(settings.closeTimeout.asJava)))
}
