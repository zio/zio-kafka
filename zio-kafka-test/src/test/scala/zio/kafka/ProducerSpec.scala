package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.config.TopicConfig
import zio._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.consumer._
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.producer.{ ByteRecord, Transaction }
import zio.kafka.serde.Serde
import zio.kafka.testkit._
import zio.stream.Take
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.nio.charset.StandardCharsets
import java.util.UUID

object ProducerSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "producerspec"

  private def asString(v: Array[Byte]) = new String(v, StandardCharsets.UTF_8)

  private def withConsumerInt(
    subscription: Subscription,
    settings: ConsumerSettings
  ): ZIO[Scope, Throwable, Dequeue[Take[Throwable, CommittableRecord[String, Int]]]] =
    Consumer.make(settings).flatMap { c =>
      c.plainStream(subscription, Serde.string, Serde.int).toQueue()
    }

  private val produceSpec =
    suite("::produce(record: ProducerRecord[Array[Byte], Array[Byte]])")(
      test("produces messages") {
        for {
          consumer <- KafkaTestUtils.makeConsumer(clientId = "producer-spec-consumer", groupId = Some("group-0"))
          producer <- KafkaTestUtils.makeProducer
          topic    <- randomTopic
          firstMessage  = "toto"
          secondMessage = "tata"
          consume = (n: Int) =>
                      consumer
                        .plainStream(Subscription.topics(topic), Serde.byteArray, Serde.byteArray)
                        .take(n.toLong)
                        .runCollect
          _ <- producer.produce(new ProducerRecord(topic, firstMessage.getBytes(StandardCharsets.UTF_8)): ByteRecord)
          first <- consume(1)
          _ <- producer.produce(new ProducerRecord(topic, secondMessage.getBytes(StandardCharsets.UTF_8)): ByteRecord)
          second <- consume(2)
        } yield assertTrue(
          first.size == 1,
          asString(first.head.value) == firstMessage,
          second.size == 2,
          asString(second(0).value) == firstMessage,
          asString(second(1).value) == secondMessage
        )
      }
    )

  private val produceAsyncSpec =
    suite("::produceAsync(record: ProducerRecord[Array[Byte], Array[Byte]])")(
      test("produces messages") {
        for {
          consumer <- KafkaTestUtils.makeConsumer(clientId = "producer-spec-consumer", groupId = Some("group-0"))
          producer <- KafkaTestUtils.makeProducer
          topic    <- randomTopic
          firstMessage  = "toto"
          secondMessage = "tata"
          consume = (n: Int) =>
                      consumer
                        .plainStream(Subscription.topics(topic), Serde.byteArray, Serde.byteArray)
                        .take(n.toLong)
                        .runCollect
          _ <- producer
                 .produceAsync(new ProducerRecord(topic, firstMessage.getBytes(StandardCharsets.UTF_8)): ByteRecord)
                 .flatten
          first <- consume(1)
          _ <- producer
                 .produceAsync(new ProducerRecord(topic, secondMessage.getBytes(StandardCharsets.UTF_8)): ByteRecord)
                 .flatten
          second <- consume(2)
        } yield assertTrue(
          first.size == 1,
          asString(first.head.value) == firstMessage,
          second.size == 2,
          asString(second(0).value) == firstMessage,
          asString(second(1).value) == secondMessage
        )
      }
    )

  private val produceChunkAsyncSpec =
    suite("::produceChunkAsync(records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]])")(
      test("produces messages") {
        for {
          consumer <- KafkaTestUtils.makeConsumer(clientId = "producer-spec-consumer", groupId = Some("group-0"))
          producer <- KafkaTestUtils.makeProducer
          topic    <- randomTopic
          firstMessage  = "toto"
          secondMessage = "tata"
          consume = (n: Int) =>
                      consumer
                        .plainStream(Subscription.topics(topic), Serde.byteArray, Serde.byteArray)
                        .take(n.toLong)
                        .runCollect
          _ <- producer
                 .produceChunkAsync(Chunk(new ProducerRecord(topic, firstMessage.getBytes(StandardCharsets.UTF_8))))
                 .flatten
          first <- consume(1)
          _ <- producer
                 .produceChunkAsync(
                   Chunk(
                     new ProducerRecord(topic, firstMessage.getBytes(StandardCharsets.UTF_8)),
                     new ProducerRecord(topic, secondMessage.getBytes(StandardCharsets.UTF_8))
                   )
                 )
                 .flatten
          second <- consume(3)
        } yield assertTrue(
          first.size == 1,
          asString(first.head.value) == firstMessage,
          second.size == 3,
          asString(second(0).value) == firstMessage,
          asString(second(1).value) == firstMessage,
          asString(second(2).value) == secondMessage
        )
      }
    )

  private val produceChunkSpec =
    suite("::produceChunk(records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]])")(
      test("produces messages") {
        for {
          consumer <- KafkaTestUtils.makeConsumer(clientId = "producer-spec-consumer", groupId = Some("group-0"))
          producer <- KafkaTestUtils.makeProducer
          topic    <- randomTopic
          firstMessage  = "toto"
          secondMessage = "tata"
          consume = (n: Int) =>
                      consumer
                        .plainStream(Subscription.topics(topic), Serde.byteArray, Serde.byteArray)
                        .take(n.toLong)
                        .runCollect
          _ <- producer
                 .produceChunk(Chunk(new ProducerRecord(topic, firstMessage.getBytes(StandardCharsets.UTF_8))))
          first <- consume(1)
          _ <- producer
                 .produceChunk(
                   Chunk(
                     new ProducerRecord(topic, firstMessage.getBytes(StandardCharsets.UTF_8)),
                     new ProducerRecord(topic, secondMessage.getBytes(StandardCharsets.UTF_8))
                   )
                 )
          second <- consume(3)
        } yield assertTrue(
          first.size == 1,
          asString(first.head.value) == firstMessage,
          second.size == 3,
          asString(second(0).value) == firstMessage,
          asString(second(1).value) == firstMessage,
          asString(second(2).value) == secondMessage
        )
      }
    )

  override def spec: Spec[TestEnvironment with Scope, Object] =
    suite("producer test suite")(
      test("one record") {
        for {
          producer <- KafkaTestUtils.makeProducer
          topic    <- randomTopic
          _        <- producer.produce(new ProducerRecord(topic, "boo", "baa"), Serde.string, Serde.string)
        } yield assertCompletes
      },
      test("a non-empty chunk of records") {
        import Subscription._

        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            c.plainStream(subscription, Serde.string, Serde.string).toQueue()
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

          producer <- KafkaTestUtils.makeProducer
          outcome  <- producer.produceChunk(chunks, Serde.string, Serde.string)
          settings <- KafkaTestUtils.consumerSettings(client, Some(group))
          record1 <- ZIO.scoped {
                       withConsumer(Topics(Set(topic1)), settings).flatMap { consumer =>
                         for {
                           messages <- consumer.take.flatMap(_.exit).mapError(_.getOrElse(new NoSuchElementException))
                           record = messages
                                      .filter(rec => rec.record.key == key1 && rec.record.value == value1)
                         } yield record
                       }
                     }
          record2 <- ZIO.scoped {
                       withConsumer(Topics(Set(topic2)), settings).flatMap { consumer =>
                         for {
                           messages <- consumer.take.flatMap(_.exit).mapError(_.getOrElse(new NoSuchElementException))
                           record = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
                         } yield record
                       }
                     }
        } yield assertTrue(
          outcome.length == 2,
          record1.nonEmpty,
          record2.nonEmpty
        )
      },
      test("a non-empty chunk of records with partial failure") {
        import Subscription._

        def withConsumer(subscription: Subscription, settings: ConsumerSettings) =
          Consumer.make(settings).flatMap { c =>
            c.plainStream(subscription, Serde.string, Serde.string).toQueue()
          }

        def makeChunk(standardTopic: String, compactedTopic: String): Chunk[ByteRecord] = {
          // Specifically a null key so publishing to compacted topic fails
          val key2: Array[Byte] = null

          Chunk.fromIterable(
            List[ByteRecord](
              new ProducerRecord(standardTopic, "boo".getBytes, "baa".getBytes),
              new ProducerRecord(compactedTopic, key2, "boo".getBytes),
              new ProducerRecord(standardTopic, "hello".getBytes, "world".getBytes)
            )
          )
        }

        for {
          compactedTopic <- randomTopic
          standardTopic  <- randomTopic
          adminClient    <- KafkaTestUtils.makeAdminClient
          _ <- adminClient
                 .createTopic(NewTopic(compactedTopic, 1, 1, Map(TopicConfig.CLEANUP_POLICY_CONFIG -> "compact")))
          group  <- randomGroup
          client <- randomClient
          chunk = makeChunk(standardTopic, compactedTopic)
          producer <- KafkaTestUtils.makeProducer
          outcome  <- producer.produceChunkAsyncWithFailures(chunk).flatten
          settings <- KafkaTestUtils.consumerSettings(client, Some(group))
          recordsConsumed <- ZIO.scoped {
                               withConsumer(Topics(Set(standardTopic)), settings).flatMap { consumer =>
                                 consumer.take.flatMap(_.exit).mapError(_.getOrElse(new NoSuchElementException))
                               }
                             }
        } yield assertTrue(
          outcome.length == 3,
          outcome(0).isRight,
          outcome(1).swap.exists(_.getMessage.contains("Compacted topic cannot accept message without key")),
          outcome(2).isRight,
          recordsConsumed.length == 2
        )
      },
      test("an empty chunk of records") {
        val chunks = Chunk.fromIterable(List.empty)
        for {
          producer <- KafkaTestUtils.makeProducer
          outcome  <- producer.produceChunk(chunks, Serde.string, Serde.string)
        } yield assertTrue(outcome.isEmpty)
      },
      test("export metrics") {
        for {
          producer <- KafkaTestUtils.makeProducer
          metrics  <- producer.metrics
        } yield assertTrue(metrics.nonEmpty)
      },
      test("partitionsFor") {
        for {
          producer <- KafkaTestUtils.makeProducer
          topic    <- randomTopic
          info     <- producer.partitionsFor(topic).debug
        } yield assertTrue(info.headOption.map(_.topic()).contains(topic))
      },
      suite("transactions")(
        test("a simple transaction") {
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient
            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            _ <- ZIO.scoped {
                   for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(initialBobAccount, Serde.string, Serde.int, None)
                     _  <- tx.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   } yield ()
                 }

            settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Subscription.topics(topic), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.exit)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                                 record = messages.filter(rec => rec.record.key == "bob")
                               } yield record
                             }
                           }
          } yield assertTrue(recordChunk.map(_.value).last == 0)
        },
        test("an aborted transaction should not be read") {
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient
            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)
            bobReceives20       = new ProducerRecord(topic, "bob", 20)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            _ <- ZIO.scoped {
                   for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(initialBobAccount, Serde.string, Serde.int, None)
                     _  <- tx.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   } yield ()
                 }
            _ <- ZIO.scoped {
                   (for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(aliceGives20, Serde.string, Serde.int, None)
                     _  <- tx.produce(bobReceives20, Serde.string, Serde.int, None)
                   } yield tx).flatMap(_.abort)
                 }.catchSome { case UserInitiatedAbort =>
                   ZIO.unit // silences the abort
                 }

            settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Subscription.topics(topic), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.exit)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                                 record = messages.filter(rec => rec.record.key == "bob")
                               } yield record
                             }
                           }
          } yield assertTrue(recordChunk.map(_.value).last == 0)
        },
        test("serialize concurrent transactions") {
          // Warning: on fast machines this test is flaky.
          // The problem is that when the consumer is reading from the topic, sometimes it only sees
          // one of the 2 produced records since the second is still in transit.
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient
            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            transaction1 = ZIO.scoped {
                             for {
                               tx <- transactionalProducer.createTransaction
                               _  <- tx.produce(initialAliceAccount, Serde.string, Serde.int, None)
                             } yield ()
                           }
            transaction2 = ZIO.scoped {
                             for {
                               tx <- transactionalProducer.createTransaction
                               _  <- tx.produce(initialBobAccount, Serde.string, Serde.int, None)
                             } yield ()
                           }
            _ <- transaction1 <&> transaction2

            settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Subscription.topics(topic), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.exit)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                               } yield messages
                             }
                           }
          } yield assert(recordChunk.map(_.value))(contains(0) && contains(20))
        },
        test("exception management") {
          for {
            topic   <- randomTopic
            client1 <- randomClient
            initialBobAccount = new ProducerRecord(topic, "bob", 0)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            result <- ZIO.scoped {
                        transactionalProducer.createTransaction.flatMap { t =>
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
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient

            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            nonTransactional    = new ProducerRecord(topic, "no one", -1)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            _ <- ZIO.scoped {
                   for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(initialBobAccount, Serde.string, Serde.int, None)
                     _  <- tx.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   } yield ()
                 }
            recordChunk <- ZIO.scoped {
                             for {
                               tx       <- transactionalProducer.createTransaction
                               _        <- tx.produce(aliceGives20, Serde.string, Serde.int, None)
                               producer <- KafkaTestUtils.makeProducer
                               _        <- producer.produce(nonTransactional, Serde.string, Serde.int)
                               settings <- KafkaTestUtils.consumerSettings(client2, Some(group))
                               recordChunk <- ZIO.scoped {
                                                withConsumerInt(Subscription.topics(topic), settings).flatMap {
                                                  consumer =>
                                                    for {
                                                      messages <- consumer.take
                                                                    .flatMap(_.exit)
                                                                    .mapError(_.getOrElse(new NoSuchElementException))
                                                      record = messages.filter(rec => rec.record.key == "no one")
                                                    } yield record
                                                }
                                              }
                             } yield recordChunk
                           }
          } yield assertTrue(recordChunk.nonEmpty)
        },
        test("interleaving transaction with transactional consumer should not be read during transaction") {
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient

            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            nonTransactional    = new ProducerRecord(topic, "no one", -1)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            _ <- ZIO.scoped {
                   for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(initialBobAccount, Serde.string, Serde.int, None)
                     _  <- tx.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   } yield ()
                 }
            assertion <- ZIO.scoped {
                           for {
                             tx       <- transactionalProducer.createTransaction
                             _        <- tx.produce(aliceGives20, Serde.string, Serde.int, None)
                             producer <- KafkaTestUtils.makeProducer
                             _        <- producer.produce(nonTransactional, Serde.string, Serde.int)
                             settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
                             recordChunk <- ZIO.scoped {
                                              withConsumerInt(Subscription.topics(topic), settings).flatMap { consumer =>
                                                for {
                                                  messages <- consumer.take
                                                                .flatMap(_.exit)
                                                                .mapError(_.getOrElse(new NoSuchElementException))
                                                  record = messages.filter(rec => rec.record.key == "no one")
                                                } yield record
                                              }
                                            }
                           } yield assertTrue(recordChunk.isEmpty)
                         }
          } yield assertion
        },
        test("interleaving transaction with transactional consumer when aborted") {
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient

            initialAliceAccount = new ProducerRecord(topic, "alice", 20)
            initialBobAccount   = new ProducerRecord(topic, "bob", 0)
            aliceGives20        = new ProducerRecord(topic, "alice", 0)
            nonTransactional    = new ProducerRecord(topic, "no one", -1)
            bobReceives20       = new ProducerRecord(topic, "bob", 20)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)
            producer              <- KafkaTestUtils.makeProducer

            _ <- ZIO.scoped {
                   for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(initialBobAccount, Serde.string, Serde.int, None)
                     _  <- tx.produce(initialAliceAccount, Serde.string, Serde.int, None)
                   } yield ()
                 }
            _ <- ZIO.scoped {
                   (for {
                     tx <- transactionalProducer.createTransaction
                     _  <- tx.produce(aliceGives20, Serde.string, Serde.int, None)
                     _  <- producer.produce(nonTransactional, Serde.string, Serde.int)
                     _  <- tx.produce(bobReceives20, Serde.string, Serde.int, None)
                   } yield tx).flatMap(_.abort)
                 }.catchSome { case UserInitiatedAbort =>
                   ZIO.unit // silences the abort
                 }

            settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
            recordChunk <- ZIO.scoped {
                             withConsumerInt(Subscription.topics(topic), settings).flatMap { consumer =>
                               for {
                                 messages <- consumer.take
                                               .flatMap(_.exit)
                                               .mapError(_.getOrElse(new NoSuchElementException))
                                 record = messages.filter(rec => rec.record.key == "no one")
                               } yield record
                             }
                           }
          } yield assertTrue(recordChunk.nonEmpty)
        },
        test("committing offsets after a successful transaction") {
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient

            initialAliceAccount  = new ProducerRecord(topic, "alice", 20)
            aliceAccountFeesPaid = new ProducerRecord(topic, "alice", 0)

            producer <- KafkaTestUtils.makeProducer
            _        <- producer.produce(initialAliceAccount, Serde.string, Serde.int)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
            committedOffset <-
              ZIO.scoped {
                Consumer.make(settings).flatMap { c =>
                  ZIO.scoped {
                    c
                      .plainStream(Subscription.topics(topic), Serde.string, Serde.int)
                      .toQueue()
                      .flatMap { q =>
                        val readAliceAccount = for {
                          messages <- q.take
                                        .flatMap(_.exit)
                                        .mapError(_.getOrElse(new NoSuchElementException))
                        } yield messages.head
                        for {
                          aliceHadMoneyCommittableMessage <- readAliceAccount
                          _ <- ZIO.scoped {
                                 for {
                                   tx <- transactionalProducer.createTransaction
                                   _ <- tx.produce(
                                          aliceAccountFeesPaid,
                                          Serde.string,
                                          Serde.int,
                                          Some(aliceHadMoneyCommittableMessage.offset)
                                        )
                                 } yield ()
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
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient

            initialAliceAccount  = new ProducerRecord(topic, "alice", 20)
            aliceAccountFeesPaid = new ProducerRecord(topic, "alice", 0)

            producer <- KafkaTestUtils.makeProducer
            _        <- producer.produce(initialAliceAccount, Serde.string, Serde.int)

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            settings <- KafkaTestUtils.transactionalConsumerSettings(group, client2)
            committedOffset <- ZIO.scoped {
                                 Consumer.make(settings).flatMap { c =>
                                   c
                                     .plainStream(Subscription.topics(topic), Serde.string, Serde.int)
                                     .toQueue()
                                     .flatMap { q =>
                                       val readAliceAccount = for {
                                         messages <- q.take
                                                       .flatMap(_.exit)
                                                       .mapError(_.getOrElse(new NoSuchElementException))
                                       } yield messages.head
                                       for {
                                         aliceHadMoneyCommittableMessage <- readAliceAccount
                                         _ <- ZIO.scoped {
                                                transactionalProducer.createTransaction.flatMap { tx =>
                                                  tx.produce(
                                                    aliceAccountFeesPaid,
                                                    Serde.string,
                                                    Serde.int,
                                                    Some(aliceHadMoneyCommittableMessage.offset)
                                                  ) *>
                                                    tx.abort
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
            client1          <- randomClient
            transactionThief <- Ref.make(Option.empty[Transaction])

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)
            _ <- ZIO.scoped {
                   transactionalProducer.createTransaction.flatMap { tx =>
                     transactionThief.set(Some(tx))
                   }
                 }
            stolenTx <- transactionThief.get
            _        <- stolenTx.get.produce(topic, 0, 0, Serde.int, Serde.int, None)
          } yield ()
          assertZIO(test.exit)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
        },
        test("fails if transaction leaks in an open transaction") {
          val test = for {
            topic            <- randomTopic
            client1          <- randomClient
            transactionThief <- Ref.make(Option.empty[Transaction])

            consumer1             <- KafkaTestUtils.makeConsumer(client1)
            transactionalProducer <- KafkaTestUtils.makeTransactionalProducer(UUID.randomUUID().toString, consumer1)

            _ <- ZIO.scoped {
                   transactionalProducer.createTransaction.flatMap { tx =>
                     transactionThief.set(Some(tx))
                   }
                 }
            stolenTx <- transactionThief.get
            _ <- ZIO.scoped {
                   transactionalProducer.createTransaction *>
                     stolenTx.get.produce(topic, 0, 0, Serde.int, Serde.int, None)
                 }
          } yield ()
          assertZIO(test.exit)(failsCause(containsCause(Cause.fail(TransactionLeaked(OffsetBatch.empty)))))
        }
      ),
      produceSpec,
      produceAsyncSpec,
      produceChunkAsyncSpec,
      produceChunkSpec
    )
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ timeout(3.minutes)
}
