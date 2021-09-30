package zio.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.admin._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.embedded.Kafka
import zio.kafka.producer._
import zio.kafka.serde.{ Deserializer, Serde, Serializer }

import java.util.UUID

object KafkaTestUtils {

  val producerSettings: ZIO[Has[Kafka], Nothing, ProducerSettings] =
    ZIO.access[Has[Kafka]](_.get[Kafka].bootstrapServers).map(ProducerSettings(_))

  val producer: ZLayer[Has[Kafka] with Blocking, Throwable, Has[Producer]] =
    (ZLayer.fromEffect(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) ++ ZLayer
      .identity[Blocking] >>>
      Producer.live

  val transactionalProducerSettings: ZIO[Has[Kafka], Nothing, TransactionalProducerSettings] =
    ZIO.access[Has[Kafka]](_.get[Kafka].bootstrapServers).map(TransactionalProducerSettings(_, "test-transaction"))

  val transactionalProducer: ZLayer[Has[Kafka] with Blocking, Throwable, Has[TransactionalProducer]] =
    (ZLayer.fromEffect(transactionalProducerSettings) ++ ZLayer.succeed(
      Serde.string: Serializer[Any, String]
    )) ++ ZLayer
      .identity[Blocking] >>>
      TransactionalProducer.live

  def produceOne(
    topic: String,
    key: String,
    message: String
  ): ZIO[Blocking with Has[Producer], Throwable, RecordMetadata] =
    Producer.produce[Any, String, String](new ProducerRecord(topic, key, message), Serde.string, Serde.string)

  def produceMany(
    topic: String,
    partition: Int,
    kvs: Iterable[(String, String)]
  ): ZIO[Blocking with Has[Producer], Throwable, Chunk[RecordMetadata]] =
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
  ): ZIO[Blocking with Has[Producer], Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](
        Chunk.fromIterable(kvs.map { case (k, v) =>
          new ProducerRecord(topic, k, v)
        }),
        Serde.string,
        Serde.string
      )

  def consumerSettings(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()
  ): URIO[Has[Kafka], ConsumerSettings] =
    ZIO.access[Has[Kafka]] { (kafka: Has[Kafka]) =>
      val settings = ConsumerSettings(kafka.get.bootstrapServers)
        .withGroupId(groupId)
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
      clientInstanceId.fold(settings)(settings.withGroupInstanceId)
    }

  def transactionalConsumerSettings(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()
  ): URIO[Has[Kafka], ConsumerSettings] =
    consumerSettings(groupId, clientId, clientInstanceId, allowAutoCreateTopics, offsetRetrieval)
      .map(
        _.withProperties(
          ConsumerConfig.ISOLATION_LEVEL_CONFIG -> "read_committed"
        )
      )

  def consumer(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZLayer[Has[Kafka] with Clock with Blocking, Throwable, Has[Consumer]] =
    (consumerSettings(groupId, clientId, clientInstanceId, allowAutoCreateTopics, offsetRetrieval).toLayer ++
      ZLayer.requires[Clock with Blocking] ++
      ZLayer.succeed(diagnostics)) >>> Consumer.live

  def consumeWithStrings[RC](groupId: String, clientId: String, subscription: Subscription)(
    r: (String, String) => URIO[RC, Unit]
  ): RIO[RC with Has[Kafka] with Blocking with Clock, Unit] =
    consumerSettings(groupId, clientId, None).flatMap { settings =>
      Consumer.consumeWith(
        settings,
        subscription,
        Deserializer.string,
        Deserializer.string
      )(r)
    }

  def adminSettings: ZIO[Has[Kafka], Nothing, AdminClientSettings] =
    ZIO.access[Has[Kafka]](_.get[Kafka].bootstrapServers).map(AdminClientSettings(_))

  def withAdmin[T](f: AdminClient => RIO[Clock with Has[Kafka] with Blocking, T]) =
    for {
      settings <- adminSettings
      fRes     <- AdminClient
                    .make(settings)
                    .use(client => f(client))
                    .provideSomeLayer[Has[Kafka]](Clock.live ++ Blocking.live)
    } yield fRes

  def randomThing(prefix: String): Task[String] =
    Task(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")

  def randomTopic: Task[String] = randomThing("topic")

  def randomGroup: Task[String] = randomThing("group")

}
