package zio.kafka.client

import org.apache.kafka.clients.admin.{
  AdminClient => JAdminClient,
  NewPartitions => JNewPartitions,
  NewTopic => JNewTopic,
  TopicDescription => JTopicDescription,
  _
}
import org.apache.kafka.common.acl.AclOperation
import org.apache.kafka.common.{ KafkaFuture, TopicPartitionInfo }
import zio._
import zio.kafka.client.BlockingTask

import scala.jdk.CollectionConverters._

trait KafkaAdmin {
  val kafkaAdmin: KafkaAdmin.Service
}

object KafkaAdmin {
  trait Service {

    /**
     * Create multiple topics.
     */
    def createTopics(
      newTopics: Iterable[NewTopic],
      createTopicOptions: Option[CreateTopicsOptions] = None
    ): BlockingTask[Unit]

    /**
     * Create a single topic.
     */
    def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): BlockingTask[Unit]

    /**
     * Delete multiple topics.
     */
    def deleteTopics(
      topics: Iterable[String],
      deleteTopicsOptions: Option[DeleteTopicsOptions] = None
    ): BlockingTask[Unit]

    /**
     * Delete a single topic.
     */
    def deleteTopic(topic: String): BlockingTask[Unit]

    /**
     * List the topics in the cluster.
     */
    def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): BlockingTask[Map[String, TopicListing]]

    /**
     * Describe the specified topics.
     */
    def describeTopics(
      topicNames: Iterable[String],
      describeTopicsOptions: Option[DescribeTopicsOptions] = None
    ): BlockingTask[Map[String, Any]]

    /**
     * Add new partitions to a topic.
     */
    def createPartitions(
      newPartitions: Map[String, NewPartitions],
      createPartitionsOptions: Option[CreatePartitionsOptions] = None
    ): BlockingTask[Unit]

  }

  def fromKafkaFuture[R, T](kfv: RIO[R, KafkaFuture[T]]): RIO[R, T] =
    kfv.flatMap { f =>
      Task.effectAsync[T] { cb =>
        f.whenComplete {
          new KafkaFuture.BiConsumer[T, Throwable] {
            def accept(t: T, e: Throwable) =
              if (e ne null) cb(Task.fail(e))
              else cb(Task.succeed(t))
          }
        }
        ()
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
    authorizedOperations: Set[AclOperation]
  )

  object TopicDescription {
    def apply(jt: JTopicDescription): TopicDescription =
      TopicDescription(jt.name, jt.isInternal, jt.partitions.asScala.toList, jt.authorizedOperations.asScala.toSet)
  }

  case class KafkaAdminClientConfig(
    bootstrapServers: List[String],
    additionalConfig: Map[String, AnyRef] = Map.empty
  )

}

/**
 * Thin wrapper around apache java AdminClient. See java api for descriptions
 * @param adminClient
 * @param semaphore
 */
case class AdminClient(private val adminClient: JAdminClient, private val semaphore: Semaphore)
    extends KafkaAdmin.Service {
  import KafkaAdmin._

  /**
   * Create multiple topics.
   */
  override def createTopics(
    newTopics: Iterable[NewTopic],
    createTopicOptions: Option[CreateTopicsOptions]
  ): BlockingTask[Unit] = {
    val asJava = newTopics.map(_.asJava).asJavaCollection
    semaphore.withPermit(
      fromKafkaFutureVoid {
        blocking
          .effectBlocking(
            createTopicOptions
              .fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts))
              .all()
          )
      }
    )
  }

  /**
   * Create a single topic.
   */
  override def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): BlockingTask[Unit] =
    createTopics(List(newTopic), Some(new CreateTopicsOptions().validateOnly(validateOnly)))

  /**
   * Delete multiple topics.
   */
  override def deleteTopics(
    topics: Iterable[String],
    deleteTopicsOptions: Option[DeleteTopicsOptions] = None
  ): BlockingTask[Unit] = {
    val asJava = topics.asJavaCollection

    semaphore.withPermit {
      fromKafkaFutureVoid {
        blocking
          .effectBlocking(
            deleteTopicsOptions
              .fold(adminClient.deleteTopics(asJava))(opts => adminClient.deleteTopics(asJava, opts))
              .all()
          )
      }
    }
  }

  /**
   * Delete a single topic.
   */
  override def deleteTopic(topic: String): BlockingTask[Unit] =
    deleteTopics(List(topic))

  /**
   * List the topics in the cluster.
   */
  override def listTopics(
    listTopicsOptions: Option[ListTopicsOptions] = None
  ): BlockingTask[Map[String, TopicListing]] =
    semaphore.withPermit {
      fromKafkaFuture {
        blocking.effectBlocking(
          listTopicsOptions.fold(adminClient.listTopics())(opts => adminClient.listTopics(opts)).namesToListings()
        )
      }.map(_.asScala.toMap)
    }

  /**
   * Describe the specified topics.
   */
  override def describeTopics(
    topicNames: Iterable[String],
    describeTopicsOptions: Option[DescribeTopicsOptions] = None
  ): BlockingTask[Map[String, TopicDescription]] = {
    val asJava = topicNames.asJavaCollection
    semaphore.withPermit {
      fromKafkaFuture {
        blocking.effectBlocking(
          describeTopicsOptions
            .fold(adminClient.describeTopics(asJava))(opts => adminClient.describeTopics(asJava, opts))
            .all()
        )
      }.map(_.asScala.view.mapValues(KafkaAdmin.TopicDescription(_)).toMap)
    }
  }

  /**
   * Add new partitions to a topic.
   */
  override def createPartitions(
    newPartitions: Map[String, NewPartitions],
    createPartitionsOptions: Option[CreatePartitionsOptions]
  ): BlockingTask[Unit] = {
    val asJava = newPartitions.view.mapValues(_.asJava).toMap.asJava

    semaphore.withPermit {
      fromKafkaFutureVoid {
        blocking.effectBlocking(
          createPartitionsOptions
            .fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts))
            .all()
        )
      }
    }
  }
}

object AdminClient {

  def make(config: KafkaAdmin.KafkaAdminClientConfig) =
    ZManaged.make {
      val configMap = (config.additionalConfig + (AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG -> config.bootstrapServers
        .mkString(","))).asJava
      for {
        ac  <- ZIO(JAdminClient.create(configMap))
        sem <- Semaphore.make(1L)
      } yield AdminClient(ac, sem)
    } { client =>
      ZIO.effectTotal(client.adminClient.close())
    }
}
