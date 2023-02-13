package zio.kafka.consumer

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.stream.{ ZSink, ZStream, ZTransducer }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment._
import zio.test.{ DefaultRunnableSpec, _ }
import zio.{ Chunk, Has, Promise, Ref, Schedule, Task, ZIO, ZLayer }

object ConsumerSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Throwable] =
    suite("Consumer Streaming")(
      testM("export metrics") {
        for {
          metrics <- Consumer.metrics
                       .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client150", Some("group1289")))
        } yield assert(metrics)(isNonEmpty)
      },
      testM("plainStream emits messages for a topic subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic150", kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set("topic150")))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client150", Some("group150")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      testM("chunk sizes") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic1289", kvs)

          sizes <- Consumer
                     .subscribeAnd(Subscription.Topics(Set("topic1289")))
                     .plainStream(Serde.string, Serde.string)
                     .take(100)
                     .mapChunks(c => Chunk(c.size))
                     .runCollect
                     .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client150", Some("group1289")))
        } yield assert(sizes)(forall(isGreaterThan(1)))
      },
      testM("Consumer.subscribeAnd works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic160", kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set("topic160")))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client160", Some("group160")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      testM("Consumer.subscribeAnd manual subscription without groupId works properly") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic161", kvs)

          records <-
            Consumer
              .subscribeAnd(Subscription.Manual(Set(new org.apache.kafka.common.TopicPartition("topic161", 0))))
              .plainStream(Serde.string, Serde.string)
              .take(5)
              .runCollect
              .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer(clientId = "client161"))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      testM("Consuming+provideCustomLayer") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("topic170", kvs)

          records <- Consumer
                       .subscribeAnd(Subscription.Topics(Set("topic170")))
                       .plainStream(Serde.string, Serde.string)
                       .take(100)
                       .runCollect
                       .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client170", Some("group170")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      testM("plainStream emits messages for a pattern subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          _ <- produceMany("pattern150", kvs)
          records <- Consumer
                       .subscribeAnd(Subscription.Pattern("pattern[0-9]+".r))
                       .plainStream(Serde.string, Serde.string)
                       .take(5)
                       .runCollect
                       .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client150", Some("group150")))
          kvOut = records.map(r => (r.record.key, r.record.value)).toList
        } yield assert(kvOut)(equalTo(kvs))
      },
      testM("receive only messages from the subscribed topic-partition when creating a manual subscription") {
        val nrPartitions = 5
        val topic        = "manual-topic0"

        for {
          _ <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }
          record <- Consumer
                      .subscribeAnd(Subscription.manual(topic, partition = 2))
                      .plainStream(Serde.string, Serde.string)
                      .take(1)
                      .runHead
                      .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client150", Some("group150")))
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2" -> "msg2")))
      },
      testM("receive from the right offset when creating a manual subscription with manual seeking") {
        val nrPartitions = 5
        val topic        = "manual-topic1"

        val manualOffsetSeek = 3

        for {
          _ <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach(1 to nrPartitions) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = (0 to 9).map(j => s"key$i-$j" -> s"msg$i-$j"))
               }
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          record <- Consumer
                      .subscribeAnd(Subscription.manual(topic, partition = 2))
                      .plainStream(Serde.string, Serde.string)
                      .take(1)
                      .runHead
                      .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                        consumer("client150", Some("group150"), offsetRetrieval = offsetRetrieval)
                      )
          kvOut = record.map(r => (r.record.key, r.record.value))
        } yield assert(kvOut)(isSome(equalTo("key2-3" -> "msg2-3")))
      },
      testM("restart from the committed position") {
        val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
        for {
          _ <- produceMany("topic1", 0, data)
          firstResults <- for {
                            results <- Consumer
                                         .subscribeAnd(Subscription.Topics(Set("topic1")))
                                         .partitionedStream(Serde.string, Serde.string)
                                         .filter(_._1 == new TopicPartition("topic1", 0))
                                         .flatMap(_._2)
                                         .take(5)
                                         .transduce(ZTransducer.collectAllN(Int.MaxValue))
                                         .mapConcatM { committableRecords =>
                                           val records = committableRecords.map(_.record)
                                           val offsetBatch =
                                             committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                           offsetBatch.commit.as(records)
                                         }
                                         .runCollect
                                         .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                                           consumer("first", Some("group1"))
                                         )
                          } yield results
          secondResults <- for {
                             results <- Consumer
                                          .subscribeAnd(Subscription.Topics(Set("topic1")))
                                          .partitionedStream(Serde.string, Serde.string)
                                          .flatMap(_._2)
                                          .take(5)
                                          .transduce(ZTransducer.collectAllN(Int.MaxValue))
                                          .mapConcatM { committableRecords =>
                                            val records = committableRecords.map(_.record)
                                            val offsetBatch =
                                              committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                            offsetBatch.commit.as(records)
                                          }
                                          .runCollect
                                          .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                                            consumer("second", Some("group1"))
                                          )
                           } yield results
        } yield assert((firstResults ++ secondResults).map(rec => rec.key() -> rec.value()).toList)(equalTo(data))
      },
      testM("partitionedStream emits messages for each partition in a separate stream") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                       .mapM(record => messagesReceived(record.partition).update(_ + 1).as(record))
                   }
                   .take(nrMessages.toLong)
                   .runDrain
                   .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client3", Some(group)))
                   .fork
          _                    <- fib.join
          messagesPerPartition <- ZIO.foreach(messagesReceived.values)(_.get)

        } yield assert(messagesPerPartition)(forall(equalTo(nrMessages / nrPartitions)))
      },
      testM("fail when the consuming effect produces a failure") {
        val topic        = "consumeWith3"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 10
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        for {
          _ <- produceMany(topic, messages)
          consumeResult <- consumeWithStrings("client3", Some("group3"), subscription) { case (_, _) =>
                             ZIO.fail(new IllegalArgumentException("consumeWith failure")).orDie
                           }.run
        } yield consumeResult.fold(
          _ => assertCompletes,
          _ => assert("result")(equalTo("Expected consumeWith to fail"))
        )
      } @@ timeout(10.seconds),
      testM("stopConsumption must stop the stream") {
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
                 .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                   consumer("client150", Some(group))
                 ) *> keepProducing
                 .set(false)
        } yield assertCompletes
      },
      testM("process outstanding commits after a graceful shutdown") {
        val kvs   = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        val topic = "test-outstanding-commits"
        for {
          group            <- randomGroup
          _                <- produceMany(topic, kvs)
          messagesReceived <- Ref.make[Int](0)
          offset <- (Consumer
                      .subscribeAnd(Subscription.topics(topic))
                      .plainStream(Serde.string, Serde.string)
                      .mapM { record =>
                        for {
                          nr <- messagesReceived.updateAndGet(_ + 1)
                          _  <- Consumer.stopConsumption.when(nr == 1)
                        } yield record.offset
                      }
                      .aggregate(Consumer.offsetBatches)
                      .mapM(_.commit)
                      .runDrain *>
                      Consumer.committed(Set(new TopicPartition(topic, 0))).map(_.values.head))
                      .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client150", Some(group)))
        } yield assert(offset.map(_.offset))(isSome(isLessThanEqualTo(10L)))
      } @@ TestAspect.ignore, // Not sure how to test this currently
      testM("offset batching collects the latest offset for all partitions") {
        val nrMessages   = 50
        val nrPartitions = 5

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                       .aggregate(Consumer.offsetBatches)
                       .take(1)
                       .mapM(_.commit)
                       .runDrain *>
                       Consumer.committed((0 until nrPartitions).map(new TopicPartition(topic, _)).toSet))
                       .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client3", Some(group)))
        } yield assert(offsets.values.map(_.map(_.offset)))(forall(isSome(equalTo(nrMessages.toLong / nrPartitions))))
      },
      testM("handle rebalancing by completing topic-partition streams") {
        val nrMessages   = 50
        val nrPartitions = 6

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                             .fromEffect(partition.runDrain)
                             .as(tp)
                         }
                         .take(nrPartitions.toLong / 2)
                         .runDrain
                         .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client1", Some(group)))
                         .fork
          _ <- Live.live(ZIO.sleep(5.seconds))
          consumer2 <- Consumer
                         .subscribeAnd(subscription)
                         .partitionedStream(Serde.string, Serde.string)
                         .take(nrPartitions.toLong / 2)
                         .runDrain
                         .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client2", Some(group)))
                         .fork
          _ <- consumer1.join
          _ <- consumer2.join
        } yield assertCompletes
      },
      testM("produce diagnostic events when rebalancing") {
        val nrMessages   = 50
        val nrPartitions = 6

        Diagnostics.SlidingQueue
          .make()
          .use { diagnostics =>
            for {
              // Produce messages on several partitions
              topic <- randomTopic
              group <- randomGroup
              _     <- Task(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
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
                                 .fromEffect(partition.runDrain)
                                 .as(tp)
                             }
                             .take(nrPartitions.toLong / 2)
                             .runDrain
                             .provideSomeLayer[Has[Kafka] with Blocking with Clock](
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
                             .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client2", Some(group)))
                             .fork
              _ <- consumer1.join
              _ <- consumer1.join
              _ <- consumer2.join
            } yield diagnosticStream.join
          }
          .flatten
          .map(diagnosticEvents => assert(diagnosticEvents.size)(isGreaterThanEqualTo(2)))
      },
      testM("support manual seeking") {
        val nrRecords        = 10
        val data             = (1 to nrRecords).toList.map(i => s"key$i" -> s"msg$i")
        val manualOffsetSeek = 3

        for {
          topic <- randomTopic
          _     <- produceMany(topic, 0, data)
          // Consume 5 records to have the offset committed at 5
          _ <- Consumer
                 .subscribeAnd(Subscription.topics(topic))
                 .plainStream(Serde.string, Serde.string)
                 .take(5)
                 .transduce(ZTransducer.collectAllN(Int.MaxValue))
                 .mapConcatM { committableRecords =>
                   val records = committableRecords.map(_.record)
                   val offsetBatch =
                     committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                   offsetBatch.commit.as(records)
                 }
                 .runCollect
                 .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client1", Some("group1")))
          // Start a new consumer with manual offset before the committed offset
          offsetRetrieval = OffsetRetrieval.Manual(tps => ZIO(tps.map(_ -> manualOffsetSeek.toLong).toMap))
          secondResults <- Consumer
                             .subscribeAnd(Subscription.topics(topic))
                             .plainStream(Serde.string, Serde.string)
                             .take(nrRecords.toLong - manualOffsetSeek)
                             .map(_.record)
                             .runCollect
                             .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                               consumer("client2", Some("group1"), offsetRetrieval = offsetRetrieval)
                             )
          // Check that we only got the records starting from the manually seek'd offset
        } yield assert(secondResults.map(rec => rec.key() -> rec.value()).toList)(equalTo(data.drop(manualOffsetSeek)))
      },
      testM("commit offsets for all consumed messages") {
        val topic        = "consumeWith2"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 50
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        def consumeIt(messagesReceived: Ref[List[(String, String)]], done: Promise[Nothing, Unit]) =
          consumeWithStrings("client3", Some("group3"), subscription)({ (key, value) =>
            (for {
              messagesSoFar <- messagesReceived.updateAndGet(_ :+ (key -> value))
              _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
            } yield ()).orDie
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
                          .provideSomeLayer[Has[Kafka] with Blocking with Clock](consumer("client3", Some("group3")))
          consumedMessages <- messagesReceived.get
        } yield assert(consumedMessages)(contains(newMessage).negate)
      },
      testM("partitions for topic doesn't fail if doesn't exist") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomThing("client")
          partitions <- Consumer
                          .partitionsFor(topic)
                          .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                            consumer(client, Some(group), allowAutoCreateTopics = false)
                          )
        } yield assert(partitions)(isEmpty)
      },
      // Test backported from fs2-kafka: https://github.com/fd4s/fs2-kafka/blob/1bd0c1f3d46b543277fce1a3cc743154c162ef09/modules/core/src/test/scala/fs2/kafka/KafkaConsumerSpec.scala#L592
      testM("should close old stream during rebalancing under load") {
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
              ZStream.fromEffect[Any, Nothing, Any](allAssignments.update({ current =>
                current.get(instance) match {
                  case Some(currentList) => current.updated(instance, currentList :+ tp.partition())
                  case None              => current.updated(instance, List(tp.partition()))
                }
              })) ++ partStream.fixed(10.millis) ++ ZStream.fromEffect(allAssignments.update({ current =>
                current.get(instance) match {
                  case Some(currentList) =>
                    val idx = currentList.indexOf(tp.partition())
                    if (idx != -1) current.updated(instance, currentList.patch(idx, Nil, 1))
                    else current
                  case None => current
                }
              }))
            }
            .flattenParUnbounded()
            .runDrain
        }

        def checkAssignments(allAssignments: Ref[Map[Int, List[Int]]])(instances: Set[Int]) =
          ZStream
            .repeatEffectWith(allAssignments.get, Schedule.spaced(30.millis))
            .filter { state =>
              state.keySet == instances &&
              instances.forall(instance => state.get(instance).exists(_.nonEmpty)) &&
              state.values.toList.flatten.sorted == partitions
            }
            .runHead
            .timeoutFail(ValidAssignmentsNotSeen(allAssignments.unsafeGet.toString))(waitTimeout)

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
                      .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                        consumer(
                          client1,
                          Some(group),
                          offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
                        )
                      )
                      .fork
          _ <- check(Set(0))
          fiber1 <- run(1, topic, allAssignments)
                      .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                        consumer(
                          client2,
                          Some(group),
                          offsetRetrieval = OffsetRetrieval.Auto(reset = AutoOffsetStrategy.Earliest)
                        )
                      )
                      .fork
          _ <- check(Set(0, 1))
          fiber2 <- run(2, topic, allAssignments)
                      .provideSomeLayer[Has[Kafka] with Blocking with Clock](
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
      testM("restartStreamsOnRebalancing mode closes all partition streams") {
        val nrPartitions = 5
        val nrMessages   = 100

        for {
          // Produce messages on several partitions
          topic <- randomTopic
          group <- randomGroup
          _     <- Task.fromTry(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))
          _ <- ZIO.foreach_(1 to nrMessages) { i =>
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
                   .mapM { partitions =>
                     ZStream
                       .fromIterable(partitions.map(_._2))
                       .flatMapPar(Int.MaxValue)(s => s)
                       .mapM(record => messagesReceived(record.partition).update(_ + 1).as(record))
                       .mapM(record => record.offset.commit)
                       .runDrain
                   }
                   .mapM(_ =>
                     drainCount.updateAndGet(_ + 1).flatMap {
                       case 2 => Consumer.stopConsumption
                       case _ => ZIO.unit
                     }
                   )
                   .runDrain
                   .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                     consumer("client1", Some(group), restartStreamOnRebalancing = true)
                   )
                   .fork
          _ <- ZIO
                 .foreach(messagesReceived.values)(_.get)
                 .map(_.sum)
                 .repeat(Schedule.recurUntil((n: Int) => n == nrMessages) && Schedule.fixed(10.millis))
          messagesReceived0 <-
            ZIO
              .foreach((0 until nrPartitions).toList) { i =>
                messagesReceived(i).get.flatMap { v =>
                  Ref.make(v).map(r => v -> r)
                } <* messagesReceived(i).set(0)
              }
              .map(_.toMap)

          fib2 <- Consumer
                    .subscribeAnd(subscription)
                    .plainStream(Serde.string, Serde.string)
                    .take(20)
                    .runDrain
                    .provideSomeLayer[Has[Kafka] with Blocking with Clock](
                      consumer("client2", Some(group))
                    )
                    .fork

          _ <- ZIO.foreach_((nrMessages + 1) to (2 * nrMessages)) { i =>
                 produceMany(topic, partition = i % nrPartitions, kvs = List(s"key$i" -> s"msg$i"))
               }
          _                     <- fib2.join
          _                     <- fib.join
          messagesPerPartition0 <- ZIO.foreach(messagesReceived0.values)(_.get)
          messagesPerPartition  <- ZIO.foreach(messagesReceived.values)(_.get)

        } yield assert(messagesPerPartition0)(forall(equalTo(nrMessages / nrPartitions))) &&
          assert(messagesPerPartition)(forall(isGreaterThan(0) && isLessThanEqualTo(nrMessages / nrPartitions)))
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded ++ ZLayer.identity[Blocking] >>> producer) ++ Kafka.embedded)
        .mapError(TestFailure.fail) ++ Clock.live
    ) @@ timeout(180.seconds)
}
