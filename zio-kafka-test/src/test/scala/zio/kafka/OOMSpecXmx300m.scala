package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import zio.ZIO
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

/**
 * This test checks the fix for the issue https://issues.apache.org/jira/browse/KAFKA-4090
 */
object OOMSpecXmx300m extends ZIOSpecDefault with KafkaRandom {

  override val kafkaPrefix: String = "oom-spec"

  override def spec: Spec[TestEnvironment, Any] =
    suite("OOM check")(
      test("producer should fail with ssl check") {
        for {
          result <- (
                      for {
                        topic <- randomTopic
                        _     <- Producer.produce(new ProducerRecord(topic, "boo", "baa"), Serde.string, Serde.string)
                      } yield ()
                    ).provideSomeLayer(KafkaTestUtils.producer).exit
        } yield assert(result)(
          fails(
            isSubtype[KafkaException](
              hasField[KafkaException, String](
                ".getMessage",
                _.getMessage,
                equalTo(
                  "Failed to create new KafkaAdminClient"
                )
              ) &&
                hasField(
                  ".getCause.getMessage",
                  _.getCause.getMessage,
                  equalTo(
                    "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                  )
                )
            )
          )
        )
      },
      test("consumer should fail with ssl check") {
        for {
          result <- (
                      for {
                        topic <- randomTopic
                        _ <- ZIO.serviceWithZIO[Consumer](
                               _.consumeWith(
                                 Subscription.Topics(Set(topic)),
                                 Serde.byteArray,
                                 Serde.byteArray
                               )(_ => ZIO.unit)
                             )
                      } yield ()
                    ).provideSomeLayer(KafkaTestUtils.consumer(clientId = "test", groupId = Some("test"))).exit
        } yield assert(result)(
          fails(
            isSubtype[KafkaException](
              hasField[KafkaException, String](
                ".getMessage",
                _.getMessage,
                equalTo(
                  "Failed to create new KafkaAdminClient"
                )
              ) &&
                hasField(
                  ".getCause.getMessage",
                  _.getCause.getMessage,
                  equalTo(
                    "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                  )
                )
            )
          )
        )
      },
      test("admin client should fail with ssl check") {
        assertZIO(KafkaTestUtils.withAdmin(_.listTopics()).exit)(
          fails(
            isSubtype[KafkaException](
              hasField[KafkaException, String](
                ".getMessage",
                _.getMessage,
                equalTo(
                  "Failed to create new KafkaAdminClient"
                )
              ) &&
                hasField(
                  ".getCause.getMessage",
                  _.getCause.getMessage,
                  equalTo(
                    "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                  )
                )
            )
          )
        )
      }
    ).provideLayerShared(Kafka.sslEmbedded) @@ withLiveClock @@ sequential
}
