package zio.kafka.embedded

import _root_.kafka.server.KafkaConfig
import io.github.embeddedkafka.EmbeddedKafkaConfig.{ defaultKafkaPort, defaultZookeeperPort }
import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.common.security.auth.SecurityProtocol
import zio._

import java.nio.file.Paths

trait Kafka {
  def bootstrapServers: List[String]
  def stop(): Task[Unit]
}

object Kafka {

  final case class Sasl(value: Kafka) extends AnyVal

  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): Task[Unit]             = ZIO.attemptBlocking(embeddedK.stop(true))
  }

  case object DefaultLocal extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:9092")
    override def stop(): Task[Unit]             = ZIO.unit
  }

  val embedded: ZLayer[Any, Throwable, Kafka] = ZLayer.scoped {
    def embeddedKafkaConfig(kafkaPort: Int, zooKeeperPort: Int): EmbeddedKafkaConfig =
      EmbeddedKafkaConfig(
        kafkaPort = kafkaPort,
        zooKeeperPort = zooKeeperPort,
        customBrokerProperties = Map(
          "group.min.session.timeout.ms"     -> "500",
          "group.initial.rebalance.delay.ms" -> "0",
          "authorizer.class.name"            -> "kafka.security.authorizer.AclAuthorizer",
          "super.users"                      -> "User:ANONYMOUS"
        )
      )

    ZIO.acquireRelease(
      ZIO.attemptBlocking(
        EmbeddedKafkaService(startKafka(defaultKafkaPort, defaultZookeeperPort, embeddedKafkaConfig))
      )
    )(_.stop().orDie)
  }

  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl] = ZLayer.scoped {
    def embeddedKafkaConfig(kafkaPort: Int, zookeeperPort: Int): EmbeddedKafkaConfig =
      EmbeddedKafkaConfig(
        zooKeeperPort = zookeeperPort,
        kafkaPort = kafkaPort,
        customBrokerProperties = Map(
          "group.min.session.timeout.ms"         -> "500",
          "group.initial.rebalance.delay.ms"     -> "0",
          "authorizer.class.name"                -> "kafka.security.authorizer.AclAuthorizer",
          "sasl.enabled.mechanisms"              -> "PLAIN",
          "sasl.mechanism.inter.broker.protocol" -> "PLAIN",
          "inter.broker.listener.name"           -> "SASL_PLAINTEXT",
          "listeners"                            -> s"SASL_PLAINTEXT://localhost:$kafkaPort",
          "advertised.listeners"                 -> s"SASL_PLAINTEXT://localhost:$kafkaPort",
          "super.users"                          -> "User:admin",
          "listener.name.sasl_plaintext.plain.sasl.jaas.config" -> """org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret" user_admin="admin-secret" user_kafkabroker1="kafkabroker1-secret";"""
        )
      )

    ZIO.acquireRelease(
      ZIO
        .attemptBlocking(
          Kafka.Sasl(EmbeddedKafkaService(startKafka(defaultKafkaPort, defaultZookeeperPort, embeddedKafkaConfig)))
        )
    )(_.value.stop().orDie)
  }

  val sslEmbedded: ZLayer[Any, Throwable, Kafka] = ZLayer.scoped {
    val keyStorePath   = Paths.get(Kafka.getClass.getResource("/keystore/kafka.keystore.jks").toURI).toFile
    val trustStorePath = Paths.get(Kafka.getClass.getResource("/truststore/kafka.truststore.jks").toURI).toFile

    def embeddedKafkaConfig(kafkaPort: Int, zookeeperPort: Int): EmbeddedKafkaConfig =
      EmbeddedKafkaConfig(
        zooKeeperPort = zookeeperPort,
        kafkaPort = kafkaPort,
        customBrokerProperties = Map(
          "group.min.session.timeout.ms"          -> "500",
          "group.initial.rebalance.delay.ms"      -> "0",
          "authorizer.class.name"                 -> "kafka.security.authorizer.AclAuthorizer",
          "super.users"                           -> "User:ANONYMOUS",
          "ssl.client.auth"                       -> "required",
          "ssl.enabled.protocols"                 -> "TLSv1.2",
          "ssl.truststore.type"                   -> "JKS",
          "ssl.keystore.type"                     -> "JKS",
          "ssl.truststore.location"               -> trustStorePath.getAbsolutePath,
          "ssl.truststore.password"               -> "123456",
          "ssl.keystore.location"                 -> keyStorePath.getAbsolutePath,
          "ssl.keystore.password"                 -> "123456",
          "ssl.key.password"                      -> "123456",
          KafkaConfig.InterBrokerListenerNameProp -> "SSL",
          KafkaConfig.ListenersProp               -> s"${SecurityProtocol.SSL}://localhost:$kafkaPort",
          KafkaConfig.AdvertisedListenersProp     -> s"${SecurityProtocol.SSL}://localhost:$kafkaPort"
        )
      )

    ZIO.acquireRelease(
      ZIO.attemptBlocking(
        EmbeddedKafkaService(startKafka(defaultKafkaPort, defaultZookeeperPort, embeddedKafkaConfig))
      )
    )(_.stop().orDie)
  }

  private def startKafka(
    kafkaPort: Int,
    zooKeeperPort: Int,
    makeConfig: (Int, Int) => EmbeddedKafkaConfig,
    maxRetry: Int = 100
  ): EmbeddedK = {
    def retry(e: Exception): EmbeddedK =
      if (maxRetry <= 0) throw new RuntimeException("Failed to start EmbeddedKafka", e)
      else startKafka(kafkaPort + 1, zooKeeperPort + 1, makeConfig, maxRetry - 1)

    try
      EmbeddedKafka.start()(makeConfig(kafkaPort, zooKeeperPort))
    catch {
      case e: org.apache.kafka.common.KafkaException
          if e.getCause != null && e.getMessage != null && e.getMessage.contains("Socket server failed to bind to") =>
        retry(e)
      case e: java.net.BindException =>
        retry(e)
    }
  }

  val local: ZLayer[Any, Nothing, Kafka] = ZLayer.succeed(DefaultLocal)
}
