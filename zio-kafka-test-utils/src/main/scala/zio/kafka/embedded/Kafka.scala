package zio.kafka.embedded

import _root_.kafka.server.KafkaConfig
import io.github.embeddedkafka.EmbeddedKafkaConfig.defaultKafkaPort
import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.common.security.auth.SecurityProtocol
import zio._
import zio.kafka.KafkaTestUtils

import scala.util.control.NonFatal

trait Kafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}

final case class EmbeddedKafkaStartException(msg: String, cause: Throwable = null) extends RuntimeException(msg, cause)

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
    def embeddedKafkaConfig: EmbeddedKafkaConfig =
      EmbeddedKafkaConfig(
        kafkaPort = 0,
        zooKeeperPort = 0,
        customBrokerProperties = Map(
          "group.min.session.timeout.ms"     -> "500",
          "group.initial.rebalance.delay.ms" -> "0",
          "authorizer.class.name"            -> "kafka.security.authorizer.AclAuthorizer",
          "super.users"                      -> "User:ANONYMOUS"
        )
      )

    ZIO.acquireRelease(
      startKafka(embeddedKafkaConfig).map(EmbeddedKafkaService.apply)
    )(_.stop())
  }

  val saslEmbedded: ZLayer[Any, Throwable, Kafka.Sasl] = ZLayer.scoped {
    def embeddedKafkaConfig: EmbeddedKafkaConfig =
      EmbeddedKafkaConfig(
        kafkaPort = 0,
        zooKeeperPort = 0,
        customBrokerProperties = Map(
          "group.min.session.timeout.ms"         -> "500",
          "group.initial.rebalance.delay.ms"     -> "0",
          "authorizer.class.name"                -> "kafka.security.authorizer.AclAuthorizer",
          "sasl.enabled.mechanisms"              -> "PLAIN",
          "sasl.mechanism.inter.broker.protocol" -> "PLAIN",
          "inter.broker.listener.name"           -> "SASL_PLAINTEXT",
          "listeners"                            -> s"SASL_PLAINTEXT://localhost:$defaultKafkaPort",
          "advertised.listeners"                 -> s"SASL_PLAINTEXT://localhost:$defaultKafkaPort",
          "super.users"                          -> "User:admin",
          "listener.name.sasl_plaintext.plain.sasl.jaas.config" -> """org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret" user_admin="admin-secret" user_kafkabroker1="kafkabroker1-secret";"""
        )
      )

    ZIO.acquireRelease(
      startKafka(embeddedKafkaConfig).map(k => Kafka.Sasl(EmbeddedKafkaService(k)))
    )(_.value.stop())
  }

  val sslEmbedded: ZLayer[Any, Throwable, Kafka] = ZLayer.scoped {
    def embeddedKafkaConfig: EmbeddedKafkaConfig = {
      val listener = s"${SecurityProtocol.SSL}://localhost:$defaultKafkaPort"

      EmbeddedKafkaConfig(
        kafkaPort = 0,
        zooKeeperPort = 0,
        customBrokerProperties = Map(
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
          KafkaConfig.ListenersProp               -> listener,
          KafkaConfig.AdvertisedListenersProp     -> listener,
          KafkaConfig.ZkConnectionTimeoutMsProp   -> s"${30.second.toMillis}"
        )
      )
    }

    ZIO.acquireRelease(
      startKafka(embeddedKafkaConfig).map(EmbeddedKafkaService.apply)
    )(_.stop())
  }

  private def startKafka(
    makeConfig: => EmbeddedKafkaConfig
  ): Task[EmbeddedK] =
    ZIO.attemptBlocking {
      try EmbeddedKafka.start()(makeConfig)
      catch {
        case NonFatal(e) => throw EmbeddedKafkaStartException("Failed to start embedded Kafka", e)
      }
    }

  val local: ZLayer[Any, Nothing, Kafka] = ZLayer.succeed(DefaultLocal)
}
