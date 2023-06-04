package zio.kafka.utils

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import zio.ZIO
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.{Kafka, KafkaRandom, KafkaTestUtils}
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

/**
 * This test checks the fix for the issue https://issues.apache.org/jira/browse/KAFKA-4090
 */
object SslHelperSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {

  override val kafkaPrefix: String = "oom-spec"

  override def spec: Spec[TestEnvironment, Any] =
    suite(".validateEndpoint")(
      test("Producer should fail due to ssl check") {
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
                ),
              ) &&
                hasField(
                  ".getCause.getMessage",
                  _.getCause.getMessage,
                  equalTo(
                    "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                  ),
                )
            )
          )
        )
      },
      test("Consumer should fail due to ssl check") {
        for {
          result <- (
                      for {
                        topic <- randomTopic
                        _     <- ZIO.serviceWithZIO[Consumer](
                                   _.consumeWith(
                                     Subscription.Topics(Set(topic)),
                                     Serde.byteArray,
                                     Serde.byteArray,
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
                ),
              ) &&
                hasField(
                  ".getCause.getMessage",
                  _.getCause.getMessage,
                  equalTo(
                    "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                  ),
                )
            )
          )
        )
      },
      test("Admin client should fail due to ssl check") {
        assertZIO(KafkaTestUtils.withAdmin(_.listTopics()).exit)(
          fails(
            isSubtype[KafkaException](
              hasField[KafkaException, String](
                ".getMessage",
                _.getMessage,
                equalTo(
                  "Failed to create new KafkaAdminClient"
                ),
              ) &&
                hasField(
                  ".getCause.getMessage",
                  _.getCause.getMessage,
                  equalTo(
                    "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                  ),
                )
            )
          )
        )
      },
    ).provideLayerShared(Kafka.sslEmbedded) @@ withLiveClock @@ sequential
}
