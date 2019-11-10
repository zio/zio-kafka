package zio.kafka.client

import org.apache.kafka.clients.admin.{AdminClient => JAdminClient, CreatePartitionsResult => JCreatePartitionsResult, CreateTopicsResult => JCreateTopicsResult, DeleteTopicsResult => JDeleteTopicsResult, DescribeTopicsResult => JDescribeTopicsResult, ListTopicsResult => JListTopicsResult, TopicDescription => JTopicDescription, 
  DeleteRecordsResult => JDeleteRecordsResult, _}
import zio._
import AdminClient._
import org.apache.kafka.common.acl.AclOperation
import org.apache.kafka.common.{KafkaFuture, TopicPartition, TopicPartitionInfo}

import scala.collection.JavaConverters._

/**
 * Thin wrapper around apache java AdminClient. See java api for descriptions
 * @param adminClient
 * @param semaphore
 */
case class AdminClient(private val adminClient: JAdminClient, private val semaphore: Semaphore) {

  def createTopics(
    newTopics: Iterable[NewTopic],
    createTopicOptions: Option[CreateTopicsOptions] = None
  ): BlockingTask[CreateTopicsResult] = {
    val asJava = newTopics.asJavaCollection
    semaphore.withPermit(
      ZIO(
        createTopicOptions.fold(adminClient.createTopics(asJava))(opts => adminClient.createTopics(asJava, opts))
      ).map(CreateTopicsResult(_))
    )
  }

  /**
   * convenience method - most people are probably happy with one at a time!
   *
   * @param newTopic
   */
  def createTopic(newTopic: NewTopic, validateOnly: Boolean = false): BlockingTask[Unit] =
    for {
      jTopRes <- semaphore.withPermit(
                  ZIO(
                    adminClient.createTopics(
                      List(newTopic).asJavaCollection,
                      new CreateTopicsOptions().validateOnly(validateOnly)
                    )
                  )
                )
      _ <- CreateTopicsResult(jTopRes).completed
    } yield ()

  def deleteTopics(
    topics: Iterable[String],
    deleteTopicsOptions: Option[DeleteTopicsOptions] = None
  ): BlockingTask[DeleteTopicsResult] = {
    val asJava = topics.asJavaCollection
    semaphore.withPermit(
      ZIO(
        deleteTopicsOptions.fold(adminClient.deleteTopics(asJava))(opts => adminClient.deleteTopics(asJava, opts))
      ).map(DeleteTopicsResult(_))
    )
  }

  def deleteTopic(topic: String): BlockingTask[Unit] =
    for {
      jRes <- semaphore.withPermit(ZIO(adminClient.deleteTopics(List(topic).asJavaCollection)))
      _    <- DeleteTopicsResult(jRes).completed
    } yield ()

  def listTopics(listTopicsOptions: Option[ListTopicsOptions] = None): BlockingTask[ListTopicsResult] =
    for {
      jRes <- semaphore.withPermit(
               ZIO(listTopicsOptions.fold(adminClient.listTopics())(opts => adminClient.listTopics(opts)))
             )
      res <- ListTopicsResult(jRes)
    } yield res

  def describeTopics(topicNames: Iterable[String],
                     describeTopicsOptions: Option[DescribeTopicsOptions] = None): BlockingTask[DescribeTopicsResult] = {
    val asJava = topicNames.asJavaCollection
    semaphore.withPermit {
      ZIO(describeTopicsOptions.fold(adminClient.describeTopics(asJava))(opts => adminClient.describeTopics(asJava, opts)))
        .map(res => DescribeTopicsResult(res))
    }
  }

  def createPartitions(newPartitions: Map[String, NewPartitions],
                       createPartitionsOptions: Option[CreatePartitionsOptions] = None): BlockingTask[CreatePartitionsResult] = {
    val asJava = newPartitions.asJava
    semaphore.withPermit {
      ZIO(createPartitionsOptions.fold(adminClient.createPartitions(asJava))(opts => adminClient.createPartitions(asJava, opts)))
        .map(res => CreatePartitionsResult(res))
    }
  }
}

object AdminClient {

  def kafkaFutureVoidToZio(kfv: KafkaFuture[Void]): Task[Unit] =
    Task.effectAsync[Unit] { cb =>
      kfv.whenComplete {
        new KafkaFuture.BiConsumer[Void, Throwable] {
          def accept(t: Void, e: Throwable) =
            if (e ne null) cb(Task.fail(e))
            else cb(Task.succeed(()))
        }
      }
      ()
    }

  def kafkaFutureToZio[T](kfv: KafkaFuture[T]): Task[T] =
    Task.effectAsync[T] { cb =>
      kfv.whenComplete {
        new KafkaFuture.BiConsumer[T, Throwable] {
          def accept(t: T, e: Throwable) =
            if (e ne null) cb(Task.fail(e))
            else cb(Task.succeed(t))
        }
      }
      ()
    }

  case class ResultMap[K, T](futures: Map[K, Task[T]]) {
    def completed: Task[Map[K, T]] =
      ZIO.collectAll {
        futures.map { case (k, vt) => vt.map(v => k -> v) }
      }.map(_.toMap)
  }

  type ResultMapS[T] = ResultMap[String, T]

  type CreateTopicsResult = ResultMapS[Unit]

  object CreateTopicsResult {
    def apply(j: JCreateTopicsResult): CreateTopicsResult =
      new CreateTopicsResult(j.values.asScala.toMap.mapValues(fv => kafkaFutureVoidToZio(fv)))
  }

  type DeleteTopicsResult = ResultMapS[Unit]

  object DeleteTopicsResult {
    def apply(j: JDeleteTopicsResult): DeleteTopicsResult =
      new DeleteTopicsResult(j.values.asScala.toMap.mapValues(fv => kafkaFutureVoidToZio(fv)))
  }

  type ListTopicsResult = Map[String, TopicListing]

  object ListTopicsResult {
    def apply(j: JListTopicsResult): BlockingTask[ListTopicsResult] =
      kafkaFutureToZio(j.namesToListings()).map { col =>
        col.asScala.toMap
      }
  }

  case class TopicDescription(name: String, internal: Boolean, partitions: List[TopicPartitionInfo], authorizedOperations: Set[AclOperation])

  object TopicDescription {
    def apply(jt: JTopicDescription): TopicDescription =
      TopicDescription(jt.name, jt.isInternal, jt.partitions.asScala.toList, jt.authorizedOperations.asScala.toSet)
  }

  type DescribeTopicsResult = ResultMapS[TopicDescription]

  object DescribeTopicsResult {
    def apply(j: JDescribeTopicsResult): DescribeTopicsResult = {
      val zios: Map[String, Task[TopicDescription]] = j.values.asScala.toMap
        .mapValues { vkf => kafkaFutureToZio(vkf).map { v => TopicDescription(v) } }
      ResultMap(zios)
    }
  }

  type CreatePartitionsResult = ResultMapS[Unit]

  object CreatePartitionsResult {
    def apply(j: JCreatePartitionsResult): CreatePartitionsResult =
      new CreatePartitionsResult(j.values.asScala.toMap.mapValues(fv => kafkaFutureVoidToZio(fv)))
    }
  
  type DeleteRecordsResult = ResultMap[TopicPartition, DeletedRecords]

  object DeleteRecordsResult {
    def apply(j: JDeleteRecordsResult): DeleteRecordsResult = {
      val zios: Map[TopicPartition, Task[DeletedRecords]] = j.lowWatermarks.asScala.toMap
        .mapValues { vkf => kafkaFutureToZio(vkf) }
      ResultMap(zios)
    }
  }

  case class KafkaAdminClientConfig(
    bootstrapServers: List[String],
    // @iravid - what compulsory config options should go here (e.g. brokers)
    additionalConfig: Map[String, AnyRef] = Map.empty
  )

  def adminClient(config: KafkaAdminClientConfig) =
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
