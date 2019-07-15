package zio.kafka.client

import com.typesafe.scalalogging.LazyLogging

import net.manub.embeddedkafka.EmbeddedKafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ Serde, Serdes }

import org.scalatest.{ Matchers, WordSpecLike }

import zio.{ Chunk, DefaultRuntime, Ref, Schedule, TaskR, UIO, ZIO }
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._

class ConsumerTest extends WordSpecLike with Matchers with LazyLogging with DefaultRuntime {
  import KafkaTestUtils._
  import Rebalance.{ Assign, Revoke }

  def pause(): ZIO[Clock, Nothing, Unit] = UIO(()).delay(2.seconds).forever

  def log(s: String): UIO[Unit] = ZIO.effectTotal(logger.info(s))

  val embeddedKafka                       = EmbeddedKafka.start()
  val bootstrapServer                     = s"localhost:${embeddedKafka.config.kafkaPort}"
  implicit val stringSerde: Serde[String] = Serdes.String()

  def settings(groupId: String, clientId: String) =
    ConsumerSettings(
      List(bootstrapServer),
      groupId,
      clientId,
      5.seconds,
      Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest")
    )

  def runWithConsumer[A](groupId: String, clientId: String)(
    r: Consumer[String, String] => TaskR[Blocking with Clock, A]
  ): A =
    unsafeRun(
      Consumer.make[String, String](settings(groupId, clientId)).use(r)
    )

  "A string consumer" can {
    "subscribe" should {
      "to a single topic with non empty name" in runWithConsumer("group1", "clientId1") { consumer =>
        for {
          outcome <- consumer.subscribe(Subscription.Topics(Set("topic1"))).either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
        } yield ()
      }

      "to a single topic with empty name" in runWithConsumer("group2", "clientId2") { consumer =>
        for {
          outcome <- consumer.subscribe(Subscription.Topics(Set("     "))).either
          _       <- ZIO.effect(outcome.isRight shouldBe false)
        } yield ()
      }

      "to topics by pattern" in runWithConsumer("group1", "clientId1") { consumer =>
        for {
          outcome <- consumer.subscribe(Subscription.Pattern("topic*")).either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
        } yield ()
      }

      "with rebalance listener" in {
        val topic              = "topic100"
        val groupId            = "group100"
        val subscription       = Subscription.Topics(Set(topic))
        val nrMessagesPerBatch = 50
        val test = for {
          c1RebalanceEvidence <- Ref.make[List[Rebalance]](List())
          c2RebalanceEvidence <- Ref.make[List[Rebalance]](List())
          _                   <- ZIO.effect(EmbeddedKafka.createCustomTopic(topic, partitions = 4))
          _                   <- produceMany(topic, (1 to nrMessagesPerBatch).toList.map(i => (s"key$i", s"msg$i")))
          _ <- {
            for {
              c1 <- Consumer.make[String, String](settings(groupId, "client1"))
              c2 <- Consumer.make[String, String](settings(groupId, "client2"))
            } yield (c1, c2)
          }.use {
            case (c1, c2) =>
              for {
                _ <- c1.subscribeWith(subscription)(
                      reb => log(s"rebalancing c1 with $reb") *> c1RebalanceEvidence.update(reb :: _).unit
                    )
                _ <- pollNtimes(20, c1)
                _ <- produceMany(topic, (1 to nrMessagesPerBatch).toList.map(i => (s"key$i", s"msg$i")))
                _ <- c2
                      .subscribeWith(subscription)(
                        reb => log(s"rebalancing c2 with $reb") *> c2RebalanceEvidence.update(reb :: _).unit
                      )
                      .delay(3.seconds)
                _             <- pollNtimes(20, c1) <&> pollNtimes(20, c2)
                eventsc2Final <- c2RebalanceEvidence.get.delay(3.seconds).map(_.reverse)
                eventsc1Final <- c1RebalanceEvidence.get.map(_.reverse)
                _             <- log(s"consumer2 rebalances: $eventsc2Final")
                _             <- log(s"consumer1 final rebalances: $eventsc1Final")
                _ <- ZIO.effect {
                      val List(Revoke(r1_1), Assign(a1_1), Revoke(r1_2), Assign(a1_2)) = eventsc1Final
                      val List(Revoke(r2_1), Assign(a2_1))                             = eventsc2Final

                      r1_1 shouldBe empty
                      a1_1.toSet shouldEqual (0 until 4).toSet.map(new TopicPartition(topic, _))
                      r1_2.toSet shouldEqual (0 until 4).toSet.map(new TopicPartition(topic, _))
                      a1_2.toSet shouldEqual (0 until 2).toSet.map(new TopicPartition(topic, _))

                      r2_1 shouldBe empty
                      a2_1.toSet shouldEqual (2 until 4).toSet.map(new TopicPartition(topic, _))
                    }
              } yield ()
          }
        } yield ()

        unsafeRun(test)
      }

      "and unsubscribe once subscribed" in runWithConsumer("group1", "clientId1") { consumer =>
        for {
          _       <- consumer.subscribe(Subscription.Topics(Set("topic1"))).either
          outcome <- consumer.unsubscribe.either
          _       <- ZIO.effect(outcome.isRight shouldBe true)
        } yield ()
      }
    }

    "poll" should {
      "receive messages produced on the topic" in runWithConsumer("group150", "client150") { consumer =>
        for {
          _       <- consumer.subscribe(Subscription.Topics(Set("topic150")))
          kvs     <- ZIO((1 to 5).toList.map(i => (s"key$i", s"msg$i")))
          _       <- produceMany("topic150", kvs)
          records <- pollNtimes(10, consumer)
          _ <- ZIO.effectTotal(records.map { r =>
                (r.key, r.value)
              } shouldEqual Chunk.fromIterable(kvs))
        } yield ()
      }
    }

    "pause and resume" should {
      "stop receiving messages on the subscribed topic during the pause, and then resume" in runWithConsumer(
        "group250",
        "client250"
      ) { consumer =>
        for {
          _        <- consumer.subscribe(Subscription.Topics(Set("topic250")))
          kv       <- ZIO((1 to 5).toList)
          _        <- ZIO.foreach(kv.map(i => (s"key$i", s"msg$i")))(i => produceOne("topic250", i._1, i._2))
          records1 <- pollNtimes(10, consumer)
          _        <- consumer.pause(Set(new TopicPartition("topic250", 0)))
          _        <- produceMany("topic250", kv.map(_ * 10).map(i => (s"key$i", s"msg$i")))
          records2 <- pollNtimes(10, consumer)
          _        <- consumer.resume(Set(new TopicPartition("topic250", 0)))
          records3 <- pollNtimes(10, consumer)
          _ <- ZIO.effectTotal {
                records1.map { r =>
                  (r.key, r.value)
                } shouldEqual Chunk.fromIterable(kv.map(i => (s"key$i", s"msg$i")))
                records2.map { r =>
                  (r.key, r.value)
                } shouldEqual Chunk.empty
                records3.map { r =>
                  (r.key, r.value)
                } shouldEqual Chunk.fromIterable(kv.map(_ * 10).map(i => (s"key$i", s"msg$i")))
              }
        } yield ()

      }
    }

    "seek" should {
      "seek to beginning" in runWithConsumer("group350", "client350") { consumer =>
        for {
          kv <- ZIO((1 to 5).toList)
          _  <- ZIO.foreach(kv.map(i => (s"key$i", s"msg$i")))(i => produceOne("topic350", i._1, i._2))
          _  <- consumer.subscribe(Subscription.Topics(Set("topic350")))
          _  <- consumer.poll(1.second).repeat(Schedule.spaced(1.second) && Schedule.recurs(2))

          _        <- consumer.seekToBeginning(Set(new TopicPartition("topic350", 0)))
          records1 <- pollNtimes(10, consumer)
          _ <- ZIO.effectTotal {
                records1.map { r =>
                  (r.key, r.value)
                } shouldEqual Chunk.fromIterable(kv.map(i => (s"key$i", s"msg$i")))
              }
        } yield ()
      }

      "seek to end" in runWithConsumer("group400", "client400") { consumer =>
        for {
          kv       <- ZIO((1 to 5).toList)
          _        <- ZIO.foreach(kv.map(i => (s"key$i", s"msg$i")))(i => produceOne("topic350", i._1, i._2))
          _        <- consumer.subscribe(Subscription.Topics(Set("topic350")))
          _        <- consumer.poll(1.second).repeat(Schedule.spaced(1.second) && Schedule.recurs(2))
          _        <- consumer.seekToEnd(Set(new TopicPartition("topic350", 0)))
          records1 <- pollNtimes(10, consumer)
          _ <- ZIO.effectTotal {
                records1 shouldEqual Chunk.empty
              }
        } yield ()
      }

      "seek to position" in runWithConsumer("group500", "client5000") { consumer =>
        for {
          kv <- ZIO((1 to 10).toList)
          _  <- ZIO.foreach(kv.map(i => (s"key$i", s"msg$i")))(i => produceOne("topic500", i._1, i._2))
          _  <- consumer.subscribe(Subscription.Topics(Set("topic500")))
          _  <- consumer.poll(1.second).repeat(Schedule.spaced(1.second) && Schedule.recurs(2))

          _        <- consumer.seek(new TopicPartition("topic500", 0), 5)
          records1 <- pollNtimes(10, consumer)
          _ <- ZIO.effectTotal {
                records1.map { r =>
                  (r.key, r.value)
                } shouldEqual Chunk.fromIterable(kv.drop(5).map(i => (s"key$i", s"msg$i")))
              }
        } yield ()
      }
    }

  }
}
