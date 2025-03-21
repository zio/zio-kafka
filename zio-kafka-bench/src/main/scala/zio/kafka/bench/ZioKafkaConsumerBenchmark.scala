package zio.kafka.bench

import org.openjdk.jmh.annotations._
import zio.{ Scope => _, _ }
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.consumer._
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.stream.ZSink

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaConsumerBenchmark extends ConsumerZioBenchmark[Kafka] {

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka] =
    Layers.embeddedKafka

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

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughput(): Any = runZIO {
    for {
      counter  <- Ref.make(0)
      consumer <- Layers.makeConsumer
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
      consumer <- Layers.makeConsumer
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
