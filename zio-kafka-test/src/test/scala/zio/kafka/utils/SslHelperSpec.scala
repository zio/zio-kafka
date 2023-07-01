package zio.kafka.utils

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.utils.Utils
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.{Consumer, Subscription}
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils.adminSettings
import zio.kafka.testkit.{Kafka, KafkaRandom, KafkaTestUtils}
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio.{Scope, ZIO}

import java.nio.channels.SocketChannel

/**
 * This test checks the fix for the issue https://issues.apache.org/jira/browse/KAFKA-4090
 */
object SslHelperSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {

  override val kafkaPrefix: String = "oom-spec"

  private val integrationTests =
    suite("Integration tests")(
      test("Producer should fail due to ssl check") {
        for {
          result <- (
            for {
              topic <- randomTopic
              _ <- Producer.produce(new ProducerRecord(topic, "boo", "baa"), Serde.string, Serde.string)
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
      test("Consumer should fail due to ssl check") {
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
      test("Admin client should fail due to ssl check") {
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
    ).provideLayerShared(Kafka.sslEmbedded)
  ,

  private val unitTests =
    suite("unit tests")(
      test("fails if the passed list of servers is empty") {
        val result = SslHelper.validateEndpoint(List.empty, Map.empty)

        assertZIO(result.exit)(
          fails(
            isSubtype[KafkaException](hasMessage(equalTo("Failed to create new KafkaAdminClient"))) &&
              hasThrowableCause(
                isSubtype[IllegalArgumentException](
                  hasMessage(equalTo("Empty bootstrapServers list"))
                )
              )
          )
        )
      },
      test("succeeds if some of the nodes are down and the configuration is correct") {
        val result =
          for {
            settings <- adminSettings
            server0 = settings.bootstrapServers.head
            address0 = Utils.getHost(server0)
            port0 = Utils.getPort(server0)
            server1 = s"$address0:${port0 + 6000}"
            settingsWithDownNode = settings.copy(bootstrapServers = settings.bootstrapServers :+ server1)
            // We simulate that the Socket opening fails for some addresses but not all
            result <- SslHelper.doValidateEndpoint { address =>
              val isValidAddress = address.getPort == port0

              if (isValidAddress) SocketChannel.open(address)
              else throw new java.net.ConnectException("Connection refused")
            }(settingsWithDownNode.bootstrapServers, settingsWithDownNode.properties)
          } yield result

        assertZIO(result.exit)(succeeds(anything))
      }.provideSomeLayer[Scope](Kafka.embedded),
      test("fails if one of the nodes is down and the configuration is incorrect") {
        val result =
          for {
            settings <- adminSettings
            server0 = settings.bootstrapServers.head
            address0 = Utils.getHost(server0)
            port0 = Utils.getPort(server0)
            server1 = s"$address0:${port0 + 6000}"
            settingsWithDownNode = settings.copy(bootstrapServers = settings.bootstrapServers :+ server1)
            // We simulate that the Socket opening fails for some addresses but not all
            result <- SslHelper.doValidateEndpoint { address =>
              val isValidAddress = address.getPort == port0

              if (isValidAddress) SocketChannel.open(address)
              else throw new java.net.ConnectException("Connection refused")
            }(settingsWithDownNode.bootstrapServers, settingsWithDownNode.properties)
          } yield result

        assertZIO(result.exit)(
          fails(
            isSubtype[KafkaException](hasMessage(equalTo("Failed to create new KafkaAdminClient"))) &&
              hasThrowableCause(
                isSubtype[IllegalArgumentException](
                  hasMessage(
                    equalTo(
                      "Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                    )
                  )
                )
              )
          )
        )
      }.provideSomeLayer[Scope](Kafka.sslEmbedded),
      test("fails if all the nodes are down") {
        val result =
          for {
            settings <- adminSettings
            server0 = settings.bootstrapServers.head
            address0 = Utils.getHost(server0)
            port0 = Utils.getPort(server0)
            server1 = s"$address0:${port0 + 6000}"
            settingsWithDownNode = settings.copy(bootstrapServers = settings.bootstrapServers :+ server1)
            // We simulate that the Socket opening always fails
            result <-
              SslHelper.doValidateEndpoint(_ => throw new java.net.ConnectException("Connection refused. 💥!"))(
                settingsWithDownNode.bootstrapServers,
                settingsWithDownNode.properties
              )
          } yield result

        assertZIO(result.exit)(
          fails(
            isSubtype[KafkaException](hasMessage(equalTo("Failed to create new KafkaAdminClient"))) &&
              hasThrowableCause(isSubtype[java.net.ConnectException](hasMessage(equalTo("Connection refused. 💥!"))))
          )
        )
      }.provideSomeLayer[Scope](Kafka.embedded)
    )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite(".validateEndpoint")(
      integrationTests,
      unitTests
    ) @@ withLiveClock @@ sequential
}
