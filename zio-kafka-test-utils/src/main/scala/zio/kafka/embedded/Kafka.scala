package zio.kafka.embedded

import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import zio._

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
      zooKeeperPort = 6000,
      kafkaPort = 6001,
      customBrokerProperties = Map("group.min.session.timeout.ms" -> "500", "group.initial.rebalance.delay.ms" -> "0")
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

  val local: ZLayer[Any, Nothing, Kafka] = ZLayer.succeed(DefaultLocal)
}
