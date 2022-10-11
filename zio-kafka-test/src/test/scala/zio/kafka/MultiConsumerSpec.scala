package zio.kafka

import zio._
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer._
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.test.TestAspect._
import zio.test._

object MultiConsumerSpec extends ZIOSpecWithKafka {
  override val kafkaPrefix: String = "consumespec"

  override def spec: Spec[TestEnvironment with Kafka, Throwable] =
    suite("MultiConsumer")(
      // TODO test all kinds of combinations of subscriptions
      test("consumes from more than one topic") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic1, kvs)
          _ <- produceMany(topic2, kvs)

          records <- MultiConsumer.make.flatMap { c =>
                       c
                         .plainStream(Subscription.Topics(Set(topic1)), Serde.string, Serde.string, 32)
                         .take(5)
                         .runCollect zipPar
                         c.plainStream(Subscription.Topics(Set(topic2)), Serde.string, Serde.string, 32)
                           .take(5)
                           .runCollect
                     }
                       .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
          (records1, records2) = records
          kvOut1               = records1.map(r => (r.record.key, r.record.value)).toList
          kvOut2               = records2.map(r => (r.record.key, r.record.value)).toList
        } yield assertTrue(kvOut1 == kvs) &&
          assertTrue(kvOut2 == kvs) &&
          assertTrue(records1.map(_.record.topic()).forall(_ == topic1)) &&
          assertTrue(records2.map(_.record.topic()).forall(_ == topic2))
      }
    ).provideSomeLayerShared[TestEnvironment with Kafka](producer ++ Scope.default) @@
      withLiveClock @@ timeout(300.seconds)
}
