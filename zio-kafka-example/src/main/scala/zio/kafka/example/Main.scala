package zio.kafka.example

import io.github.embeddedkafka.{ EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig }
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.consumer.Consumer.AutoOffsetStrategy
import zio.kafka.consumer.{ Consumer, ConsumerSettings, OffsetBatch, Subscription }
import zio.kafka.producer.{ Producer, ProducerSettings }
import zio.kafka.serde.Serde
import zio.logging.backend.SLF4J

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
        "authorizer.class.name"            -> "kafka.security.authorizer.AclAuthorizer",
        "super.users"                      -> "User:ANONYMOUS"
      )
    )
    ZIO.acquireRelease(ZIO.attempt(EmbeddedKafkaService(EmbeddedKafka.start())))(_.stop())
  }
}

object Main extends ZIOAppDefault {

  /**
   * See `zio-logging` documentation: https://zio.github.io/zio-logging/docs/overview/overview_slf4j
   */
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val topic = "test-topic"

  private val consumerLayer: ZLayer[MyKafka, Throwable, Consumer] =
    ZLayer.scoped {
      ZIO.serviceWithZIO[MyKafka] { kafka =>
        val consumerSettings =
          ConsumerSettings(kafka.bootstrapServers)
            .withGroupId("group1")
            .withOffsetRetrieval(Consumer.OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest))
        Consumer.make(consumerSettings)
      }
    }

  private val producerLayer: ZLayer[MyKafka, Throwable, Producer] =
    ZLayer.scoped {
      ZIO.serviceWithZIO[MyKafka] { kafka =>
        val producerSettings = ProducerSettings(kafka.bootstrapServers)
        Producer.make(producerSettings)
      }
    }

  override def run: ZIO[ZIOAppArgs with Scope, Any, Any] =
    ZIO.addFinalizer(ZIO.logInfo("Stopping app")) *>
      (
        for {
          _ <- ZIO.logInfo(s"Starting app")
          _ <- Producer.produceChunk(
                 Chunk.fromIterable(1 to 1000).map(n => new ProducerRecord(topic, n, n.toString)),
                 Serde.int,
                 Serde.string
               )
          _ <- Consumer
                 .plainStream(Subscription.topics(topic), Serde.int, Serde.string)
                 .take(100)
                 .groupedWithin(10, 100.millis)
                 .mapZIOPar(2)(c => ZIO.debug(c.size) as c.map(_.offset))
                 .map(OffsetBatch.apply)
                 .debug("Offset")
                 .mapZIO(_.commit)
                 .debug("Commit")
                 .runDrain
          _ <- ZIO.logInfo("Ready!")
        } yield ()
      ).provide(
        MyKafka.embedded,
        consumerLayer,
        producerLayer
      )
}
