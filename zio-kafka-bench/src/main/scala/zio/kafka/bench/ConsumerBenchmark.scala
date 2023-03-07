package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.runner.{ Runner, RunnerException }
import zio.kafka.KafkaTestUtils.{ consumer, produceMany, producer }
import zio.kafka.bench.ZioBenchmark.randomThing
import zio.kafka.consumer.{ Consumer, Offset, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.{ &, durationInt, Ref, Schedule, ZIO, ZLayer }

object ConsumerBenchmark {
  @throws[RunnerException]
  def main(args: Array[String]): Unit = {
    val opt = new OptionsBuilder().include(classOf[ConsumerBenchmark].getSimpleName).forks(1).build
    new Runner(opt).run
    ()
  }
}

@State(Scope.Benchmark)
@Fork(0)
class ConsumerBenchmark extends ZioBenchmark[Kafka & Producer] {
  val topic1       = "topic1"
  val nrPartitions = 6
  val nrMessages   = 50000
  val kvs          = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka & Producer] =
    ZLayer.make[Kafka & Producer](Kafka.embedded, producer).orDie

  override def initialize: ZIO[Kafka & Producer, Throwable, Any] = for {
    _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
    _ <- produceMany(topic1, kvs)
  } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughput(): Any = runZIO {
    for {
      client <- randomThing("client")
      group  <- randomThing("group")

      _ <- Consumer
             .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
             .take(nrMessages.toLong)
             .runDrain
             .provideSome[Kafka](
               consumer(
                 client,
                 Some(group),
                 properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1000")
               )
             )
             .timeoutFail(new RuntimeException("Timeout"))(30.seconds)
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughputWithCommits(): Any = runZIO {
    for {
      client <- randomThing("client")
      group  <- randomThing("group")

      counter <- Ref.make(0)
      _ <- ZIO.logAnnotate("consumer", "1") {
             Consumer
               .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
               .map(_.offset)
               .tap(_ => counter.update(_ + 1))
               .aggregateAsyncWithin(ZSink.collectAll[Offset], Schedule.spaced(100.millis))
               .map(OffsetBatch.apply)
               .mapZIO(_.commit)
               .takeUntilZIO(_ => counter.get.map(_ == nrMessages))
               .runDrain
               .provideSome[Kafka](
                 consumer(
                   client,
                   Some(group),
                   properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1000")
                 )
               )
               .timeoutFail(new RuntimeException("Timeout"))(30.seconds)
           }
    } yield ()
  }
}
