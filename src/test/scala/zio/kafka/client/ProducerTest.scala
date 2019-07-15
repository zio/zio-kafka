package zio.kafka.client

import com.typesafe.scalalogging.LazyLogging

import net.manub.embeddedkafka.{ EmbeddedKafka, EmbeddedKafkaConfig }

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ Serde, Serdes }

import org.scalatest.{ Matchers, WordSpecLike }

import scalaz.zio.{ Chunk, DefaultRuntime, TaskR, UIO, ZIO }
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.duration._

class ProducerTest extends WordSpecLike with Matchers with LazyLogging with DefaultRuntime {

  def pause(): ZIO[Clock, Nothing, Unit] = UIO(()).delay(2.seconds).forever

  def log(s: String): UIO[Unit] = ZIO.effectTotal(logger.info(s))

  implicit val config                     = EmbeddedKafkaConfig(kafkaPort = 0, zooKeeperPort = 0)
  val embeddedKafka                       = EmbeddedKafka.start()
  val bootstrapServer                     = s"localhost:${embeddedKafka.config.kafkaPort}"
  implicit val stringSerde: Serde[String] = Serdes.String()

  val settings =
    ProducerSettings(
      List(bootstrapServer),
      5.seconds,
      Map.empty
    )

  def runWithProducer[A](
    r: Producer[String, String] => TaskR[Blocking with Clock, A]
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
        val withConsumer = Consumer.make[String, String](
          ConsumerSettings(
            List(bootstrapServer),
            "testGroup",
            "testClient",
            5.seconds,
            Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest")
          )
        )
        withConsumer.use { consumer =>
          for {
            outcome  <- producer.produceChunk(chunks).either
            _        <- ZIO.effect(outcome.isRight shouldBe true)
            _        <- ZIO.effect(outcome.toOption.get.size shouldBe 2)
            messages <- consumer.subscribe(Pattern(topic1)) *> consumer.poll(5.seconds)
            _ <- ZIO.effect(messages.exists {
                  case (_, recs) => recs.filter(rec => rec.key == key1 && rec.value == value1).length == 1
                } shouldBe true)
            messages <- consumer.subscribe(Pattern(topic2)) *> consumer.poll(5.seconds)
            _ <- ZIO.effect(messages.exists {
                  case (_, recs) => recs.filter(rec => rec.key == key2 && rec.value == value2).length == 1
                } shouldBe true)
          } yield ()
        }
      }

      "an empty chunk of records" in runWithProducer { producer =>
        val chunks = Chunk.fromIterable(List.empty)
        for {
          outcome <- producer.produceChunk(chunks).either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
          _       <- ZIO.effect(outcome.toOption.get.isEmpty shouldBe true)
        } yield ()
      }
    }
  }
}
