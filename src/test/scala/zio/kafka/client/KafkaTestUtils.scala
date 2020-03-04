package zio.kafka.client

import java.util.UUID

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.Consumer.OffsetRetrieval
import zio.kafka.client.Producer.Service
import zio.kafka.client.diagnostics.Diagnostics
import zio.kafka.client.embedded.Kafka
import zio.kafka.client.serde.{ Deserializer, Serde, Serializer }

object KafkaTestUtils {
  def producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO.access[Kafka](_.get[Kafka.Service].bootstrapServers).map(ProducerSettings(_))

  type StringProducer = Producer[Any, String, String]
  type StringConsumer = Consumer[Any, String, String]

  val testProducer: ZLayer[Kafka, Throwable, StringProducer] =
    (ZLayer.fromEffect(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) >>>
      Producer.live[Any, String, String]

  def withProducerStrings[R, A](
    r: Producer.Service[Any, String, String] => RIO[R, A]
  ): ZIO[R with StringProducer, Throwable, A] =
    ZIO.accessM(env => r(env.get))

  def produce[R, K, V](
    record: ProducerRecord[K, V]
  )(implicit ts: Tagged[Service[R, K, V]]): RIO[R with Producer[R, K, V] with Blocking, Task[RecordMetadata]] =
    ZIO.accessM(_.get.produce(record))

  def produceOne(t: String, k: String, m: String): ZIO[Blocking with StringProducer, Throwable, RecordMetadata] =
    produce[Any, String, String](new ProducerRecord(t, k, m)).flatten

  def produceMany(t: String, kvs: Iterable[(String, String)]) =
    withProducerStrings { p =>
      val records = kvs.map {
        case (k, v) => new ProducerRecord[String, String](t, k, v)
      }
      val chunk = Chunk.fromIterable(records)
      p.produceChunk(chunk)
    }.flatten

  def produceMany(topic: String, partition: Int, kvs: Iterable[(String, String)]) =
    withProducerStrings { p =>
      val records = kvs.map {
        case (k, v) => new ProducerRecord[String, String](topic, partition, null, k, v)
      }
      val chunk = Chunk.fromIterable(records)
      p.produceChunk(chunk)
    }.flatten

  def consumerSettings(groupId: String, clientId: String, offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()) =
    ZIO
      .access[Kafka](_.get[Kafka.Service].bootstrapServers)
      .map(
        ConsumerSettings(_)
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
      )

  def withConsumerStrings[RC, A](
    groupId: String,
    clientId: String,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()
  )(
    r: Consumer.Service[Any, String, String] => RIO[RC, A]
  ): ZIO[RC with Clock with Blocking with Kafka, Throwable, A] = {
    val layers = (ZLayer.requires[Clock] ++ ZLayer
      .requires[Blocking] ++ ZLayer.succeed(diagnostics) ++
      ZLayer.succeed(Serde.string: Deserializer[Any, String]) ++ consumerSettings(groupId, clientId, offsetRetrieval).toLayer) >>> Consumer.live
    layers.build.use { consumer =>
      r(consumer.get)
    }
  }

  def consumeWithStrings[RC](groupId: String, clientId: String, subscription: Subscription)(
    r: (String, String) => URIO[RC, Unit]
  ): RIO[RC with Blocking with Clock with Kafka, Unit] =
    withConsumerStrings(groupId, clientId)(_.consumeWith(subscription)(r))

  def adminSettings =
    ZIO.access[Kafka](_.get[Kafka.Service].bootstrapServers).map(AdminClientSettings(_))

  def withAdmin[T](f: AdminClient => RIO[Clock with Kafka with Blocking, T]) =
    for {
      settings <- adminSettings
      fRes <- AdminClient
               .make(settings)
               .use { client =>
                 f(client)
               }
               .provideSomeLayer[Kafka](Clock.live ++ Blocking.live)
    } yield fRes

  def randomThing(prefix: String) =
    for {
      l <- Task(UUID.randomUUID())
    } yield s"$prefix-$l"

  def randomTopic = randomThing("topic")

  def randomGroup = randomThing("group")

}
