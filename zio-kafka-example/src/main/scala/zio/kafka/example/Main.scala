package zio.kafka.example

import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import zio._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.{ Consumer, ConsumerSettings, Subscription }
import zio.kafka.serde.Serde

trait Kafka {
  def bootstrapServers: List[String]
  def stop(): UIO[Unit]
}

object Kafka {
  final case class EmbeddedKafkaService(embeddedK: EmbeddedK) extends Kafka {
    override def bootstrapServers: List[String] = List(s"localhost:${embeddedK.config.kafkaPort}")
    override def stop(): UIO[Unit]              = ZIO.succeed(embeddedK.stop(true))
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
}

object Main extends ZIOAppDefault {

  private val topic = "test-topic"

  private def consumerLayer(kafka: Kafka): ZLayer[Any, Throwable, Consumer] = {
    val consumerSettings =
      ConsumerSettings(kafka.bootstrapServers)
        .withPollTimeout(500.millis)
        .withGroupId("test")

    ZLayer.make[Consumer](
      ZLayer.succeed(consumerSettings),
      ZLayer.succeed(Diagnostics.NoOp),
      Consumer.live
    )
  }

  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    ZIO.addFinalizer(ZIO.logInfo("Stopping app")) *>
      (
        for {
          _     <- ZIO.logInfo(s"Starting app")
          kafka <- ZIO.service[Kafka]
          stream = Consumer
                     .plainStream(Subscription.topics(topic), Serde.string, Serde.string)
                     .provideLayer(consumerLayer(kafka))
          _        <- ZIO.logInfo(s"Consuming messages...")
          consumed <- stream.take(1000).tap(r => ZIO.logInfo(s"Consumed record $r")).runCount
          _        <- ZIO.logInfo(s"Consumed $consumed records")
        } yield ()
      ).provideSomeLayer[ZIOAppArgs with Scope](Kafka.embedded)

}
