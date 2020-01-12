package zio.kafka.client

import org.apache.kafka.clients.admin.{
  AdminClient => JAdminClient,
  NewTopic => JNewTopic,
  NewPartitions => JNewPartitions,
  TopicDescription => JTopicDescription,
  _
}
import org.apache.kafka.common.acl.AclOperation
import org.apache.kafka.common.{ KafkaFuture, TopicPartitionInfo }
import zio._

import scala.jdk.CollectionConverters._, scala.collection.compat._

/**
 * Thin wrapper around apache java AdminClient. See java api for descriptions
 * @param adminClient
 * @param semaphore
 */
case class AdminClient(private val adminClient: JAdminClient, private val semaphore: Semaphore) {
  import AdminClient._

  /**
   * Create multiple topics.
   */
  def createTopics(
    newTopics: Iterable[NewTopic],
    createTopicOptions: Option[CreateTopicsOptions] = None
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
  def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): BlockingTask[Unit] =
    createTopics(List(newTopic), Some(new CreateTopicsOptions().validateOnly(validateOnly)))

  /**
   * Delete multiple topics.
   */
  def deleteTopics(
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
  def deleteTopic(topic: String): BlockingTask[Unit] =
    deleteTopics(List(topic))

  /**
   * List the topics in the cluster.
   */
  def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): BlockingTask[Map[String, TopicListing]] =
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
  def describeTopics(
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
      }.map(_.asScala.view.mapValues(AdminClient.TopicDescription(_)).toMap)
    }
  }

  /**
   * Add new partitions to a topic.
   */
  def createPartitions(
    newPartitions: Map[String, NewPartitions],
    createPartitionsOptions: Option[CreatePartitionsOptions] = None
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

  case class KafkaAdminClientConfig(
    bootstrapServers: List[String],
    additionalConfig: Map[String, AnyRef] = Map.empty
  )

  def make(config: KafkaAdminClientConfig) =
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
