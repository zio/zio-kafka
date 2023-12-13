package zio.kafka.example2

import org.apache.kafka.clients.consumer.ConsumerConfig
import zio.kafka.consumer.{Consumer, ConsumerSettings, RebalanceListener, Subscription}
import zio.kafka.example.MyKafka
import zio.kafka.serde.Serde
import zio._
import zio.kafka.consumer.diagnostics.{DiagnosticEvent, Diagnostics}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.logging.backend.SLF4J
import zio.stream.ZStream

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val TOPIC = "topic1"

  def consumer: ZIO[Consumer with Scope, Throwable, Unit] = for {
    stop <- Promise.make[Nothing, Unit]
    c <- (
      ZStream.finalizer(ZIO.logInfo("Consumer finalizing")) *>
          ZStream.unwrap {
              ZIO.succeed {
                  Consumer
                    .plainStream(Subscription.topics(TOPIC), Serde.string, Serde.string)
                }
                .interruptible
            }
            //  .take(50)
            .chunksWith(_.takeUntilZIO(_ => stop.isDone))
            .tap(r => ZIO.logInfo(s"${r.value} p:${r.offset.partition}, o:${r.offset.offset}"))
            .mapZIO(ZIO.succeed(_).delay(100.millis))
            .map(_.offset)
            .aggregateAsync(Consumer.offsetBatches)
            .tap(offsets => ZIO.logInfo(s"Going to commit: ${offsets.offsets.map { case (k, v) => s"${k.partition()}: ${v.offset()}" }}"))
            .mapZIO(_.commit)
        )
        .runDrain
        .zipLeft(ZIO.logInfo("Consumer finalized"))
        .uninterruptible <& ZIO.never.onInterrupt(ZIO.logInfo("Stopping the consumer") *> stop.succeed(()))
  } yield c

  def consumerLayer: ZLayer[MyKafka, Throwable, Consumer] = ZLayer.scoped {
    for {
      bootstrapServers <- ZIO.serviceWith[MyKafka](_.bootstrapServers)
      value: Map[String, AnyRef] = Map[String, AnyRef](
        ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG -> "60000",
        ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG -> "250"
      )
      c <- Consumer.make(
        ConsumerSettings(bootstrapServers)
          .withGroupId("tester")
          .withMaxPollRecords(10)
          .withRebalanceSafeCommits(true)
          .withMaxRebalanceDuration(4.seconds)
          .withoutPartitionPreFetching
          .withOffsetRetrieval(
            Consumer.OffsetRetrieval.Auto(Consumer.AutoOffsetStrategy.Earliest)
          )
          .withProperties(value)
          .withRebalanceListener(RebalanceListener.apply(
            (assigned, _) => {
              ZIO.logInfo(s"Assigned Partitions: ${assigned.map(_.partition())}")
            },
            (revoked, _) => {
              ZIO.logInfo(s"Revoked Partitions: ${revoked.map(_.partition())}")
            }
          )),
        diagnostics = new Diagnostics {
          override def emit(event: => DiagnosticEvent): UIO[Unit] = ZIO.logDebug("Diagnostics: " + event)
        }
      )
      _ <- ZIO.addFinalizer(ZIO.logDebug("-------- Consumer layer shutdown started --------"))
    } yield c
  }

  def producerLayer = ZLayer.scoped {
    for {
      bootstrapServers <- ZIO.serviceWith[MyKafka](_.bootstrapServers)
      p <- Producer.make(ProducerSettings(bootstrapServers))
    } yield p
  }

  override def run = {
    (for {
      _ <- ZIO.addFinalizer(ZIO.logDebug("-------- Main shutdown started --------"))
      _ <- ZIO.logInfo("Starting")
      _ <- ZIO.serviceWithZIO[Producer](_.produce(TOPIC, null, "hello world", Serde.string, Serde.string)).schedule(Schedule.spaced(10.millis)).forkDaemon
      _ <- consumer.timeout(5.seconds)
//      f <- consumer.fork
//      _ <- f.interrupt.delay(5.seconds)
//      _ <- f.join
      _ <- ZIO.logInfo("DONE")
    } yield ())
      .provideSome[Scope](
        MyKafka.embedded,
        producerLayer,
        consumerLayer
      )
  }
}
