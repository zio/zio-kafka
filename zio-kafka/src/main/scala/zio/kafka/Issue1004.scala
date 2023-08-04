package zio.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.admin.{ AdminClient, AdminClientSettings }
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer.Subscription.Topics
import zio.kafka.consumer.{ CommittableRecord, Consumer, ConsumerSettings, OffsetBatch }
import zio.kafka.producer._
import zio.logging.backend.SLF4J
import zio.kafka.serde.Serde
import zio.stream.ZSink

object ReproducerProducer extends ZIOAppDefault {

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Nothing, Any] = {
    val appLogic =
      (ZIO.serviceWithZIO[AdminClient](_.createTopic(NewTopic("_test_topic1", 2, 1))).ignore *>
        ZIO.serviceWithZIO[AdminClient](_.createTopic(NewTopic("_test_topic2", 2, 1))).ignore *>
        ZIO.foreach(Chunk.fromIterable((1 to 200000))) { _ =>
          for {
            key   <- ZIO.randomWith(_.nextUUID)
            value <- ZIO.randomWith(_.nextInt)
          } yield new ProducerRecord("_test_topic1", key.toString, value.toString)
        }).flatMap(Producer.produceChunk(_, Serde.string, Serde.string))

    val producerSettings = ProducerSettings(List("localhost:9092"))

    appLogic
      .provide(
        ZLayer.scoped(Producer.make(producerSettings)),
        ZLayer.succeed(AdminClientSettings(List("localhost:9092"))),
        AdminClient.live,
        zio.Runtime.removeDefaultLoggers,
        SLF4J.slf4j
      )
      .tapErrorCause(ZIO.debug(_))
      .exit

  }

}

object ReproducerConsumer extends ZIOAppDefault { self =>

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Nothing, Any] = {

    val consumerSettings = ConsumerSettings(List("localhost:9092"))
      .withGroupId("test_consumer_group")
      .withCloseTimeout(30000.milliseconds)
      .withPollTimeout(50.milliseconds)
      .withOffsetRetrieval(OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest))
      .withProperties(
        ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG    -> "45000",
        ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG    -> "30000",
        ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG -> "30000",
        ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG     -> "10000",
        ConsumerConfig.FETCH_MIN_BYTES_CONFIG       -> "1",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG      -> "500",
        ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG  -> "300000"
      )

    val consumer = Consumer
      .partitionedStream(Topics(Set("_test_topic1", "_test_topic2")), Serde.string, Serde.string)
      .flatMapPar(10) { case (_, partition) =>
        partition
          .aggregateAsyncWithin(
            ZSink.foldUntil[CommittableRecord[String, String], (OffsetBatch, Chunk[CommittableRecord[String, String]])](
              (OffsetBatch.empty, Chunk.empty[CommittableRecord[String, String]]),
              2048
            ) { case ((offsets, records), record) =>
              (offsets.add(record.offset), records.appended(record))
            },
            Schedule.fixed(5.seconds)
          )
      }
      .mapZIO { case (offsets, records) =>
        for {
          _ <- ZIO.logDebug(s"Consumer processing batch of ${records.size}. Committing offset...")
          _ <- offsets.commitOrRetry(Schedule.spaced(100.milliseconds) && Schedule.recurs(10))
          _ <- ZIO.logDebug("Commit done")
        } yield ()
      }
      .provideLayer(ZLayer.scoped(Consumer.make(consumerSettings)))

    val producerSettings = ProducerSettings(List("localhost:9092"))

    consumer.runDrain
      .provide(ZLayer.scoped(Producer.make(producerSettings)) ++ zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j)
      .tapErrorCause(ZIO.debug(_))
      .exitCode
  }

}
