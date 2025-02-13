package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{
  ConsumerConfig,
  ConsumerPartitionAssignor,
  CooperativeStickyAssignor,
  RangeAssignor
}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, CommitTimeout, OffsetRetrieval }
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization.{
  ConsumerFinalized,
  RunloopFinalized,
  SubscriptionFinalized
}
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.producer.TransactionalProducer
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaRandom, KafkaTestUtils }
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.reflect.ClassTag

//noinspection SimplifyAssertInspection
object ConsumerSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "consumespec"

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("Consumer Streaming")(
      test("export metrics") {
        for {
          client   <- randomClient
          group    <- randomGroup
          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          metrics  <- consumer.metrics
        } yield assert(metrics)(isNonEmpty)
      },
      test("plainStream emits messages for a topic subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          records <- consumer
                       .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                       .take(5)
                       .runCollect
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("chunk sizes") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          sizes <- consumer
                     .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                     .take(100)
                     .mapChunks(c => Chunk(c.size))
                     .runCollect
        } yield assert(sizes)(forall(isGreaterThan(1)))
      },
      test("Manual subscription without groupId works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client)
          records <-
            consumer
              .plainStream(
                Subscription.Manual(Set(new org.apache.kafka.common.TopicPartition(topic, 0))),
                Serde.string,
                Serde.string
              )
              .take(5)
              .runCollect
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("Consuming+provideCustomLayer") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          records <- consumer
                       .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                       .take(100)
                       .runCollect
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("plainStream emits messages for a pattern subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, "pattern150", kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          records <- consumer
                       .plainStream(Subscription.Pattern("pattern[0-9]+".r), Serde.string, Serde.string)
                       .take(5)
                       .runCollect
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("receive only messages from the subscribed topic-partition when creating a manual subscription") {
        val partitionCount = 5

        for {
          client <- randomClient
          group  <- randomGroup
          topic  <- randomTopic

          _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
          producer <- KafkaTestUtils.makeProducer
          _ <- ZIO.foreachDiscard(1 to partitionCount) { i =>
                 KafkaTestUtils.produceMany(
                   producer,
                   topic,
                   partition = i % partitionCount,
                   kvs = List(s"key$i" -> s"msg$i")
                 )
               }

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          record <- consumer
                      .plainStream(Subscription.manual(topic, partition = 2), Serde.string, Serde.string)
                      .take(1)
                      .runHead
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2" -> "msg2")))
      },
      test("receive from the right offset when creating a manual subscription with manual seeking") {
        val partitionCount = 5

        val manualOffsetSeek = 3

        for {
          client <- randomClient
          group  <- randomGroup
          topic  <- randomTopic

          _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
          producer <- KafkaTestUtils.makeProducer
          _ <- ZIO.foreachDiscard(1 to partitionCount) { i =>
                 KafkaTestUtils.produceMany(
                   producer,
                   topic,
                   partition = i % partitionCount,
                   kvs = (0 to 9).map(j => s"key$i-$j" -> s"msg$i-$j")
                 )
               }

          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO.attempt(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          consumer <- KafkaTestUtils.makeConsumer(client, Some(group), offsetRetrieval = offsetRetrieval)
          record <- consumer
                      .plainStream(Subscription.manual(topic, partition = 2), Serde.string, Serde.string)
                      .take(1)
                      .runHead
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

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, 0, data)

          consumer1 <- KafkaTestUtils.makeConsumer(first, Some(group))
          firstResults <- for {
                            results <- consumer1
                                         .partitionedStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                                         .filter(_._1 == new TopicPartition(topic, 0))
                                         .flatMap(_._2)
                                         .take(5)
                                         .transduce(ZSink.collectAllN[CommittableRecord[String, String]](5))
                                         .mapConcatZIO { committableRecords =>
                                           val records     = committableRecords.map(_.record)
                                           val offsetBatch = OffsetBatch(committableRecords.map(_.offset))

                                           offsetBatch.commit.as(records)
                                         }
                                         .runCollect
                          } yield results

          consumer2 <- KafkaTestUtils.makeConsumer(second, Some(group))
          secondResults <- for {
                             results <-
                               consumer2
                                 .partitionedStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                                 .flatMap(_._2)
                                 .take(5)
                                 .transduce(ZSink.collectAllN[CommittableRecord[String, String]](20))
                                 .mapConcatZIO { committableRecords =>
                                   val records     = committableRecords.map(_.record)
                                   val offsetBatch = OffsetBatch(committableRecords.map(_.offset))

                                   offsetBatch.commit.as(records)
                                 }
                                 .runCollect
                           } yield results
        } yield assert((firstResults ++ secondResults).map(rec => rec.key() -> rec.value()).toList)(equalTo(data))
      },
      test("partitionedStream emits messages for each partition in a separate stream") {
        val messageCount   = 50
        val partitionCount = 5

        for {
          // Produce messages on several partitions
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
          producer <- KafkaTestUtils.makeProducer
          _ <- ZIO.foreachDiscard(1 to messageCount) { i =>
                 KafkaTestUtils.produceMany(
                   producer,
                   topic,
                   partition = i % partitionCount,
                   kvs = List(s"key$i" -> s"msg$i")
                 )
               }

          // Consume messages
          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          messagesReceived <-
            ZIO.foreach((0 until partitionCount).toList)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
          subscription = Subscription.topics(topic)
          fib <- consumer
                   .partitionedStream(subscription, Serde.string, Serde.string)
                   .flatMapPar(partitionCount) { case (_, partition) =>
                     partition
                       .mapZIO(record => messagesReceived(record.partition).update(_ + 1).as(record))
                   }
                   .take(messageCount.toLong)
                   .runDrain
                   .fork
          _                    <- fib.join
          messagesPerPartition <- ZIO.foreach(messagesReceived.values)(_.get)

        } yield assert(messagesPerPartition)(forall(equalTo(messageCount / partitionCount)))
      },
      test("fail when the consuming effect produces a failure") {
        val messageCount = 10
        val messages     = (1 to messageCount).toList.map(i => (s"key$i", s"msg$i"))

        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, messages)

          subscription = Subscription.topics(topic)
          consumeResult <- KafkaTestUtils
                             .consumeWithStrings(client, Some(group), subscription) { _ =>
                               ZIO.die(new IllegalArgumentException("consumeWith failure"))
                             }
                             .exit
        } yield consumeResult.foldExit[TestResult](
          _ => assertCompletes,
          _ => assert("result")(equalTo("Expected consumeWith to fail"))
        )
      } @@ timeout(10.seconds),
      test("stopConsumption must end streams while still processing commits") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          producer <- KafkaTestUtils.makeProducer

          keepProducing <- Ref.make(true)
          _ <- KafkaTestUtils
                 .produceOne(producer, topic, "key", "value")
                 .repeatWhileZIO(_ => keepProducing.get)
                 .fork
          _ <- consumer
                 .partitionedStream(Subscription.topics(topic), Serde.string, Serde.string)
                 .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
                   partitionStream.zipWithIndex.tap { case (record, idx) =>
                     consumer.stopConsumption *>
                       (consumer.stopConsumption <* ZIO.logDebug("Stopped consumption")).when(idx == 3) *>
                       record.offset.commit <* ZIO.logDebug(s"Committed $idx")
                   }.tap { case (_, idx) => ZIO.logDebug(s"Consumed $idx") }
                 }
                 .runDrain
                 .zipLeft(ZIO.logDebug("Stream completed"))
          _ <- keepProducing.set(false)
        } yield assertCompletes
      },
      test("process outstanding commits after a graceful shutdown") {
        val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        val topic = "test-outstanding-commits"
        for {
          group  <- randomGroup
          client <- randomClient

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          messagesReceived <- Ref.make[Int](0)
          consumer         <- KafkaTestUtils.makeConsumer(client, Some(group))
          offset <- consumer
                      .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                      .mapConcatZIO { record =>
                        for {
                          nr <- messagesReceived.updateAndGet(_ + 1)
                          _  <- consumer.stopConsumption.when(nr == 10)
                        } yield if (nr < 10) Seq(record.offset) else Seq.empty
                      }
                      .transduce(Consumer.offsetBatches)
                      .mapZIO(_.commit)
                      .runDrain *>
                      consumer.committed(Set(new TopicPartition(topic, 0))).map(_.values.head)
        } yield assert(offset.map(_.offset))(isSome(equalTo(9L)))
      },
      test("process outstanding commits after a graceful shutdown with aggregateAsync using `maxRebalanceDuration`") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          producer <- KafkaTestUtils.makeProducer
          _ <- KafkaTestUtils.scheduledProduce(producer, topic, Schedule.fixed(50.millis).jittered).runDrain.forkScoped

          consumer <- KafkaTestUtils.makeConsumer(
                        client,
                        Some(group),
                        commitTimeout = 4.seconds,
                        rebalanceSafeCommits = true,
                        maxRebalanceDuration = 6.seconds
                      )
          lastProcessedOffset <- Ref.make(0L)
          offset <- consumer
                      .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                      .mapZIO { record =>
                        for {
                          nr <- lastProcessedOffset.updateAndGet(_ + 1)
                          _  <- consumer.stopConsumption.when(nr == 10)
                        } yield record.offset
                      }
                      .aggregateAsync(Consumer.offsetBatches)
                      .mapZIO(_.commit)
                      .runDrain *>
                      consumer.committed(Set(new TopicPartition(topic, 0))).map(_.values.head)

          lastOffset <- lastProcessedOffset.get
        } yield assertTrue(offset.map(_.offset).contains(lastOffset))
      } @@ TestAspect.nonFlaky(2),
      test("a consumer timeout interrupts the stream and shuts down the consumer") {
        // Setup of this test:
        // - Set the max poll interval very low: a couple of seconds.
        // - Continuously produce records so that data is always available.
        // - Consumer 1 consumes very slowly; each chunk takes more than the max poll interval.
        // - Consumer 2 is fast.
        // - We assert that consumer 1 is interrupted and that consumer 2 is stopped.
        for {
          topic1   <- randomTopic
          topic2   <- randomTopic
          group    <- randomGroup
          clientId <- randomClient
          _        <- KafkaTestUtils.createCustomTopic(topic1)
          _        <- KafkaTestUtils.createCustomTopic(topic2)

          producerSchedule = Schedule.fixed(500.millis).jittered
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.scheduledProduce(producer, topic1, producerSchedule).runDrain.forkScoped
          _        <- KafkaTestUtils.scheduledProduce(producer, topic2, producerSchedule).runDrain.forkScoped

          settings <- KafkaTestUtils
                        .consumerSettings(
                          clientId = clientId,
                          groupId = Some(group),
                          maxPollInterval = 2.seconds,
                          `max.poll.records` = 2
                        )
                        .map(_.withoutPartitionPreFetching.withPollTimeout(100.millis))
          consumer <- Consumer.make(settings)
          // The slow consumer:
          c1 <- consumer
                  .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                  // Consumer timeout detection is done per chunk. From here on, work per chunk
                  .chunks
                  // Sleep for some seconds to simulate a consumer that is stuck (only sleep for the first chunk).
                  // Because slow consumers are only detected once every run-loop, detection can take many seconds.
                  .tap { c =>
                    ZIO.sleep(10.seconds).when(c.head.key == "key0")
                  }
                  // Use `take` to ensure the test ends quickly, even when the interrupt fails to occur.
                  .take(8)
                  .runDrain
                  .exit
                  .fork
          // Another consumer:
          _ <- consumer
                 .plainStream(Subscription.topics(topic2), Serde.string, Serde.string)
                 .runDrain
                 .forkScoped
          c1Exit        <- c1.join
          subscriptions <- consumer.subscription.delay(500.millis)
        } yield assertTrue(
          c1Exit.isFailure,
          subscriptions.isEmpty
        )
      },
      test("a slow producer does not interrupt the stream") {
        ZIO.scoped {
          for {
            topic    <- randomTopic
            group    <- randomGroup
            clientId <- randomClient
            _        <- KafkaTestUtils.createCustomTopic(topic)

            // A slow producer
            producer <- KafkaTestUtils.makeProducer
            _        <- KafkaTestUtils.scheduledProduce(producer, topic, Schedule.fixed(1.second)).runDrain.forkScoped

            settings <- KafkaTestUtils.consumerSettings(
                          clientId = clientId,
                          groupId = Some(group),
                          maxPollInterval = 300.millis
                        )
            consumer <- Consumer.make(settings.withPollTimeout(50.millis))
            consumed <- consumer
                          .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                          .take(2)
                          .runCollect
          } yield assertTrue(consumed.size == 2)
        }
      },
      test("offset batching collects the latest offset for all partitions") {
        val nrMessages     = 50
        val partitionCount = 5

        for {
          // Produce messages on several partitions
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient

          _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
          producer <- KafkaTestUtils.makeProducer
          _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                 KafkaTestUtils.produceMany(
                   producer,
                   topic,
                   partition = i % partitionCount,
                   kvs = List(s"key$i" -> s"msg$i")
                 )
               }

          // Consume messages
          subscription = Subscription.topics(topic)
          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          offsets <- (consumer
                       .partitionedStream(subscription, Serde.string, Serde.string)
                       .flatMapPar(partitionCount)(_._2.map(_.offset))
                       .take(nrMessages.toLong)
                       .transduce(Consumer.offsetBatches)
                       .take(1)
                       .mapZIO(_.commit)
                       .runDrain *>
                       consumer.committed((0 until partitionCount).map(new TopicPartition(topic, _)).toSet))
        } yield assert(offsets.values.map(_.map(_.offset)))(forall(isSome(equalTo(nrMessages.toLong / partitionCount))))
      },
      test("commits an offset with metadata") {
        for {
          topic    <- randomTopic
          group    <- randomGroup
          metadata <- randomThing("metadata")
          client   <- randomClient

          _        <- KafkaTestUtils.createCustomTopic(topic)
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceOne(producer, topic, "key", "msg")

          // Consume messages
          subscription = Subscription.topics(topic)
          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          offsets <- consumer
                       .partitionedStream(subscription, Serde.string, Serde.string)
                       .flatMap(_._2.map(_.offset.withMetadata(metadata)))
                       .take(1)
                       .transduce(Consumer.offsetBatches)
                       .take(1)
                       .mapZIO(_.commit)
                       .runDrain *>
                       consumer.committed(Set(new TopicPartition(topic, 0)))
        } yield assert(offsets.values.headOption.flatten.map(_.metadata))(isSome(equalTo(metadata)))
      },
      test("access to the java consumer must be fair") {
        val kvs = (1 to 10).toList.map(i => (s"key$i", s"msg$i"))

        val expectedResult = (0 to 9).toList.map(i => i.toLong -> i.toLong)

        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          committedOffsetRef <- Ref.make(Seq.empty[(Long, Long)])
          topicPartition = new TopicPartition(topic, 0)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group), commitTimeout = 2.seconds)
          _ <- consumer
                 .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                 .take(10)
                 .map(_.offset)
                 .mapZIO(offsetBatch =>
                   consumer
                     .committed(Set(topicPartition))
                     .flatMap(offset =>
                       committedOffsetRef.update(map =>
                         map :+ (offsetBatch.offset -> offset(topicPartition).map(_.offset()).getOrElse(0L))
                       ) *> offsetBatch.commit
                     )
                 )
                 .runDrain
          offsets <- committedOffsetRef.get
        } yield assert(offsets)(equalTo(expectedResult))
      } @@ TestAspect.timeout(20.seconds),
      test("handle rebalancing by completing topic-partition streams") {
        val nrMessages     = 50
        val partitionCount = 6 // Must be even and strictly positive

        for {
          // Produce messages on several partitions
          topic   <- randomTopic
          group   <- randomGroup
          client1 <- randomClient
          client2 <- randomClient

          _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
          producer <- KafkaTestUtils.makeProducer
          _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                 KafkaTestUtils
                   .produceMany(producer, topic, partition = i % partitionCount, kvs = List(s"key$i" -> s"msg$i"))
               }

          consumer1 <- KafkaTestUtils.makeConsumer(client1, Some(group))
          consumer2 <- KafkaTestUtils.makeConsumer(client2, Some(group))

          // Consume messages
          subscription = Subscription.topics(topic)
          assignedPartitionsRef <- Ref.make(Set.empty[Int]) // Set of partition numbers
          // Create a Promise to signal when consumer1 has processed half the partitions
          consumer1Ready <- Promise.make[Nothing, Unit]
          consumer1Fib <- consumer1
                            .partitionedStream(subscription, Serde.string, Serde.string)
                            .flatMapPar(partitionCount) { case (tp, partition) =>
                              ZStream
                                .fromZIO(
                                  consumer1Ready
                                    .succeed(())
                                    .whenZIO(
                                      assignedPartitionsRef
                                        .updateAndGet(_ + tp.partition())
                                        .map(_.size >= (partitionCount / 2))
                                    ) *>
                                    partition.runDrain
                                )
                                .as(tp)
                            }
                            .take(partitionCount.toLong / 2)
                            .runDrain
                            .fork
          _ <- consumer1Ready.await
          consumer2Fib <- consumer2
                            .partitionedStream(subscription, Serde.string, Serde.string)
                            .take(partitionCount.toLong / 2)
                            .runDrain
                            .fork
          _ <- consumer1Fib.join
          _ <- consumer2Fib.join
        } yield assertCompletes
      },
      test("produce diagnostic events when rebalancing") {
        val nrMessages     = 50
        val partitionCount = 6

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

                _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
                producer <- KafkaTestUtils.makeProducer
                _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                       KafkaTestUtils
                         .produceMany(producer, topic, partition = i % partitionCount, kvs = List(s"key$i" -> s"msg$i"))
                     }

                consumer1 <- KafkaTestUtils.makeConsumer(client1, Some(group), diagnostics = diagnostics)
                consumer2 <- KafkaTestUtils.makeConsumer(client2, Some(group))

                // Consume messages
                subscription = Subscription.topics(topic)
                consumer1Ready        <- Promise.make[Nothing, Unit]
                assignedPartitionsRef <- Ref.make(Set.empty[Int]) // Set of partition numbers
                consumer1Fib <- consumer1
                                  .partitionedStream(subscription, Serde.string, Serde.string)
                                  .flatMapPar(partitionCount) { case (tp, partition) =>
                                    ZStream
                                      .fromZIO(
                                        consumer1Ready
                                          .succeed(())
                                          .whenZIO(
                                            assignedPartitionsRef
                                              .updateAndGet(_ + tp.partition())
                                              .map(_.size >= (partitionCount / 2))
                                          ) *>
                                          partition.runDrain
                                      )
                                      .as(tp)
                                  }
                                  .take(partitionCount.toLong / 2)
                                  .runDrain
                                  .fork
                diagnosticStream <- ZStream
                                      .fromQueue(diagnostics.queue)
                                      .collect { case rebalance: DiagnosticEvent.Rebalance => rebalance }
                                      .runCollect
                                      .fork
                _ <- consumer1Ready.await
                consumer2Fib <- consumer2
                                  .partitionedStream(subscription, Serde.string, Serde.string)
                                  .take(partitionCount.toLong / 2)
                                  .runDrain
                                  .fork
                _ <- consumer1Fib.join
                _ <- consumer2Fib.join
              } yield diagnosticStream.join
            }
        }.flatten
          .map(diagnosticEvents => assert(diagnosticEvents.size)(isGreaterThanEqualTo(2)))
      },
      suite("support manual seeking") {
        val manualOffsetSeek = 3L
        def manualSeekTest(
          defaultStrategy: AutoOffsetStrategy
        ): ZIO[Scope & Kafka, Throwable, Map[Int, Long]] = {
          val nrRecords      = 10
          val data           = (1 to nrRecords).map(i => s"key$i" -> s"msg$i")
          val partitionCount = 4
          for {
            topic   <- randomTopic
            group   <- randomGroup
            client1 <- randomClient
            client2 <- randomClient

            _        <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
            producer <- KafkaTestUtils.makeProducer
            produceToAll = ZIO.foreachDiscard(0 until partitionCount) { p =>
                             KafkaTestUtils.produceMany(producer, topic, p, data)
                           }
            _ <- produceToAll

            // Consume 5 records from partitions 0, 1 and 2 (not 3). This sets the current offset to 5.
            consumer1 <- KafkaTestUtils.makeConsumer(client1, Some(group))
            _ <- ZIO
                   .foreachDiscard(Chunk(0, 1, 2)) { partition =>
                     consumer1
                       .plainStream(Subscription.manual(topic, partition), Serde.string, Serde.string)
                       .take(5)
                       .transduce(ZSink.collectAllN[CommittableRecord[String, String]](5))
                       .mapConcatZIO { committableRecords =>
                         val records     = committableRecords.map(_.record)
                         val offsetBatch = OffsetBatch(committableRecords.map(_.offset))
                         offsetBatch.commit.as(records)
                       }
                       .runCollect
                   }
            // Start a new consumer with manual offsets. The given offset per partition is:
            //  p0: 3, before the current offset => should consume from the given offset
            //  p1: _Maxvalue_, offset out of range (invalid) => should consume using default strategy
            //  p2: _nothing_ given => should consume from the committed offset
            //  p4: _nothing_ given => should consume using default strategy
            offsetRetrieval = OffsetRetrieval.Manual(
                                getOffsets = _ =>
                                  ZIO.attempt(
                                    Map(
                                      new TopicPartition(topic, 0) -> manualOffsetSeek,
                                      new TopicPartition(topic, 1) -> Long.MaxValue
                                    )
                                  ),
                                defaultStrategy = defaultStrategy
                              )
            // Start the second consumer.
            consumer2 <- KafkaTestUtils.makeConsumer(client2, Some(group), offsetRetrieval = offsetRetrieval)
            c2Fib <- consumer2
                       .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                       .runFoldWhile(Map.empty[Int, Long])(_.size < partitionCount) { case (acc, record) =>
                         if (acc.contains(record.partition)) acc
                         else acc + (record.partition -> record.offset.offset)
                       }
                       .fork
            // For defaultStrategy 'latest' the second consumer won't see a record until we produce some more.
            produceFib <- produceToAll
                            .repeat(Schedule.spaced(1.second))
                            .when(defaultStrategy == AutoOffsetStrategy.Latest)
                            .fork
            c2Results <- c2Fib.join
            _         <- produceFib.interrupt
          } yield c2Results
        }

        Seq(
          test("manual seek with earliest default strategy") {
            for {
              consumeStartOffsets <- manualSeekTest(AutoOffsetStrategy.Earliest)
            } yield assertTrue(
              consumeStartOffsets(0) == manualOffsetSeek,
              consumeStartOffsets(1) == 0L,
              consumeStartOffsets(2) == 5L,
              consumeStartOffsets(3) == 0L
            )
          },
          test("manual seek with latest default strategy") {
            for {
              consumeStartOffsets <- manualSeekTest(AutoOffsetStrategy.Latest)
            } yield assertTrue(
              consumeStartOffsets(0) == manualOffsetSeek,
              consumeStartOffsets(1) >= 10L,
              consumeStartOffsets(2) == 5L,
              consumeStartOffsets(3) >= 10L
            )
          }
        )
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
          KafkaTestUtils
            .consumeWithStrings(client, Some(group), subscription)(record =>
              for {
                messagesSoFar <- messagesReceived.updateAndGet(_ :+ (record.key() -> record.value()))
                _             <- ZIO.when(messagesSoFar.size == nrMessages)(done.succeed(()))
              } yield ()
            )
            .fork

        for {
          topic    <- randomTopic
          group    <- randomGroup
          client   <- randomClient
          producer <- KafkaTestUtils.makeProducer

          subscription = Subscription.Topics(Set(topic))

          done             <- Promise.make[Nothing, Unit]
          messagesReceived <- Ref.make(List.empty[(String, String)])
          _                <- KafkaTestUtils.produceMany(producer, topic, messages)
          fib              <- consumeIt(client, group, subscription, messagesReceived, done)
          _ <-
            done.await *> Live
              .live(
                ZIO.sleep(3.seconds)
              ) // TODO the sleep is necessary for the outstanding commits to be flushed. Maybe we can fix that another way
          _ <- fib.interrupt
          _ <- KafkaTestUtils.produceOne(producer, topic, "key-new", "msg-new")

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          newMessage <- consumer
                          .plainStream(subscription, Serde.string, Serde.string)
                          .take(1)
                          .map(r => (r.record.key(), r.record.value()))
                          .run(ZSink.collectAll[(String, String)])
                          .map(_.head)
                          .orDie
          consumedMessages <- messagesReceived.get
        } yield assert(consumedMessages)(contains(newMessage).negate)
      },
      suite("rebalanceSafeCommits prevents processing messages twice when rebalancing")({

        /**
         * Outline of this test:
         *   - A producer generates some messages on every partition of a topic (2 partitions),
         *   - A consumer starts reading from the topic. It is the only consumer so it handles all partitions.
         *   - After a few messages a second consumer is started. One partition will be re-assigned.
         *
         * Since the first consumer is slow, we expect it to not have committed the offsets yet when the rebalance
         * happens. As a consequence, the second consumer would see some messages the first consumer already consumed.
         *
         * '''However,''' since we enable `rebalanceSafeCommits` on the first consumer, no messages should be consumed
         * by both consumers.
         */
        def testForPartitionAssignmentStrategy[T <: ConsumerPartitionAssignor: ClassTag] =
          test(implicitly[ClassTag[T]].runtimeClass.getName) {
            val partitionCount = 2

            def makeConsumer(
              clientId: String,
              groupId: String,
              rebalanceSafeCommits: Boolean
            ): ZIO[Scope with Kafka, Throwable, Consumer] =
              for {
                settings <- KafkaTestUtils.consumerSettings(
                              clientId = clientId,
                              groupId = Some(groupId),
                              `max.poll.records` = 1,
                              rebalanceSafeCommits = rebalanceSafeCommits,
                              maxRebalanceDuration = 60.seconds
                            )
                consumer <- Consumer.make(settings)
              } yield consumer

            for {
              topic <- randomTopic
              subscription = Subscription.topics(topic)
              clientId1 <- randomClient
              clientId2 <- randomClient
              groupId   <- randomGroup
              _         <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
              // Produce one message to all partitions, every 500 ms
              producer <- KafkaTestUtils.makeProducer
              pFib <- ZStream
                        .fromSchedule(Schedule.fixed(500.millis))
                        .mapZIO { i =>
                          ZIO.foreachDiscard(0 until partitionCount) { p =>
                            KafkaTestUtils.produceMany(producer, topic, p, Seq((s"key-$p-$i", s"msg-$p-$i")))
                          }
                        }
                        .runDrain
                        .fork
              _         <- ZIO.logDebug("Starting consumer 1")
              c1        <- makeConsumer(clientId1, groupId, rebalanceSafeCommits = true)
              c1Sleep   <- Ref.make[Int](3)
              c1Started <- Promise.make[Nothing, Unit]
              c1Keys    <- Ref.make(Chunk.empty[String])
              fib1 <-
                ZIO
                  .logAnnotate("consumer", "1") {
                    // When the stream ends, the topic subscription ends as well. Because of that the consumer
                    // shuts down and commits are no longer possible. Therefore, we signal the second consumer in
                    // such a way that it doesn't close the stream.
                    c1
                      .plainStream(subscription, Serde.string, Serde.string)
                      .tap(record =>
                        ZIO.logDebug(
                          s"Received record with offset ${record.partition}:${record.offset.offset} and key ${record.key}"
                        )
                      )
                      .tap { record =>
                        // Signal consumer 2 can start when a record is seen for every partition.
                        for {
                          keys <- c1Keys.updateAndGet(_ :+ record.key)
                          _    <- c1Started.succeed(()).when(keys.map(_.split('-')(1)).toSet.size == partitionCount)
                        } yield ()
                      }
                      // Buffer so that the above can run ahead of the below, this is important;
                      // we want consumer 2 to start before consumer 1 commits.
                      .buffer(partitionCount)
                      .mapZIO { record =>
                        for {
                          s <- c1Sleep.get
                          _ <- ZIO.sleep(s.seconds)
                          _ <- ZIO.logDebug(
                                 s"Committing offset ${record.partition}:${record.offset.offset} for key ${record.key}"
                               )
                          _ <- record.offset.commit
                        } yield record.key
                      }
                      .runCollect
                      .map(_.toSet)
                  }
                  .fork
              _  <- c1Started.await
              _  <- ZIO.logDebug("Starting consumer 2")
              c2 <- makeConsumer(clientId2, groupId, rebalanceSafeCommits = false)
              fib2 <- ZIO
                        .logAnnotate("consumer", "2") {
                          c2
                            .plainStream(subscription, Serde.string, Serde.string)
                            .tap(msg => ZIO.logDebug(s"Received ${msg.key}"))
                            .mapZIO(msg => msg.offset.commit.as(msg.key))
                            .take(5)
                            .runCollect
                            .map(_.toSet)
                        }
                        .fork
              _                   <- ZIO.logDebug("Waiting for consumers to end")
              c2Keys: Set[String] <- fib2.join
              _                   <- ZIO.logDebug("Consumer 2 ready")
              _                   <- c1.stopConsumption
              _                   <- c1Sleep.set(0)
              c1Keys: Set[String] <- fib1.join
              _                   <- ZIO.logDebug("Consumer 1 ready")
              _                   <- pFib.interrupt
            } yield assertTrue((c1Keys intersect c2Keys).isEmpty)
          }

        // Test for both default partition assignment strategies
        Seq(
          testForPartitionAssignmentStrategy[RangeAssignor],
          testForPartitionAssignmentStrategy[CooperativeStickyAssignor]
        )
      }: _*),
      test("external commits are used when rebalanceSafeCommits is enabled") {

        /*
         * Outline of this test
         *   - A producer generates some messages on every partition of a topic (2 partitions),
         *   - Consumer 1 starts reading from the topic. It is the only consumer so it handles all partitions. This
         *     consumer has `rebalanceSafeCommits` enabled. It does not commit offsets, but it does register external
         *     commits (it does not actually commit anywhere). In addition we set `maxRebalanceDuration` to 20 seconds.
         *   - After a few messages consumer 2 is started and a rebalance starts.
         *   - We measure how long the rebalance takes.
         *
         * When the rebalance finishes immediately, we know that the external commits were used. If it finishes in 20
         * seconds, we know that the external commits were not used.
         */
        val partitionCount = 2

        def makeConsumer(
          clientId: String,
          groupId: String,
          rebalanceSafeCommits: Boolean,
          diagnostics: Diagnostics
        ): ZIO[Scope with Kafka, Throwable, Consumer] =
          for {
            settings <- KafkaTestUtils.consumerSettings(
                          clientId = clientId,
                          groupId = Some(groupId),
                          `max.poll.records` = 1,
                          rebalanceSafeCommits = rebalanceSafeCommits,
                          maxRebalanceDuration = 20.seconds,
                          commitTimeout = 1.second
                        )
            consumer <- Consumer.make(settings, diagnostics)
          } yield consumer

        for {
          topic <- randomTopic
          subscription = Subscription.topics(topic)
          clientId1 <- randomClient
          clientId2 <- randomClient
          groupId   <- randomGroup
          _         <- KafkaTestUtils.createCustomTopic(topic, partitionCount)
          // Produce one message to all partitions, every 500 ms
          producer <- KafkaTestUtils.makeProducer
          _ <- ZStream
                 .fromSchedule(Schedule.fixed(500.millis))
                 .mapZIO { i =>
                   ZIO.foreachDiscard(0 until partitionCount) { p =>
                     KafkaTestUtils.produceMany(producer, topic, p, Seq((s"key-$p-$i", s"msg-$p-$i")))
                   }
                 }
                 .runDrain
                 .fork
          _                       <- ZIO.logDebug("Starting consumer 1")
          rebalanceEndTimePromise <- Promise.make[Nothing, Instant]
          c1Diagnostics = new Diagnostics {
                            override def emit(event: => DiagnosticEvent): UIO[Unit] = event match {
                              case r: DiagnosticEvent.Rebalance if r.assigned.size == 1 =>
                                ZIO.logDebug(s"Rebalance finished: $r") *>
                                  Clock.instant.flatMap(rebalanceEndTimePromise.succeed).unit
                              case r: DiagnosticEvent.Rebalance =>
                                ZIO.logDebug(s"Rebalance finished: $r")
                              case _ => ZIO.unit
                            }
                          }
          c1        <- makeConsumer(clientId1, groupId, rebalanceSafeCommits = true, c1Diagnostics)
          c1Started <- Promise.make[Nothing, Unit]
          c1Offsets <- Ref.make(Chunk.empty[Offset])
          _ <-
            ZIO
              .logAnnotate("consumer", "1") {
                // When the stream ends, the topic subscription ends as well. Because of that the consumer
                // shuts down and commits are no longer possible. Therefore, we signal the second consumer in
                // such a way that it doesn't close the stream.
                c1
                  .plainStream(subscription, Serde.string, Serde.string)
                  .tap { record =>
                    for {
                      _ <-
                        ZIO.logDebug(
                          s"Received record with offset ${record.partition}:${record.offset.offset} and key ${record.key}"
                        )
                      // Signal that consumer 2 can start when a record was seen for every partition.
                      offsets <- c1Offsets.updateAndGet(_ :+ record.offset)
                      _       <- c1Started.succeed(()).when(offsets.map(_.partition).toSet.size == partitionCount)
                      // Register an external commit (which we're not actually doing 😀)
                      _ <- c1.registerExternalCommits(OffsetBatch(record.offset)).unit
                    } yield ()
                  }
                  .runDrain
              }
              .fork
          _                  <- c1Started.await
          _                  <- ZIO.logDebug("Starting consumer 2")
          c2                 <- makeConsumer(clientId2, groupId, rebalanceSafeCommits = false, Diagnostics.NoOp)
          rebalanceStartTime <- Clock.instant
          _ <- ZIO
                 .logAnnotate("consumer", "2") {
                   c2
                     .plainStream(subscription, Serde.string, Serde.string)
                     .tap(msg => ZIO.logDebug(s"Received ${msg.key}"))
                     .runDrain
                 }
                 .fork
          _                <- ZIO.logDebug("Waiting for rebalance to end")
          rebalanceEndTime <- rebalanceEndTimePromise.await
          _                <- c1.stopConsumption *> c2.stopConsumption
          rebalanceDuration = Duration
                                .fromInterval(rebalanceStartTime, rebalanceEndTime)
                                .truncatedTo(ChronoUnit.SECONDS)
                                .getSeconds
                                .toInt
          _ <- ZIO.logDebug(s"Rebalance took $rebalanceDuration seconds")
        } yield assertTrue(rebalanceDuration <= 2)
      },
      test("partitions for topic doesn't fail if doesn't exist") {
        for {
          topic      <- randomTopic
          group      <- randomGroup
          client     <- randomClient
          consumer   <- KafkaTestUtils.makeConsumer(client, Some(group), allowAutoCreateTopics = false)
          partitions <- consumer.partitionsFor(topic)
        } yield assert(partitions)(isEmpty)
      },
      // Test backported from fs2-kafka: https://github.com/fd4s/fs2-kafka/blob/1bd0c1f3d46b543277fce1a3cc743154c162ef09/modules/core/src/test/scala/fs2/kafka/KafkaConsumerSpec.scala#L592
      test("should close old stream during rebalancing under load") {
        val nrMessages   = 50000
        val nrPartitions = 3
        val partitions   = (0 until nrPartitions).toList
        val waitTimeout  = 15.seconds

        final case class ValidAssignmentsNotSeen(instances: Set[Int], st: String)
            extends RuntimeException(s"Valid assignment not seen for instances $instances: $st")

        def run(consumer: Consumer, instance: Int, topic: String, allAssignments: Ref[Map[Int, List[Int]]]) =
          ZIO.logAnnotate("consumer", instance.toString) {
            val subscription = Subscription.topics(topic)
            consumer
              .partitionedStream(subscription, Serde.string, Serde.string)
              .flatMapPar(Int.MaxValue) { case (tp, partStream) =>
                val registerAssignment = ZStream.logDebug(s"Registering partition ${tp.partition()}") *>
                  ZStream.fromZIO {
                    allAssignments.update { current =>
                      current.get(instance) match {
                        case Some(currentList) => current.updated(instance, currentList :+ tp.partition())
                        case None              => current.updated(instance, List(tp.partition()))
                      }
                    }
                  }
                val deregisterAssignment = ZStream.logDebug(s"Deregistering partition ${tp.partition()}") *>
                  ZStream.finalizer {
                    allAssignments.update { current =>
                      current.get(instance) match {
                        case Some(currentList) =>
                          val idx = currentList.indexOf(tp.partition())
                          if (idx != -1) current.updated(instance, currentList.patch(idx, Nil, 1))
                          else current
                        case None => current
                      }
                    }
                  }

                (registerAssignment *> deregisterAssignment *> partStream).drain
              }
              .runDrain
          }

        // Check every 30 millis (for at most 15 seconds) that the following condition holds:
        // - all instances are assigned to a partition,
        // - each instance has a partition assigned,
        // - all partitions are assigned.
        // Fail when the condition is not observed.
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
            .someOrElseZIO(allAssignments.get.map(as => ValidAssignmentsNotSeen(instances, as.toString)).flip)

        def makeConsumer(client: String, group: String): ZIO[Scope & Kafka, Throwable, Consumer] =
          KafkaTestUtils.makeConsumer(
            client,
            Some(group),
            offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
          )

        for {
          // Produce messages on several partitions
          topic     <- randomTopic
          group     <- randomGroup
          client1   <- randomThing("client-1")
          client2   <- randomThing("client-2")
          client3   <- randomThing("client-3")
          consumer1 <- makeConsumer(client1, group)
          consumer2 <- makeConsumer(client2, group)
          consumer3 <- makeConsumer(client3, group)

          _        <- KafkaTestUtils.createCustomTopic(topic, nrPartitions)
          producer <- KafkaTestUtils.makeProducer
          _ <- KafkaTestUtils
                 .produceMany(producer, topic, kvs = (0 until nrMessages).map(n => s"key-$n" -> s"value->$n"))

          allAssignments <- Ref.make(Map.empty[Int, List[Int]])
          check = checkAssignments(allAssignments)(_)
          fiber0 <- run(consumer1, 0, topic, allAssignments).fork
          _      <- check(Set(0))
          fiber1 <- run(consumer2, 1, topic, allAssignments).fork
          _      <- check(Set(0, 1))
          fiber2 <- run(consumer3, 2, topic, allAssignments).fork
          _      <- check(Set(0, 1, 2))
          _      <- fiber2.interrupt
          _      <- allAssignments.update(_ - 2)
          _      <- check(Set(0, 1))
          _      <- fiber1.interrupt
          _      <- allAssignments.update(_ - 1)
          _      <- check(Set(0))
          _      <- fiber0.interrupt
        } yield assertCompletes
      },
      test("restartStreamsOnRebalancing mode closes all partition streams") {
        // Test plan:
        // - Throughout the test, continuously produce to all partitions of a topic.
        // - Start consumer 1:
        //   - track which partitions are assigned after each rebalance,
        //   - track which streams stopped.
        // - Start consumer 2 but finish after just a few records. This results in 2 rebalances for consumer 1.
        // - Verify that in the first rebalance, consumer 1 ends the streams for _all_ partitions,
        //   and then starts them again.
        //
        // NOTE: we need to use the cooperative sticky assignor. The default assignor `ConsumerPartitionAssignor`,
        // revokes all partitions and re-assigns them on every rebalance. This means that all streams are restarted
        // on every rebalance, exactly what `restartStreamOnRebalancing` would have caused. In other words, with the
        // default assignor the externally visible behavior is the same, regardless of whether
        // `restartStreamOnRebalancing` is `true` or `false`.

        val nrPartitions = 5
        val partitionIds = Chunk.fromIterable(0 until nrPartitions)

        def awaitRebalance[A](partitionAssignments: Ref[Chunk[A]], nr: Int): ZIO[Any, Nothing, Unit] =
          partitionAssignments.get
            .repeat(
              Schedule.recurUntil((_: Chunk[A]).size >= nr) && Schedule.fixed(100.millis)
            )
            .unit

        for {
          // Produce messages on several partitions
          _       <- ZIO.logDebug("Starting test")
          topic   <- randomTopic
          group   <- randomGroup
          client1 <- randomClient
          client2 <- randomClient

          _ <- KafkaTestUtils.createCustomTopic(topic, nrPartitions)

          // Continuously produce messages throughout the test
          producer <- KafkaTestUtils.makeProducer
          _ <- ZStream
                 .fromSchedule(Schedule.fixed(100.millis))
                 .mapZIO { i =>
                   ZIO.foreach(partitionIds) { p =>
                     KafkaTestUtils.produceMany(producer, topic, p, Seq((s"key.$p.$i", s"msg.$p.$i")))
                   }
                 }
                 .runDrain
                 .forkScoped

          // Consumer 1
          streamsStarted <- Ref.make[Chunk[Set[Int]]](Chunk.empty)
          streamsStopped <- Ref.make[Chunk[Int]](Chunk.empty)
          consumer1Settings <-
            KafkaTestUtils
              .consumerSettings(
                client1,
                Some(group),
                restartStreamOnRebalancing = true,
                properties = Map(
                  ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG -> classOf[CooperativeStickyAssignor].getName
                )
              )
          consumer1 <- Consumer.make(consumer1Settings)
          fib1 <- ZIO
                    .logAnnotate("consumer", "1") {
                      consumer1
                        .partitionedAssignmentStream(Subscription.topics(topic), Serde.string, Serde.string)
                        .rechunk(1)
                        .mapZIO { assignments =>
                          ZIO.logDebug(s"Got partition assignment ${assignments.map(_._1).mkString(",")}") *>
                            streamsStarted.update(_ :+ assignments.map(_._1.partition()).toSet) *>
                            ZStream
                              .fromIterable(assignments)
                              .flatMapPar(Int.MaxValue) { case (tp, partitionStream) =>
                                ZStream.finalizer {
                                  ZIO.logDebug(s"Stream for ${tp.toString} is done") *>
                                    streamsStopped.update(_ :+ tp.partition())
                                } *>
                                  partitionStream.mapChunksZIO { records =>
                                    OffsetBatch(records.map(_.offset)).commit.as(records)
                                  }
                              }
                              .runDrain
                        }
                        .runDrain
                    }
                    .fork

          // Wait until consumer 1 was assigned some partitions
          _ <- awaitRebalance(streamsStarted, 1)

          // Consumer 2
          // Stop after receiving 20 messages, causing two rebalancing events for consumer 1.
          consumer2 <- KafkaTestUtils.makeConsumer(client2, Some(group))
          _ <- ZIO
                 .logAnnotate("consumer", "2") {
                   consumer2
                     .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                     .take(20)
                     .runDrain
                 }
                 .forkScoped

          // Wait until consumer 1's partitions were revoked, and assigned again
          _ <- awaitRebalance(streamsStarted, 3)
          _ <- fib1.interrupt

          // The started streams after each rebalance
          streamsStarted <- streamsStarted.get
          _              <- ZIO.logDebug(s"partitions for started streams: $streamsStarted")

          streamsStopped <- streamsStopped.get
          _              <- ZIO.logDebug(s"partitions for stopped streams: $streamsStopped")
        } yield assertTrue(
          // During the first rebalance, all partitions are stopped:
          streamsStopped.take(nrPartitions).toSet == partitionIds.toSet,
          // Some streams that were assigned at the beginning, are started after the first rebalance:
          (streamsStarted(0) intersect streamsStarted(1)).nonEmpty
        )
      },
      test("handles RebalanceInProgressExceptions transparently") {
        val nrPartitions = 5
        val nrMessages   = 10000

        def makeConsumer(clientId: String, groupId: String) =
          KafkaTestUtils
            .consumerSettings(
              clientId = clientId,
              groupId = Some(groupId),
              clientInstanceId = None,
              properties = Map(
                ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG -> classOf[CooperativeStickyAssignor].getName
              )
            )
            .map(_.withPollTimeout(500.millis))
            .flatMap(settings => Consumer.make(settings))

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup

          _        <- KafkaTestUtils.createCustomTopic(topic, nrPartitions)
          producer <- KafkaTestUtils.makeProducer
          _ <- ZIO
                 .foreachDiscard(1 to nrMessages) { i =>
                   KafkaTestUtils
                     .produceMany(producer, topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                 }
                 .forkScoped

          // Consume messages
          messagesReceivedConsumer1 <- Ref.make[Int](0)
          messagesReceivedConsumer2 <- Ref.make[Int](0)
          drainCount                <- Ref.make(0)
          subscription = Subscription.topics(topic)
          stopConsumer1 <- Promise.make[Nothing, Unit]
          consumer1     <- makeConsumer("consumer1", group)
          fib <-
            ZIO
              .logAnnotate("consumer", "1") {
                consumer1
                  .partitionedAssignmentStream(subscription, Serde.string, Serde.string)
                  .rechunk(1)
                  .mapZIOPar(16) { partitions =>
                    ZIO.logDebug(s"Consumer 1 got new partition assignment: ${partitions.map(_._1.toString)}") *>
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
                  .tapError(e => ZIO.logErrorCause(e.getMessage, Cause.fail(e)))
              }
              .forkScoped

          _ <- messagesReceivedConsumer1.get
                 .repeat(Schedule.recurUntil((n: Int) => n >= 20) && Schedule.fixed(100.millis))
          _ <- ZIO.logDebug("Starting consumer 2")

          consumer2 <- makeConsumer("consumer2", group)
          fib2 <-
            ZIO
              .logAnnotate("consumer", "2") {
                consumer2
                  .plainStream(subscription, Serde.string, Serde.string)
                  .mapZIO(record => messagesReceivedConsumer2.update(_ + 1).as(record))
                  .map(_.offset)
                  .aggregateAsync(Consumer.offsetBatches)
                  .mapZIO(offsetBatch => offsetBatch.commit)
                  .runDrain
                  .tapError(e => ZIO.logErrorCause("Error in consumer 2", Cause.fail(e)))
              }
              .forkScoped

          _ <- messagesReceivedConsumer2.get
                 .repeat(Schedule.recurUntil((n: Int) => n >= 20) && Schedule.fixed(100.millis))
          _ <- stopConsumer1.succeed(())
          _ <- fib.join
          _ <- fib2.interrupt
        } yield assertCompletes
      },
      suite("does not process messages twice for transactional producer, even when rebalancing")({

        /**
         * Outline of this test:
         *   - A producer generates some messages on topic A,
         *   - a transactional consumer/producer pair (copier1) reads these and copies them to topic B transactionally,
         *   - after a few messages we start a second transactional consumer/producer pair (copier2) that does the same
         *     (in the same consumer group), this triggers a rebalance,
         *   - produce some more messages to topic A,
         *   - a consumer that empties topic B,
         *   - when enough messages have been received, the copiers are interrupted.
         *
         * We will assert that the produced messages to topic A correspond exactly with the read messages from topic B.
         */
        def testForPartitionAssignmentStrategy[T <: ConsumerPartitionAssignor: ClassTag] =
          test(implicitly[ClassTag[T]].runtimeClass.getName) {
            val partitionCount                                    = 6
            val messageCount                                      = 5000
            val allMessages                                       = (1 to messageCount).map(i => s"$i" -> f"msg$i%06d")
            val (messagesBeforeRebalance, messagesAfterRebalance) = allMessages.splitAt(messageCount / 2)

            def makeCopyingTransactionalConsumer(
              name: String,
              consumerGroupId: String,
              clientId: String,
              fromTopic: String,
              toTopic: String,
              consumerCreated: Promise[Throwable, Unit]
            ): ZIO[Kafka, Throwable, Unit] =
              ZIO.logAnnotate("consumer", name) {
                ZIO.scoped {
                  (for {
                    consumer                <- ZIO.service[Consumer]
                    consumedMessagesCounter <- Ref.make(0)
                    _ <- consumedMessagesCounter.get
                           .flatMap(consumed => ZIO.logDebug(s"Consumed so far: $consumed"))
                           .repeat(Schedule.fixed(1.second))
                           .fork

                    transactionalId   <- randomThing("transactional")
                    tProducerSettings <- KafkaTestUtils.transactionalProducerSettings(transactionalId)
                    tProducer <-
                      TransactionalProducer.make(tProducerSettings, consumer)

                    tConsumer <-
                      consumer
                        .partitionedStream(Subscription.topics(fromTopic), Serde.string, Serde.string)
                        .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
                          ZStream.fromZIO(consumerCreated.succeed(())) *>
                            partitionStream.mapChunksZIO { records =>
                              ZIO.scoped {
                                for {
                                  t <- tProducer.createTransaction
                                  _ <- t.produceChunkBatch(
                                         records.map(r => new ProducerRecord(toTopic, r.key, r.value)),
                                         Serde.string,
                                         Serde.string,
                                         OffsetBatch(records.map(_.offset))
                                       )
                                  _ <- consumedMessagesCounter.update(_ + records.size)
                                } yield Chunk.empty
                              }
                            }
                        }
                        .runDrain
                        .tapError(e => ZIO.logError(s"Error: $e") *> consumerCreated.fail(e)) <* ZIO.logDebug("Done")
                  } yield tConsumer)
                    .provideSome[Kafka & Scope](
                      KafkaTestUtils.transactionalConsumer(
                        clientId,
                        consumerGroupId,
                        rebalanceSafeCommits = true,
                        properties = Map(
                          ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG ->
                            implicitly[ClassTag[T]].runtimeClass.getName,
                          ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "200"
                        )
                      )
                    )
                }
              }

            for {
              topicA <- randomTopic
              topicB <- randomTopic
              _      <- KafkaTestUtils.createCustomTopic(topicA, partitionCount)
              _      <- KafkaTestUtils.createCustomTopic(topicB, partitionCount)

              producer <- KafkaTestUtils.makeProducer
              _        <- KafkaTestUtils.produceMany(producer, topicA, messagesBeforeRebalance)

              copyingGroup <- randomGroup

              _ <- ZIO.logDebug("Starting copier 1")
              copier1ClientId = copyingGroup + "-1"
              copier1Created <- Promise.make[Throwable, Unit]
              copier1 <- makeCopyingTransactionalConsumer(
                           "1",
                           copyingGroup,
                           copier1ClientId,
                           topicA,
                           topicB,
                           copier1Created
                         ).fork
              _ <- copier1Created.await

              _ <- ZIO.logDebug("Starting copier 2")
              copier2ClientId = copyingGroup + "-2"
              copier2Created <- Promise.make[Throwable, Unit]
              copier2 <- makeCopyingTransactionalConsumer(
                           "2",
                           copyingGroup,
                           copier2ClientId,
                           topicA,
                           topicB,
                           copier2Created
                         ).fork
              _ <- ZIO.logDebug("Waiting for copier 2 to start")
              _ <- copier2Created.await

              _ <- ZIO.logDebug("Producing some more messages")
              _ <- KafkaTestUtils.produceMany(producer, topicA, messagesAfterRebalance)

              _                 <- ZIO.logDebug("Collecting messages on topic B")
              groupB            <- randomGroup
              validatorClientId <- randomClient
              consumer <- KafkaTestUtils.makeTransactionalConsumer(
                            validatorClientId,
                            groupB,
                            properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "200")
                          )
              messagesOnTopicB <- ZIO.logAnnotate("consumer", "validator") {
                                    consumer
                                      .plainStream(Subscription.topics(topicB), Serde.string, Serde.string)
                                      .mapChunks(_.map(_.value))
                                      .take(messageCount.toLong)
                                      .timeout(10.seconds)
                                      .runCollect
                                      .tapError(e => ZIO.logError(s"Error: $e")) <* ZIO.logDebug("Done")
                                  }
              _ <- copier1.interrupt
              _ <- copier2.interrupt
              messagesOnTopicBCount         = messagesOnTopicB.size
              messagesOnTopicBDistinctCount = messagesOnTopicB.distinct.size
            } yield assertTrue(messageCount == messagesOnTopicBCount && messageCount == messagesOnTopicBDistinctCount)
          }

        // Test for both default partition assignment strategies
        Seq(
          testForPartitionAssignmentStrategy[RangeAssignor]
//          testForPartitionAssignmentStrategy[CooperativeStickyAssignor] // TODO not yet supported
        )

      }: _*) @@ TestAspect.nonFlaky(2),
      test("running streams don't stall after a poll timeout") {
        for {
          topic      <- randomTopic
          clientId   <- randomClient
          _          <- KafkaTestUtils.createCustomTopic(topic)
          settings   <- KafkaTestUtils.consumerSettings(clientId).map(_.withPollTimeout(50.millis))
          producer   <- KafkaTestUtils.makeProducer
          _          <- KafkaTestUtils.produceOne(producer, topic, "key1", "message1")
          recordsOut <- Queue.unbounded[Unit]
          consumer   <- Consumer.make(settings)
          _ <- consumer
                 .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                 .tap(r => ZIO.logDebug(r.toString))
                 .foreach(_ => recordsOut.offer(()))
                 .forkScoped
          _ <- recordsOut.take       // first record consumed
          _ <- ZIO.sleep(200.millis) // wait for `pollTimeout`
          _ <- KafkaTestUtils.produceOne(producer, topic, "key2", "message2")
          _ <- recordsOut.take
        } yield assertCompletes
      },
      suite("issue #856")(
        test(
          "Booting a Consumer to do something else than consuming should not fail with a timeout exception"
        ) {
          def test(diagnostics: Diagnostics) =
            for {
              clientId <- randomClient
              settings <- KafkaTestUtils.consumerSettings(
                            clientId = clientId,
                            maxPollInterval = 500.millis
                          )
              _ <- Consumer.make(settings, diagnostics = diagnostics)
              _ <- ZIO.sleep(1.second)
            } yield assertCompletes

          for {
            diagnostics <- Diagnostics.SlidingQueue.make(1000)
            testResult <- ZIO.scoped {
                            test(diagnostics)
                          }
            finalizationEvents <- diagnostics.queue.takeAll.map(_.filter(_.isInstanceOf[Finalization]))
          } yield testResult && assert(finalizationEvents)(hasSameElements(Chunk(ConsumerFinalized)))
        },
        suite(
          "Ordering of finalizers matters. If subscriptions are finalized after the runloop, it creates a deadlock"
        )(
          test("When not consuming, the Runloop is not started so only the Consumer is finalized") {
            def test(diagnostics: Diagnostics): ZIO[Scope & Kafka, Throwable, TestResult] =
              for {
                clientId <- randomClient
                settings <- KafkaTestUtils.consumerSettings(clientId = clientId)
                _        <- Consumer.make(settings, diagnostics = diagnostics)
              } yield assertCompletes

            for {
              diagnostics <- Diagnostics.SlidingQueue.make(1000)
              testResult <- ZIO.scoped {
                              test(diagnostics)
                            }
              finalizationEvents <- diagnostics.queue.takeAll.map(_.filter(_.isInstanceOf[Finalization]))
            } yield testResult && assert(finalizationEvents)(hasSameElements(Chunk(ConsumerFinalized)))
          },
          test("When consuming, the Runloop is started. The finalization orders matters to avoid a deadlock") {
            // This test ensures that we're not inadvertently introducing a deadlock by changing the order of finalizers.

            def test(diagnostics: Diagnostics): ZIO[Scope & Kafka, Throwable, TestResult] =
              for {
                clientId <- randomClient
                topic    <- randomTopic

                producer <- KafkaTestUtils.makeProducer
                _        <- KafkaTestUtils.produceOne(producer, topic, "key1", "message1")

                settings <- KafkaTestUtils.consumerSettings(clientId = clientId)
                consumer <- Consumer.make(settings, diagnostics = diagnostics)
                // Starting a consumption session to start the Runloop.
                consumed0 <- consumer
                               .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                               .take(1)
                               .runCount
                consumed1 <- consumer
                               .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                               .take(1)
                               .runCount
              } yield assert(consumed0)(equalTo(1L)) && assert(consumed1)(equalTo(1L))

            for {
              diagnostics <- Diagnostics.SlidingQueue.make(1000)
              testResult <- ZIO.scoped {
                              test(diagnostics)
                            }
              finalizationEvents <- diagnostics.queue.takeAll.map(_.filter(_.isInstanceOf[Finalization]))
            } yield testResult && assert(finalizationEvents)(
              // The order is very important.
              // The subscription must be finalized before the runloop, otherwise it creates a deadlock.
              equalTo(
                Chunk(
                  SubscriptionFinalized,
                  SubscriptionFinalized,
                  RunloopFinalized,
                  ConsumerFinalized
                )
              )
            )
          },
          test(
            "it's possible to start a new consumption session from a Consumer that had a consumption session stopped previously"
          ) {
            val numberOfMessages: Int           = 100000
            val messagesToConsumeBeforeStop     = 1000 // Adjust this threshold as needed
            val kvs: Iterable[(String, String)] = Iterable.tabulate(numberOfMessages)(i => (s"key-$i", s"msg-$i"))

            def test(diagnostics: Diagnostics): ZIO[Scope & Kafka, Throwable, TestResult] =
              for {
                clientId <- randomClient
                topic    <- randomTopic

                producer <- KafkaTestUtils.makeProducer
                _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

                settings <- KafkaTestUtils.consumerSettings(clientId = clientId)
                consumer <- Consumer.make(settings, diagnostics = diagnostics)
                // Create a Ref to track messages consumed and a Promise to signal when to stop consumption
                messagesConsumedRef <- Ref.make(0)
                stopPromise         <- Promise.make[Nothing, Unit]
                // Starting a consumption session to start the Runloop.
                fiber <- consumer
                           .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                           .mapZIO { _ =>
                             messagesConsumedRef.updateAndGet(_ + 1).flatMap { count =>
                               if (count >= messagesToConsumeBeforeStop) stopPromise.succeed(()).as(1L)
                               else ZIO.succeed(1L)
                             }
                           }
                           .take(numberOfMessages.toLong)
                           .runSum
                           .forkScoped

                // Wait for the consumption to reach the desired threshold
                _         <- stopPromise.await
                _         <- consumer.stopConsumption
                consumed0 <- fiber.join
                _         <- ZIO.logDebug(s"consumed0: $consumed0")

                consumed1 <- consumer
                               .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                               .take(numberOfMessages.toLong)
                               .runCount
              } yield assert(consumed0)(isGreaterThan(0L) && isLessThan(numberOfMessages.toLong)) &&
                assert(consumed1)(equalTo(numberOfMessages.toLong))

            for {
              diagnostics <- Diagnostics.SlidingQueue.make(1000)
              testResult <- ZIO.scoped {
                              test(diagnostics)
                            }
              finalizationEvents <- diagnostics.queue.takeAll.map(_.filter(_.isInstanceOf[Finalization]))
            } yield testResult && assert(finalizationEvents)(
              // The order is very important.
              // The subscription must be finalized before the runloop, otherwise it creates a deadlock.
              equalTo(
                Chunk(
                  SubscriptionFinalized,
                  RunloopFinalized,
                  ConsumerFinalized
                )
              )
            )
          }
        )
      ),
      test("commit timeout") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group), commitTimeout = 2.seconds)
          result <- consumer
                      .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                      .take(11)
                      .map(_.offset)
                      .aggregateAsync(Consumer.offsetBatches)
                      .mapZIO(_.commit) // Hangs without timeout
                      .runDrain
                      .exit
        } yield assert(result)(equalTo(Exit.fail(CommitTimeout)))
      } @@ TestAspect.flaky(10) @@ TestAspect.timeout(20.seconds)
    )
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ timeout(2.minutes) @@ TestAspect.timed

}
