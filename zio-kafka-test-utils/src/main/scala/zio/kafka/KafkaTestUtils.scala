package zio.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.kafka.admin._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.embedded.Kafka
import zio.kafka.producer._
import zio.kafka.serde.{ Deserializer, Serde, Serializer }

object KafkaTestUtils {

  val producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(ProducerSettings(_))

  val producer: ZLayer[Kafka, Throwable, Producer] =
    (ZLayer.fromZIO(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) >>>
      Producer.live

  val transactionalProducerSettings: ZIO[Kafka, Nothing, TransactionalProducerSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(TransactionalProducerSettings(_, "test-transaction"))

  val transactionalProducer: ZLayer[Kafka, Throwable, TransactionalProducer] =
    (ZLayer.fromZIO(transactionalProducerSettings) ++ ZLayer.succeed(
      Serde.string: Serializer[Any, String]
    )) >>>
      TransactionalProducer.live

  def produceOne(
    topic: String,
    key: String,
    message: String
  ): ZIO[Producer, Throwable, RecordMetadata] =
    Producer.produce[Any, String, String](new ProducerRecord(topic, key, message), Serde.string, Serde.string)

  def produceMany(
    topic: String,
    partition: Int,
    kvs: Iterable[(String, String)]
  ): ZIO[Producer, Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](
        Chunk.fromIterable(kvs.map { case (k, v) =>
          new ProducerRecord(topic, partition, null, k, v)
        }),
        Serde.string,
        Serde.string
      )

  def produceMany(
    topic: String,
    kvs: Iterable[(String, String)]
  ): ZIO[Producer, Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](
        Chunk.fromIterable(kvs.map { case (k, v) =>
          new ProducerRecord(topic, k, v)
        }),
        Serde.string,
        Serde.string
      )

  def consumerSettings(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    restartStreamOnRebalancing: Boolean = false,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings] =
    ZIO.serviceWith[Kafka] { (kafka: Kafka) =>
      val settings = ConsumerSettings(kafka.bootstrapServers)
        .withClientId(clientId)
        .withCloseTimeout(5.seconds)
        .withProperties(
          ConsumerConfig.AUTO_OFFSET_RESET_CONFIG        -> "earliest",
          ConsumerConfig.METADATA_MAX_AGE_CONFIG         -> "100",
          ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG       -> "3000",
          ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG    -> "250",
          ConsumerConfig.MAX_POLL_RECORDS_CONFIG         -> "10",
          ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG -> allowAutoCreateTopics.toString
        )
        .withPerPartitionChunkPrefetch(16)
        .withOffsetRetrieval(offsetRetrieval)
        .withRestartStreamOnRebalancing(restartStreamOnRebalancing)
        .withProperties(properties)

      val withClientInstanceId = clientInstanceId.fold(settings)(settings.withGroupInstanceId)
      groupId.fold(withClientInstanceId)(withClientInstanceId.withGroupId)
    }

  def transactionalConsumerSettings(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    restartStreamOnRebalancing: Boolean = false,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings] =
    consumerSettings(
      clientId,
      Some(groupId),
      clientInstanceId,
      allowAutoCreateTopics,
      offsetRetrieval,
      restartStreamOnRebalancing,
      properties
    )
      .map(
        _.withProperties(
          ConsumerConfig.ISOLATION_LEVEL_CONFIG -> "read_committed"
        )
      )

  def consumer(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    restartStreamOnRebalancing: Boolean = false
  ): ZLayer[Kafka, Throwable, Consumer] =
    (ZLayer(
      consumerSettings(
        clientId,
        groupId,
        clientInstanceId,
        allowAutoCreateTopics,
        offsetRetrieval,
        restartStreamOnRebalancing
      )
    ) ++ ZLayer.succeed(diagnostics)) >>> Consumer.live

  def transactionalConsumer(
    clientId: String,
    groupId: String,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    restartStreamOnRebalancing: Boolean = false,
    properties: Map[String, String] = Map.empty,
    rebalanceListener: RebalanceListener = RebalanceListener.noop
  ): ZLayer[Kafka, Throwable, Consumer] =
    (ZLayer(
      transactionalConsumerSettings(
        groupId,
        clientId,
        clientInstanceId,
        allowAutoCreateTopics,
        offsetRetrieval,
        restartStreamOnRebalancing,
        properties
      ).map(_.withRebalanceListener(rebalanceListener))
    ) ++ ZLayer.succeed(diagnostics)) >>> Consumer.live

  def consumeWithStrings[RC](clientId: String, groupId: Option[String] = None, subscription: Subscription)(
    r: (String, String) => URIO[Any, Unit]
  ): RIO[Kafka, Unit] =
    consumerSettings(clientId, groupId, None).flatMap { settings =>
      Consumer.consumeWith[Any, Any, String, String](
        settings,
        subscription,
        Deserializer.string,
        Deserializer.string
      )(r)
    }

  def adminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(AdminClientSettings(_))

  def withAdmin[T](f: AdminClient => RIO[Kafka, T]) =
    for {
      settings <- adminSettings
      fRes <- ZIO.scoped {
                AdminClient
                  .make(settings)
                  .flatMap(client => f(client))
              }
    } yield fRes
}
