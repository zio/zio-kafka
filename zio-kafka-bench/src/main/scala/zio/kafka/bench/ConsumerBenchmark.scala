package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.openjdk.jmh.annotations._
import zio.kafka.KafkaTestUtils.{ consumer, produceMany, producer }
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.{ Consumer, Offset, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.{ durationInt, Ref, Schedule, ZIO, ZLayer }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ConsumerBenchmark extends ZioBenchmark[Kafka with Producer] {
  val topic1                      = "topic1"
  val nrPartitions                = 6
  val nrMessages                  = 50000
  val kvs: List[(String, String)] = List.tabulate(nrMessages)(i => (s"key$i", s"msg$i"))

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer.make[Kafka with Producer](Kafka.embedded, producer).orDie

  override def initialize: ZIO[Kafka with Producer, Throwable, Any] = for {
    _ <- ZIO.succeed(EmbeddedKafka.deleteTopics(List(topic1))).ignore
    _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
    _ <- produceMany(topic1, kvs)
  } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughput(): Any = runZIO {
    for {
      counter <- Ref.make(0)
      _ <- Consumer
             .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
             .tap { _ =>
               counter.updateAndGet(_ + 1).flatMap(count => Consumer.stopConsumption.when(count == nrMessages))
             }
             .runDrain
             .provideSome[Kafka](
               consumer(
                 randomThing("client"),
                 Some(randomThing("group")),
                 properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1000")
               )
             )
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughputWithCommits(): Any = runZIO {
    for {
      counter <- Ref.make(0)
      _ <- ZIO.logAnnotate("consumer", "1") {
             Consumer
               .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
               .map(_.offset)
               .aggregateAsyncWithin(ZSink.collectAll[Offset], Schedule.fixed(100.millis))
               .tap(batch => counter.update(_ + batch.size))
               .map(OffsetBatch.apply)
               .mapZIO(_.commit)
               .takeUntilZIO(_ => counter.get.map(_ >= nrMessages))
               .runDrain
               .provideSome[Kafka](
                 consumer(
                   randomThing("client"),
                   Some(randomThing("group")),
                   properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1000")
                 )
               )
           }
    } yield ()
  }
}
