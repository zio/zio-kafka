package zio.kafka.client

import com.typesafe.scalalogging.LazyLogging
import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.scalatest.{ EitherValues, Matchers, WordSpecLike }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.serde.Serde
import zio.stream.ZSink

class ConsumerTest extends WordSpecLike with Matchers with LazyLogging with DefaultRuntime with EitherValues {
  import KafkaTestUtils._

  def pause(): ZIO[Clock, Nothing, Unit] = UIO(()).delay(2.seconds).forever

  def log(s: String): UIO[Unit] = ZIO.effectTotal(logger.info(s))

  val embeddedKafka   = EmbeddedKafka.start()
  val bootstrapServer = s"localhost:${embeddedKafka.config.kafkaPort}"

  def settings(groupId: String, clientId: String) =
    ConsumerSettings(
      List(bootstrapServer),
      groupId,
      clientId,
      5.seconds,
      Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest"),
      250.millis,
      250.millis,
      1
    )

  def runWithConsumer[A](groupId: String, clientId: String)(
    r: Consumer => RIO[Blocking with Clock, A]
  ): A =
    unsafeRun(
      Consumer.make(settings(groupId, clientId)).use(r)
    )

  "A string consumer" when {
    "polling" should {
      "receive messages produced on the topic" in runWithConsumer("group150", "client150") { consumer =>
        for {
          kvs <- ZIO((1 to 5).toList.map(i => (s"key$i", s"msg$i")))
          _   <- produceMany("topic150", kvs)
          records <- consumer
                      .subscribeAnd(Subscription.Topics(Set("topic150")))
                      .plainStream(Serde.string, Serde.string)
                      .flattenChunks
                      .take(5)
                      .runCollect
          _ <- ZIO.effectTotal(records.map { r =>
                (r.record.key, r.record.value)
              } shouldEqual kvs)
        } yield ()
      }
    }

    "using a serializer with an environment" should {
      "get access to the environment" in unsafeRun {
        val topic        = "consumeWith5"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 50
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        import zio.console._
        implicit val loggingStringSerde: Serde[Console, String] = Serde.string
          .inmapM(s => putStrLn(s"Deserialized ${s}").as(s))(s => putStrLn(s"Serializing ${s}").as(s))

        for {
          done             <- Promise.make[Nothing, Unit]
          messagesReceived <- Ref.make(List.empty[(String, String)])
          _                <- produceMany(topic, messages)
          fib <- Consumer
                  .consumeWith(settings("group3", "client3"), subscription, loggingStringSerde, loggingStringSerde) {
                    (key, value) =>
                      (for {
                        messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
                        _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
                      } yield ()).orDie
                  }
                  .fork
          _ <- done.await
          _ <- fib.interrupt
          _ <- fib.join.ignore
        } yield succeed
      }
    }

    "committing" should {
      "restart from the committed position" in unsafeRun {
        val data = (1 to 10).toList.map(i => s"key$i" -> s"msg$i")
        for {
          _ <- produceMany("topic1", 0, data)
          firstResults <- Consumer.make(settings("group1", "first")).use { consumer =>
                           for {
                             results <- consumer
                                         .subscribeAnd(Subscription.Topics(Set("topic1")))
                                         .partitionedStream(Serde.string, Serde.string)
                                         .filter(_._1 == new TopicPartition("topic1", 0))
                                         .flatMap(_._2.flattenChunks)
                                         .take(5)
                                         .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                                         .mapM { committableRecords =>
                                           val records = committableRecords.map(_.record)
                                           val offsetBatch =
                                             committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                           offsetBatch.commit.as(records)
                                         }
                                         .mapConcat(Chunk.fromIterable)
                                         .runCollect
                           } yield results
                         }
          secondResults <- Consumer.make(settings("group1", "second")).use { consumer =>
                            for {
                              results <- consumer
                                          .subscribeAnd(Subscription.Topics(Set("topic1")))
                                          .partitionedStream(Serde.string, Serde.string)
                                          .flatMap(_._2.flattenChunks)
                                          .take(5)
                                          .transduce(ZSink.collectAll[CommittableRecord[String, String]])
                                          .mapM { committableRecords =>
                                            val records = committableRecords.map(_.record)
                                            val offsetBatch =
                                              committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                                            offsetBatch.commit.as(records)
                                          }
                                          .mapConcat(Chunk.fromIterable)
                                          .runCollect
                            } yield results
                          }
        } yield (firstResults ++ secondResults).map(rec => rec.key() -> rec.value()) shouldEqual data
      }
    }

    "consuming using consumeWith" should {
      "consume all the messages on a topic" in unsafeRun {
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
          fib <- Consumer
                  .consumeWith(
                    settings("group3", "client3"),
                    subscription,
                    Serde.string,
                    Serde.string
                  ) { (key, value) =>
                    (for {
                      messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
                      _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
                    } yield ()).orDie
                  }
                  .fork
          _ <- done.await
          _ <- fib.interrupt
          _ <- fib.join.ignore
        } yield succeed
      }

      "commit offsets for all consumed messages" in unsafeRun {
        val topic        = "consumeWith2"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 50
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        for {
          done             <- Promise.make[Nothing, Unit]
          messagesReceived <- Ref.make(List.empty[(String, String)])
          _                <- produceMany(topic, messages)
          fib <- Consumer
                  .consumeWith(settings("group3", "client3"), subscription, Serde.string, Serde.string)({
                    (key, value) =>
                      (for {
                        messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
                        _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
                      } yield ()).orDie
                  })
                  .fork
          _ <- done.await *> ZIO.sleep(3.seconds) // TODO the sleep is necessary for the outstanding commits to be flushed. Maybe we can fix that another way
          _ <- fib.interrupt
          _ <- fib.join.ignore
          _ <- produceOne(topic, "key-new", "msg-new")
          newMessage <- Consumer
                         .make(settings("group3", "client3"))
                         .use { c =>
                           c.subscribe(subscription) *> c
                             .plainStream(Serde.string, Serde.string)
                             .take(1)
                             .flattenChunks
                             .map(r => (r.record.key(), r.record.value()))
                             .run(ZSink.collectAll[(String, String)])
                             .map(_.head)
                         }
                         .orDie
          consumedMessages <- messagesReceived.get
        } yield consumedMessages shouldNot contain(newMessage)
      }

      "fail when the consuming effect produces a failure" in unsafeRun {
        val topic        = "consumeWith3"
        val subscription = Subscription.Topics(Set(topic))
        val nrMessages   = 10
        val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

        for {
          messagesReceived <- Ref.make(List.empty[(String, String)])
          _                <- produceMany(topic, messages)
          fib <- Consumer
                  .consumeWith(
                    settings("group3", "client3"),
                    subscription,
                    Serde.string,
                    Serde.string
                  ) { (key, value) =>
                    (for {
                      messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
                      _ <- Task.when(messagesSoFar.size == 3)(
                            ZIO.die(new IllegalArgumentException("consumeWith failure"))
                          )
                    } yield ()).orDie
                  }
                  .fork
          testResult <- fib.map(_ => fail("Expected consumeWith to fail")).orElse(Fiber.succeed(succeed)).join
        } yield testResult
      }
    }

    "re-balancing" should {
      "hand-over revoked and continue on assigned partition after re-balance listener execution" in unsafeRun {

        val topic = "rebalance-test"

        sealed trait ConsumerEvent { val consumerId: Int }
        case class Consumed(override val consumerId: Int, k: Int, v: Int) extends ConsumerEvent
        case class Revoked(override val consumerId: Int)                  extends ConsumerEvent
        case class Assigned(override val consumerId: Int)                 extends ConsumerEvent

        for {
          _               <- ZIO.effectTotal(EmbeddedKafka.createCustomTopic(topic, partitions = 2))
          consumerEvents  <- Ref.make[Seq[ConsumerEvent]](Seq())
          p0Value         <- Ref.make[Int](0)
          p1Value         <- Ref.make[Int](0)
          partitionToggle <- Ref.make[Int](1)
          producer <- (for {
                       p <- partitionToggle.update(p => if (p == 0) 1 else 0)
                       v <- if (p == 0) p0Value.update(_ + 1) else p1Value.update(_ + 1)
                       _ <- produceOne(topic, p, p.toString, v.toString)
                     } yield ()).repeat(Schedule.spaced(1.second)).fork
          startC2       <- Promise.make[Nothing, Unit]
          done          <- Promise.make[Nothing, Unit]
          c1ReceivedRef <- Ref.make(List.empty[(Int, Int)])
          c2ReceivedRef <- Ref.make(List.empty[(Int, Int)])
          consumer1 <- Consumer
                        .consumeWith(
                          settings("rebalanceGroup", "rebalanceClient"),
                          Subscription
                            .topics(topic)
                            .withRebalanceListener(
                              RebalanceListener(
                                _ => consumerEvents.update(_ :+ Assigned(1)) *> UIO.succeed(()),
                                _ => consumerEvents.update(_ :+ Revoked(1)) *> UIO.succeed(())
                              )
                            ),
                          Serde.string,
                          Serde.string
                        ) { (sKey, sValue) =>
                          (for {
                            key         <- IO(Integer.parseInt(sKey))
                            value       <- IO(Integer.parseInt(sValue))
                            received    <- c1ReceivedRef.update(_ :+ (key -> value))
                            startC2Done <- startC2.isDone
                            _ <- Task.when(List(0, 1).forall(received.map(_._1).contains) && !startC2Done)(
                                  startC2.succeed(())
                                )
                            _ <- consumerEvents.update(_ :+ Consumed(1, key, value))
                          } yield ()).orDie
                        }
                        .fork
          _ <- startC2.await
          consumer2 <- Consumer
                        .consumeWith(
                          settings("rebalanceGroup", "rebalanceClient"),
                          Subscription
                            .topics(topic)
                            .withRebalanceListener(
                              RebalanceListener(
                                _ => consumerEvents.update(_ :+ Assigned(2)) *> UIO.succeed(()),
                                _ => consumerEvents.update(_ :+ Revoked(2)) *> UIO.succeed(())
                              )
                            ),
                          Serde.string,
                          Serde.string
                        ) { (sKey, sValue) =>
                          (for {
                            key      <- IO(Integer.parseInt(sKey))
                            value    <- IO(Integer.parseInt(sValue))
                            received <- c2ReceivedRef.update(_ :+ (key -> value))
                            _        <- Task.when(received.size == 5)(done.succeed(()))
                            _        <- consumerEvents.update(_ :+ Consumed(2, key, value))
                          } yield ()).orDie
                        }
                        .fork
          _      <- done.await
          _      <- consumer2.interrupt
          _      <- consumer1.interrupt
          _      <- producer.interrupt
          events <- consumerEvents.get
        } yield {
          val consumer1Events = events.filter(_.consumerId == 1)
          val consumer2Events = events.filter(_.consumerId == 2)

          val consumer1EventsBeforeRebal = consumer1Events
            .dropWhile({
              case _: Revoked | _: Assigned => true
              case _                        => false
            })
            .takeWhile({
              case _: Consumed => true
              case _           => false
            })

          val consumer1RebalEvents = consumer1Events
            .dropWhile({
              case _: Revoked | _: Assigned => true
              case _                        => false
            })
            .dropWhile({
              case _: Consumed => true
              case _           => false
            })
            .takeWhile({
              case _: Revoked | _: Assigned => true
              case _                        => false
            })

          val consumer1EventsAfterRebal = consumer1Events
            .dropWhile({
              case _: Revoked | _: Assigned => true
              case _                        => false
            })
            .dropWhile({
              case _: Consumed => true
              case _           => false
            })
            .dropWhile({
              case _: Revoked | _: Assigned => true
              case _                        => false
            })

          val consumer2FirstMessage = consumer2Events
            .dropWhile({
              case _: Revoked | _: Assigned => true
              case _                        => false
            })
            .headOption match {
            case Some(c @ Consumed(_, _, _)) => c
            case _                           => fail("Expected consumer2 to have consumed some messages but none found")
          }

          // Consumer1 Revoke should be followed by Assigned
          consumer1RebalEvents match {
            case Seq(_: Revoked, _: Assigned) => succeed
            case s                            => fail(s"Expected 'revoked' then 'assigned' but found $s")
          }

          // Consumer1's first message after re-balance should follow it's last message before re-balance of the same partition
          val firstConsumedAfterRebal: Consumed =
            consumer1EventsAfterRebal.headOption match {
              case Some(c @ Consumed(_, _, _)) => c
              case _                           => fail("Expected consumer1 to have consumed messages after re-balance but none found")
            }

          consumer1EventsBeforeRebal.reverse
            .find({
              case Consumed(1, firstConsumedAfterRebal.k, _) => true
              case _                                         => false
            }) match {
            case Some(Consumed(1, firstConsumedAfterRebal.k, lastValBeforeRebal)) =>
              if (lastValBeforeRebal == (firstConsumedAfterRebal.v - 1)) succeed
              else
                fail(
                  s"Expected value of message for partition ${firstConsumedAfterRebal.k} " +
                    s"before re-balance to be ${firstConsumedAfterRebal.v - 1}, " +
                    s"but was $lastValBeforeRebal"
                )
            case _ =>
              fail(
                s"Expected consumer1 to have consumed messages from partition ${firstConsumedAfterRebal.k} but none found"
              )
          }

          // Consumer2's first message after re-balance should follow Consumer1's last message before re-balance of the same partition
          consumer1EventsBeforeRebal.reverse
            .find({
              case Consumed(1, consumer2FirstMessage.k, _) => true
              case _                                       => false
            }) match {
            case Some(Consumed(1, consumer2FirstMessage.k, lastValBeforeRebal)) =>
              if (lastValBeforeRebal == (consumer2FirstMessage.v - 1)) succeed
              else
                fail(
                  s"Expected value of message for partition ${consumer2FirstMessage.k} " +
                    s"before re-balance to be ${firstConsumedAfterRebal.v - 1}, " +
                    s"but was $lastValBeforeRebal"
                )
            case _ =>
              fail(
                s"Expected consumer1 to have consumed messages from partition ${consumer2FirstMessage.k} but none found"
              )
          }
        }
      }
    }
  }
}
