package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.clock.Clock
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.stream.Take
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object ProducerSpec extends DefaultRunnableSpec {
  override def spec =
    suite("producer test suite")(
      testM("one record") {
        for {
          _ <- Producer.produce[Any, String, String](new ProducerRecord("topic", "boo", "baa"))
        } yield assertCompletes
      },
      testM("a non-empty chunk of records") {
        import Subscription._

        val (topic1, key1, value1) = ("topic1", "boo", "baa")
        val (topic2, key2, value2) = ("topic2", "baa", "boo")
        val chunks = Chunk.fromIterable(
          List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
        )
        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).build.flatMap { service =>
            val c = service.get
            (c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.string).toQueue())
          }

        for {
          outcome  <- Producer.produceChunk[Any, String, String](chunks).flatten
          settings <- consumerSettings("testGroup", "testClient")
          record1 <- withConsumer(Topics(Set(topic1)), settings).use { consumer =>
                      for {
                        messages <- Take.option(consumer.take).someOrFail(new NoSuchElementException)
                        record = messages
                          .filter(rec => rec.record.key == key1 && rec.record.value == value1)
                          .toSeq
                      } yield record
                    }
          record2 <- withConsumer(Topics(Set(topic2)), settings).use { consumer =>
                      for {
                        messages <- Take.option(consumer.take).someOrFail(new NoSuchElementException)
                        record   = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
                      } yield record
                    }
        } yield {
          assert(outcome.length)(equalTo(2)) &&
          assert(record1)(isNonEmpty) &&
          assert(record2.length)(isGreaterThan(0))
        }
      },
      testM("an empty chunk of records") {
        val chunks = Chunk.fromIterable(List.empty)
        for {
          outcome <- Producer.produceChunk[Any, String, String](chunks).flatten
        } yield assert(outcome.length)(equalTo(0))
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded >>> stringProducer) ++ Kafka.embedded).mapError(TestFailure.fail) ++ Clock.live
    )
}
