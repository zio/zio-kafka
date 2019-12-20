package zio.kafka.client

import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.common.TopicPartition
import zio.test.environment.Live
import zio._
import zio.kafka.client.KafkaTestUtils._
import zio.kafka.client.serde.Serde
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.duration._
import zio.test.{ DefaultRunnableSpec, _ }
import zio.test.TestAspect._
import ConsumerTest._
import zio.clock.Clock
import zio.kafka.client.ConsumerStream.OffsetStorage
import zio.kafka.client.diagnostics.{ DiagnosticEvent, Diagnostics }

/**
 * Health warning - if extending these tests, be aware that consumer requires the live clock.
 * For this reason, we provide kafkaEnvrionment and use the consumer wrapper methods in KafkaTestUtils.
 * Or you can can use Live.live here but it's probably easier to use the wrappers
 */
object ConsumerTestSuite
    extends DefaultRunnableSpec(
      suite("Consumer Streaming")(
        List(
          plainStreamTopic,
          plainStreamPattern,
          plainStreamManual,
          plainStreamManualAssignmentManualSeek,
          restartFromCommittedPosition,
          partitionedStreamBasic,
          consumeWithFailStream,
          stopConsumptionStopStream,
          stopConsumptionCommits,
          offsetBatching,
          rebalancing,
          diagnosticRelabancing,
          manualSeek
        ).map(_ @@ after(ZIO.sleep(1.second).provide(Clock.Live))): _* // There's some flaky behavior here
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment) @@ timeout(180.seconds)
    )

object ConsumerTest {
  val plainStreamTopic = testM("plainStream emits messages for a topic subscription") {
    val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
    for {
      _        <- produceMany("topic150", kvs)
      clientId <- randomClientId

      records <- withConsumerSettings("group150", clientId) { settings =>
                  ConsumerStream
                    .plain(settings, Subscription.topics("topic150"), Serde.string, Serde.string)
                    .use {
                      case (_, stream) =>
                        stream.flattenChunks
                          .take(5)
                          .runCollect
                    }
                }
      kvOut = records.map { r =>
        (r.record.key, r.record.value)
      }
    } yield assert(kvOut, equalTo(kvs))
  }

  val plainStreamPattern = testM("plainStream emits messages for a pattern subscription") {
    val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
    for {
      _        <- produceMany("pattern150", kvs)
      clientId <- randomClientId
      records <- withConsumerSettings("group150", clientId) { settings =>
                  ConsumerStream
                    .plain(settings, Subscription.pattern("pattern[0-9]+".r), Serde.string, Serde.string)
                    .use {
                      case (_, stream) =>
                        stream.flattenChunks
                          .take(5)
                          .runCollect
                    }
                }
      kvOut = records.map { r =>
        (r.record.key, r.record.value)
      }
    } yield assert(kvOut, equalTo(kvs))
  }

  val plainStreamManual =
    testM("receive only messages from the subscribed topic-partition when creating a manual subscription") {
      val nrPartitions = 5
      val topic        = "manual-topic"

      for {
        clientId <- randomClientId
        _        <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
        _ <- ZIO.traverse(1 to nrPartitions) { i =>
              produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
            }
        record <- withConsumerSettings("group150", clientId) { settings =>
                   ConsumerStream
                     .plain(settings, Subscription.manual(topic, partition = 2), Serde.string, Serde.string)
                     .map(_._2)
                     .use { stream =>
                       stream.flattenChunks
                         .take(1)
                         .runHead
                     }
                 }
        kvOut = record.map(r => (r.record.key, r.record.value))
      } yield assert(kvOut, isSome(equalTo("key2" -> "msg2")))
    }

  val plainStreamManualAssignmentManualSeek =
    testM("receive from the right offset when creating a manual subscription with manual seeking") {
      val nrPartitions = 5
      val topic        = "manual-topic"

      val manualOffsetSeek = 3

      for {
        _ <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
        _ <- ZIO.traverse(1 to nrPartitions) { i =>
              produceMany(topic, partition = i % nrPartitions, kvs = (1 to 10).map(j => s"key$i-$j" -> s"msg$i-$j"))
            }
        clientId <- randomClientId
        record <- withConsumerSettings("group150", clientId) { settings =>
                   ConsumerStream
                     .plain(
                       settings,
                       Subscription.manual(topic, partition = 2),
                       Serde.string,
                       Serde.string,
                       offsetStorage = OffsetStorage.Manual(tps => ZIO(tps.map(_ -> manualOffsetSeek.toLong).toMap))
                     )
                     .map(_._2)
                     .use { stream =>
                       stream.flattenChunks
                         .take(1)
                         .runHead
                     }
                 }
        kvOut = record.map(r => (r.record.key, r.record.value))
      } yield assert(kvOut, isSome(equalTo("key2-3" -> "msg2-3")))
    }

  val restartFromCommittedPosition = testM("restart from the committed position") {
    val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
    for {
      _              <- produceMany("topic1", 0, data)
      clientIdFirst  <- randomClientId
      clientIdSecond <- randomClientId
      firstResults <- withConsumerSettings("group1", clientIdFirst) { settings =>
                       ConsumerStream
                         .partitioned(settings, Subscription.topics("topic1"), Serde.string, Serde.string)
                         .use {
                           case (_, stream) =>
                             stream
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
                         }
                     }
      secondResults <- withConsumerSettings("group1", clientIdSecond) { settings =>
                        ConsumerStream
                          .partitioned(settings, Subscription.topics("topic1"), Serde.string, Serde.string)
                          .use {
                            case (_, stream) =>
                              stream
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
                          }
                      }
    } yield assert((firstResults ++ secondResults).map(rec => rec.key() -> rec.value()), equalTo(data))
  }

  val manualSeek = testM("support manual seeking") {
    val nrRecords        = 10
    val data             = (1 to nrRecords).toList.map(i => s"key$i" -> s"msg$i")
    val manualOffsetSeek = 3

    for {
      topic          <- randomTopic
      clientIdFirst  <- randomClientId
      clientIdSecond <- randomClientId
      _              <- produceMany(topic, 0, data)
      // Consume 5 records to have the offset committed at 5
      _ <- withConsumerSettings("group1", clientIdFirst) { settings =>
            ConsumerStream
              .plain(settings, Subscription.topics(topic), Serde.string, Serde.string)
              .use {
                case (_, stream) =>
                  stream.flattenChunks
                    .take(5)
                    .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                    .mapConcatM { committableRecords =>
                      val records = committableRecords.map(_.record)
                      val offsetBatch =
                        committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                      offsetBatch.commit.as(records)
                    }
                    .runCollect
              }
          }
      // Start a new consumer with manual offset before the committed offset
      secondResults <- withConsumerSettings("group1", clientIdSecond) { settings =>
                        ConsumerStream
                          .plain(
                            settings,
                            Subscription.topics(topic),
                            Serde.string,
                            Serde.string,
                            offsetStorage =
                              OffsetStorage.Manual(tps => ZIO(tps.map(_ -> manualOffsetSeek.toLong).toMap))
                          )
                          .use {
                            case (_, stream) =>
                              stream
                                .take(nrRecords - manualOffsetSeek)
                                .map(_.record)
                                .runCollect
                          }
                      }
      // Check that we only got the records starting from the manually seek'd offset
    } yield assert(secondResults.map(rec => rec.key() -> rec.value()), equalTo(data.drop(manualOffsetSeek)))
  }

  val partitionedStreamBasic = testM("partitionedStream emits messages for each partition in a separate stream") {
    val nrMessages   = 50
    val nrPartitions = 5

    for {
      // Produce messages on several partitions
      topic    <- randomTopic
      group    <- randomGroup
      clientId <- randomClientId
      _        <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
      _ <- ZIO.traverse(1 to nrMessages) { i =>
            produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
          }

      // Consume messages
      messagesReceived <- ZIO.traverse(0 until nrPartitions)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
      subscription     = Subscription.topics(topic)
      _ <- withConsumerSettings(group, clientId) { settings =>
            ConsumerStream.partitioned(settings, subscription, Serde.string, Serde.string).use {
              case (_, stream) =>
                stream
                  .flatMapPar(nrPartitions) {
                    case (_, partition) =>
                      partition.mapM { record =>
                        messagesReceived(record.partition).update(_ + 1).as(record)
                      }.flattenChunks
                  }
                  .take(nrMessages)
                  .runDrain
            }
          }
      messagesPerPartition <- ZIO.traverse(messagesReceived.values)(_.get)

    } yield assert(messagesPerPartition, forall(equalTo(nrMessages / nrPartitions)))
  }

  val consumeWithFailStream = testM("fail when the consuming effect produces a failure") {
    val topic        = "consumeWith3"
    val subscription = Subscription.Topics(Set(topic))
    val nrMessages   = 10
    val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

    for {
      clientId <- randomClientId
      _        <- produceMany(topic, messages)
      consumeResult <- consumeWithStrings("group3", clientId, subscription) {
                        case (_, _) =>
                          ZIO.fail(new IllegalArgumentException("consumeWith failure")).orDie
                      }.run
    } yield consumeResult.fold(
      _ => assertCompletes,
      result => assert(result, equalTo("Expected consumeWith to fail"))
    )
  }

  val stopConsumptionStopStream = testM("stopConsumption must stop the stream") {
    val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
    for {
      topic            <- randomTopic
      group            <- randomGroup
      clientId         <- randomClientId
      _                <- produceMany(topic, kvs)
      messagesReceived <- Ref.make[Int](0)
      _ <- withConsumerSettings(group, clientId) { settings =>
            ConsumerStream.plain(settings, Subscription.topics(topic), Serde.string, Serde.string).use {
              case (control, stream) =>
                stream.mapM { _ =>
                  for {
                    nr <- messagesReceived.update(_ + 1)
                    _  <- control.stopConsumption.when(nr == 3)
                  } yield ()
                }.flattenChunks.runDrain
            }
          }
      nr <- messagesReceived.get
    } yield assert(nr, isLessThanEqualTo(10)) // NOTE this depends on a max_poll_records setting of 10
  }

  val stopConsumptionCommits = testM("process outstanding commits after a graceful shutdown") {
    val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
    val topic = "test-outstanding-commits"
    for {
      group            <- randomGroup
      clientId         <- randomClientId
      clientId2        <- randomClientId
      _                <- produceMany(topic, kvs)
      messagesReceived <- Ref.make[Int](0)
      offset <- withConsumerSettings(group, clientId) { settings =>
                 ConsumerStream.plain(settings, Subscription.topics(topic), Serde.string, Serde.string).use {
                   case (control, stream) =>
                     stream.mapM { record =>
                       for {
                         nr <- messagesReceived.update(_ + 1)
                         _  <- control.stopConsumption.when(nr == 1)
                       } yield record.offset
                     }.flattenChunks
                       .aggregate(ConsumerStream.offsetBatches)
                       .mapM(_.commit)
                       .runDrain *>
                       Consumer
                         .make(settings.copy(clientId = clientId2))
                         .use(_.committed(Set(new TopicPartition(topic, 0))).map(_.values.head))
                 }
               }
    } yield assert(offset.map(_.offset), isSome(isLessThanEqualTo(10L))) // NOTE this depends on a max_poll_records setting of 10
  }

  val offsetBatching = testM("offset batching collects the latest offset for all partitions") {
    val nrMessages   = 50
    val nrPartitions = 5

    for {
      // Produce messages on several partitions
      topic     <- randomTopic
      group     <- randomGroup
      clientId  <- randomClientId
      clientId2 <- randomClientId
      _         <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
      _ <- ZIO.traverse(1 to nrMessages) { i =>
            produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
          }

      // Consume messages
      subscription = Subscription.topics(topic)
      offsets <- withConsumerSettings(group, clientId) { settings =>
                  ConsumerStream.partitioned(settings, subscription, Serde.string, Serde.string).use {
                    case (_, stream) =>
                      stream
                        .flatMapPar(nrPartitions)(_._2.map(_.offset).flattenChunks)
                        .take(nrMessages)
                        .aggregate(ConsumerStream.offsetBatches)
                        .take(1)
                        .mapM(_.commit)
                        .runDrain *> Consumer
                        .make(settings.copy(clientId = clientId2))
                        .use(_.committed((0 until nrPartitions).map(new TopicPartition(topic, _)).toSet))
                  }
                }
    } yield assert(offsets.values.map(_.map(_.offset)), forall(isSome(equalTo(nrMessages / nrPartitions))))
  }

  val rebalancing = testM("handle rebalancing by completing topic-partition streams") {
    val nrMessages   = 50
    val nrPartitions = 6

    for {
      // Produce messages on several partitions
      topic     <- randomTopic
      group     <- randomGroup
      clientId  <- randomClientId
      clientId2 <- randomClientId
      _         <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
      _ <- ZIO.traverse(1 to nrMessages) { i =>
            produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
          }

      // Consume messages
      subscription = Subscription.topics(topic)
      consumer1 <- withConsumerSettings(group, clientId) { settings =>
                    ConsumerStream.partitioned(settings, subscription, Serde.string, Serde.string).use {
                      case (_, stream) =>
                        stream
                          .flatMapPar(nrPartitions) {
                            case (tp, partition) =>
                              ZStream
                                .fromEffect(partition.flattenChunks.runDrain)
                                .as(tp)
                          }
                          .take(nrPartitions / 2)
                          .runDrain
                    }
                  }.fork
      _ <- Live.live(ZIO.sleep(5.seconds))
      consumer2 <- withConsumerSettings(group, clientId2) { settings =>
                    ConsumerStream.partitioned(settings, subscription, Serde.string, Serde.string).use {
                      case (_, stream) =>
                        stream
                          .take(nrPartitions / 2)
                          .runDrain
                    }
                  }.fork
      _ <- consumer1.join
      _ <- consumer2.join
    } yield assertCompletes
  }

  val diagnosticRelabancing = testM("produce diagnostic events when rebalancing") {
    val nrMessages   = 50
    val nrPartitions = 6

    Diagnostics.SlidingQueue
      .make()
      .use { diagnostics =>
        for {
          // Produce messages on several partitions
          topic     <- randomTopic
          group     <- randomGroup
          clientId  <- randomClientId
          clientId2 <- randomClientId
          _         <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.traverse(1 to nrMessages) { i =>
                produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
              }

          // Consume messages
          subscription = Subscription.topics(topic)
          consumer1 <- withConsumerSettings(group, clientId) { settings =>
                        ConsumerStream
                          .partitioned(settings, subscription, Serde.string, Serde.string, diagnostics = diagnostics)
                          .use {
                            case (_, stream) =>
                              stream
                                .flatMapPar(nrPartitions) {
                                  case (tp, partition) =>
                                    ZStream
                                      .fromEffect(partition.flattenChunks.runDrain)
                                      .as(tp)
                                }
                                .take(nrPartitions / 2)
                                .runDrain
                          }
                      }.fork
          diagnosticStream <- ZStream
                               .fromQueue(diagnostics.queue)
                               .collect { case rebalance: DiagnosticEvent.Rebalance => rebalance }
                               .runCollect
                               .fork
          _ <- Live.live(ZIO.sleep(5.seconds))
          consumer2 <- withConsumerSettings(group, clientId2) { settings =>
                        ConsumerStream
                          .partitioned(settings, subscription, Serde.string, Serde.string)
                          .use {
                            case (_, stream) =>
                              stream
                                .take(nrPartitions / 2)
                                .runDrain
                          }
                      }.fork
          _ <- consumer1.join
          _ <- consumer1.join
          _ <- consumer2.join
        } yield diagnosticStream.join
      }
      .flatten
      .map { diagnosticEvents =>
        assert(diagnosticEvents.size, isGreaterThanEqualTo(2))
      }
  }
}
