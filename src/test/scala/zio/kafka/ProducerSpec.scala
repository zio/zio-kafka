package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ Consumer, ConsumerSettings, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.serde.Serde
import zio.test.Assertion._
import zio.test._

object ProducerSpec extends DefaultRunnableSpec {
  def withConsumerInt(subscription: Subscription, settings: ConsumerSettings) =
    Consumer.make(settings).flatMap { c =>
      c.subscribe(subscription).toManaged *> c.plainStream(Serde.string, Serde.int).toQueue()
    }

  override def spec =
    suite("producer test suite")(
      test("one record") {
        for {
          _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
        } yield assertCompletes
      },
      test("a non-empty chunk of records") {
        import Subscription._

        val (topic1, key1, value1) = ("topic1", "boo", "baa")
        val (topic2, key2, value2) = ("topic2", "baa", "boo")
        val chunks = Chunk.fromIterable(
          List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
        )
        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            (c.subscribe(subscription).toManaged *> c.plainStream(Serde.string, Serde.string).toQueue())
          }

        for {
          outcome  <- Producer.produceChunk(chunks, Serde.string, Serde.string)
          settings <- consumerSettings("testClient", Some("testGroup"))
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
        } yield assertTrue(outcome.length == 2) &&
          assertTrue(record1.nonEmpty) &&
          assertTrue(record2.length > 0)
      },
      test("an empty chunk of records") {
        val chunks = Chunk.fromIterable(List.empty)
        for {
          outcome <- Producer.produceChunk(chunks, Serde.string, Serde.string)
        } yield assertTrue(outcome.length == 0)
      },
      test("export metrics") {
        for {
          metrics <- Producer.metrics
        } yield assertTrue(metrics.nonEmpty)
      },
      test("a simple transaction") {
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
        } yield assertTrue(recordChunk.map(_.value).last == 0)
      },
      test("an aborted transaction should not be read") {
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
        } yield assertTrue(recordChunk.map(_.value).last == 0)
      },
      test("serialize concurrent transactions") {
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
      test("exception management") {
        val initialBobAccount = new ProducerRecord("accounts3", "bob", 0)

        val failingTransaction1 = TransactionalProducer.createTransaction.use { t =>
          t.produce(
            initialBobAccount,
            Serde.string,
            Serde.int.contramap((_: Int) => throw new RuntimeException("test")),
            None
          )
        }

        assertM(failingTransaction1.unit.exit)(dies(hasMessage(equalTo("test"))))
      },
      test("interleaving transaction with non-transactional consumer") {
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
                           settings <- consumerSettings("testClient4", Some("testGroup4"))
                           recordChunk <- withConsumerInt(Topics(Set("accounts4")), settings).use { consumer =>
                                            for {
                                              messages <- consumer.take
                                                            .flatMap(_.done)
                                                            .mapError(_.getOrElse(new NoSuchElementException))
                                              record = messages.filter(rec => rec.record.key == "no one")
                                            } yield record
                                          }
                         } yield assertTrue(recordChunk.nonEmpty)
                       }
        } yield assertion
      },
      test("interleaving transaction with transactional consumer should not be read during transaction") {
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
                         } yield assertTrue(recordChunk.isEmpty)
                       }
        } yield assertion
      },
      test("interleaving transaction with transactional consumer when aborted") {
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
        } yield assertTrue(recordChunk.nonEmpty)
      },
      test("committing offsets after a successful transaction") {
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

        } yield assertTrue(committedOffset.get.offset() == 1L)
      },
      test("not committing offsets after a failed transaction") {
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
      test("fails if transaction leaks") {
        val test = for {
          transactionThief <- Ref.make(Option.empty[Transaction])
          _ <- TransactionalProducer.createTransaction.use { t =>
                 transactionThief.set(Some(t))
               }
          t <- transactionThief.get
          _ <- t.get.produce("any-topic", 0, 0, Serde.int, Serde.int, None)
        } yield ()
        assertM(test.exit)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
      },
      test("fails if transaction leaks in an open transaction") {
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
        assertM(test.exit)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
      }
    ).provideSomeShared[TestEnvironment](
      ((Kafka.embedded >>> producer) ++
        (Kafka.embedded >>> transactionalProducer) ++
        Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    )
}
