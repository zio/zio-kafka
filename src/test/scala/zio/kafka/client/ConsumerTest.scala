package zio.kafka.client

import com.typesafe.scalalogging.LazyLogging
import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ Serde, Serdes }
import org.scalatest.{ Matchers, WordSpecLike }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import org.apache.kafka.common.TopicPartition
import zio.stream.ZSink

class ConsumerTest extends WordSpecLike with Matchers with LazyLogging with DefaultRuntime {
  import KafkaTestUtils._

  def pause(): ZIO[Clock, Nothing, Unit] = UIO(()).delay(2.seconds).forever

  def log(s: String): UIO[Unit] = ZIO.effectTotal(logger.info(s))

  val embeddedKafka                       = EmbeddedKafka.start()
  val bootstrapServer                     = s"localhost:${embeddedKafka.config.kafkaPort}"
  implicit val stringSerde: Serde[String] = Serdes.String()

  def settings(groupId: String, clientId: String) =
    ConsumerSettings(
      List(bootstrapServer),
      groupId,
      clientId,
      5.seconds,
      Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest"),
      250.millis,
      250.millis,
      1
    )

  def runWithConsumer[A](groupId: String, clientId: String)(
    r: Consumer[String, String] => RIO[Blocking with Clock, A]
  ): A =
    unsafeRun(
      Consumer.make[String, String](settings(groupId, clientId)).use(r)
    )

  "A string consumer" when {
    "polling" should {
      "receive messages produced on the topic" in runWithConsumer("group150", "client150") { consumer =>
        for {
          _       <- consumer.subscribe(Subscription.Topics(Set("topic150")))
          kvs     <- ZIO((1 to 5).toList.map(i => (s"key$i", s"msg$i")))
          _       <- produceMany("topic150", kvs)
          records <- consumer.plain.flattenChunks.take(5).runCollect
          _ <- ZIO.effectTotal(records.map { r =>
                (r.record.key, r.record.value)
              } shouldEqual kvs)
        } yield ()
      }
    }

    "committing" should {
      "restart from the committed position" in unsafeRun {
        val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
        for {
          _ <- produceMany("topic1", 0, data)
          firstResults <- Consumer.make[String, String](settings("group1", "first")).use { consumer =>
                           for {
                             _ <- consumer.subscribe(Subscription.Topics(Set("topic1")))
                             results <- consumer.partitioned
                                         .filter(_._1 == new TopicPartition("topic1", 0))
                                         .flatMap(_._2.flattenChunks)
                                         .take(5)
                                         .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                                         .mapM { committableRecords =>
                                           val records = committableRecords.map(_.record)
                                           val offsetBatch =
                                             committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                           offsetBatch.commit.as(records)
                                         }
                                         .mapConcat(Chunk.fromIterable)
                                         .runCollect
                           } yield results
                         }
          secondResults <- Consumer.make[String, String](settings("group1", "second")).use { consumer =>
                            for {
                              _ <- consumer.subscribe(Subscription.Topics(Set("topic1")))
                              results <- consumer.partitioned
                                          .flatMap(_._2.flattenChunks)
                                          .take(5)
                                          .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                                          .mapM { committableRecords =>
                                            val records = committableRecords.map(_.record)
                                            val offsetBatch =
                                              committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                            offsetBatch.commit.as(records)
                                          }
                                          .mapConcat(Chunk.fromIterable)
                                          .runCollect
                            } yield results
                          }
        } yield (firstResults ++ secondResults).map(rec => rec.key() -> rec.value()) shouldEqual data
      }
    }
  }
}
