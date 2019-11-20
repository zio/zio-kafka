package zio.kafka.client

import net.manub.embeddedkafka.EmbeddedKafka
import zio.kafka.client.serde.Serde
import zio.stream.ZSink
import zio.test._
import zio.test.Assertion._
import zio.test.DefaultRunnableSpec
import KafkaTestUtils._
import org.apache.kafka.common.TopicPartition
import zio._

/**
 * Health warning - if extending these tests, be aware that consumer requires the live clock.
 * For this reason, we provide kafkaEnvrionment and use the consumer wrapper methods in KafkaTestUtils.
 * Or you can can use Live.live here but it's probably easier to use the wrappers
 */
object ConsumerTest
    extends DefaultRunnableSpec(
      suite("consumer test suite pt1")(
        testM("receive messages produced on the topic") {
          val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
          for {
            _ <- produceMany("topic150", kvs)

            records <- withConsumer("group150", "client150") { consumer =>
                        consumer
                          .subscribeAnd(Subscription.Topics(Set("topic150")))
                          .plainStream(Serde.string, Serde.string)
                          .flattenChunks
                          .take(5)
                          .runCollect
                      }
            kvOut = records.map { r =>
              (r.record.key, r.record.value)
            }
          } yield assert(kvOut, equalTo(kvs))
        },
        testM("receive messages produced on the topic pattern") {
          val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
          for {
            _ <- produceMany("pattern150", kvs)
            records <- withConsumer("group150", "client150") { consumer =>
                        consumer
                          .subscribeAnd(Subscription.Pattern("pattern[0-9]+".r))
                          .plainStream(Serde.string, Serde.string)
                          .flattenChunks
                          .take(5)
                          .runCollect
                      }
            kvOut = records.map { r =>
              (r.record.key, r.record.value)
            }
          } yield assert(kvOut, equalTo(kvs))
        },
        testM("receive only messages from the subscribed topic-partition when creating a manual subscription") {
          val nrPartitions = 5
          val topic        = "manual-topic"

          for {
            _ <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
            _ <- ZIO.traverse(1 to nrPartitions) { i =>
                  produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                }
            record <- withConsumer("group150", "client150") { consumer =>
                       consumer
                         .subscribeAnd(Subscription.manual(topic, partition = 2))
                         .plainStream(Serde.string, Serde.string)
                         .flattenChunks
                         .tap(r => ZIO(println(r.toString)))
                         .take(1)
                         .runHead
                     }
            kvOut = record.map(r => (r.record.key, r.record.value))
          } yield assert(kvOut, isSome(equalTo("key2" -> "msg2")))
        },
        testM("restart from the committed position") {
          val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
          for {
            _ <- produceMany("topic1", 0, data)
            firstResults <- withConsumer("group1", "first") {
                             consumer =>
                               for {
                                 results <- consumer
                                             .subscribeAnd(Subscription.Topics(Set("topic1")))
                                             .partitionedStream(Serde.string, Serde.string)
                                             .filter(_._1 == new TopicPartition("topic1", 0))
                                             .flatMap(_._2.flattenChunks)
                                             .take(5)
                                             .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                                             .mapConcatM { committableRecords =>
                                               val records = committableRecords.map(_.record)
                                               val offsetBatch =
                                                 committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                               offsetBatch.commit.as(records)
                                             }
                                             .runCollect
                               } yield results
                           }
            secondResults <- withConsumer("group1", "second") {
                              consumer =>
                                for {
                                  results <- consumer
                                              .subscribeAnd(Subscription.Topics(Set("topic1")))
                                              .partitionedStream(Serde.string, Serde.string)
                                              .flatMap(_._2.flattenChunks)
                                              .take(5)
                                              .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                                              .mapConcatM { committableRecords =>
                                                val records = committableRecords.map(_.record)
                                                val offsetBatch =
                                                  committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                                offsetBatch.commit.as(records)
                                              }
                                              .runCollect
                                } yield results
                            }
          } yield assert((firstResults ++ secondResults).map(rec => rec.key() -> rec.value()), equalTo(data))
        },
        testM("consume all the messages on a topic") {
          val topic        = "consumeWith"
          val subscription = Subscription.Topics(Set(topic))
          val nrMessages   = 50
          val nrPartitions = 5

          for {
            // Produce messages on several partitions
            _ <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = 5))
            _ <- ZIO.traverse(1 to nrMessages) { i =>
                  produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                }

            // Consume messages
            done             <- Promise.make[Nothing, Unit]
            messagesReceived <- Ref.make(List.empty[(String, String)])
            fib <- consumeWithStrings("group3", "client3", subscription) { (key, value) =>
                    (for {
                      messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
                      _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
                    } yield ()).orDie
                  }.fork
            _ <- done.await
            _ <- fib.interrupt
          } yield assertCompletes
        },
        testM("fail when the consuming effect produces a failure") {
          val topic        = "consumeWith3"
          val subscription = Subscription.Topics(Set(topic))
          val nrMessages   = 10
          val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

          for {
            messagesReceived <- Ref.make(List.empty[(String, String)])
            _                <- produceMany(topic, messages)
            consumeResult <- consumeWithStrings("group3", "client3", subscription) { (key, value) =>
                              (for {
                                messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
                                _ <- Task.when(messagesSoFar.size == 3)(
                                      ZIO.die(new IllegalArgumentException("consumeWith failure"))
                                    )
                              } yield ()).orDie
                            }.run
          } yield consumeResult.fold(
            _ => assertCompletes,
            _ => assert("result", equalTo("Expected consumeWith to fail"))
          )
        },
        testM("not receive messages after shutting down") {
          val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
          for {
            _ <- produceMany("topic150", kvs)
            records <- withConsumer("group150", "client150") { consumer =>
                        consumer.stopConsumption *>
                          consumer
                            .subscribeAnd(Subscription.Topics(Set("topic150")))
                            .plainStream(Serde.string, Serde.string)
                            .flattenChunks
                            .take(5)
                            .runCollect
                      }
          } yield assert(records, isEmpty)
        }
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
    )
