package zio.kafka.client

import zio.test._
import KafkaTestUtils._
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.TopicPartition
import zio.test.environment.Live
// import org.apache.kafka.common.TopicPartition
import zio._
import zio.duration._
import zio.kafka.client.serde.Serde
import MultiConsumerTestHelper._

object MultiConsumerTest
    extends DefaultRunnableSpec(
      suite("consumer test suit3 - parallel consumers")(
        testM("test parallel consumers") {
          for {
            _ <- makeTopic("topic1", 5)
            consumed = 0.until(5).map(i => MultiConsumerTestHelper.consumeN("topic1", "client1", i, 10))
            _ <- makeMany("topic1", 100)
            _ <- Live.live(ZIO.sleep(2.seconds))
            allConsuming <- ZIO.collectAllPar(consumed).fork
            results <- allConsuming.join
            topicInfo <- withAdmin { admin =>
              admin.describeTopics(List("topic1"))
            }
            allInfo <- topicInfo.completed
            infoHead = allInfo.head
            _ <- Live.live(console.putStrLn(s"${infoHead._1} ${infoHead._2}"))
            _ <- Live.live(console.putStrLn(results.flatten.map(_.toString).mkString("\n")))
          } yield {
            assertCompletes
          }
        }
      ).provideManagedShared(KafkaTestUtils.kafkaEnvironment)
    )

object MultiConsumerTestHelper {
  def makeTopic(name: String, nParts: Int) = withAdmin { admin =>
    for {
      _ <- admin.createTopic(new NewTopic(name, nParts, 1))
      _ <- ZIO.sleep(3.seconds)

    } yield ()
  }

  def consumeN(topic: String, groupId: String, consumerIndex: Int, nTakes: Int) =
    withConsumer(groupId, "client1") { consumer =>
      val tp = new TopicPartition(topic, consumerIndex)
      for {
        endOffsets <- consumer.endOffsets(Set(tp))
        eOff = endOffsets(tp)
        data <- if (eOff > 0L)
          consumer
            .subscribeAnd(Subscription.Topics(Set(topic)))
            .partitionedStream(Serde.string, Serde.string)
            .filter(_._1 == tp)
            .flatMap(_._2.flattenChunks)
            .take(nTakes)
            .runCollect
            .map(x => x.map { item => consumerIndex -> item })
        else IO.succeed(List.empty)
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