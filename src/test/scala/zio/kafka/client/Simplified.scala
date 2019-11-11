package zio.kafka.client

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.client.KafkaTestUtils._
import zio.kafka.client.serde.Serde
import zio.test.{DefaultRunnableSpec, assertCompletes, suite, testM}
import zio.duration._
import zio.kafka.client.MultiConsumerTestHelper._
import zio.test.environment.Live

object Simplified
  extends DefaultRunnableSpec(
    suite("consumer test suite3 - parallel consumers")(
/*      testM("test parallel consumers") {
        for {
          _ <- makeMany("topic1", 100)
          _ <- Live.live(ZIO.sleep(2.seconds))
          consumed = List(MultiConsumerTestHelper.consumeN("topic1", "client1", 0, 10))
          results <- ZIO.collectAllPar(consumed)
          _ <- Live.live(console.putStrLn(results.flatten.map(_.toString).mkString("\n")))
        } yield {
          assertCompletes
        }
      },*/
      testM("test single consumer") {
        for {
          _ <- makeMany("topic1", 100)
          _ <- Live.live(ZIO.sleep(2.seconds))
          results <- MultiConsumerTestHelper.consumeN("topic1", "client1", 0, 10)
          _ <- Live.live(console.putStrLn(results.map(_.toString).mkString("\n")))
        } yield {
          assertCompletes
        }
      },
/*
      testM("test parallel consumers") {
        for {
          _ <- makeMany("topic1", 100)
          _ <- Live.live(ZIO.sleep(2.seconds))
          consumed = MultiConsumerTestHelper.consumeN("topic1", "client1", 0, 10)
          fiber <- consumed.fork
          results <- fiber.join
          _ <- Live.live(console.putStrLn(results.map(_.toString).mkString("\n")))
        } yield {
          assertCompletes
        }
      },
*/
    ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
  )

object SimplifiedHelper {
  case class UsefulInfo(consumerIndex: Int, topic: String, partition: Int, offset: Long, key: String, value: String)

  def consumeN(topic: String, groupId: String, consumerIndex: Int, nTakes: Int) =
    withConsumer(groupId, "client1") { consumer =>
      val tp = new TopicPartition(topic, consumerIndex)
      for {

        data <- consumer
            .subscribeAnd(Subscription.Topics(Set(topic)))
            .partitionedStream(Serde.string, Serde.string)
            .filter(_._1 == tp)
            .flatMap(_._2.flattenChunks)
            .take(nTakes)
            .timeout(5.seconds)
            .runCollect
            .map(x => x.map { item => UsefulInfo(consumerIndex, item.record.topic, item.record.partition, item.offset.offset, item.record.key, item.record.value) })
      } yield data
    }

  def makeMany(topic: String, howMany: Int) = {
    val many = 1.to(howMany).map { i =>
      val k = i % 8
      (s"key-$k", s"value-$i")
    }
    produceMany(topic, many)
  }

}
