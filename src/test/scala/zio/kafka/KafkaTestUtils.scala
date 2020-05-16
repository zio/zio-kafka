package zio.kafka

import java.util.UUID

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
import zio.kafka.serde.{ Deserializer, Serde, Serializer }
import zio.kafka.producer._

object KafkaTestUtils {
  type StringProducer = Producer[Any, String, String]

  val producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO.access[Kafka](_.get[Kafka.Service].bootstrapServers).map(ProducerSettings(_))

  val stringProducer: ZLayer[Kafka, Throwable, StringProducer] =
    (ZLayer.fromEffect(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) >>>
      Producer.live[Any, String, String]

  def produceOne(
    topic: String,
    key: String,
    message: String
  ): ZIO[Blocking with StringProducer, Throwable, RecordMetadata] =
    Producer.produce[Any, String, String](new ProducerRecord(topic, key, message))

  def produceMany(
    topic: String,
    partition: Int,
    kvs: Iterable[(String, String)]
  ): ZIO[Blocking with StringProducer, Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](Chunk.fromIterable(kvs.map {
        case (k, v) => new ProducerRecord(topic, partition, null, k, v)
      }))

  def produceMany(
    topic: String,
    kvs: Iterable[(String, String)]
  ): ZIO[Blocking with StringProducer, Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](Chunk.fromIterable(kvs.map {
        case (k, v) => new ProducerRecord(topic, k, v)
      }))

  def consumerSettings(groupId: String, clientId: String, offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()) =
    ZIO.access[Kafka] { kafka: Kafka =>
      ConsumerSettings(kafka.get.bootstrapServers)
        .withGroupId(groupId)
        .withClientId(clientId)
        .withCloseTimeout(5.seconds)
        .withProperties(
          ConsumerConfig.AUTO_OFFSET_RESET_CONFIG     -> "earliest",
          ConsumerConfig.METADATA_MAX_AGE_CONFIG      -> "100",
          ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG    -> "1000",
          ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG -> "250",
          ConsumerConfig.MAX_POLL_RECORDS_CONFIG      -> "10"
        )
        .withPerPartitionChunkPrefetch(16)
        .withOffsetRetrieval(offsetRetrieval)
    }

  def consumer(
    groupId: String,
    clientId: String,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZLayer[Kafka with Clock with Blocking, Throwable, Consumer] =
    (ZLayer.fromEffect(consumerSettings(groupId, clientId, offsetRetrieval)) ++
      ZLayer.requires[Clock with Blocking] ++
      ZLayer.succeed(diagnostics)) >>> Consumer.live

  def consumeWithStrings[RC](groupId: String, clientId: String, subscription: Subscription)(
    r: (String, String) => URIO[RC, Unit]
  ): RIO[RC with Blocking with Clock with Kafka, Unit] =
    consumerSettings(groupId, clientId).flatMap { settings =>
      Consumer.consumeWith(
        settings,
        subscription,
        Deserializer.string,
        Deserializer.string
      )(r)
    }

  def adminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO.access[Kafka](_.get[Kafka.Service].bootstrapServers).map(AdminClientSettings(_))

  def withAdmin[T](f: AdminClient => RIO[Clock with Kafka with Blocking, T]) =
    for {
      settings <- adminSettings
      fRes <- AdminClient
               .make(settings)
               .use(client => f(client))
               .provideSomeLayer[Kafka](Clock.live ++ Blocking.live)
    } yield fRes
  def randomThing(prefix: String): Task[String] =
    Task(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")

  def randomTopic: Task[String] = randomThing("topic")

  def randomGroup: Task[String] = randomThing("group")

}
