package zio.kafka.bench

import org.openjdk.jmh.annotations._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.{ Consumer, Offset, OffsetBatch, Subscription }
import zio.kafka.diagnostics.Diagnostics
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils
import zio.stream.ZSink
import zio.{ Scope => ZScope, _ }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaConsumerBenchmark extends ConsumerZioBenchmark[Kafka] {

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka] =
    ZLayer.make[Kafka](Kafka.embedded).orDie

  override def initialize: ZIO[Kafka, Throwable, Any] =
    ZIO.scoped {
      for {
        adminClient <- KafkaTestUtils.makeAdminClient
        _           <- adminClient.deleteTopic(topic1).ignore
        _           <- adminClient.createTopic(NewTopic(topic1, partitionCount, replicationFactor = 1))
        producer    <- KafkaTestUtils.makeProducer
        _           <- KafkaTestUtils.produceMany(producer, topic1, kvs)
      } yield ()
    }

  private def makeConsumer: ZIO[ZScope & Kafka, Throwable, Consumer] =
    for {
      settings <- KafkaTestUtils
                    .consumerSettings(
                      randomThing("client"),
                      Some(randomThing("group")),
                      `max.poll.records` = 1000
                    )
                    .map(_.withPartitionPreFetchBufferLimit(8192))
      consumer <- Consumer.make(settings, Diagnostics.NoOp)
    } yield consumer

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughput(): Any = runZIO {
    for {
      counter  <- Ref.make(0)
      consumer <- makeConsumer
      _ <- consumer
             .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
             .chunks
             .tap { batch =>
               counter
                 .updateAndGet(_ + batch.size)
                 .flatMap(count => consumer.stopConsumption.when(count >= recordCount))
             }
             .runDrain
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughputWithCommits(): Any = runZIO {
    for {
      counter  <- Ref.make(0)
      consumer <- makeConsumer
      _ <- ZIO.logAnnotate("consumer", "1") {
             consumer
               .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
               .map(_.offset)
               .aggregateAsyncWithin(ZSink.collectAll[Offset], Schedule.fixed(100.millis))
               .tap(batch => counter.update(_ + batch.size))
               .map(OffsetBatch.apply)
               .mapZIO(_.commit)
               .takeUntilZIO(_ => counter.get.map(_ >= recordCount))
               .runDrain
           }
    } yield ()
  }
}
