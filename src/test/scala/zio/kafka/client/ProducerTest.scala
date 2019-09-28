package zio.kafka.client

import com.typesafe.scalalogging.LazyLogging
import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.scalatest.{ Matchers, WordSpecLike }
import zio.{ Chunk, DefaultRuntime, RIO, UIO, ZIO }
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.stream.Take

class ProducerTest extends WordSpecLike with Matchers with LazyLogging with DefaultRuntime {

  def pause(): ZIO[Clock, Nothing, Unit] = UIO(()).delay(2.seconds).forever

  def log(s: String): UIO[Unit] = ZIO.effectTotal(logger.info(s))

  implicit val config = EmbeddedKafkaConfig(kafkaPort = 0, zooKeeperPort = 0)
  val embeddedKafka   = EmbeddedKafka.start()
  val bootstrapServer = s"localhost:${embeddedKafka.config.kafkaPort}"

  val settings =
    ProducerSettings(
      List(bootstrapServer),
      5.seconds,
      Map.empty
    )

  def runWithProducer[A](
    r: Producer[String, String] => RIO[Blocking with Clock, A]
  ): A =
    unsafeRun(
      Producer.make[String, String](settings).use(r)
    )

  "A string producer" can {
    "produce" should {
      "one record" in runWithProducer { producer =>
        for {
          outcome <- producer.produce(new ProducerRecord("topic", "boo", "baa")).either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
        } yield ()
      }

      "a non empty chunk of records" in runWithProducer { producer =>
        import Subscription._

        val (topic1, key1, value1) = ("topic1", "boo", "baa")
        val (topic2, key2, value2) = ("topic2", "baa", "boo")
        val chunks = Chunk.fromIterable(
          List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
        )
        def withConsumer(subscription: Subscription) =
          Consumer
            .make[String, String](
              ConsumerSettings(
                List(bootstrapServer),
                "testGroup",
                "testClient",
                5.seconds,
                Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest"),
                250.millis,
                250.millis,
                1
              )
            )
            .flatMap(c => c.subscribe(subscription).toManaged_ *> c.plainStream.toQueue())

        for {
          outcome <- producer.produceChunk(chunks).either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
          _       <- ZIO.effect(outcome.right.get.size shouldBe 2)
          _ <- withConsumer(Topics(Set(topic1))).use { consumer =>
                for {
                  messages <- Take.option(consumer.take).someOrFail(new NoSuchElementException)
                  record   = messages.filter(rec => rec.record.key == key1 && rec.record.value == value1).toSeq
                  _        <- ZIO.effect(record should not be empty)
                } yield ()
              }
          _ <- withConsumer(Topics(Set(topic2))).use { consumer =>
                for {
                  messages <- Take.option(consumer.take).someOrFail(new NoSuchElementException)
                  record   = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
                  _        <- ZIO.effect(record should not be empty)
                } yield ()
              }
        } yield ()
      }

      "an empty chunk of records" in runWithProducer { producer =>
        val chunks = Chunk.fromIterable(List.empty)
        for {
          outcome <- producer.produceChunk(chunks).either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
          _       <- ZIO.effect(outcome.right.get.isEmpty shouldBe true)
        } yield ()
      }
    }
  }
}
