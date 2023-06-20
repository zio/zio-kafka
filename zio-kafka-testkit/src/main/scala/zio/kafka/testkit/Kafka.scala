package zio.kafka.testkit

import _root_.kafka.server.KafkaConfig
import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import zio._

trait Kafka {
  def bootstrapServers: List[String]

  def stop(): UIO[Unit]
}

final case class EmbeddedKafkaStartException(msg: String, cause: Throwable = null) extends RuntimeException(msg, cause)

object Kafka {

  /**
   * Creates an in-memory Kafka instance with a random port.
   */
  val embedded: ZLayer[Any, Throwable, Kafka] = embeddedWith(_ => Map.empty)

  /**
   * Creates an in-memory Kafka instance with a random port.
   * @param customBrokerProps
   *   add/update broker properties
   */
  def embeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka] =
    embeddedWithBrokerProps(
      _ =>
        Map(
          "group.min.session.timeout.ms"     -> "500",
          "group.initial.rebalance.delay.ms" -> "0",
          "authorizer.class.name"            -> "kafka.security.authorizer.AclAuthorizer",
          "super.users"                      -> "User:ANONYMOUS"
        ),
      customBrokerProps
    )

  /**
   * Creates an in-memory Kafka instance with a random port and SASL authentication configured.
   */
  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl] = saslEmbeddedWith(_ => Map.empty)

  /**
   * Creates an in-memory Kafka instance with a random port and SASL authentication configured.
   * @param customBrokerProps
   *   add/update broker properties
   */
  def saslEmbeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka.Sasl] =
    embeddedWithBrokerProps(
      ports =>
        Map(
          "group.min.session.timeout.ms"         -> "500",
          "group.initial.rebalance.delay.ms"     -> "0",
          "authorizer.class.name"                -> "kafka.security.authorizer.AclAuthorizer",
          "sasl.enabled.mechanisms"              -> "PLAIN",
          "sasl.mechanism.inter.broker.protocol" -> "PLAIN",
          "inter.broker.listener.name"           -> "SASL_PLAINTEXT",
          "listeners"                            -> s"SASL_PLAINTEXT://localhost:${ports.kafkaPort}",
          "advertised.listeners"                 -> s"SASL_PLAINTEXT://localhost:${ports.kafkaPort}",
          "super.users"                          -> "User:admin",
          "listener.name.sasl_plaintext.plain.sasl.jaas.config" -> """org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret" user_admin="admin-secret" user_kafkabroker1="kafkabroker1-secret";"""
        ),
      customBrokerProps
    ).project(Sasl(_))

  /**
   * Creates an in-memory Kafka instance with a random port and SSL authentication configured.
   */
  val sslEmbedded: ZLayer[Any, Throwable, Kafka] = sslEmbeddedWith(_ => Map.empty)

  /**
   * reates an in-memory Kafka instance with a random port and SSL authentication configured.
   * @param customBrokerProps
   *   add/update broker properties
   */
  def sslEmbeddedWith(customBrokerProps: Ports => Map[String, String]): ZLayer[Any, Throwable, Kafka] =
    embeddedWithBrokerProps(
      ports =>
        Map(
          "group.min.session.timeout.ms"          -> "500",
          "group.initial.rebalance.delay.ms"      -> "0",
          "authorizer.class.name"                 -> "kafka.security.authorizer.AclAuthorizer",
          "super.users"                           -> "User:ANONYMOUS",
          "ssl.client.auth"                       -> "required",
          "ssl.enabled.protocols"                 -> "TLSv1.2",
          "ssl.truststore.type"                   -> "JKS",
          "ssl.keystore.type"                     -> "JKS",
          "ssl.truststore.location"               -> KafkaTestUtils.trustStoreFile.getAbsolutePath,
          "ssl.truststore.password"               -> "123456",
          "ssl.keystore.location"                 -> KafkaTestUtils.keyStoreFile.getAbsolutePath,
          "ssl.keystore.password"                 -> "123456",
          "ssl.key.password"                      -> "123456",
          KafkaConfig.InterBrokerListenerNameProp -> "SSL",
          KafkaConfig.ListenersProp               -> s"SSL://localhost:${ports.kafkaPort}",
          KafkaConfig.AdvertisedListenersProp     -> s"SSL://localhost:${ports.kafkaPort}",
          KafkaConfig.ZkConnectionTimeoutMsProp   -> s"${30.second.toMillis}"
        ),
      customBrokerProps
    )

  /**
   * Will connect to a Kafka instance running on localhost:9092 (with Docker, for example).
   */
  val local: ULayer[Kafka] = ZLayer.succeed(DefaultLocal)

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
                                ports.zookeeperPort,
                                brokerProps
                              )
        kafka <- ZIO.acquireRelease(
                   ZIO
                     .attemptBlocking(EmbeddedKafkaService(EmbeddedKafka.start()(embeddedKafkaConfig)))
                     .catchNonFatalOrDie { e =>
                       ZIO.fail(EmbeddedKafkaStartException("Failed to start embedded Kafka", e))
                     }
                 )(_.stop())
      } yield kafka
    }

  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
  }

  case object DefaultLocal extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:9092")
    override def stop(): UIO[Unit]              = ZIO.unit
  }

  final case class Ports(kafkaPort: Int, zookeeperPort: Int)

  private val ref = Ref.unsafe.make(Ports(6001, 7001))(Unsafe.unsafe)

  private val nextPorts: ZIO[Any, Nothing, Ports] =
    ref.getAndUpdate(ports => Ports(ports.kafkaPort + 1, ports.zookeeperPort + 1))
}
