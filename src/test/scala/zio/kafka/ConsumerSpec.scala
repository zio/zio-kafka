package zio.kafka.consumer

import io.github.embeddedkafka.EmbeddedKafka
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

  override def spec: ZSpec[TestEnvironment with Kafka, Throwable] =
    suite("Consumer Streaming")(
      test("export metrics") {
        for {
          metrics <- Consumer.metrics
                       .provideSomeLayer[Kafka](consumer("client150", Some("group1289")))
        } yield assert(metrics)(isNonEmpty)
      },
      test("plainStream emits messages for a topic subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic150", kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set("topic150")))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer("client150", Some("group150")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("chunk sizes") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic1289", kvs)

          sizes <- Consumer
                     .subscribeAnd(Subscription.Topics(Set("topic1289")))
                     .plainStream(Serde.string, Serde.string)
                     .take(100)
                     .mapChunks(c => Chunk(c.size))
                     .runCollect
                     .provideSomeLayer[Kafka](consumer("client150", Some("group1289")))
        } yield assert(sizes)(forall(isGreaterThan(1)))
      },
      test("Consumer.subscribeAnd works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic160", kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set("topic160")))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer("client160", Some("group160")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("Consumer.subscribeAnd manual subscription without groupId works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic161", kvs)

          records <-
            Consumer
              .subscribeAnd(Subscription.Manual(Set(new org.apache.kafka.common.TopicPartition("topic161", 0))))
              .plainStream(Serde.string, Serde.string)
              .take(5)
              .runCollect
              .provideSomeLayer[Kafka](consumer(clientId = "client161"))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("Consuming+provideCustomLayer") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic170", kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set("topic170")))
                       .plainStream(Serde.string, Serde.string)
                       .take(100)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer("client170", Some("group170")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("plainStream emits messages for a pattern subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("pattern150", kvs)
          records <- Consumer
                       .subscribeAnd(Subscription.Pattern("pattern[0-9]+".r))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Kafka](consumer("client150", Some("group150")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      test("receive only messages from the subscribed topic-partition when creating a manual subscription") {
        val nrPartitions = 5
        val topic        = "manual-topic0"

        for {
          _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }
          record <- Consumer
                      .subscribeAnd(Subscription.manual(topic, partition = 2))
                      .plainStream(Serde.string, Serde.string)
                      .take(1)
                      .runHead
                      .provideSomeLayer[Kafka](consumer("client150", Some("group150")))
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2" -> "msg2")))
      },
      test("receive from the right offset when creating a manual subscription with manual seeking") {
        val nrPartitions = 5
        val topic        = "manual-topic1"

        val manualOffsetSeek = 3

        for {
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
                        consumer("client150", Some("group150"), offsetRetrieval = offsetRetrieval)
                      )
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2-3" -> "msg2-3")))
      },
      test("restart from the committed position") {
        val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
        for {
          _ <- produceMany("topic1111", 0, data)
          firstResults <- for {
                            results <- Consumer
                                         .subscribeAnd(Subscription.Topics(Set("topic1111")))
                                         .partitionedStream(Serde.string, Serde.string)
                                         .filter(_._1 == new TopicPartition("topic1111", 0))
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
                                           consumer("first", Some("group1"))
                                         )
                          } yield results
          secondResults <- for {
                             results <- Consumer
                                          .subscribeAnd(Subscription.Topics(Set("topic1111")))
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
                                            consumer("second", Some("group1"))
                                          )
                           } yield results
        } yield assert((firstResults ++ secondResults).map(rec => rec.key() -> rec.value()).toList)(equalTo(data))
      },
      test("partitionedStream emits messages for each partition in a separate stream") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                   .provideSomeLayer[Kafka](consumer("client3", Some(group)))
                   .fork
          _                    <- fib.join
          messagesPerPartition <- ZIO.foreach(messagesReceived.values)(_.get)

        } yield assert(messagesPerPartition)(forall(equalTo(nrMessages / nrPartitions)))
      },
      test("fail when the consuming effect produces a failure") {
        val topic        = "consumeWith3"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 10
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        for {
          _ <- produceMany(topic, messages)
          consumeResult <- consumeWithStrings("client3", Some("group3"), subscription) { case (_, _) =>
                             ZIO.die(new IllegalArgumentException("consumeWith failure"))
                           }.exit
        } yield consumeResult.fold(
          _ => assertCompletes,
          _ => assert("result")(equalTo("Expected consumeWith to fail"))
        )
      } @@ timeout(10.seconds),
      test("stopConsumption must stop the stream") {
        for {
          topic         <- randomTopic
          group         <- randomGroup
          keepProducing <- Ref.make(true)
          _             <- (produceOne(topic, "key", "value") *> keepProducing.get).repeatWhile(b => b).fork
          _ <- Consumer
                 .subscribeAnd(Subscription.topics(topic))
                 .plainStream(Serde.string, Serde.string)
                 .zipWithIndex
                 .tap { case (_, idx) => Consumer.stopConsumption.when(idx == 3) }
                 .runDrain
                 .provideSomeLayer[Kafka](
                   consumer("client150", Some(group))
                 ) *> keepProducing
                 .set(false)
        } yield assertCompletes
      },
      test("process outstanding commits after a graceful shutdown") {
        val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        val topic = "test-outstanding-commits"
        for {
          group            <- randomGroup
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
                      .provideSomeLayer[Kafka](consumer("client150", Some(group)))
        } yield assert(offset.map(_.offset))(isSome(isLessThanEqualTo(10L)))
      } @@ TestAspect.ignore, // Not sure how to test this currently
      test("offset batching collects the latest offset for all partitions") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                       .provideSomeLayer[Kafka](consumer("client3", Some(group)))
        } yield assert(offsets.values.map(_.map(_.offset)))(forall(isSome(equalTo(nrMessages.toLong / nrPartitions))))
      },
      test("handle rebalancing by completing topic-partition streams") {
        val nrMessages   = 50
        val nrPartitions = 6

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                         .provideSomeLayer[Kafka](consumer("client1", Some(group)))
                         .fork
          _ <- Live.live(ZIO.sleep(5.seconds))
          consumer2 <- Consumer
                         .subscribeAnd(subscription)
                         .partitionedStream(Serde.string, Serde.string)
                         .take(nrPartitions.toLong / 2)
                         .runDrain
                         .provideSomeLayer[Kafka](consumer("client2", Some(group)))
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
                topic <- randomTopic
                group <- randomGroup
                _     <- Task.attempt(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                                 consumer("client1", Some(group), diagnostics = diagnostics)
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
                               .provideSomeLayer[Kafka](consumer("client2", Some(group)))
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
          topic <- randomTopic
          group <- randomGroup
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
                 .provideSomeLayer[Kafka](consumer("client1", Some(group)))
          // Start a new consumer with manual offset before the committed offset
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO.attempt(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          secondResults <- Consumer
                             .subscribeAnd(Subscription.topics(topic))
                             .plainStream(Serde.string, Serde.string)
                             .take(nrRecords.toLong - manualOffsetSeek)
                             .map(_.record)
                             .runCollect
                             .provideSomeLayer[Kafka](
                               consumer("client2", Some(group), offsetRetrieval = offsetRetrieval)
                             )
          // Check that we only got the records starting from the manually seek'd offset
        } yield assert(secondResults.map(rec => rec.key() -> rec.value()).toList)(equalTo(data.drop(manualOffsetSeek)))
      },
      test("commit offsets for all consumed messages") {
        val topic        = "consumeWith2"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 50
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        def consumeIt(messagesReceived: Ref[List[(String, String)]], done: Promise[Nothing, Unit]) =
          consumeWithStrings("client3", Some("group3"), subscription)({ (key, value) =>
            for {
              messagesSoFar <- messagesReceived.updateAndGet(_ :+ (key -> value))
              _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
            } yield ()
          }).fork

        for {
          done             <- Promise.make[Nothing, Unit]
          messagesReceived <- Ref.make(List.empty[(String, String)])
          _                <- produceMany(topic, messages)
          fib              <- consumeIt(messagesReceived, done)
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
                          .provideSomeLayer[Kafka](consumer("client3", Some("group3")))
          consumedMessages <- messagesReceived.get
        } yield assert(consumedMessages)(contains(newMessage).negate)
      },
      test("partitions for topic doesn't fail if doesn't exist") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomThing("client")
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

              registerAssignment.drain ++ partStream.fixed(10.millis) ++ deregisterAssignment.drain
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
          _              <- Task.fromTry(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
      }
    ).provideSomeLayerShared[TestEnvironment with Kafka](
      producer.mapError(TestFailure.fail)
    ) @@ withLiveClock @@ timeout(180.seconds)
}
