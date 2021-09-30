package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.serde.Serde
import zio.test.Assertion._
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

        val (topic1, key1, value1) = ("topic1", "boo", "baa")
        val (topic2, key2, value2) = ("topic2", "baa", "boo")
        val chunks = Chunk.fromIterable(
          List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
        )
        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            (c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.string).toQueue())
          }

        for {
          outcome  <- Producer.produceChunk(chunks, Serde.string, Serde.string)
          settings <- consumerSettings("testGroup", "testClient")
          record1 <- withConsumer(Topics(Set(topic1)), settings).use { consumer =>
                       for {
                         messages <- consumer.take.flatMap(_.done).mapError(_.getOrElse(new NoSuchElementException))
                         record = messages
                                    .filter(rec => rec.record.key == key1 && rec.record.value == value1)
                                    .toSeq
                       } yield record
                     }
          record2 <- withConsumer(Topics(Set(topic2)), settings).use { consumer =>
                       for {
                         messages <- consumer.take.flatMap(_.done).mapError(_.getOrElse(new NoSuchElementException))
                         record = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
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
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                   t.produce(initialAliceAccount, Serde.string, Serde.int, None)
               }
          settings <- transactionalConsumerSettings("testGroup0", "testClient0")
          recordChunk <- withConsumerInt(Topics(Set("accounts0")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                             record = messages.filter(rec => rec.record.key == "bob")
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
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                   t.produce(initialAliceAccount, Serde.string, Serde.int, None)
               }
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(aliceGives20, Serde.string, Serde.int, None) *>
                   t.produce(bobReceives20, Serde.string, Serde.int, None) *>
                   t.abort
               }.catchSome { case UserInitiatedAbort =>
                 ZIO.unit // silences the abort
               }
          settings <- transactionalConsumerSettings("testGroup1", "testClient1")
          recordChunk <- withConsumerInt(Topics(Set("accounts1")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                             record = messages.filter(rec => rec.record.key == "bob")
                           } yield record
                         }
        } yield assert(recordChunk.map(_.value).last)(equalTo(0))
      },
      testM("serialize concurrent transactions") {
        import Subscription._

        val initialAliceAccount = new ProducerRecord("accounts2", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts2", "bob", 0)

        val transaction1 = TransactionalProducer.createTransaction.use { t =>
          t.produce(initialAliceAccount, Serde.string, Serde.int, None)
        }
        val transaction2 = TransactionalProducer.createTransaction.use { t =>
          t.produce(initialBobAccount, Serde.string, Serde.int, None)
        }

        for {
          _        <- transaction1 <&> transaction2
          settings <- transactionalConsumerSettings("testGroup2", "testClient2")
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
            Serde.int.contramap((_: Int) => throw new RuntimeException("test")),
            None
          )
        }

        assertM(failingTransaction1.unit.run)(dies(hasMessage(equalTo("test"))))
      },
      testM("interleaving transaction with non-transactional consumer") {
        import Subscription._

        val initialAliceAccount = new ProducerRecord("accounts4", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts4", "bob", 0)
        val nonTransactional    = new ProducerRecord("accounts4", "no one", -1)
        val aliceGives20        = new ProducerRecord("accounts4", "alice", 0)

        for {
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                   t.produce(initialAliceAccount, Serde.string, Serde.int, None)
               }
          assertion <- TransactionalProducer.createTransaction.use { t =>
                         for {
                           _        <- t.produce(aliceGives20, Serde.string, Serde.int, None)
                           _        <- Producer.produce(nonTransactional, Serde.string, Serde.int)
                           settings <- consumerSettings("testGroup4", "testClient4")
                           recordChunk <- withConsumerInt(Topics(Set("accounts4")), settings).use { consumer =>
                                            for {
                                              messages <- consumer.take
                                                            .flatMap(_.done)
                                                            .mapError(_.getOrElse(new NoSuchElementException))
                                              record = messages.filter(rec => rec.record.key == "no one")
                                            } yield record
                                          }
                         } yield assert(recordChunk)(isNonEmpty)
                       }
        } yield assertion
      },
      testM("interleaving transaction with transactional consumer should not be read during transaction") {
        import Subscription._

        val initialAliceAccount = new ProducerRecord("accounts5", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts5", "bob", 0)
        val nonTransactional    = new ProducerRecord("accounts5", "no one", -1)
        val aliceGives20        = new ProducerRecord("accounts5", "alice", 0)

        for {
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                   t.produce(initialAliceAccount, Serde.string, Serde.int, None)
               }
          assertion <- TransactionalProducer.createTransaction.use { t =>
                         for {
                           _        <- t.produce(aliceGives20, Serde.string, Serde.int, None)
                           _        <- Producer.produce(nonTransactional, Serde.string, Serde.int)
                           settings <- transactionalConsumerSettings("testGroup5", "testClient5")
                           recordChunk <- withConsumerInt(Topics(Set("accounts5")), settings).use { consumer =>
                                            for {
                                              messages <- consumer.take
                                                            .flatMap(_.done)
                                                            .mapError(_.getOrElse(new NoSuchElementException))
                                              record = messages.filter(rec => rec.record.key == "no one")
                                            } yield record
                                          }
                         } yield assert(recordChunk)(isEmpty)
                       }
        } yield assertion
      },
      testM("interleaving transaction with transactional consumer when aborted") {
        import Subscription._

        val initialAliceAccount = new ProducerRecord("accounts6", "alice", 20)
        val initialBobAccount   = new ProducerRecord("accounts6", "bob", 0)
        val aliceGives20        = new ProducerRecord("accounts6", "alice", 0)
        val nonTransactional    = new ProducerRecord("accounts6", "no one", -1)
        val bobReceives20       = new ProducerRecord("accounts6", "bob", 20)

        for {
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                   t.produce(initialAliceAccount, Serde.string, Serde.int, None)
               }
          _ <- TransactionalProducer.createTransaction.use { t =>
                 t.produce(aliceGives20, Serde.string, Serde.int, None) *>
                   Producer.produce(nonTransactional, Serde.string, Serde.int) *>
                   t.produce(bobReceives20, Serde.string, Serde.int, None) *>
                   t.abort
               }.catchSome { case UserInitiatedAbort =>
                 ZIO.unit // silences the abort
               }
          settings <- transactionalConsumerSettings("testGroup6", "testClient6")
          recordChunk <- withConsumerInt(Topics(Set("accounts6")), settings).use { consumer =>
                           for {
                             messages <- consumer.take
                                           .flatMap(_.done)
                                           .mapError(_.getOrElse(new NoSuchElementException))
                             record = messages.filter(rec => rec.record.key == "no one")
                           } yield record
                         }
        } yield assert(recordChunk)(isNonEmpty)
      },
      testM("committing offsets after a successful transaction") {
        import Subscription._

        val initialAliceAccount  = new ProducerRecord("accounts7", "alice", 20)
        val AliceAccountFeesPaid = new ProducerRecord("accounts7", "alice", 0)

        for {
          _        <- Producer.produce(initialAliceAccount, Serde.string, Serde.int)
          settings <- transactionalConsumerSettings("testGroup7", "testClient7")
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
                    _ <- TransactionalProducer.createTransaction.use { t =>
                           t.produce(
                             AliceAccountFeesPaid,
                             Serde.string,
                             Serde.int,
                             Some(aliceHadMoneyCommittableMessage.offset)
                           )
                         }
                    aliceTopicPartition = new TopicPartition("accounts7", aliceHadMoneyCommittableMessage.partition)
                    committed <- c.committed(Set(aliceTopicPartition))
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
          _        <- Producer.produce(initialAliceAccount, Serde.string, Serde.int)
          settings <- transactionalConsumerSettings("testGroup8", "testClient8")
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
                                     _ <- TransactionalProducer.createTransaction.use { t =>
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
                                     aliceTopicPartition =
                                       new TopicPartition("accounts8", aliceHadMoneyCommittableMessage.partition)
                                     committed <- c.committed(Set(aliceTopicPartition))
                                   } yield committed(aliceTopicPartition)
                                 }
                             }

        } yield assert(committedOffset)(isNone)
      },
      testM("fails if transaction leaks") {
        val test = for {
          transactionThief <- Ref.make(Option.empty[Transaction])
          _ <- TransactionalProducer.createTransaction.use { t =>
                 transactionThief.set(Some(t))
               }
          t <- transactionThief.get
          _ <- t.get.produce("any-topic", 0, 0, Serde.int, Serde.int, None)
        } yield ()
        assertM(test.run)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
      },
      testM("fails if transaction leaks in an open transaction") {
        val test = for {
          transactionThief <- Ref.make(Option.empty[Transaction])
          _ <- TransactionalProducer.createTransaction.use { t =>
                 transactionThief.set(Some(t))
               }
          t <- transactionThief.get
          _ <- TransactionalProducer.createTransaction.use { _ =>
                 t.get.produce("any-topic", 0, 0, Serde.int, Serde.int, None)
               }
        } yield ()
        assertM(test.run)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded ++ ZLayer.identity[Blocking] >>> producer) ++
        (Kafka.embedded ++ ZLayer.identity[Blocking] >>> transactionalProducer) ++
        Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    )
}
