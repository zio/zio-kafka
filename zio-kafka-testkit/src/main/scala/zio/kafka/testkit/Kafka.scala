package zio.kafka.testkit

import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import zio._

import java.io.IOException
import java.net.ServerSocket

/**
 * A Kafka broker that tests can connect to. Get one from the layers in the companion object: [[Kafka.embedded]] starts
 * an embedded broker, and [[Kafka.local]] uses a broker that is already running on `localhost:9092`.
 *
 * {{{
 * import zio._
 * import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
 *
 * val producedOffset: Task[Long] =
 *   ZIO
 *     .scoped {
 *       for {
 *         producer <- KafkaTestUtils.makeProducer
 *         metadata <- KafkaTestUtils.produceOne(producer, "my-topic", "key", "value")
 *       } yield metadata.offset()
 *     }
 *     .provide(Kafka.embedded)
 * }}}
 *
 * @see
 *   [[https://zio.dev/zio-kafka/writing-tests/ Test Producers and Consumers with the Test Kit]]
 */
trait Kafka {

  /** The bootstrap servers of this Kafka broker, to put in the settings of a producer, consumer or admin client. */
  def bootstrapServers: List[String]

  /** Stops this Kafka broker. */
  def stop(): UIO[Unit]
}

/**
 * The failure of the layers that start an embedded Kafka broker, when starting the broker fails.
 *
 * @param msg
 *   the message of the exception
 * @param cause
 *   the exception that made starting the broker fail
 */
final case class EmbeddedKafkaStartException(msg: String, cause: Throwable = null) extends RuntimeException(msg, cause)

/**
 * Layers that provide a [[Kafka]] broker for tests.
 */
object Kafka {

  /**
   * Starts an embedded Kafka broker in the current JVM, on a random port. The broker is stopped when the layer's scope
   * closes.
   */
  val embedded: ZLayer[Any, Throwable, Kafka] = embeddedWith(_ => Map.empty)

  /**
   * Starts an embedded Kafka broker in the current JVM, on a random port. The broker is stopped when the layer's scope
   * closes.
   *
   * @param customBrokerProps
   *   add/update broker properties. The function gets the ports of the broker, and its properties replace the
   *   properties of the same name that the layer sets itself.
   */
  def embeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka] =
    embeddedWithBrokerProps(
      _ =>
        Map(
          "group.min.session.timeout.ms"     -> "500",
          "group.initial.rebalance.delay.ms" -> "0",
          "authorizer.class.name"            -> "org.apache.kafka.metadata.authorizer.StandardAuthorizer",
          "super.users"                      -> "User:ANONYMOUS"
        ),
      customBrokerProps
    )

  /**
   * Starts an embedded Kafka broker in the current JVM, on a random port, with SASL authentication configured. The
   * broker is stopped when the layer's scope closes.
   */
  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl] = saslEmbeddedWith(_ => Map.empty)

  /**
   * Starts an embedded Kafka broker in the current JVM, on a random port, with SASL authentication configured. The
   * broker is stopped when the layer's scope closes.
   *
   * @param customBrokerProps
   *   add/update broker properties. The function gets the ports of the broker, and its properties replace the
   *   properties of the same name that the layer sets itself.
   */
  def saslEmbeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka.Sasl] =
    embeddedWithBrokerProps(
      ports => {
        val brokerListener     = s"BROKER://localhost:${ports.kafkaPort}"
        val controllerListener = s"CONTROLLER://localhost:${ports.controllerPort}"
        Map(
          "group.min.session.timeout.ms"         -> "500",
          "group.initial.rebalance.delay.ms"     -> "0",
          "listeners"                            -> s"$brokerListener,$controllerListener",
          "advertised.listeners"                 -> brokerListener,
          "listener.security.protocol.map"       -> "BROKER:SASL_PLAINTEXT,CONTROLLER:SASL_PLAINTEXT",
          "inter.broker.listener.name"           -> "BROKER",
          "controller.listener.names"            -> "CONTROLLER",
          "sasl.enabled.mechanisms"              -> "PLAIN",
          "sasl.mechanism.controller.protocol"   -> "PLAIN",
          "sasl.mechanism.inter.broker.protocol" -> "PLAIN",
          "authorizer.class.name"                -> "org.apache.kafka.metadata.authorizer.StandardAuthorizer",
          "allow.everyone.if.no.acl.found"       -> "false",
          "super.users"                          -> "User:admin",
          "listener.name.broker.plain.sasl.jaas.config" ->
            ("""org.apache.kafka.common.security.plain.PlainLoginModule required """ +
              """username="admin" password="admin-secret" """ +
              """user_admin="admin-secret" """ +
              """user_kafkabroker1="kafkabroker1-secret";"""),
          "listener.name.controller.plain.sasl.jaas.config" ->
            ("""org.apache.kafka.common.security.plain.PlainLoginModule required """ +
              """username="admin" password="admin-secret" """ +
              """user_admin="admin-secret";""")
        )
      },
      customBrokerProps
    ).project(Sasl(_))

  /**
   * Starts an embedded Kafka broker in the current JVM, on a random port, with SSL configured. The broker is stopped
   * when the layer's scope closes. See [[sslEmbeddedWith]] for what the broker can be used for.
   */
  val sslEmbedded: ZLayer[Any, Throwable, Kafka] = sslEmbeddedWith(_ => Map.empty)

  /**
   * Starts an embedded Kafka broker in the current JVM, on a random port, with SSL configured. The broker is stopped
   * when the layer's scope closes.
   *
   * ⚠️ This does not create an actually working broker since authentication is not configured correctly. It is however
   * sufficient to test the SSL check that zio-kafka does before connecting to a broker with plain-text settings.
   *
   * @param customBrokerProps
   *   add/update broker properties. The function gets the ports of the broker, and its properties replace the
   *   properties of the same name that the layer sets itself.
   */
  def sslEmbeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka] =
    embeddedWithBrokerProps(
      ports => {
        val brokerListener     = s"BROKER://localhost:${ports.kafkaPort}"
        val controllerListener = s"CONTROLLER://localhost:${ports.controllerPort}"
        Map(
          "group.min.session.timeout.ms"     -> "500",
          "group.initial.rebalance.delay.ms" -> "0",
          "listeners"                        -> s"$brokerListener,$controllerListener",
          "advertised.listeners"             -> brokerListener,
          "listener.security.protocol.map"   -> "BROKER:SSL,CONTROLLER:PLAINTEXT",
          "inter.broker.listener.name"       -> "BROKER",
          "controller.listener.names"        -> "CONTROLLER",
          "authorizer.class.name"            -> "org.apache.kafka.metadata.authorizer.StandardAuthorizer",
          "super.users"                      -> "User:ANONYMOUS",
          "ssl.client.auth"                  -> "required",
          "ssl.enabled.protocols"            -> "TLSv1.2,TLSv1.3",
          "ssl.truststore.type"              -> "JKS",
          "ssl.keystore.type"                -> "JKS",
          "ssl.truststore.location"          -> KafkaTestUtils.trustStoreFile.getAbsolutePath,
          "ssl.truststore.password"          -> "123456",
          "ssl.keystore.location"            -> KafkaTestUtils.keyStoreFile.getAbsolutePath,
          "ssl.keystore.password"            -> "123456",
          "ssl.key.password"                 -> "123456"
        )
      },
      customBrokerProps
    )

  /**
   * Will connect to a Kafka instance running on localhost:9092 (with Docker, for example).
   */
  val local: ULayer[Kafka] = ZLayer.succeed(DefaultLocal)

  /**
   * A [[Kafka]] broker that has SASL authentication configured. It is a separate type so that tests can ask for it in
   * the environment, next to a plain [[Kafka]].
   *
   * @param value
   *   the Kafka broker
   */
  final case class Sasl(value: Kafka) extends AnyVal

  private def embeddedWithBrokerProps(
    presetProps: Ports => Map[String, String],
    customProps: Ports => Map[String, String]
  ): ZLayer[Any, Throwable, Kafka] =
    ZLayer.scoped {
      for {
        ports <- nextPorts
        brokerProps = presetProps(ports) ++ customProps(ports) // custom is after to allow overriding
        embeddedKafkaConfig = EmbeddedKafkaConfig(
                                ports.kafkaPort,
                                ports.controllerPort,
                                brokerProps
                              )
        kafka <- ZIO.acquireRelease(
                   ZIO
                     .attemptBlocking(EmbeddedKafkaService(EmbeddedKafka.start()(embeddedKafkaConfig)))
                     .catchAll { e =>
                       ZIO.fail(EmbeddedKafkaStartException("Failed to start embedded Kafka", e))
                     }
                 )(_.stop())
      } yield kafka
    }

  /**
   * A [[Kafka]] that wraps an embedded Kafka broker. Its bootstrap server is `localhost` on the broker's port.
   *
   * @param embeddedK
   *   the embedded broker
   */
  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
  }

  /**
   * A [[Kafka]] for a broker that runs on `localhost:9092`. Its `stop` does nothing.
   */
  case object DefaultLocal extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:9092")
    override def stop(): UIO[Unit]              = ZIO.unit
  }

  /**
   * The ports of an embedded Kafka broker.
   *
   * @param kafkaPort
   *   the port that clients connect to
   * @param controllerPort
   *   the port of the broker's controller
   */
  final case class Ports(kafkaPort: Int, controllerPort: Int)

  private val ref = {
    val p1 = findNextFreePort(2000 + scala.util.Random.nextInt(40000))
    val p2 = findNextFreePort(p1 + 1)
    Ref.unsafe.make(Ports(p1, p2))(Unsafe.unsafe)
  }

  private val nextPorts: ZIO[Any, Nothing, Ports] =
    ref.updateAndGet { ports =>
      val p1 = findNextFreePort(ports.controllerPort + 1)
      val p2 = findNextFreePort(p1 + 1)
      Ports(p1, p2)
    }

  private def findNextFreePort(searchFrom: Int): Int =
    Iterator
      .from(searchFrom)
      .find { attemptPort =>
        try {
          val s = new ServerSocket(attemptPort)
          s.setReuseAddress(true)
          s.close()
          true
        } catch {
          case _: IOException => false
        }
      }
      .getOrElse(throw new AssertionError("Could not get a free port!"))
}
