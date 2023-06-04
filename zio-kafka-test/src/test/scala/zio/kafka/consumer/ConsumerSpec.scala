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
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.producer.TransactionalProducer
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils._
import zio.kafka.testkit.{ Kafka, KafkaRandom }
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import scala.reflect.ClassTag

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
                 .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                 .zipWithIndex
                 .tap { case (record, idx) =>
                   (Consumer.stopConsumption <* ZIO.logDebug("Stopped consumption")).when(idx == 3) *>
                     record.offset.commit <* ZIO.logDebug(s"Committed $idx")
                 }
                 .tap { case (_, idx) => ZIO.logDebug(s"Consumed $idx") }
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
      test("handle rebalancing by completing topic-partition streams") {
        val nrMessages   = 50
        val nrPartitions = 6

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
        val nrPartitions = 5
        val nrMessages   = 100

        for {
          // Produce messages on several partitions
          _       <- ZIO.logDebug("Starting test")
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
          fib <- ZIO
                   .logAnnotate("consumer", "1") {
                     Consumer
                       .partitionedAssignmentStream(subscription, Serde.string, Serde.string)
                       .rechunk(1)
                       .mapZIO { partitions =>
                         ZIO.logDebug(s"Got partition assignment ${partitions.map(_._1).mkString(",")}") *>
                           ZStream
                             .fromIterable(partitions)
                             .flatMapPar(Int.MaxValue) { case (tp, partitionStream) =>
                               ZStream.finalizer(ZIO.logDebug(s"TP ${tp.toString} finalizer")) *>
                                 partitionStream.mapChunksZIO { records =>
                                   OffsetBatch(records.map(_.offset)).commit *> messagesReceived(tp.partition)
                                     .update(_ + records.size)
                                     .as(records)
                                 }
                             }
                             .runDrain
                       }
                       .mapZIO(_ =>
                         drainCount.updateAndGet(_ + 1).flatMap {
                           case 2 => ZIO.logDebug("Stopping consumption") *> Consumer.stopConsumption
                           // 1: when consumer on fib2 starts
                           // 2: when consumer on fib2 stops, end of test
                           case _ => ZIO.unit
                         }
                       )
                       .runDrain
                       .provideSomeLayer[Kafka](
                         consumer(
                           client1,
                           Some(group),
                           clientInstanceId = Some("consumer1"),
                           restartStreamOnRebalancing = true,
                           properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "10")
                         )
                       )
                   }
                   .fork
          // fib is running, consuming all the published messages from all partitions.
          // Waiting until it recorded all messages
          _ <- ZIO
                 .foreach(messagesReceived.values)(_.get)
                 .map(_.sum)
                 .repeat(Schedule.recurUntil((n: Int) => n == nrMessages) && Schedule.fixed(100.millis))

          // Starting a new consumer that will stop after receiving 20 messages,
          // causing two rebalancing events for fib1 consumers on start and stop
          fib2 <- ZIO
                    .logAnnotate("consumer", "2") {
                      Consumer
                        .plainStream(subscription, Serde.string, Serde.string)
                        .take(20)
                        .runDrain
                        .provideSomeLayer[Kafka](
                          consumer(
                            client2,
                            Some(group),
                            clientInstanceId = Some("consumer2"),
                            properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "10")
                          )
                        )
                    }
                    .fork

          // Waiting until fib1's partition streams got restarted because of the rebalancing
          _ <- drainCount.get.repeat(Schedule.recurUntil((n: Int) => n == 1) && Schedule.fixed(100.millis))
          _ <- ZIO.logDebug("Consumer 1 finished rebalancing")

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
          _ <- ZIO.logDebug("Consumer 2 done")
          _ <- fib.join
          _ <- ZIO.logDebug("Consumer 1 done")
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
      } @@ TestAspect.nonFlaky(3),
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
      }
    )
      .provideSome[Scope & Kafka](producer)
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ TestAspect.sequential @@ timeout(2.minutes)

}
