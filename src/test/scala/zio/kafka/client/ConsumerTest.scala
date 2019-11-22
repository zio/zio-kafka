package zio.kafka.client

import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.client.KafkaTestUtils._
import zio.kafka.client.serde.Serde
import zio.stream.ZSink
import zio.test.Assertion._
import zio.duration._
import zio.test.{ DefaultRunnableSpec, _ }
import zio.test.TestAspect._

/**
 * Health warning - if extending these tests, be aware that consumer requires the live clock.
 * For this reason, we provide kafkaEnvrionment and use the consumer wrapper methods in KafkaTestUtils.
 * Or you can can use Live.live here but it's probably easier to use the wrappers
 */
object ConsumerTest
    extends DefaultRunnableSpec(
      suite("Consumer Streaming")(
        testM("plainStream emits messages for a topic subscription") {
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
        testM("plainStream emits messages for a pattern subscription") {
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
        testM("partitionedStream emits messages for each partition in a separate stream") {
          val nrMessages   = 50
          val nrPartitions = 5

          for {
            // Produce messages on several partitions
            topic <- randomTopic
            group <- randomGroup
            _     <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
            _ <- ZIO.traverse(1 to nrMessages) { i =>
                  produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                }

            // Consume messages
            messagesReceived <- ZIO.traverse(0 until nrPartitions)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
            subscription     = Subscription.topics(topic)
            fib <- withConsumer(group, "client3") { consumer =>
                    consumer
                      .subscribeAnd(subscription)
                      .partitionedStream(Serde.string, Serde.string)
                      .flatMapPar(nrPartitions) {
                        case (_, partition) =>
                          partition.mapM { record =>
                            messagesReceived(record.partition).update(_ + 1).as(record)
                          }.flattenChunks
                      }
                      .take(nrMessages)
                      .runDrain
                  }.fork
            _                    <- fib.join
            messagesPerPartition <- ZIO.traverse(messagesReceived.values)(_.get)

          } yield assert(messagesPerPartition, forall(equalTo(nrMessages / nrPartitions)))
        },
        testM("fail when the consuming effect produces a failure") {
          val topic        = "consumeWith3"
          val subscription = Subscription.Topics(Set(topic))
          val nrMessages   = 10
          val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

          for {
            _ <- produceMany(topic, messages)
            consumeResult <- consumeWithStrings("group3", "client3", subscription) {
                              case (_, _) =>
                                ZIO.fail(new IllegalArgumentException("consumeWith failure")).orDie
                            }.run
          } yield consumeResult.fold(
            _ => assertCompletes,
            _ => assert("result", equalTo("Expected consumeWith to fail"))
          )
        },
        testM("stopConsumption must stop the stream") {
          val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
          for {
            topic            <- randomTopic
            group            <- randomGroup
            _                <- produceMany(topic, kvs)
            messagesReceived <- Ref.make[Int](0)
            _ <- withConsumer(group, "client150") { consumer =>
                  consumer
                    .subscribeAnd(Subscription.topics(topic))
                    .plainStream(Serde.string, Serde.string)
                    .mapM { _ =>
                      for {
                        nr <- messagesReceived.update(_ + 1)
                        _  <- consumer.stopConsumption.when(nr == 3)
                      } yield ()
                    }
                    .flattenChunks
                    .runDrain
                }
            nr <- messagesReceived.get
          } yield assert(nr, isLessThanEqualTo(10)) // NOTE this depends on a max_poll_records setting of 10
        },
        testM("process outstanding commits after a graceful shutdown") {
          val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
          val topic = "test-outstanding-commits"
          for {
            group            <- randomGroup
            _                <- produceMany(topic, kvs)
            messagesReceived <- Ref.make[Int](0)
            offset <- withConsumer(group, "client150") { consumer =>
                       consumer
                         .subscribeAnd(Subscription.topics(topic))
                         .plainStream(Serde.string, Serde.string)
                         .mapM { record =>
                           for {
                             nr <- messagesReceived.update(_ + 1)
                             _  <- consumer.stopConsumption.when(nr == 1)
                           } yield record.offset
                         }
                         .flattenChunks
                         .aggregate(Consumer.offsetBatches)
                         .mapM(_.commit)
                         .runDrain *>
                         consumer.committed(new TopicPartition(topic, 0))
                     }
          } yield assert(offset.offset, isLessThanEqualTo(10L)) // NOTE this depends on a max_poll_records setting of 10
        },
        testM("offset batching collects the latest offset for all partitions") {
          val nrMessages   = 50
          val nrPartitions = 5

          for {
            // Produce messages on several partitions
            topic <- randomTopic
            group <- randomGroup
            _     <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
            _ <- ZIO.traverse(1 to nrMessages) { i =>
                  produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                }

            // Consume messages
            messagesReceived <- ZIO.traverse(0 until nrPartitions)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
            subscription     = Subscription.topics(topic)
            offsets <- withConsumer(group, "client3") { consumer =>
                        consumer
                          .subscribeAnd(subscription)
                          .partitionedStream(Serde.string, Serde.string)
                          .flatMapPar(nrPartitions)(_._2.map(_.offset).flattenChunks)
                          .take(nrMessages)
                          .aggregate(Consumer.offsetBatches)
                          .take(1)
                          .mapM(_.commit)
                          .runDrain *>
                          ZIO
                            .traverse((0 until nrPartitions).map(new TopicPartition(topic, _)))(
                              tp => consumer.committed(tp).map(tp -> _)
                            )
                            .map(_.toMap)
                      }
          } yield assert(offsets.values.map(_.offset), forall(equalTo(nrMessages / nrPartitions)))
        }
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment) @@ timeout(180.seconds)
    )
