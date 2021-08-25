package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object ProducerSpec extends DefaultRunnableSpec {
  override def spec =
    suite("producer test suite")(
      testM("one record") {
        for {
          _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
        } yield assertCompletes
      },
      testM("a non-empty chunk of records") {
        import Subscription._

        val (topic1, key1, value1)                                               = ("topic1", "boo", "baa")
        val (topic2, key2, value2)                                               = ("topic2", "baa", "boo")
        val chunks                                                               = Chunk.fromIterable(
          List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
        )
        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            (c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.string).toQueue())
          }

        for {
          outcome  <- Producer.produceChunk(chunks, Serde.string, Serde.string)
          settings <- consumerSettings("testGroup", "testClient")
          record1  <- withConsumer(Topics(Set(topic1)), settings).use { consumer =>
                        for {
                          messages <- consumer.take.flatMap(_.done).mapError(_.getOrElse(new NoSuchElementException))
                          record    = messages
                                        .filter(rec => rec.record.key == key1 && rec.record.value == value1)
                                        .toSeq
                        } yield record
                      }
          record2  <- withConsumer(Topics(Set(topic2)), settings).use { consumer =>
                        for {
                          messages <- consumer.take.flatMap(_.done).mapError(_.getOrElse(new NoSuchElementException))
                          record    = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
                        } yield record
                      }
        } yield assert(outcome.length)(equalTo(2)) &&
          assert(record1)(isNonEmpty) &&
          assert(record2.length)(isGreaterThan(0))
      },
      testM("an empty chunk of records") {
        val chunks = Chunk.fromIterable(List.empty)
        for {
          outcome <- Producer.produceChunk(chunks, Serde.string, Serde.string)
        } yield assert(outcome.length)(equalTo(0))
      },
      testM("export metrics") {
        for {
          metrics <- Producer.metrics
        } yield assert(metrics)(isNonEmpty)
      },
      testM("a simple transaction") {
        import Subscription._

        val initialAliceAccount                                                  = new ProducerRecord("accounts", "alice", 20)
        val initialBobAccount                                                    = new ProducerRecord("accounts", "bob", 0)
        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.int).toQueue()
          }

        for {
          _           <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(initialBobAccount, Serde.string, Serde.int) *>
                             t.produce(initialAliceAccount, Serde.string, Serde.int)
                         }
          settings    <- consumerSettings("testGroup", "testClient")
          recordChunk <- withConsumer(Topics(Set("accounts")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                             record    = messages.filter(rec => rec.record.key == "bob")
                           } yield record
                         }
        } yield assert(recordChunk.map(_.value).last)(equalTo(0))
      },
      testM("an aborted transaction should not be read") {
        import Subscription._

        val initialAliceAccount                                                  = new ProducerRecord("accounts", "alice", 20)
        val initialBobAccount                                                    = new ProducerRecord("accounts", "bob", 0)
        val aliceGives20                                                         = new ProducerRecord("accounts", "alice", 0)
        val bobReceives20                                                        = new ProducerRecord("accounts", "bob", 20)
        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.int).toQueue()
          }

        for {
          _           <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(initialBobAccount, Serde.string, Serde.int) *>
                             t.produce(initialAliceAccount, Serde.string, Serde.int)
                         }
          _           <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(aliceGives20, Serde.string, Serde.int) *>
                             t.produce(bobReceives20, Serde.string, Serde.int) *>
                             t.abort
                         }
          settings    <- consumerSettings("testGroup", "testClient")
          recordChunk <- withConsumer(Topics(Set("accounts")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                             record    = messages.filter(rec => rec.record.key == "bob")
                           } yield record
                         }
        } yield assert(recordChunk.map(_.value).last)(equalTo(0))
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded ++ ZLayer.identity[Blocking] >>> producer) ++
        (Kafka.embedded ++ ZLayer.identity[Blocking] >>> transactionalProducer) ++
        Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    )
}
