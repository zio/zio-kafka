package zio.kafka.embedded

import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import zio._
import _root_.kafka.server.KafkaConfig
import io.github.embeddedkafka.EmbeddedKafkaConfig.defaultKafkaPort
import org.apache.kafka.common.security.auth.SecurityProtocol

import java.nio.file.Paths

trait Kafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}

object Kafka {

  final case class Sasl(value: Kafka) extends AnyVal

  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
  }

  case object DefaultLocal extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:9092")
    override def stop(): UIO[Unit]              = ZIO.unit
  }

  val embedded: ZLayer[Any, Throwable, Kafka] = ZLayer.scoped {
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      customBrokerProperties = Map(
        "group.min.session.timeout.ms"     -> "500",
        "group.initial.rebalance.delay.ms" -> "0",
        "authorizer.class.name"            -> "kafka.security.authorizer.AclAuthorizer",
        "super.users"                      -> "User:ANONYMOUS"
      )
    )
    ZIO.acquireRelease(ZIO.attempt(EmbeddedKafkaService(EmbeddedKafka.start())))(_.stop())
  }

  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl] = ZLayer.scoped {
    val kafkaPort = 6003
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      zooKeeperPort = 6002,
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
    ZIO.acquireRelease(ZIO.attempt(Kafka.Sasl(EmbeddedKafkaService(EmbeddedKafka.start()))))(_.value.stop())
  }

  val sslEmbedded: ZLayer[Any, Throwable, Kafka] = ZLayer.scoped {
    val listener = s"${SecurityProtocol.SSL}://localhost:$defaultKafkaPort"

    val keyStorePath   = Paths.get(Kafka.getClass.getResource("/keystore/kafka.keystore.jks").toURI).toFile
    val trustStorePath = Paths.get(Kafka.getClass.getResource("/truststore/kafka.truststore.jks").toURI).toFile

    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
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
        KafkaConfig.ListenersProp               -> listener,
        KafkaConfig.AdvertisedListenersProp     -> listener
      )
    )
    ZIO.acquireRelease(ZIO.attempt(EmbeddedKafkaService(EmbeddedKafka.start())))(_.stop())
  }

  val local: ZLayer[Any, Nothing, Kafka] = ZLayer.succeed(DefaultLocal)
}
