package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.{ CommittableRecord, Consumer, ConsumerSettings, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.{ Producer, Transaction, TransactionalProducer }
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.serde.Serde
import zio.stream.Take
import zio.test.Assertion._
import zio.test.TestAspect.withLiveClock
import zio.test._

object ProducerSpec extends ZIOKafkaSpec {
  override val kafkaPrefix: String = "producerspec"

  def withConsumerInt(
    subscription: Subscription,
    settings: ConsumerSettings
  ): ZIO[Any with Scope, Throwable, Dequeue[Take[Throwable, CommittableRecord[String, Int]]]] =
    Consumer.make(settings).flatMap { c =>
      c.subscribe(subscription) *> c.plainStream(Serde.string, Serde.int).toQueue()
    }

  override def spec: Spec[TestEnvironment & Kafka, Object] =
    suite("producer test suite")(
      test("one record") {
        for {
          topic <- randomTopic
          _     <- Producer.produce(new ProducerRecord(topic, "boo", "baa"), Serde.string, Serde.string)
        } yield assertCompletes
      },
      test("a non-empty chunk of records") {
        import Subscription._

        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            c.subscribe(subscription) *> c.plainStream(Serde.string, Serde.string).toQueue()
          }

        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          group  <- randomGroup
          client <- randomClient
          key1   = "boo"
          value1 = "baa"
          key2   = "baa"
          value2 = "boo"
          chunks = Chunk.fromIterable(
                     List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
                   )

          outcome  <- Producer.produceChunk(chunks, Serde.string, Serde.string)
          settings <- consumerSettings(client, Some(group))
          record1 <- ZIO.scoped {
                       withConsumer(Topics(Set(topic1)), settings).flatMap { consumer =>
                         for {
                           messages <- consumer.take.flatMap(_.done).mapError(_.getOrElse(new NoSuchElementException))
                           record = messages
                                      .filter(rec => rec.record.key == key1 && rec.record.value == value1)
                         } yield record
                       }
                     }
          record2 <- ZIO.scoped {
                       withConsumer(Topics(Set(topic2)), settings).flatMap { consumer =>
                         for {
                           messages <- consumer.take.flatMap(_.done).mapError(_.getOrElse(new NoSuchElementException))
                           record = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
                         } yield record
                       }
                     }
        } yield assertTrue(outcome.length == 2) &&
          assertTrue(record1.nonEmpty) &&
          assertTrue(record2.nonEmpty)
      },
      test("an empty chunk of records") {
        val chunks = Chunk.fromIterable(List.empty)
        for {
          outcome <- Producer.produceChunk(chunks, Serde.string, Serde.string)
        } yield assertTrue(outcome.isEmpty)
      },
      test("export metrics") {
        for {
          metrics <- Producer.metrics
        } yield assertTrue(metrics.nonEmpty)
      },
      suite("transactions")(
        test("a simple transaction") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient
            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)

            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                       t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   }
                 }
            settings <- transactionalConsumerSettings(group, client)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Topics(Set(topic)), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.done)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                                 record = messages.filter(rec => rec.record.key == "bob")
                               } yield record
                             }
                           }
          } yield assertTrue(recordChunk.map(_.value).last == 0)
        },
        test("an aborted transaction should not be read") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient
            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)
            bobReceives20       = new ProducerRecord(topic, "bob", 20)

            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                       t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   }
                 }
            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(aliceGives20, Serde.string, Serde.int, None) *>
                       t.produce(bobReceives20, Serde.string, Serde.int, None) *>
                       t.abort
                   }
                 }.catchSome { case UserInitiatedAbort =>
                   ZIO.unit // silences the abort
                 }
            settings <- transactionalConsumerSettings(group, client)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Topics(Set(topic)), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.done)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                                 record = messages.filter(rec => rec.record.key == "bob")
                               } yield record
                             }
                           }
          } yield assertTrue(recordChunk.map(_.value).last == 0)
        },
        test("serialize concurrent transactions") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient
            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)

            transaction1 = ZIO.scoped {
                             TransactionalProducer.createTransaction.flatMap { t =>
                               t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                             }
                           }
            transaction2 = ZIO.scoped {
                             TransactionalProducer.createTransaction.flatMap { t =>
                               t.produce(initialBobAccount, Serde.string, Serde.int, None)
                             }
                           }

            _        <- transaction1 <&> transaction2
            settings <- transactionalConsumerSettings(group, client)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Topics(Set(topic)), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.done)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                               } yield messages
                             }
                           }
          } yield assert(recordChunk.map(_.value))(contains(0) && contains(20))
        },
        test("exception management") {
          for {
            topic <- randomTopic
            initialBobAccount = new ProducerRecord(topic, "bob", 0)

            result <- ZIO.scoped {
                        TransactionalProducer.createTransaction.flatMap { t =>
                          t.produce(
                            initialBobAccount,
                            Serde.string,
                            Serde.int.contramap((_: Int) => throw new RuntimeException("test")),
                            None
                          )
                        }
                      }.unit.exit
          } yield assert(result)(dies(hasMessage(equalTo("test"))))
        },
        test("interleaving transaction with non-transactional consumer") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient

            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            nonTransactional    = new ProducerRecord(topic, "no one", -1)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)

            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                       t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   }
                 }
            assertion <- ZIO.scoped {
                           TransactionalProducer.createTransaction.flatMap { t =>
                             for {
                               _        <- t.produce(aliceGives20, Serde.string, Serde.int, None)
                               _        <- Producer.produce(nonTransactional, Serde.string, Serde.int)
                               settings <- consumerSettings(client, Some(group))
                               recordChunk <- ZIO.scoped {
                                                withConsumerInt(Topics(Set(topic)), settings).flatMap { consumer =>
                                                  for {
                                                    messages <- consumer.take
                                                                  .flatMap(_.done)
                                                                  .mapError(_.getOrElse(new NoSuchElementException))
                                                    record = messages.filter(rec => rec.record.key == "no one")
                                                  } yield record

                                                }
                                              }
                             } yield assertTrue(recordChunk.nonEmpty)
                           }
                         }
          } yield assertion
        },
        test("interleaving transaction with transactional consumer should not be read during transaction") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient

            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            nonTransactional    = new ProducerRecord(topic, "no one", -1)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)

            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                       t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   }
                 }
            assertion <- ZIO.scoped {
                           TransactionalProducer.createTransaction.flatMap { t =>
                             for {
                               _        <- t.produce(aliceGives20, Serde.string, Serde.int, None)
                               _        <- Producer.produce(nonTransactional, Serde.string, Serde.int)
                               settings <- transactionalConsumerSettings(group, client)
                               recordChunk <- ZIO.scoped {
                                                withConsumerInt(Topics(Set(topic)), settings).flatMap { consumer =>
                                                  for {
                                                    messages <- consumer.take
                                                                  .flatMap(_.done)
                                                                  .mapError(_.getOrElse(new NoSuchElementException))
                                                    record = messages.filter(rec => rec.record.key == "no one")
                                                  } yield record
                                                }
                                              }
                             } yield assertTrue(recordChunk.isEmpty)
                           }
                         }
          } yield assertion
        },
        test("interleaving transaction with transactional consumer when aborted") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient

            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)
            nonTransactional    = new ProducerRecord(topic, "no one", -1)
            bobReceives20       = new ProducerRecord(topic, "bob", 20)

            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(initialBobAccount, Serde.string, Serde.int, None) *>
                       t.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   }
                 }
            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     t.produce(aliceGives20, Serde.string, Serde.int, None) *>
                       Producer.produce(nonTransactional, Serde.string, Serde.int) *>
                       t.produce(bobReceives20, Serde.string, Serde.int, None) *>
                       t.abort
                   }
                 }.catchSome { case UserInitiatedAbort =>
                   ZIO.unit // silences the abort
                 }
            settings <- transactionalConsumerSettings(group, client)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Topics(Set(topic)), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.done)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                                 record = messages.filter(rec => rec.record.key == "no one")
                               } yield record
                             }
                           }
          } yield assertTrue(recordChunk.nonEmpty)
        },
        test("committing offsets after a successful transaction") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient

            initialAliceAccount  = new ProducerRecord(topic, "alice", 20)
            aliceAccountFeesPaid = new ProducerRecord(topic, "alice", 0)

            _        <- Producer.produce(initialAliceAccount, Serde.string, Serde.int)
            settings <- transactionalConsumerSettings(group, client)
            committedOffset <-
              ZIO.scoped {
                Consumer.make(settings).flatMap { c =>
                  c.subscribe(Topics(Set(topic))) *>
                    ZIO.scoped {
                      c
                        .plainStream(Serde.string, Serde.int)
                        .toQueue()
                        .flatMap { q =>
                          val readAliceAccount = for {
                            messages <- q.take
                                          .flatMap(_.done)
                                          .mapError(_.getOrElse(new NoSuchElementException))
                          } yield messages.head
                          for {
                            aliceHadMoneyCommittableMessage <- readAliceAccount
                            _ <- ZIO.scoped {
                                   TransactionalProducer.createTransaction.flatMap { t =>
                                     t.produce(
                                       aliceAccountFeesPaid,
                                       Serde.string,
                                       Serde.int,
                                       Some(aliceHadMoneyCommittableMessage.offset)
                                     )
                                   }
                                 }
                            aliceTopicPartition =
                              new TopicPartition(topic, aliceHadMoneyCommittableMessage.partition)
                            committed <- c.committed(Set(aliceTopicPartition))
                          } yield committed(aliceTopicPartition)
                        }
                    }
                }
              }

          } yield assertTrue(committedOffset.get.offset() == 1L)
        },
        test("not committing offsets after a failed transaction") {
          import Subscription._

          for {
            topic  <- randomTopic
            group  <- randomGroup
            client <- randomClient

            initialAliceAccount  = new ProducerRecord(topic, "alice", 20)
            aliceAccountFeesPaid = new ProducerRecord(topic, "alice", 0)

            _        <- Producer.produce(initialAliceAccount, Serde.string, Serde.int)
            settings <- transactionalConsumerSettings(group, client)
            committedOffset <- ZIO.scoped {
                                 Consumer.make(settings).flatMap { c =>
                                   c.subscribe(Topics(Set(topic))) *> c
                                     .plainStream(Serde.string, Serde.int)
                                     .toQueue()
                                     .flatMap { q =>
                                       val readAliceAccount = for {
                                         messages <- q.take
                                                       .flatMap(_.done)
                                                       .mapError(_.getOrElse(new NoSuchElementException))
                                       } yield messages.head
                                       for {
                                         aliceHadMoneyCommittableMessage <- readAliceAccount
                                         _ <- ZIO.scoped {
                                                TransactionalProducer.createTransaction.flatMap { t =>
                                                  t.produce(
                                                    aliceAccountFeesPaid,
                                                    Serde.string,
                                                    Serde.int,
                                                    Some(aliceHadMoneyCommittableMessage.offset)
                                                  ) *>
                                                    t.abort
                                                }
                                              }.catchSome { case UserInitiatedAbort =>
                                                ZIO.unit // silences the abort
                                              }
                                         aliceTopicPartition =
                                           new TopicPartition(topic, aliceHadMoneyCommittableMessage.partition)
                                         committed <- c.committed(Set(aliceTopicPartition))
                                       } yield committed(aliceTopicPartition)
                                     }
                                 }
                               }
          } yield assert(committedOffset)(isNone)
        },
        test("fails if transaction leaks") {
          val test = for {
            topic            <- randomTopic
            transactionThief <- Ref.make(Option.empty[Transaction])
            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     transactionThief.set(Some(t))
                   }
                 }
            t <- transactionThief.get
            _ <- t.get.produce(topic, 0, 0, Serde.int, Serde.int, None)
          } yield ()
          assertZIO(test.exit)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
        },
        test("fails if transaction leaks in an open transaction") {
          val test = for {
            topic            <- randomTopic
            transactionThief <- Ref.make(Option.empty[Transaction])
            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { t =>
                     transactionThief.set(Some(t))
                   }
                 }
            t <- transactionThief.get
            _ <- ZIO.scoped {
                   TransactionalProducer.createTransaction.flatMap { _ =>
                     t.get.produce(topic, 0, 0, Serde.int, Serde.int, None)
                   }
                 }
          } yield ()
          assertZIO(test.exit)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
        }
      )
    ).provideSomeLayerShared[TestEnvironment & Kafka](
      (KafkaTestUtils.producer ++ transactionalProducer)
        .mapError(TestFailure.fail)
    ) @@ withLiveClock @@ TestAspect.timeout(2.minutes) @@ TestAspect.sequential
}
