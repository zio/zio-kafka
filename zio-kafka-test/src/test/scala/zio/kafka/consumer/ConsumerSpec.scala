package zio.kafka.consumer

import io.github.embeddedkafka.EmbeddedKafka
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
import zio.kafka.producer.{ Producer, TransactionalProducer }
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils._
import zio.kafka.testkit.{ Kafka, KafkaRandom }
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import scala.reflect.ClassTag

//noinspection SimplifyAssertInspection
object ConsumerSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "consumespec"

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
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
                       .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
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
                     .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                     .take(100)
                     .mapChunks(c => Chunk(c.size))
                     .runCollect
                     .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(sizes)(forall(isGreaterThan(1)))
      },
      test("Manual subscription without groupId works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic  <- randomTopic
          client <- randomClient

          _ <- produceMany(topic, kvs)

          records <-
            Consumer
              .plainStream(
                Subscription.Manual(Set(new org.apache.kafka.common.TopicPartition(topic, 0))),
                Serde.string,
                Serde.string
              )
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
                       .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
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
                       .plainStream(Subscription.Pattern("pattern[0-9]+".r), Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("receive only messages from the subscribed topic-partition when creating a manual subscription") {
        val nrPartitions = 5

        for {
          client <- randomClient
          group  <- randomGroup
          topic  <- randomTopic

          _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreachDiscard(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }
          record <- Consumer
                      .plainStream(Subscription.manual(topic, partition = 2), Serde.string, Serde.string)
                      .take(1)
                      .runHead
                      .provideSomeLayer[Kafka](consumer(client, Some(group)))
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2" -> "msg2")))
      },
      test("receive from the right offset when creating a manual subscription with manual seeking") {
        val nrPartitions = 5

        val manualOffsetSeek = 3

        for {
          client <- randomClient
          group  <- randomGroup
          topic  <- randomTopic

          _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreachDiscard(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = (0 to 9).map(j => s"key$i-$j" -> s"msg$i-$j"))
               }
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO.attempt(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          record <- Consumer
                      .plainStream(Subscription.manual(topic, partition = 2), Serde.string, Serde.string)
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
                                         .provideSomeLayer[Kafka](
                                           consumer(first, Some(group))
                                         )
                          } yield results
          secondResults <- for {
                             results <-
                               Consumer
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
          _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }

          // Consume messages
          messagesReceived <- ZIO.foreach((0 until nrPartitions).toList)(i => Ref.make[Int](0).map(i -> _)).map(_.toMap)
          subscription = Subscription.topics(topic)
          fib <- Consumer
                   .partitionedStream(subscription, Serde.string, Serde.string)
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
          consumeResult <- consumeWithStrings(client, Some(group), subscription) { _ =>
                             ZIO.die(new IllegalArgumentException("consumeWith failure"))
                           }.exit
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

          keepProducing <- Ref.make(true)
          _             <- produceOne(topic, "key", "value").repeatWhileZIO(_ => keepProducing.get).fork
          _ <- Consumer
                 .partitionedStream(Subscription.topics(topic), Serde.string, Serde.string)
                 .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
                   partitionStream.zipWithIndex.tap { case (record, idx) =>
                     Consumer.stopConsumption *>
                       (Consumer.stopConsumption <* ZIO.logDebug("Stopped consumption")).when(idx == 3) *>
                       record.offset.commit <* ZIO.logDebug(s"Committed $idx")
                   }.tap { case (_, idx) => ZIO.logDebug(s"Consumed $idx") }
                 }
                 .runDrain
                 .tap(_ => ZIO.logDebug("Stream completed"))
                 .provideSomeLayer[Kafka](
                   consumer(client, Some(group))
                 )
          _ <- keepProducing.set(false)
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
                      .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                      .mapConcatZIO { record =>
                        for {
                          nr <- messagesReceived.updateAndGet(_ + 1)
                          _  <- Consumer.stopConsumption.when(nr == 10)
                        } yield if (nr < 10) Seq(record.offset) else Seq.empty
                      }
                      .transduce(Consumer.offsetBatches)
                      .mapZIO(_.commit)
                      .runDrain *>
                      Consumer.committed(Set(new TopicPartition(topic, 0))).map(_.values.head))
                      .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(offset.map(_.offset))(isSome(equalTo(9L)))
      },
      test("process outstanding commits after a graceful shutdown with aggregateAsync using `maxRebalanceDuration`") {
        val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        val topic = "test-outstanding-commits"
        for {
          group            <- randomGroup
          client           <- randomClient
          _                <- produceMany(topic, kvs)
          messagesReceived <- Ref.make[Int](0)
          offset <- (
            Consumer
              .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
              .mapConcatZIO { record =>
                for {
                  nr <- messagesReceived.updateAndGet(_ + 1)
                  _  <- Consumer.stopConsumption.when(nr == 10)
                } yield if (nr < 10) Seq(record.offset) else Seq.empty
              }
              .aggregateAsync(Consumer.offsetBatches)
              .mapZIO(_.commit)
              .runDrain *>
              Consumer.committed(Set(new TopicPartition(topic, 0))).map(_.values.head)
            )
            .provideSomeLayer[Kafka](
              consumer(
                client, Some(group), commitTimeout = 4.seconds,
                rebalanceSafeCommits = true, maxRebalanceDuration = 6.seconds
              )
            )
        } yield {
          assertTrue(offset.map(_.offset).contains(9L))
        }
      } @@ TestAspect.nonFlaky(5),
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
          _        <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic1, partitions = 1))
          _        <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic2))
          settings <- consumerSettings(
                        clientId = clientId,
                        groupId = Some(group),
                        maxPollInterval = 2.seconds,
                        `max.poll.records` = 2
                      )
                        .map(_.withoutPartitionPreFetching.withPollTimeout(100.millis))
          consumer <- Consumer.make(settings)
          _        <- scheduledProduce(topic1, Schedule.fixed(500.millis).jittered).runDrain.forkScoped
          _        <- scheduledProduce(topic2, Schedule.fixed(500.millis).jittered).runDrain.forkScoped
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
      test("a slow producer doesnot interrupt the stream") {
        ZIO.scoped {
          for {
            topic    <- randomTopic
            group    <- randomGroup
            clientId <- randomClient
            _        <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic))
            settings <- consumerSettings(
                          clientId = clientId,
                          groupId = Some(group),
                          maxPollInterval = 300.millis
                        )
            consumer <- Consumer.make(settings.withPollTimeout(50.millis))
            // A slow producer
            _ <- scheduledProduce(topic, Schedule.fixed(1.second)).runDrain.forkScoped
            consumed <- consumer
                          .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                          .take(2)
                          .runCollect
          } yield assertTrue(consumed.size == 2)
        }
      },
      test("offset batching collects the latest offset for all partitions") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomClient
          _      <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }

          // Consume messages
          subscription = Subscription.topics(topic)
          offsets <- (Consumer
                       .partitionedStream(subscription, Serde.string, Serde.string)
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
      test("commits an offset with metadata") {
        for {
          topic    <- randomTopic
          group    <- randomGroup
          metadata <- randomThing("metadata")
          client   <- randomClient
          _        <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = 1))
          _        <- produceOne(topic, "key", "msg")

          // Consume messages
          subscription = Subscription.topics(topic)
          offsets <- (Consumer
                       .partitionedStream(subscription, Serde.string, Serde.string)
                       .flatMap(_._2.map(_.offset.withMetadata(metadata)))
                       .take(1)
                       .transduce(Consumer.offsetBatches)
                       .take(1)
                       .mapZIO(_.commit)
                       .runDrain *>
                       Consumer.committed(Set(new TopicPartition(topic, 0))))
                       .provideSomeLayer[Kafka](consumer(client, Some(group)))
        } yield assert(offsets.values.headOption.flatten.map(_.metadata))(isSome(equalTo(metadata)))
      },
      test("access to the java consumer must be fair") {
        val kvs = (1 to 10).toList.map(i => (s"key$i", s"msg$i"))

        val expectedResult = (0 to 9).toList.map(i => i.toLong -> i.toLong)

        for {
          topic  <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _                  <- produceMany(topic, kvs)
          committedOffsetRef <- Ref.make(Seq.empty[(Long, Long)])

          topicPartition = new TopicPartition(topic, 0)

          _ <- Consumer
                 .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                 .take(10)
                 .map(_.offset)
                 .mapZIO(offsetBatch =>
                   Consumer
                     .committed(Set(topicPartition))
                     .flatMap(offset =>
                       committedOffsetRef.update(map =>
                         map :+ (offsetBatch.offset -> offset(topicPartition).map(_.offset()).getOrElse(0L))
                       ) *> offsetBatch.commit
                     )
                 )
                 .runDrain
                 .provideSomeLayer[Kafka](consumer(client, Some(group), commitTimeout = 2.seconds))
          offsets <- committedOffsetRef.get
        } yield assert(offsets)(equalTo(expectedResult))
      } @@ TestAspect.timeout(20.seconds),
      test("handle rebalancing by completing topic-partition streams") {
        val nrMessages   = 50
        val nrPartitions = 6 // Must be even and strictly positive

        for {
          // Produce messages on several partitions
          topic   <- randomTopic
          group   <- randomGroup
          client1 <- randomClient
          client2 <- randomClient

          _ <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }

          // Consume messages
          subscription = Subscription.topics(topic)
          consumer1 <- Consumer
                         .partitionedStream(subscription, Serde.string, Serde.string)
                         .flatMapPar(nrPartitions) { case (tp, partition) =>
                           ZStream
                             .fromZIO(partition.runDrain)
                             .as(tp)
                         }
                         .take(nrPartitions.toLong / 2)
                         .runDrain
                         .provideSomeLayer[Kafka](consumer(client1, Some(group)))
                         .fork
          _ <- Live.live(ZIO.sleep(5.seconds))
          consumer2 <- Consumer
                         .partitionedStream(subscription, Serde.string, Serde.string)
                         .take(nrPartitions.toLong / 2)
                         .runDrain
                         .provideSomeLayer[Kafka](consumer(client2, Some(group)))
                         .fork
          _ <- consumer1.join
          _ <- consumer2.join
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
                _ <- ZIO.foreachDiscard(1 to nrMessages) { i =>
                       produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
                     }

                // Consume messages
                subscription = Subscription.topics(topic)
                consumer1 <- Consumer
                               .partitionedStream(subscription, Serde.string, Serde.string)
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
                               .partitionedStream(subscription, Serde.string, Serde.string)
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
                 .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                 .take(5)
                 .transduce(ZSink.collectAllN[CommittableRecord[String, String]](5))
                 .mapConcatZIO { committableRecords =>
                   val records     = committableRecords.map(_.record)
                   val offsetBatch = OffsetBatch(committableRecords.map(_.offset))

                   offsetBatch.commit.as(records)
                 }
                 .runCollect
                 .provideSomeLayer[Kafka](consumer(client1, Some(group)))
          // Start a new consumer with manual offset before the committed offset
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO.attempt(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          secondResults <- Consumer
                             .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                             .take(nrRecords.toLong - manualOffsetSeek)
                             .map(_.record)
                             .runCollect
                             .provideSomeLayer[Kafka](
                               consumer(client2, Some(group), offsetRetrieval = offsetRetrieval)
                             )
          // Check that we only got the records starting from the manually seek'd offset
        } yield assert(secondResults.map(rec => rec.key() -> rec.value()).toList)(
          equalTo(data.drop(manualOffsetSeek))
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
          consumeWithStrings(client, Some(group), subscription)({ record =>
            for {
              messagesSoFar <- messagesReceived.updateAndGet(_ :+ (record.key() -> record.value()))
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
          newMessage <- Consumer
                          .plainStream(subscription, Serde.string, Serde.string)
                          .take(1)
                          .map(r => (r.record.key(), r.record.value()))
                          .run(ZSink.collectAll[(String, String)])
                          .map(_.head)
                          .orDie
                          .provideSomeLayer[Kafka](consumer(client, Some(group)))
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
                settings <- consumerSettings(
                              clientId = clientId,
                              groupId = Some(groupId),
                              `max.poll.records` = 1,
                              rebalanceSafeCommits = rebalanceSafeCommits
                            )
                consumer <- Consumer.make(settings)
              } yield consumer

            for {
              topic <- randomTopic
              subscription = Subscription.topics(topic)
              clientId1 <- randomClient
              clientId2 <- randomClient
              groupId   <- randomGroup
              _         <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = partitionCount))
              // Produce one message to all partitions, every 500 ms
              pFib <- ZStream
                        .fromSchedule(Schedule.fixed(500.millis))
                        .mapZIO { i =>
                          ZIO.foreachDiscard(0 until partitionCount) { p =>
                            produceMany(topic, p, Seq((s"key-$p-$i", s"msg-$p-$i")))
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

        final case class ValidAssignmentsNotSeen(instances: Set[Int], st: String)
            extends RuntimeException(s"Valid assignment not seen for instances $instances: $st")

        def run(instance: Int, topic: String, allAssignments: Ref[Map[Int, List[Int]]]) =
          ZIO.logAnnotate("consumer", instance.toString) {
            val subscription = Subscription.topics(topic)
            Consumer
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

        def createConsumer(client: String, group: String): ZLayer[Kafka, Throwable, Consumer] =
          consumer(
            client,
            Some(group),
            offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
          )

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
                      .provideSomeLayer[Kafka](createConsumer(client1, group))
                      .fork
          _ <- check(Set(0))
          fiber1 <- run(1, topic, allAssignments)
                      .provideSomeLayer[Kafka](createConsumer(client2, group))
                      .fork
          _ <- check(Set(0, 1))
          fiber2 <- run(2, topic, allAssignments)
                      .provideSomeLayer[Kafka](createConsumer(client3, group))
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

          _ <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))

          // Continuously produce messages throughout the test
          _ <- ZStream
                 .fromSchedule(Schedule.fixed(100.millis))
                 .mapZIO { i =>
                   ZIO.foreach(partitionIds) { p =>
                     produceMany(topic, p, Seq((s"key.$p.$i", s"msg.$p.$i")))
                   }
                 }
                 .runDrain
                 .forkScoped

          // Consumer 1
          streamsStarted <- Ref.make[Chunk[Set[Int]]](Chunk.empty)
          streamsStopped <- Ref.make[Chunk[Int]](Chunk.empty)
          consumer1Settings <-
            consumerSettings(
              client1,
              Some(group),
              restartStreamOnRebalancing = true
            ).map {
              _.withProperties(
                ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG -> classOf[CooperativeStickyAssignor].getName
              )
            }
          fib1 <- ZIO
                    .logAnnotate("consumer", "1") {
                      Consumer
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
                        .provideSomeLayer[Kafka](
                          ZLayer.succeed(consumer1Settings) >>> minimalConsumer()
                        )
                    }
                    .fork

          // Wait until consumer 1 was assigned some partitions
          _ <- awaitRebalance(streamsStarted, 1)

          // Consumer 2
          // Stop after receiving 20 messages, causing two rebalancing events for consumer 1.
          consumer2Settings <- consumerSettings(client2, Some(group))
          _ <- ZIO
                 .logAnnotate("consumer", "2") {
                   Consumer
                     .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                     .take(20)
                     .runDrain
                     .provideSomeLayer[Kafka](
                       ZLayer.succeed(consumer2Settings) >>> minimalConsumer()
                     )
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

        def customConsumer(clientId: String, groupId: Option[String]) =
          ZLayer(
            consumerSettings(
              clientId = clientId,
              groupId = groupId,
              clientInstanceId = None
            ).map(
              _.withProperties(
                ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG -> classOf[CooperativeStickyAssignor].getName
              )
                .withPollTimeout(500.millis)
            )
          ) ++ ZLayer.succeed(Diagnostics.NoOp) >>> Consumer.live

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
            ZIO
              .logAnnotate("consumer", "1") {
                Consumer
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
                  .provideSomeLayer[Kafka](
                    customConsumer("consumer1", Some(group)) ++ Scope.default
                  )
                  .tapError(e => ZIO.logErrorCause(e.getMessage, Cause.fail(e)))
              }
              .forkScoped

          _ <- messagesReceivedConsumer1.get
                 .repeat(Schedule.recurUntil((n: Int) => n >= 20) && Schedule.fixed(100.millis))
          _ <- ZIO.logDebug("Starting consumer 2")

          fib2 <-
            ZIO
              .logAnnotate("consumer", "2") {
                Consumer
                  .plainStream(subscription, Serde.string, Serde.string)
                  .mapZIO(record => messagesReceivedConsumer2.update(_ + 1).as(record))
                  .map(_.offset)
                  .aggregateAsync(Consumer.offsetBatches)
                  .mapZIO(offsetBatch => offsetBatch.commit)
                  .runDrain
                  .provideSomeLayer[Kafka](
                    customConsumer("consumer2", Some(group))
                  )
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
         *     (in the same consumer group) this triggers a rebalance,
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

            def transactionalRebalanceListener(streamCompleteOnRebalanceRef: Ref[Option[Promise[Nothing, Unit]]]) =
              RebalanceListener(
                onAssigned = (_, _) => ZIO.unit,
                onRevoked = (_, _) =>
                  streamCompleteOnRebalanceRef.get.flatMap {
                    case Some(p) =>
                      ZIO.logDebug("onRevoked, awaiting stream completion") *>
                        p.await.timeoutFail(new InterruptedException("Timed out waiting stream to complete"))(1.minute)
                    case None => ZIO.unit
                  },
                onLost = (_, _) => ZIO.logDebug("Lost some partitions")
              )

            def makeCopyingTransactionalConsumer(
              name: String,
              consumerGroupId: String,
              clientId: String,
              fromTopic: String,
              toTopic: String,
              tProducer: TransactionalProducer,
              consumerCreated: Promise[Nothing, Unit]
            ): ZIO[Kafka, Throwable, Unit] =
              ZIO.logAnnotate("consumer", name) {
                for {
                  consumedMessagesCounter <- Ref.make(0)
                  _ <- consumedMessagesCounter.get
                         .flatMap(consumed => ZIO.logDebug(s"Consumed so far: $consumed"))
                         .repeat(Schedule.fixed(1.second))
                         .fork
                  streamCompleteOnRebalanceRef <- Ref.make[Option[Promise[Nothing, Unit]]](None)
                  tConsumer <-
                    Consumer
                      .partitionedAssignmentStream(Subscription.topics(fromTopic), Serde.string, Serde.string)
                      .mapZIO { assignedPartitions =>
                        for {
                          p <- Promise.make[Nothing, Unit]
                          _ <- streamCompleteOnRebalanceRef.set(Some(p))
                          _ <- ZIO.logDebug(s"${assignedPartitions.size} partitions assigned")
                          _ <- consumerCreated.succeed(())
                          partitionStreams = assignedPartitions.map(_._2)
                          s <- ZStream
                                 .mergeAllUnbounded(64)(partitionStreams: _*)
                                 .mapChunksZIO { records =>
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
                                   }.uninterruptible
                                 }
                                 .runDrain
                                 .ensuring {
                                   for {
                                     _ <- streamCompleteOnRebalanceRef.set(None)
                                     _ <- p.succeed(())
                                     c <- consumedMessagesCounter.get
                                     _ <- ZIO.logDebug(s"Consumed $c messages")
                                   } yield ()
                                 }
                        } yield s
                      }
                      .runDrain
                      .provideSome[Kafka](
                        transactionalConsumer(
                          clientId,
                          consumerGroupId,
                          offsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
                          restartStreamOnRebalancing = true,
                          properties = Map(
                            ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG ->
                              implicitly[ClassTag[T]].runtimeClass.getName,
                            ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "200"
                          ),
                          rebalanceListener = transactionalRebalanceListener(streamCompleteOnRebalanceRef)
                        )
                      )
                      .tapError(e => ZIO.logError(s"Error: $e")) <* ZIO.logDebug("Done")
                } yield tConsumer
              }

            for {
              tProducerSettings <- transactionalProducerSettings
              tProducer         <- TransactionalProducer.make(tProducerSettings)

              topicA <- randomTopic
              topicB <- randomTopic
              _      <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topicA, partitions = partitionCount))
              _      <- ZIO.attempt(EmbeddedKafka.createCustomTopic(topicB, partitions = partitionCount))

              _ <- produceMany(topicA, messagesBeforeRebalance)

              copyingGroup <- randomGroup

              _ <- ZIO.logDebug("Starting copier 1")
              copier1ClientId = copyingGroup + "-1"
              copier1Created <- Promise.make[Nothing, Unit]
              copier1 <- makeCopyingTransactionalConsumer(
                           "1",
                           copyingGroup,
                           copier1ClientId,
                           topicA,
                           topicB,
                           tProducer,
                           copier1Created
                         ).fork
              _ <- copier1Created.await

              _ <- ZIO.logDebug("Starting copier 2")
              copier2ClientId = copyingGroup + "-2"
              copier2Created <- Promise.make[Nothing, Unit]
              copier2 <- makeCopyingTransactionalConsumer(
                           "2",
                           copyingGroup,
                           copier2ClientId,
                           topicA,
                           topicB,
                           tProducer,
                           copier2Created
                         ).fork
              _ <- ZIO.logDebug("Waiting for copier 2 to start")
              _ <- copier2Created.await

              _ <- ZIO.logDebug("Producing some more messages")
              _ <- produceMany(topicA, messagesAfterRebalance)

              _                 <- ZIO.logDebug("Collecting messages on topic B")
              groupB            <- randomGroup
              validatorClientId <- randomClient
              messagesOnTopicB <- ZIO.logAnnotate("consumer", "validator") {
                                    Consumer
                                      .plainStream(Subscription.topics(topicB), Serde.string, Serde.string)
                                      .map(_.value)
                                      .timeout(5.seconds)
                                      .runCollect
                                      .provideSome[Kafka](
                                        transactionalConsumer(
                                          validatorClientId,
                                          groupB,
                                          offsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
                                          properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "200")
                                        )
                                      )
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

      }: _*) @@ TestAspect.nonFlaky(3),
      test("running streams don't stall after a poll timeout") {
        for {
          topic      <- randomTopic
          clientId   <- randomClient
          _          <- ZIO.fromTry(EmbeddedKafka.createCustomTopic(topic))
          settings   <- consumerSettings(clientId)
          consumer   <- Consumer.make(settings.withPollTimeout(50.millis))
          recordsOut <- Queue.unbounded[Unit]
          _          <- produceOne(topic, "key1", "message1")
          _ <- consumer
                 .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                 .tap(r => ZIO.logDebug(r.toString))
                 .foreach(_ => recordsOut.offer(()))
                 .forkScoped
          _ <- recordsOut.take       // first record consumed
          _ <- ZIO.sleep(200.millis) // wait for `pollTimeout`
          _ <- produceOne(topic, "key2", "message2")
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
              settings <- consumerSettings(
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
                settings <- consumerSettings(clientId = clientId)
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

            def test(diagnostics: Diagnostics): ZIO[Producer & Scope & Kafka, Throwable, TestResult] =
              for {
                clientId <- randomClient
                topic    <- randomTopic
                settings <- consumerSettings(clientId = clientId)
                consumer <- Consumer.make(settings, diagnostics = diagnostics)
                _        <- produceOne(topic, "key1", "message1")
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
            // NOTE:
            // When this test fails with the message `100000 was not less than 100000`, it's because
            // your computer is so fast that the first consumer already consumed all 100000 messages.
            val numberOfMessages: Int           = 100000
            val kvs: Iterable[(String, String)] = Iterable.tabulate(numberOfMessages)(i => (s"key-$i", s"msg-$i"))

            def test(diagnostics: Diagnostics): ZIO[Producer & Scope & Kafka, Throwable, TestResult] =
              for {
                clientId <- randomClient
                topic    <- randomTopic
                settings <- consumerSettings(clientId = clientId)
                consumer <- Consumer.make(settings, diagnostics = diagnostics)
                _        <- produceMany(topic, kvs)
                // Starting a consumption session to start the Runloop.
                fiber <- consumer
                           .plainStream(Subscription.manual(topic -> 0), Serde.string, Serde.string)
                           .take(numberOfMessages.toLong)
                           .runCount
                           .forkScoped
                _         <- ZIO.sleep(200.millis)
                _         <- consumer.stopConsumption
                consumed0 <- fiber.join
                _         <- ZIO.logDebug(s"consumed0: $consumed0")

                _ <- ZIO.logDebug("About to sleep 5 seconds")
                _ <- ZIO.sleep(5.seconds)
                _ <- ZIO.logDebug("Slept 5 seconds")
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

          _ <- produceMany(topic, kvs)

          result <- Consumer
                      .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                      .take(11)
                      .map(_.offset)
                      .aggregateAsync(Consumer.offsetBatches)
                      .mapZIO(_.commit) // Hangs without timeout
                      .runDrain
                      .exit
                      .provideSomeLayer[Kafka](consumer(client, Some(group), commitTimeout = 2.seconds))
        } yield assert(result)(equalTo(Exit.fail(CommitTimeout)))
      } @@ TestAspect.flaky(10) @@ TestAspect.timeout(20.seconds)
    )
      .provideSome[Scope & Kafka](producer)
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ sequential @@ timeout(2.minutes)

}
