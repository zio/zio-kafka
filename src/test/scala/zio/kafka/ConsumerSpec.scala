package zio.kafka.consumer

import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.KafkaTestUtils._
import zio.kafka.embedded.Kafka
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test.environment._
import zio.test.{ DefaultRunnableSpec, _ }

object ConsumerSpec extends DefaultRunnableSpec {
  override def spec: ZSpec[TestEnvironment, Throwable] =
    suite("Consumer")(
      testM("export metrics") {
        for {
          metrics <- Consumer.metrics
                      .provideSomeLayer[Kafka with Blocking with Clock](consumer("group1289", "client150"))
        } yield assert(metrics)(isNonEmpty)
      },
      testM("partitions for topic doesn't fail if doesn't exist") {
        for {
          topic  <- randomTopic
          group  <- randomGroup
          client <- randomThing("client")
          partitions <- Consumer
                         .partitionsFor(topic)
                         .provideSomeLayer[Kafka with Blocking with Clock](
                           consumer(group, client, allowAutoCreateTopics = false)
                         )
        } yield assert(partitions)(isEmpty)
      }
    ).provideSomeLayerShared[TestEnvironment](
      ((Kafka.embedded >>> stringProducer) ++ Kafka.embedded).mapError(TestFailure.fail) ++ Clock.live
    ) @@ timeout(180.seconds)
}
