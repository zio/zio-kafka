package zio.kafka.client

import zio.test._
import KafkaTestUtils._
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.TopicPartition
import zio.test.environment.Live
import zio._
import zio.duration._
import zio.kafka.client.serde.Serde

object MultiConsumerTestHelper {
  def makeTopic(name: String, nParts: Int) = withAdmin { admin =>
    for {
      _ <- admin.createTopic(new NewTopic(name, nParts, 1))
      _ <- ZIO.sleep(3.seconds)
    } yield ()
  }

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
            .runCollect
            .map(x => x.map { item => UsefulInfo(consumerIndex, item.record.topic, item.record.partition, item.offset.offset, item.record.key, item.record.value) })
      } yield data
    }

  def makeMany(topic: String, howMany: Int) = {
    val many = 1.to(howMany).map { i =>
      val k = i // % 8
      (s"key-$k", s"value-$i")
    }
    produceMany(topic, many)
  }

    val test1 = testM("test multiple consumers") {
      for {
        topic <- randomTopic
        consumerGroupId <- randomGroup
        _ <- makeTopic(topic, 5)
        _ <- makeMany(topic, 1000)
        _ <- Live.live(ZIO.sleep(2.seconds))
        consumed = 0.to(4).map(i => MultiConsumerTestHelper.consumeN(topic, consumerGroupId, i, 3))
        _ <- ZIO.collectAll(consumed)
      } yield {
        assertCompletes
      }
    }
    val test2 = testM("test parallel consumers") {
      for {
        topic <- randomTopic
        consumerGroupId <- randomGroup
        _ <- makeTopic(topic, 5)
        _ <- makeMany(topic, 1000)
        _ <- Live.live(ZIO.sleep(2.seconds))
        consumed = 0.to(4).map(i => MultiConsumerTestHelper.consumeN(topic, consumerGroupId, i, 3))
        _ <- ZIO.collectAllPar(consumed)
      } yield {
        assertCompletes
      }
    }
    val test3 = testM("test lots of stuff") {
      for {
        topic <- randomTopic
        consumerGroupId <- randomGroup
        _ <- makeMany(topic, 100000)
        _ <- Live.live(ZIO.sleep(2.seconds))
        results <- MultiConsumerTestHelper.consumeN(topic, consumerGroupId, 0, 100000)
        _ <- Live.live(console.putStrLn(s"${results.size} processed"))
      } yield {
        assertCompletes
      }
    }


}