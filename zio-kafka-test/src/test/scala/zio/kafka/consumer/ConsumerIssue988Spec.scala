package zio.kafka.consumer

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils._
import zio.kafka.testkit.{Kafka, KafkaRandom}
import zio.test.TestAspect._
import zio.test._

//noinspection SimplifyAssertInspection
object ConsumerIssue988Spec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "consumespec2"

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("Consumer Streaming")(
      test("consumes from two topic subscriptions without duplicates") {
        def plainStream(topic: String) =
          Consumer.plainStream(Subscription.topics(topic), Serde.string, Serde.string)

        // .delay imitates long processing
        def readCount(topic: String)(counter: Ref[Int]) =
          plainStream(topic).debug("readCount msg").runForeach(_.offset.commit.delay(500.millis) *> counter.update(_ + 1))

        def readOne(topic: String) =
          plainStream(topic).take(1).runDrain *> ZIO.logInfo("readOne ready")

        def writeSingleTo(topics: String*) =
          ZIO.foreachParDiscard(topics) { topic =>
            ZIO.logInfo(s"Producing 1 message to $topic") *>
            produceOne(topic, s"key-$topic", s"value-$topic")
          }

        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          c = consumer(client, Some(group))

          counter <- Ref.make[Int](0)
          _ <- (
            readCount(topic1)(counter).fork *>
              (readOne(topic2) <&> writeSingleTo(topic1, topic2))
            )
            .zipPar(ZIO.sleep(3.seconds)) // give consumer some time to poll events
            .provideSome[Producer with Kafka](c)

          count <- counter.get
        } yield assertTrue(count == 1)
      } @@ withLiveClock,
    )
      .provideSome[Kafka](producer)
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ sequential @@ timeout(2.minutes)

}
