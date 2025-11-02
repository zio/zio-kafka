package zio.kafka.example

import zio._
import zio.kafka.consumer.Subscription
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils
import zio.kafka.testkit._
import zio.test.Assertion.hasSameElements
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

/**
 * Used to write documentation
 */
object ConsumerSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Consumer test suite")(
        test("minimal example") {
          val kvs: List[(String, String)] = (1 to 5).toList.map(i => (s"key-$i", s"msg-$i"))
          for {
            topic  <- Random.nextUUID.map("topic-" + _.toString)
            client <- Random.nextUUID.map("client-" + _.toString)
            group  <- Random.nextUUID.map("group-" + _.toString)

            _ <- KafkaTestUtils.createCustomTopic(topic, partitionCount = 3)

            producer <- KafkaTestUtils.makeProducer
            _        <- KafkaTestUtils.produceMany(producer, topic, kvs) // Produces messages to the topic.

            consumer <- KafkaTestUtils.makeConsumer(clientId = client, groupId = Some(group))
            records  <- consumer
                         .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                         .take(5)
                         .runCollect
            consumed = records.map(r => (r.record.key, r.record.value)).toList
          } yield assert(consumed)(hasSameElements(kvs))
        }
      )
        .provideSomeShared[Scope](Kafka.embedded) // Provide an instance of Kafka for the entire suite
    ) @@ withLiveClock @@ timeout(2.minutes)
}
