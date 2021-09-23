package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.{ Producer, Transaction, TransactionLeaked }
import zio.kafka.producer.UserInitiatedAbort
import zio.kafka.serde.Serde
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object TransactionsSpec extends DefaultRunnableSpec {
  def withConsumerInt(subscription: Subscription, settings: ConsumerSettings) =
    Consumer.make(settings).flatMap { c =>
      c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.int).toQueue()
    }

  override def spec =
    suite("transactional producer test suite")(
      testM("a simple transaction") {
        import Subscription._

        val initialAliceAccount = new ProducerRecord("accounts0", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts0", "bob", 0)

        for {
          _           <- Producer.beginTransaction.use { t =>
                           t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                             t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                         }
          settings    <- transactionalConsumerSettings("testGroup0", "testClient0")
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
          _           <- Producer.beginTransaction.use { t =>
                           t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                             t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                         }
          _           <- Producer.beginTransaction.use { t =>
                           t.produce(aliceGives20, Serde.string, Serde.int, None) *>
                             t.produce(bobReceives20, Serde.string, Serde.int, None) *>
                             t.abort
                         }.catchSome { case UserInitiatedAbort =>
                           ZIO.unit // silences the abort
                         }
          settings    <- transactionalConsumerSettings("testGroup1", "testClient1")
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

        val transaction1 = Producer.beginTransaction.use { t =>
          t.produce(initialAliceAccount, Serde.string, Serde.int, None)
        }
        val transaction2 = Producer.beginTransaction.use { t =>
          t.produce(initialBobAccount, Serde.string, Serde.int, None)
        }

        for {
          _           <- transaction1 <&> transaction2
          settings    <- transactionalConsumerSettings("testGroup2", "testClient2")
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

        val failingTransaction1 = Producer.beginTransaction.use { t =>
          t.produce(
            initialBobAccount,
            Serde.string,
            Serde.int.contramap((_: Int) => throw new RuntimeException("test")),
            None
          )
        }

        assertM(failingTransaction1.unit.run)(dies(hasMessage(equalTo("test"))))
      },
      testM("committing offsets after a successful transaction") {
        import Subscription._

        val initialAliceAccount  = new ProducerRecord("accounts7", "alice", 20)
        val AliceAccountFeesPaid = new ProducerRecord("accounts7", "alice", 0)

        for {
          _               <- Producer.beginTransaction.use(_.produce(initialAliceAccount, Serde.string, Serde.int, None))
          settings        <- transactionalConsumerSettings("testGroup7", "testClient7")
          committedOffset <-
            Consumer.make(settings).use { c =>
              c.subscribe(Topics(Set("accounts7"))) *> c
                .plainStream(Serde.string, Serde.int)
                .toQueue()
                .use { q =>
                  val readAliceAccount = for {
                    messages <- q.take
                                  .flatMap(_.done)
                                  .mapError(_.getOrElse(new NoSuchElementException))
                  } yield messages.head
                  for {
                    aliceHadMoneyCommittableMessage <- readAliceAccount
                    _                               <- Producer.beginTransaction.use { t =>
                                                         t.produce(
                                                           AliceAccountFeesPaid,
                                                           Serde.string,
                                                           Serde.int,
                                                           Some(aliceHadMoneyCommittableMessage.offset)
                                                         )
                                                       }
                    aliceTopicPartition              = new TopicPartition("accounts7", aliceHadMoneyCommittableMessage.partition)
                    committed                       <- c.committed(Set(aliceTopicPartition))
                  } yield committed(aliceTopicPartition)
                }
            }

        } yield assert(committedOffset.get.offset())(equalTo(1L))
      },
      testM("not committing offsets after a failed transaction") {
        import Subscription._

        val initialAliceAccount  = new ProducerRecord("accounts8", "alice", 20)
        val AliceAccountFeesPaid = new ProducerRecord("accounts8", "alice", 0)

        for {
          _               <- Producer.beginTransaction.use(_.produce(initialAliceAccount, Serde.string, Serde.int, None))
          settings        <- transactionalConsumerSettings("testGroup8", "testClient8")
          committedOffset <- Consumer.make(settings).use { c =>
                               c.subscribe(Topics(Set("accounts8"))) *> c
                                 .plainStream(Serde.string, Serde.int)
                                 .toQueue()
                                 .use { q =>
                                   val readAliceAccount = for {
                                     messages <- q.take
                                                   .flatMap(_.done)
                                                   .mapError(_.getOrElse(new NoSuchElementException))
                                   } yield messages.head
                                   for {
                                     aliceHadMoneyCommittableMessage <- readAliceAccount
                                     _                               <- Producer.beginTransaction.use { t =>
                                                                          t.produce(
                                                                            AliceAccountFeesPaid,
                                                                            Serde.string,
                                                                            Serde.int,
                                                                            Some(aliceHadMoneyCommittableMessage.offset)
                                                                          ) *>
                                                                            t.abort
                                                                        }.catchSome { case UserInitiatedAbort =>
                                                                          ZIO.unit // silences the abort
                                                                        }
                                     aliceTopicPartition              =
                                       new TopicPartition("accounts8", aliceHadMoneyCommittableMessage.partition)
                                     committed                       <- c.committed(Set(aliceTopicPartition))
                                   } yield committed(aliceTopicPartition)
                                 }
                             }

        } yield assert(committedOffset)(isNone)
      },
      testM("fails if transaction leaks") {
        val test = for {
          transactionThief <- Ref.make(Option.empty[Transaction])
          _                <- Producer.beginTransaction.use { t =>
                                transactionThief.set(Some(t))
                              }
          t                <- transactionThief.get
          _                <- t.get.produce("any-topic", 0, 0, Serde.int, Serde.int, None)
        } yield ()
        assertM(test.run)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
      },
      testM("fails if transaction leaks in an open transaction") {
        val test = for {
          transactionThief <- Ref.make(Option.empty[Transaction])
          _                <- Producer.beginTransaction.use { t =>
                                transactionThief.set(Some(t))
                              }
          t                <- transactionThief.get
          _                <- Producer.beginTransaction.use { _ =>
                                t.get.produce("any-topic", 0, 0, Serde.int, Serde.int, None)
                              }
        } yield ()
        assertM(test.run)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded ++ ZLayer.identity[Blocking] >>> transactionalProducer) ++
        Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    )
}
