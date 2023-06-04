package zio.kafka.example

import zio._
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils.{consumer, produceMany, producer}
import zio.kafka.testkit._
import zio.test.Assertion.hasSameElements
import zio.test.TestAspect.{sequential, timeout}
import zio.test._

/**
 * Used to write documentation
 */
object ConsumerSpec extends ZIOSpecDefault with KafkaRandom {
  override def kafkaPrefix: String = "consumer-spec"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Consumer test suite")(
        test("minimal example") {
          val kvs: List[(String, String)] = (1 to 5).toList.map(i => (s"key-$i", s"msg-$i"))
          for {
            topic  <- randomTopic
            client <- randomClient
            group  <- randomGroup

            _ <- produceMany(topic, kvs) // Comes from `KafkaTestUtils`. Produces messages to the topic.

            records <- Consumer
                         .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                         .take(5)
                         .runCollect
                         .provideSome[Kafka](
                           // Comes from `KafkaTestUtils`
                           consumer(clientId = client, groupId = Some(group))
                         )
            consumed = records.map(r => (r.record.key, r.record.value)).toList
          } yield assert(consumed)(hasSameElements(kvs))
        }
      )
        .provideSome[Kafka](producer) // Here, we provide a new instance of Producer per test
        .provideSomeShared[Scope](Kafka.embedded) // Here, we provide an instance of Kafka for the entire suite
    ) @@ timeout(2.minutes) @@ sequential
}
