package zio.kafka.client

import java.util.UUID

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.Consumer.OffsetRetrieval
import zio.kafka.client.diagnostics.Diagnostics
import zio.kafka.client.serde.Serde
import zio.kafka.client.embedded.Kafka
import zio._

object KafkaTestUtils {
  def producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO.access[Kafka](_.get[Kafka.Service].bootstrapServers).map(ProducerSettings(_))

  def withProducer[R, A, K, V](
    r: Producer[Any, K, V] => RIO[R, A],
    kSerde: Serde[Any, K],
    vSerde: Serde[Any, V]
  ): RIO[R with Blocking with Kafka, A] =
    for {
      settings <- producerSettings
      producer = Producer.make(settings, kSerde, vSerde)
      produced <- producer.use(r)
    } yield produced

  def withProducerStrings[R, A](
    r: Producer[Any, String, String] => RIO[R, A]
  ) =
    withProducer(r, Serde.string, Serde.string)

  def produceOne(t: String, k: String, m: String) =
    withProducerStrings { p =>
      p.produce(new ProducerRecord(t, k, m))
    }.flatten

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

  def consumeWithStrings[R](groupId: String, clientId: String, subscription: Subscription)(
    r: (String, String) => URIO[R, Unit]
  ): RIO[R with Blocking with Clock with Kafka, Unit] =
    for {
      settings <- consumerSettings(groupId, clientId)
      consumed <- Consumer.consumeWith(settings, subscription, Serde.string, Serde.string)(r)
    } yield consumed

  def withConsumer[R, A](
    groupId: String,
    clientId: String,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()
  )(r: Consumer => RIO[R, A]) =
    for {
      settings <- consumerSettings(groupId, clientId, offsetRetrieval)
      consumer = Consumer.make(settings, diagnostics)
      consumed <- consumer.use(r)
    } yield consumed

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
