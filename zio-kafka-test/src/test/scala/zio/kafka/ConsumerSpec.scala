package zio.kafka.consumer

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.{ ConsumerConfig, CooperativeStickyAssignor }
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.KafkaTestUtils._
import zio.kafka.ZIOSpecWithKafka
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object ConsumerSpec extends ZIOSpecWithKafka {
  override val kafkaPrefix: String = "consumespec"

  override def spec: Spec[TestEnvironment & Kafka, Throwable] =
    suite("Consumer Streaming")(
      test("export metrics") {
        for {
          client <- randomClient
          group  <- randomGroup
          metrics <- Consumer.metrics
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(metrics)(isNonEmpty)
      },
      test("plainStream emits messages for a topic subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic, kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set(topic)))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("chunk sizes") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic, kvs)

          sizes <- Consumer
                     .subscribeAnd(Subscription.Topics(Set(topic)))
                     .plainStream(Serde.string, Serde.string)
                     .take(100)
                     .mapChunks(c => Chunk(c.size))
                     .runCollect
                     .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(sizes)(forall(isGreaterThan(1)))
      },
      test("Consumer.subscribeAnd works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic, kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set(topic)))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("Consumer.subscribeAnd manual subscription without groupId works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient

          _ <- produceMany(topic, kvs)

          records <-
            Consumer
              .subscribeAnd(Subscription.Manual(Set(new org.apache.kafka.common.TopicPartition(topic, 0))))
              .plainStream(Serde.string, Serde.string)
              .take(5)
              .runCollect
              .provideSomeLayer[Kafka](consumer(clientId = client))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("Consuming+provideCustomLayer") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic, kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set(topic)))
                       .plainStream(Serde.string, Serde.string)
                       .take(100)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("plainStream emits messages for a pattern subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany("pattern150", kvs)
          records <- Consumer
                       .subscribeAnd(Subscription.Pattern("pattern[0-9]+".r))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("receive only messages from the subscribed topic-partition when creating a manual subscription") {
        val nrPartitions = 5
        val topic        = "manual-topic0"

        for {
          client <- randomClient
          group  <- randomGroup

          _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }
          record <- Consumer
                      .subscribeAnd(Subscription.manual(topic, partition = 2))
                      .plainStream(Serde.string, Serde.string)
                      .take(1)
                      .runHead
                      .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2" -> "msg2")))
      },
      test("receive from the right offset when creating a manual subscription with manual seeking") {
        val nrPartitions = 5
        val topic        = "manual-topic1"

        val manualOffsetSeek = 3

        for {
          client <- randomClient
          group  <- randomGroup

          _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreachDiscard(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = (0 to 9).map(j => s"key$i-$j" -> s"msg$i-$j"))
               }
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO.attempt(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          record <- Consumer
                      .subscribeAnd(Subscription.manual(topic, partition = 2))
                      .plainStream(Serde.string, Serde.string)
                      .take(1)
                      .runHead
                      .provideSomeLayer[Kafka](
                        consumer(client, Some(group), offsetRetrieval = offsetRetrieval)
                      )
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2-3" -> "msg2-3")))
      },
      test("restart from the committed position") {
        val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
        for {
          topic  <- randomTopic
          group  <- randomGroup
          first  <- randomClient
          second <- randomClient

          _ <- produceMany(topic, 0, data)
          firstResults <- for {
                            results <- Consumer
                                         .subscribeAnd(Subscription.Topics(Set(topic)))
                                         .partitionedStream(Serde.string, Serde.string)
                                         .filter(_._1 == new TopicPartition(topic, 0))
                                         .flatMap(_._2)
                                         .take(5)
                                         .transduce(ZSink.collectAllN[CommittableRecord[String, String]](5))
                                         .mapConcatZIO { committableRecords =>
                                           val records = committableRecords.map(_.record)
                                           val offsetBatch =
                                             committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                           offsetBatch.commit.as(records)
                                         }
                                         .runCollect
                                         .provideSomeLayer[Kafka](
                                           consumer(first, Some(group))
                                         )
                          } yield results
          secondResults <- for {
                             results <- Consumer
                                          .subscribeAnd(Subscription.Topics(Set(topic)))
                                          .partitionedStream(Serde.string, Serde.string)
                                          .flatMap(_._2)
                                          .take(5)
                                          .transduce(ZSink.collectAllN[CommittableRecord[String, String]](20))
                                          .mapConcatZIO { committableRecords =>
                                            val records = committableRecords.map(_.record)
                                            val offsetBatch =
                                              committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                            offsetBatch.commit.as(records)
                                          }
                                          .runCollect
                                          .provideSomeLayer[Kafka](
                                            consumer(second, Some(group))
                                          )
                           } yield results
        } yield assert((firstResults ++ secondResults).map(rec => rec.key() -> rec.value()).toList)(equalTo(data))
      },
      test("partitionedStream emits messages for each partition in a separate stream") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          _ <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach(1 to nrMessages) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }

          // Consume messages
          messagesReceived <- ZIO.foreach((0 until nrPartitions).toList)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
          subscription = Subscription.topics(topic)
          fib <- Consumer
                   .subscribeAnd(subscription)
                   .partitionedStream(Serde.string, Serde.string)
                   .flatMapPar(nrPartitions) { case (_, partition) =>
                     partition
                       .mapZIO(record => messagesReceived(record.partition).update(_ + 1).as(record))
                   }
                   .take(nrMessages.toLong)
                   .runDrain
                   .provideSomeLayer[Kafka](consumer(client, Some(group)))
                   .fork
          _                    <- fib.join
          messagesPerPartition <- ZIO.foreach(messagesReceived.values)(_.get)

        } yield assert(messagesPerPartition)(forall(equalTo(nrMessages / nrPartitions)))
      },
      test("fail when the consuming effect produces a failure") {
        val nrMessages = 10
        val messages   = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient
          subscription = Subscription.Topics(Set(topic))
          _ <- produceMany(topic, messages)
          consumeResult <- consumeWithStrings(client, Some(group), subscription) { case (_, _) =>
                             ZIO.die(new IllegalArgumentException("consumeWith failure"))
                           }.exit
        } yield consumeResult.foldExit[TestResult](
          _ => assertCompletes,
          _ => assert("result")(equalTo("Expected consumeWith to fail"))
        )
      } @@ timeout(10.seconds),
      test("stopConsumption must stop the stream") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          keepProducing <- Ref.make(true)
          _             <- (produceOne(topic, "key", "value") *> keepProducing.get).repeatWhile(b => b).fork
          _ <- Consumer
                 .subscribeAnd(Subscription.topics(topic))
                 .plainStream(Serde.string, Serde.string)
                 .zipWithIndex
                 .tap { case (_, idx) => Consumer.stopConsumption.when(idx == 3) }
                 .runDrain
                 .provideSomeLayer[Kafka](
                   consumer(client, Some(group))
                 ) *> keepProducing
                 .set(false)
        } yield assertCompletes
      },
      test("process outstanding commits after a graceful shutdown") {
        val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        val topic = "test-outstanding-commits"
        for {
          group            <- randomGroup
          client           <- randomClient
          _                <- produceMany(topic, kvs)
          messagesReceived <- Ref.make[Int](0)
          offset <- (Consumer
                      .subscribeAnd(Subscription.topics(topic))
                      .plainStream(Serde.string, Serde.string)
                      .mapZIO { record =>
                        for {
                          nr <- messagesReceived.updateAndGet(_ + 1)
                          _  <- Consumer.stopConsumption.when(nr == 1)
                        } yield record.offset
                      }
                      .transduce(Consumer.offsetBatches)
                      .mapZIO(_.commit)
                      .runDrain *>
                      Consumer.committed(Set(new TopicPartition(topic, 0))).map(_.values.head))
                      .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(offset.map(_.offset))(isSome(isLessThanEqualTo(10L)))
      } @@ TestAspect.ignore, // Not sure how to test this currently
      test("offset batching collects the latest offset for all partitions") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient
          _      <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach(1 to nrMessages) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }

          // Consume messages
          messagesReceived <- ZIO.foreach((0 until nrPartitions).toList)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
          subscription = Subscription.topics(topic)
          offsets <- (Consumer
                       .subscribeAnd(subscription)
                       .partitionedStream(Serde.string, Serde.string)
                       .flatMapPar(nrPartitions)(_._2.map(_.offset))
                       .take(nrMessages.toLong)
                       .transduce(Consumer.offsetBatches)
                       .take(1)
                       .mapZIO(_.commit)
                       .runDrain *>
                       Consumer.committed((0 until nrPartitions).map(new TopicPartition(topic, _)).toSet))
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(offsets.values.map(_.map(_.offset)))(forall(isSome(equalTo(nrMessages.toLong / nrPartitions))))
      },
      test("handle rebalancing by completing topic-partition streams") {
        val nrMessages   = 9000
        val nrPartitions = 6

        def diagnostics(consumer: Int, committed: Ref[Map[Int, Long]]) =
          Diagnostics {
            case e: DiagnosticEvent.Rebalance =>
              ZIO.debug(s"Consumer $consumer: ${e}. Offsets: ${committed}")
            case DiagnosticEvent.Commit.Started(offsets) =>
              ZIO.debug(s"Consumer $consumer starting committed ${offsets.map { case (tp, offset) =>
                  s"${tp.partition()}->${offset}"
                }.mkString(",")}")
            case DiagnosticEvent.Commit.Success(offsets) =>
              ZIO.debug(s"Consumer $consumer committed ${offsets.map { case (tp, offset) =>
                  s"for tp ${tp.partition()}->${offset.offset()}"
                }.mkString(",")}")
          }

        for {
          // Produce messages on several partitions
          topic   <- randomTopic
          group   <- randomGroup
          client1 <- randomClient
          client2 <- randomClient

          _ <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          messages = (1 to nrMessages).map(i => s"$i" -> s"msg$i")
          _ <- produceMany(topic, messages)

          // Consume messages
          subscription = Subscription.topics(topic)
          _            = println("Starting first consumer")
          consumer1Receiving <- Promise.make[Nothing, Unit]
          recordCounter      <- Ref.make(0)

          messagesConsumed <- Ref.make(Map.empty[Int, Chunk[CommittableRecord[String, String]]])
          lastCommitted    <- Ref.make(Map.empty[Int, Long])
          lastCommitted2   <- Ref.make(Map.empty[Int, Long])

          consumer1 <-
            Consumer
              .subscribeAnd(subscription)
              .plainStream(Serde.string, Serde.string, 1)
              .mapChunksZIO { records =>
                consumer1Receiving.succeed(()) *>
                  recordCounter.update(_ + records.size) *>
                  messagesConsumed.update(m => m.updated(1, m.getOrElse(1, Chunk.empty) ++ records)) *>
                  ZIO.sleep(1.second) *> {
                    val batch =
                      OffsetBatch(
                        records.map(_.offset)
                      )

                    (batch.commit *> lastCommitted.update(_ ++ batch.offsets.map { case (k, v) =>
                      k.partition() -> v
                    })).uninterruptible
                  } *> ZIO
                    .debug(s"Consumer 1 committed offsets for ${records.size} records")
                    .as(records) // .as(s"Consumer 2: ${(record.partition, record.key)}")
//                }
              }
              .timeout(5.seconds)
              .runDrain
              .provideSomeLayer[Kafka](
                consumer(
                  client1,
                  Some(group),
                  diagnostics = diagnostics(1, lastCommitted),
                  restartStreamOnRebalancing = false
                )
              )
              .tapError(e => ZIO.debug(s"Error consumer 1: ${e}"))
              .fork
          _ <- consumer1Receiving.await *> ZIO.sleep(5.seconds)

          _ = println("Starting second consumer")
          consumer2 <- Consumer
                         .subscribeAnd(subscription)
                         .plainStream(Serde.string, Serde.string)
                         .mapChunksZIO { case records =>
//                           println(s"Consumer 2 starting ${tp.partition()}")
//                           partition.mapChunksZIO { records =>
                           recordCounter.update(_ + records.size) *>
                             messagesConsumed.update(m => m.updated(2, m.getOrElse(2, Chunk.empty) ++ records)) *> ZIO
                               .sleep(1.second) *> {
                               val batch =
                                 OffsetBatch(
                                   records.map(_.offset)
                                 )

                               (batch.commit *> lastCommitted.update(_ ++ batch.offsets.map { case (k, v) =>
                                 k.partition() -> v
                               })).uninterruptible
                             } *> ZIO
                               .debug(s"Consumer 2 committed offsets for ${records.size} records")
                               .as(records) // .as(s"Consumer 2: ${(record.partition, record.key)}")
//                           }
                         }
                         .timeout(5.seconds)
                         .runDrain
                         .provideSomeLayer[Kafka](
                           consumer(
                             client2,
                             Some(group),
                             diagnostics = diagnostics(2, lastCommitted2),
                             restartStreamOnRebalancing = false
                           )
                         )
                         .tapError(e => ZIO.debug(s"Error consumer 2: ${e}"))
                         .fork
          _                 <- consumer1.join
          _                 <- consumer2.join
          nrRecordsConsumed <- recordCounter.get
          _                 <- ZIO.debug(s"Consumed ${nrRecordsConsumed} records")
          recordsConsumed   <- messagesConsumed.get
          byConsumerPartition = recordsConsumed.view
                                  .mapValues(records =>
                                    records.map(r => r.partition -> r.offset).groupBy(_._1).view.mapValues(_.size).toMap
                                  )
                                  .toMap
          _ = println(s"Consumed records by consumer by partition: ${byConsumerPartition}")
//
//          _ =
//            println(
//              s"Consumer 1 consumed: ${recordsConsumed(1).sortBy(r => (r.partition, r.key.toInt)).map(r => s"${r.partition}->${r.key}").mkString("\n")}"
//            )
//          _ =
//            println(
//              s"Consumer 2 consumed: ${recordsConsumed(2).sortBy(r => (r.partition, r.key.toInt)).map(r => s"${r.partition}->${r.key}").mkString("\n")}"
//            )
        } yield assertCompletes
      },
      test("produce diagnostic events when rebalancing") {
        val nrMessages   = 50
        val nrPartitions = 6

        ZIO.scoped {
          Diagnostics.SlidingQueue
            .make()
            .flatMap { diagnostics =>
              for {
                // Produce messages on several partitions
                topic   <- randomTopic
                group   <- randomGroup
                client1 <- randomClient
                client2 <- randomClient

                _ <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
                _ <- ZIO.foreach(1 to nrMessages) { i =>
                       produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                     }

                // Consume messages
                subscription = Subscription.topics(topic)
                consumer1 <- Consumer
                               .subscribeAnd(subscription)
                               .partitionedStream(Serde.string, Serde.string)
                               .flatMapPar(nrPartitions) { case (tp, partition) =>
                                 ZStream
                                   .fromZIO(partition.runDrain)
                                   .as(tp)
                               }
                               .take(nrPartitions.toLong / 2)
                               .runDrain
                               .provideSomeLayer[Kafka](
                                 consumer(client1, Some(group), diagnostics = diagnostics)
                               )
                               .fork
                diagnosticStream <- ZStream
                                      .fromQueue(diagnostics.queue)
                                      .collect { case rebalance: DiagnosticEvent.Rebalance => rebalance }
                                      .runCollect
                                      .fork
                _ <- ZIO.sleep(5.seconds)
                consumer2 <- Consumer
                               .subscribeAnd(subscription)
                               .partitionedStream(Serde.string, Serde.string)
                               .take(nrPartitions.toLong / 2)
                               .runDrain
                               .provideSomeLayer[Kafka](consumer(client2, Some(group)))
                               .fork
                _ <- consumer1.join
                _ <- consumer1.join
                _ <- consumer2.join
              } yield diagnosticStream.join
            }
        }.flatten
          .map(diagnosticEvents => assert(diagnosticEvents.size)(isGreaterThanEqualTo(2)))
      },
      test("support manual seeking") {
        val nrRecords        = 10
        val data             = (1 to nrRecords).toList.map(i => s"key$i" -> s"msg$i")
        val manualOffsetSeek = 3

        for {
          topic   <- randomTopic
          group   <- randomGroup
          client1 <- randomClient
          client2 <- randomClient

          _ <- produceMany(topic, 0, data)
          // Consume 5 records to have the offset committed at 5
          _ <- Consumer
                 .subscribeAnd(Subscription.topics(topic))
                 .plainStream(Serde.string, Serde.string)
                 .take(5)
                 .transduce(ZSink.collectAllN[CommittableRecord[String, String]](5))
                 .mapConcatZIO { committableRecords =>
                   val records = committableRecords.map(_.record)
                   val offsetBatch =
                     committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                   offsetBatch.commit.as(records)
                 }
                 .runCollect
                 .provideSomeLayer[Kafka](consumer(client1, Some(group)))
          // Start a new consumer with manual offset before the committed offset
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO.attempt(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          secondResults <- Consumer
                             .subscribeAnd(Subscription.topics(topic))
                             .plainStream(Serde.string, Serde.string)
                             .take(nrRecords.toLong - manualOffsetSeek)
                             .map(_.record)
                             .runCollect
                             .provideSomeLayer[Kafka](
                               consumer(client2, Some(group), offsetRetrieval = offsetRetrieval)
                             )
          // Check that we only got the records starting from the manually seek'd offset
        } yield assert(secondResults.map(rec => rec.key() -> rec.value()).toList)(equalTo(data.drop(manualOffsetSeek)))
      },
      test("commit offsets for all consumed messages") {
        val nrMessages = 50
        val messages   = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        def consumeIt(
          client: String,
          group: String,
          subscription: Subscription,
          messagesReceived: Ref[List[(String, String)]],
          done: Promise[Nothing, Unit]
        ) =
          consumeWithStrings(client, Some(group), subscription)({ (key, value) =>
            for {
              messagesSoFar <- messagesReceived.updateAndGet(_ :+ (key -> value))
              _             <- ZIO.when(messagesSoFar.size == nrMessages)(done.succeed(()))
            } yield ()
          }).fork

        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient
          subscription = Subscription.Topics(Set(topic))

          done             <- Promise.make[Nothing, Unit]
          messagesReceived <- Ref.make(List.empty[(String, String)])
          _                <- produceMany(topic, messages)
          fib              <- consumeIt(client, group, subscription, messagesReceived, done)
          _ <-
            done.await *> Live
              .live(
                ZIO.sleep(3.seconds)
              ) // TODO the sleep is necessary for the outstanding commits to be flushed. Maybe we can fix that another way
          _ <- fib.interrupt
          _ <- produceOne(topic, "key-new", "msg-new")
          newMessage <- (Consumer.subscribe(subscription) *> Consumer
                          .plainStream(Serde.string, Serde.string)
                          .take(1)
                          .map(r => (r.record.key(), r.record.value()))
                          .run(ZSink.collectAll[(String, String)])
                          .map(_.head)
                          .orDie)
                          .provideSomeLayer[Kafka](consumer(client, Some(group)))
          consumedMessages <- messagesReceived.get
        } yield assert(consumedMessages)(contains(newMessage).negate)
      },
      test("partitions for topic doesn't fail if doesn't exist") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient
          partitions <- Consumer
                          .partitionsFor(topic)
                          .provideSomeLayer[Kafka](
                            consumer(client, Some(group), allowAutoCreateTopics = false)
                          )
        } yield assert(partitions)(isEmpty)
      },
      // Test backported from fs2-kafka: https://github.com/fd4s/fs2-kafka/blob/1bd0c1f3d46b543277fce1a3cc743154c162ef09/modules/core/src/test/scala/fs2/kafka/KafkaConsumerSpec.scala#L592
      test("should close old stream during rebalancing under load") {
        val nrMessages   = 50000
        val nrPartitions = 3
        val partitions   = (0 until nrPartitions).toList
        val waitTimeout  = 15.seconds

        final case class ValidAssignmentsNotSeen(st: String) extends RuntimeException(s"Valid assignment not seen: $st")

        def run(instance: Int, topic: String, allAssignments: Ref[Map[Int, List[Int]]]) = {
          val subscription = Subscription.topics(topic)
          Consumer
            .subscribeAnd(subscription)
            .partitionedStream(Serde.string, Serde.string)
            .map { case (tp, partStream) =>
              val registerAssignment = ZStream.fromZIO(allAssignments.update { current =>
                current.get(instance) match {
                  case Some(currentList) => current.updated(instance, currentList :+ tp.partition())
                  case None              => current.updated(instance, List(tp.partition()))
                }
              })
              val deregisterAssignment = ZStream.fromZIO(allAssignments.update({ current =>
                current.get(instance) match {
                  case Some(currentList) =>
                    val idx = currentList.indexOf(tp.partition())
                    if (idx != -1) current.updated(instance, currentList.patch(idx, Nil, 1))
                    else current
                  case None => current
                }
              }))

              registerAssignment.drain ++ partStream.schedule(Schedule.fixed(10.millis)) ++ deregisterAssignment.drain
            }
            .flattenParUnbounded()
            .runDrain
        }

        def checkAssignments(allAssignments: Ref[Map[Int, List[Int]]])(instances: Set[Int]) =
          ZStream
            .repeatZIOWithSchedule(allAssignments.get, Schedule.spaced(30.millis))
            .filter { state =>
              state.keySet == instances &&
              instances.forall(instance => state.get(instance).exists(_.nonEmpty)) &&
              state.values.toList.flatten.sorted == partitions
            }
            .runHead
            .timeout(waitTimeout)
            .someOrElseZIO(allAssignments.get.map(as => ValidAssignmentsNotSeen(as.toString)).flip)

        for {
          // Produce messages on several partitions
          topic          <- randomTopic
          group          <- randomGroup
          client1        <- randomThing("client-1")
          client2        <- randomThing("client-2")
          client3        <- randomThing("client-3")
          _              <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _              <- produceMany(topic, kvs = (0 until nrMessages).map(n => s"key-$n" -> s"value->$n"))
          allAssignments <- Ref.make(Map.empty[Int, List[Int]])
          check = checkAssignments(allAssignments)(_)
          fiber0 <- run(0, topic, allAssignments)
                      .provideSomeLayer[Kafka](
                        consumer(
                          client1,
                          Some(group),
                          offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
                        )
                      )
                      .fork
          _ <- check(Set(0))
          fiber1 <- run(1, topic, allAssignments)
                      .provideSomeLayer[Kafka](
                        consumer(
                          client2,
                          Some(group),
                          offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
                        )
                      )
                      .fork
          _ <- check(Set(0, 1))
          fiber2 <- run(2, topic, allAssignments)
                      .provideSomeLayer[Kafka](
                        consumer(
                          client3,
                          Some(group),
                          offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
                        )
                      )
                      .fork
          _ <- check(Set(0, 1, 2))
          _ <- fiber2.interrupt
          _ <- allAssignments.update(_ - 2)
          _ <- check(Set(0, 1))
          _ <- fiber1.interrupt
          _ <- allAssignments.update(_ - 1)
          _ <- check(Set(0))
          _ <- fiber0.interrupt
        } yield assertCompletes
      },
      test("restartStreamsOnRebalancing mode closes all partition streams") {
        val nrPartitions = 5
        val nrMessages   = 100

        for {
          // Produce messages on several partitions
          topic   <- randomTopic
          group   <- randomGroup
          client1 <- randomClient
          client2 <- randomClient

          _ <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }

          // Consume messages
          messagesReceived <-
            ZIO.foreach((0 until nrPartitions).toList)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
          drainCount <- Ref.make(0)
          subscription = Subscription.topics(topic)
          fib <- Consumer
                   .subscribeAnd(subscription)
                   .partitionedAssignmentStream(Serde.string, Serde.string)
                   .rechunk(1)
                   .mapZIO { partitions =>
                     ZStream
                       .fromIterable(partitions.map(_._2))
                       .flatMapPar(Int.MaxValue)(s => s)
                       .mapZIO(record => messagesReceived(record.partition).update(_ + 1).as(record))
                       .mapZIO(record => record.offset.commit)
                       .runDrain
                   }
                   .mapZIO(_ =>
                     drainCount.updateAndGet(_ + 1).flatMap {
                       case 2 => Consumer.stopConsumption
                       // 1: when consumer on fib2 starts
                       // 2: when consumer on fib2 stops, end of test
                       case _ => ZIO.unit
                     }
                   )
                   .runDrain
                   .provideSomeLayer[Kafka](
                     consumer(client1, Some(group), restartStreamOnRebalancing = true)
                   )
                   .fork
          // fib is running, consuming all the published messages from all partitions.
          // Waiting until it recorded all messages
          _ <- ZIO
                 .foreach(messagesReceived.values)(_.get)
                 .map(_.sum)
                 .repeat(Schedule.recurUntil((n: Int) => n == nrMessages) && Schedule.fixed(100.millis))

          // Starting a new consumer that will stop after receiving 20 messages,
          // causing two rebalancing events for fib1 consumers on start and stop
          fib2 <- Consumer
                    .subscribeAnd(subscription)
                    .plainStream(Serde.string, Serde.string)
                    .take(20)
                    .runDrain
                    .provideSomeLayer[Kafka](
                      consumer(client2, Some(group))
                    )
                    .fork

          // Waiting until fib1's partition streams got restarted because of the rebalancing
          _ <- drainCount.get.repeat(Schedule.recurUntil((n: Int) => n == 1) && Schedule.fixed(100.millis))

          // All messages processed, the partition streams of fib are still running.
          // Saving the values and resetting the counters
          messagesReceived0 <-
            ZIO
              .foreach((0 until nrPartitions).toList) { i =>
                messagesReceived(i).get.flatMap { v =>
                  Ref.make(v).map(r => i -> r)
                } <* messagesReceived(i).set(0)
              }
              .map(_.toMap)

          // Publishing another N messages - now they will be distributed among the two consumers until
          // fib2 stops after 20 messages
          _ <- ZIO.foreachDiscard((nrMessages + 1) to (2 * nrMessages)) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }
          _ <- fib2.join
          _ <- fib.join
          // fib2 terminates after 20 messages, fib terminates after fib2 because of the rebalancing (drainCount==2)
          messagesPerPartition0 <-
            ZIO.foreach(messagesReceived0.values)(_.get) // counts from the first N messages (single consumer)
          messagesPerPartition <-
            ZIO.foreach(messagesReceived.values)(_.get) // counts from fib after the second consumer joined

          // The first set must contain all the produced messages
          // The second set must have at least one and maximum N-20 (because fib2 stops after consuming 20) -
          // the exact count cannot be known because fib2's termination triggers fib1's rebalancing asynchronously.
        } yield assert(messagesPerPartition0)(forall(equalTo(nrMessages / nrPartitions))) &&
          assert(messagesPerPartition.view.sum)(isGreaterThan(0) && isLessThanEqualTo(nrMessages - 20))
      },
      test("handles RebalanceInProgressExceptions transparently") {
        val nrPartitions = 5
        val nrMessages   = 10000

        def customConsumer(clientId: String, groupId: Option[String]) =
          (ZLayer(
            consumerSettings(
              clientId = clientId,
              groupId = groupId,
              clientInstanceId = None
            ).map(
              _.withProperties(
                ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG -> classOf[CooperativeStickyAssignor].getName
              )
                .withPollInterval(500.millis)
                .withPollTimeout(500.millis)
            )
          ) ++ ZLayer.succeed(Diagnostics.NoOp) >>> Consumer.live)

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup

          _ <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO
                 .foreachDiscard(1 to nrMessages) { i =>
                   produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                 }
                 .forkScoped

          // Consume messages
          messagesReceivedConsumer1 <- Ref.make[Int](0)
          messagesReceivedConsumer2 <- Ref.make[Int](0)
          drainCount                <- Ref.make(0)
          subscription = Subscription.topics(topic)
          stopConsumer1 <- Promise.make[Nothing, Unit]
          fib <-
            Consumer
              .subscribeAnd(subscription)
              .partitionedAssignmentStream(Serde.string, Serde.string)
              .rechunk(1)
              .mapZIOPar(16) { partitions =>
                ZIO.logInfo(s"Consumer 1 got new partition assignment: ${partitions.map(_._1.toString)}") *>
                  ZStream
                    .fromIterable(partitions.map(_._2))
                    .flatMapPar(Int.MaxValue)(s => s)
                    .mapZIO(record => messagesReceivedConsumer1.update(_ + 1).as(record))
                    .map(_.offset)
                    .aggregateAsync(Consumer.offsetBatches)
                    .mapZIO(offsetBatch => offsetBatch.commit)
                    .runDrain
              }
              .mapZIO(_ => drainCount.updateAndGet(_ + 1))
              .interruptWhen(stopConsumer1.await)
              .runDrain
              .provideSomeLayer[Kafka](
                customConsumer("consumer1", Some(group)) ++ Scope.default
              )
              .tapError(e => ZIO.logErrorCause(e.getMessage, Cause.fail(e)))
              .forkScoped

          _ <- messagesReceivedConsumer1.get
                 .repeat(Schedule.recurUntil((n: Int) => n >= 20) && Schedule.fixed(100.millis))
          _ <- ZIO.logInfo("Starting consumer 2")

          fib2 <-
            Consumer
              .subscribeAnd(subscription)
              .plainStream(Serde.string, Serde.string)
              .mapZIO(record => messagesReceivedConsumer2.update(_ + 1).as(record))
              .map(_.offset)
              .aggregateAsync(Consumer.offsetBatches)
              .mapZIO(offsetBatch => offsetBatch.commit)
              .runDrain
              .provideSomeLayer[Kafka](
                customConsumer("consumer2", Some(group))
              )
              .tapError(e => ZIO.logErrorCause("Error in consumer 2", Cause.fail(e)))
              .forkScoped

          _ <- messagesReceivedConsumer2.get
                 .repeat(Schedule.recurUntil((n: Int) => n >= 20) && Schedule.fixed(100.millis))
          _ <- stopConsumer1.succeed(())
          _ <- fib.join
          _ <- fib2.interrupt
        } yield assertCompletes
      }
    ).provideSomeLayerShared[TestEnvironment & Kafka](producer ++ Scope.default) @@ withLiveClock @@ timeout(
      300.seconds
    )
}
