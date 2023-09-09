package zio.kafka.utils

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.utils.Utils
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.admin.AdminClientSettings
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils.adminSettings
import zio.kafka.testkit.{ Kafka, KafkaRandom, KafkaTestUtils }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio.{ durationInt, Scope, Task, ZIO }

import java.nio.channels.SocketChannel

/**
 * This test checks the fix for the issue https://issues.apache.org/jira/browse/KAFKA-4090
 */
//noinspection SimplifyAssertInspection
object SslHelperSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {

  override val kafkaPrefix: String = "oom-spec"

  private val integrationTests =
    suite("Integration tests")(
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

  private val unitTests =
    suite("Unit tests")(
      test("fails if the passed list of servers is empty") {
        val result = SslHelper.validateEndpoint(Map.empty)

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
      suite("is resilient: if some Nodes, not all, are down that doesn't affect the result of the function")(
        test("succeeds if some of the nodes are down and the SSL configuration is correct") {
          val result: Task[Unit] =
            (
              for {
                settings <- adminSettings // Non-SSL settings used with a non-SSL cluster
                server0              = settings.bootstrapServers.head
                address0             = Utils.getHost(server0)
                port0                = Utils.getPort(server0)
                server1              = s"$address0:${port0 + 6000}"
                settingsWithDownNode = settings.withBootstrapServers(settings.bootstrapServers :+ server1)
                // We simulate that the Socket opening fails for some Nodes but not all
                result <- SslHelper.doValidateEndpoint { address =>
                            val isValidAddress = address.getPort == port0

                            if (isValidAddress) SocketChannel.open(address)
                            else throw new java.net.ConnectException("Connection refused")
                          }(settingsWithDownNode.properties)
              } yield result
            ).provide(Kafka.embedded)

          assertZIO(result.exit)(succeeds(anything))
        },
        test("fails if some of the nodes are down and the SSL configuration is incorrect") {
          val result: Task[Unit] =
            (
              for {
                settings <- adminSettings // Non-SSL settings used with an SSL-configured cluster
                server0              = settings.bootstrapServers.head
                address0             = Utils.getHost(server0)
                port0                = Utils.getPort(server0)
                server1              = s"$address0:${port0 + 6000}"
                settingsWithDownNode = settings.withBootstrapServers(settings.bootstrapServers :+ server1)
                // We simulate that the Socket opening fails for some Nodes but not all
                result <- SslHelper.doValidateEndpoint { address =>
                            val isValidAddress = address.getPort == port0

                            if (isValidAddress) SocketChannel.open(address)
                            else throw new java.net.ConnectException("Connection refused")
                          }(settingsWithDownNode.properties)
              } yield result
            ).provide(Kafka.sslEmbedded)

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
        },
        test("fails if all the nodes are down") {
          val result: Task[Unit] =
            for {
              _ <- ZIO.unit
              // We don't care about the bootstraps servers here as we'll simulate that all Nodes are down
              settings             = AdminClientSettings(List("localhost:9999"))
              server0              = settings.bootstrapServers.head
              address0             = Utils.getHost(server0)
              port0                = Utils.getPort(server0)
              server1              = s"$address0:${port0 + 6000}"
              settingsWithDownNode = settings.withBootstrapServers(settings.bootstrapServers :+ server1)
              // We simulate that the Socket opening always fails (ie. that all Nodes are down)
              result <-
                SslHelper.doValidateEndpoint(_ => throw new java.net.ConnectException("Connection refused. 💥!"))(
                  settingsWithDownNode.properties
                )
            } yield result

          assertZIO(result.exit)(
            fails(
              isSubtype[KafkaException](hasMessage(equalTo("Failed to create new KafkaAdminClient"))) &&
                hasThrowableCause(isSubtype[java.net.ConnectException](hasMessage(equalTo("Connection refused. 💥!"))))
            )
          )
        },
        suite("timeout mechanism")(
          test("The `SocketChannel` is closed when we timeout the `SocketChannel.open` call") {
            // Using a private IP address allows us to simulate a Node that is not reachable
            // See:
            //   - https://stackoverflow.com/questions/44929981/how-to-simulate-network-delay-on-a-local-port#comment76837344_44929981
            //   - https://stackoverflow.com/a/904609/2431728
            val host = "10.255.255.1"
            val port = "9999"
            val settings = AdminClientSettings(List(s"$host:$port"))
              .withProperty("request.timeout.ms", 1.second.toMillis.toString)

            val array = Array.fill[Any](3)(null)

            val result =
              SslHelper.doValidateEndpoint { address =>
                array(0) = s"Starting creating SocketChannel for address: $address"
                try {
                  val socket = SocketChannel.open(address) // blocking call
                  // This will never be called as the ZIO will be interrupted before
                  array(1) = s"Done creating SocketChannel for address: $address"
                  socket
                } catch {
                  case e: Throwable =>
                    array(2) = e
                    throw e
                }
              }(settings.properties)

            // custom assertion. More readable with a proper name.
            val hasNoMessage = not(hasMessage(anything))

            assertZIO(result.exit)(
              fails(
                isSubtype[KafkaException](hasMessage(equalTo("Failed to create new KafkaAdminClient"))) &&
                  hasThrowableCause(
                    isSubtype[java.util.concurrent.TimeoutException](
                      hasMessage(equalTo(s"Failed to contact /10.255.255.1:$port"))
                    )
                  )
              )
            ) &&
            assert(array(0))(equalTo("Starting creating SocketChannel for address: /10.255.255.1:9999")) &&
            assert(array(1))(isNull) &&
            assert(array(2))(isSubtype[java.nio.channels.ClosedByInterruptException](hasNoMessage))
          } @@ nonFlaky(5),
          test("The `SocketChannel` is closed when an exterior interruption is triggered") {
            // Using a private IP address allows us to simulate a Node that is not reachable
            // See:
            //   - https://stackoverflow.com/questions/44929981/how-to-simulate-network-delay-on-a-local-port#comment76837344_44929981
            //   - https://stackoverflow.com/a/904609/2431728
            val host = "10.255.255.1"
            val port = "9999"
            val settings = AdminClientSettings(List(s"$host:$port"))
              .withProperty("request.timeout.ms", 30.second.toMillis.toString)

            val array = Array.fill[Any](3)(null)

            val result =
              SslHelper.doValidateEndpoint { address =>
                array(0) = s"Starting creating SocketChannel for address: $address"
                try {
                  val socket = SocketChannel.open(address) // blocking call
                  // This will never be called as the ZIO will be interrupted before
                  array(1) = s"Done creating SocketChannel for address: $address"
                  socket
                } catch {
                  case e: Throwable =>
                    array(2) = e
                    throw e
                }
              }(settings.properties).fork.flatMap(fiber => fiber.interrupt.delay(1.seconds))

            // custom assertion. More readable with a proper name.
            val hasNoMessage = not(hasMessage(anything))

            assertZIO(result)(isInterrupted) &&
            assert(array(0))(equalTo("Starting creating SocketChannel for address: /10.255.255.1:9999")) &&
            assert(array(1))(isNull) &&
            assert(array(2))(isSubtype[java.nio.channels.ClosedByInterruptException](hasNoMessage))
          } @@ nonFlaky(5)
        )
      )
    )

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite(".validateEndpoint")(
      integrationTests,
      unitTests
    ) @@ withLiveClock @@ sequential

  implicit class SettingsHelper(adminClientSettings: AdminClientSettings) {
    def bootstrapServers: List[String] = adminClientSettings.properties
      .getOrElse(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "")
      .toString
      .split(",")
      .toList
  }
}
