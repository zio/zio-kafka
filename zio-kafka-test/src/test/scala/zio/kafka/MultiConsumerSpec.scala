package zio.kafka

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.KafkaTestUtils._
import zio.kafka.consumer._
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.ZStream
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object MultiConsumerSpec extends ZIOSpecWithKafka {
  override val kafkaPrefix: String = "consumespec"

  override def spec: Spec[TestEnvironment with Kafka, Throwable] =
    suite("MultiConsumer")(
      // TODO test all kinds of combinations of subscriptions
      test("consumes from two topic subscriptions") {
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
      },
      test("gives an error when attempting to consume using a manual subscription") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic1, kvs)
          _ <- produceMany(topic2, kvs)

          result <- MultiConsumer.make.flatMap { c =>
                      c
                        .plainStream(Subscription.Topics(Set(topic1)), Serde.string, Serde.string, 32)
                        .runCollect zipPar
                        c.plainStream(
                          Subscription.Manual(Set(new TopicPartition(topic2, 1))),
                          Serde.string,
                          Serde.string,
                          32
                        ).runCollect
                    }
                      .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
                      .unit
                      .exit
        } yield assert(result)(fails(isSubtype[InvalidSubscriptionUnion](anything)))
      },
      test("distributes records (randomly) from overlapping subscriptions over all subscribers") {
        val kvs = (1 to 500).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic1, kvs)
          _ <- produceMany(topic2, kvs)

          consumer1GotMessage <- Promise.make[Nothing, Unit]
          consumer2GotMessage <- Promise.make[Nothing, Unit]
          _ <- MultiConsumer.make.flatMap { c =>
                 c
                   .plainStream(Subscription.Topics(Set(topic1)), Serde.string, Serde.string, 32)
                   .tap(_ => consumer1GotMessage.succeed(()))
                   .merge(
                     c.plainStream(Subscription.Topics(Set(topic1)), Serde.string, Serde.string, 32)
                       .tap(_ => consumer2GotMessage.succeed(()))
                   )
                   .interruptWhen(consumer1GotMessage.await *> consumer2GotMessage.await)
                   .runCollect
               }
                 .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
        } yield assertCompletes
      },
      test("can handle unsubscribing during the lifetime of other streams") {
        val kvs = (1 to 50).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- produceMany(topic1, kvs)
          _ <- produceMany(topic2, kvs)

          _ <- MultiConsumer.make.flatMap { c =>
                 c
                   .plainStream(Subscription.Topics(Set(topic1)), Serde.string, Serde.string, 32)
                   .take(100)
                   .merge(
                     c.plainStream(Subscription.Topics(Set(topic2)), Serde.string, Serde.string, 32)
                       .take(50) *> ZStream.fromZIO(produceMany(topic1, kvs))
                   )
                   .runCollect
               }
                 .provideSomeLayer[Kafka with Scope with Producer](consumer(client, Some(group)))
        } yield assertCompletes
      }
    ).provideSomeLayerShared[TestEnvironment with Kafka](producer ++ Scope.default) @@
      withLiveClock @@ timeout(30.seconds)
}
