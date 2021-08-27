package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.TransactionalProducer.UserInitiatedAbort
import zio.kafka.serde.Serde
import zio.test.Assertion._
import zio.test.TestAspect.ignore
import zio.test._
import zio.test.environment.TestEnvironment

object ProducerSpec extends DefaultRunnableSpec {
  def withConsumerInt(subscription: Subscription, settings: ConsumerSettings) =
    Consumer.make(settings).flatMap { c =>
      c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.int).toQueue()
    }

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

        val initialAliceAccount = new ProducerRecord("accounts0", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts0", "bob", 0)

        for {
          _           <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(initialBobAccount, Serde.string, Serde.int) *>
                             t.produce(initialAliceAccount, Serde.string, Serde.int)
                         }
          settings    <- consumerSettings("testGroup0", "testClient0")
          recordChunk <- withConsumerInt(Topics(Set("accounts0")), settings).use { consumer =>
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

        val initialAliceAccount = new ProducerRecord("accounts1", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts1", "bob", 0)
        val aliceGives20        = new ProducerRecord("accounts1", "alice", 0)
        val bobReceives20       = new ProducerRecord("accounts1", "bob", 20)

        for {
          _           <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(initialBobAccount, Serde.string, Serde.int) *>
                             t.produce(initialAliceAccount, Serde.string, Serde.int)
                         }
          _           <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(aliceGives20, Serde.string, Serde.int) *>
                             t.produce(bobReceives20, Serde.string, Serde.int) *>
                             t.abort
                         }.catchSome { case UserInitiatedAbort =>
                           ZIO.unit // silences the abort
                         }
          settings    <- consumerSettings("testGroup1", "testClient1")
          recordChunk <- withConsumerInt(Topics(Set("accounts1")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                             record    = messages.filter(rec => rec.record.key == "bob")
                           } yield record
                         }
        } yield assert(recordChunk.map(_.value).last)(equalTo(0))
      },
      testM("serialize concurrent transactions") {
        import Subscription._

        val initialAliceAccount = new ProducerRecord("accounts2", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts2", "bob", 0)

        val transaction1 = TransactionalProducer.createTransaction.use { t =>
          t.produce(initialAliceAccount, Serde.string, Serde.int)
        }
        val transaction2 = TransactionalProducer.createTransaction.use { t =>
          t.produce(initialBobAccount, Serde.string, Serde.int)
        }

        for {
          _           <- transaction1 <&> transaction2
          settings    <- consumerSettings("testGroup2", "testClient2")
          recordChunk <- withConsumerInt(Topics(Set("accounts2")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                           } yield messages
                         }
        } yield assert(recordChunk.map(_.value))(contains(0) && contains(20))
      },
      testM("exception management") {
        val initialBobAccount = new ProducerRecord("accounts3", "bob", 0)

        val failingTransaction1 = TransactionalProducer.createTransaction.use { t =>
          t.produce(
            initialBobAccount,
            Serde.string,
            Serde.int.contramap((_: Int) => throw new RuntimeException("test"))
          )
        }

        val failingBob = for {
          _ <- failingTransaction1
        } yield ()
        assertM(failingBob.run)(dies(hasMessage(equalTo("test"))))
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded ++ ZLayer.identity[Blocking] >>> producer) ++
        (Kafka.embedded ++ ZLayer.identity[Blocking] >>> transactionalProducer) ++
        Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    )
}
