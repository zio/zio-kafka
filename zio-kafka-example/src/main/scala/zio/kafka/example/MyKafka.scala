package zio.kafka.example

import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import zio._

trait MyKafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}

object MyKafka {
  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends MyKafka {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
  }

  val embedded: ZLayer[Any, Throwable, MyKafka] = ZLayer.scoped {
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      customBrokerProperties = Map(
        "group.min.session.timeout.ms"     -> "500",
        "group.initial.rebalance.delay.ms" -> "0",
        "authorizer.class.name"            -> "org.apache.kafka.metadata.authorizer.StandardAuthorizer",
        "super.users"                      -> "User:ANONYMOUS"
      )
    )
    ZIO.acquireRelease(ZIO.attempt(EmbeddedKafkaService(EmbeddedKafka.start())))(_.stop())
  }
}
