package zio.kafka.example

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde.Serde
import zio.logging.backend.SLF4J

import java.util.UUID

object Transactional extends ZIOAppDefault {

  /**
   * See `zio-logging` documentation: https://zio.github.io/zio-logging/docs/overview/overview_slf4j
   */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val topic = "transactional-test-topic"

  private def consumerSettings: ZLayer[MyKafka, Throwable, ConsumerSettings] = ZLayer {
    for {
      kafka <- ZIO.service[MyKafka]
    } yield ConsumerSettings(kafka.bootstrapServers)
      .withGroupId("transactional-example-app")
      .withRebalanceSafeCommits(true) // required for transactional producing
      .withMaxRebalanceDuration(30.seconds)
  }

  private val producerSettings: ZLayer[MyKafka, Throwable, TransactionalProducerSettings] = ZLayer {
    for {
      kafka <- ZIO.service[MyKafka]
    } yield TransactionalProducerSettings(
      bootstrapServers = kafka.bootstrapServers,
      transactionalId = UUID.randomUUID().toString
    )
  }

  private val runConsumerStream: ZIO[Consumer & TransactionalProducer, Throwable, Unit] =
    for {
      consumer              <- ZIO.service[Consumer]
      transactionalProducer <- ZIO.service[TransactionalProducer]
      _                     <- ZIO.logInfo(s"Consuming messages from topic $topic...")
      _ <- consumer
             .plainStream(Subscription.topics(topic), Serde.int, Serde.long)
             .mapChunks { records: Chunk[CommittableRecord[Int, Long]] =>
               records.map { record =>
                 val key: Int         = record.record.key()
                 val value: Long      = record.record.value()
                 val newValue: String = "Copy of " + value.toString

                 val producerRecord: ProducerRecord[Int, String] =
                   new ProducerRecord("my-output-topic", key, newValue)
                 (producerRecord, record.offset)
               }
             }
             .mapChunksZIO { recordsAndOffsets: Chunk[(ProducerRecord[Int, String], Offset)] =>
               ZIO.scoped {
                 for {
                   _  <- ZIO.addFinalizer(ZIO.logInfo("Completing transaction"))
                   tx <- transactionalProducer.createTransaction
                   _ <- {
                     val (records, offsets) = recordsAndOffsets.unzip
                     tx.produceChunkBatch(
                       records,
                       Serde.int,
                       Serde.string,
                       OffsetBatch(offsets)
                     )
                   }
                 } yield Chunk.empty
               }.uninterruptible
             }
             .runDrain
    } yield ()

  override def run: ZIO[Scope, Any, Any] =
    ZIO.logInfo("Starting app") *>
      ZIO.addFinalizer(ZIO.logInfo("Stopping app")) *>
      runConsumerStream
        .provide(
          consumerSettings,
          ZLayer.succeed(Consumer.NoDiagnostics),
          Consumer.live,
          producerSettings,
          TransactionalProducer.live,
          MyKafka.embedded
        )

}
