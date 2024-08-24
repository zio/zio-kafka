package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.openjdk.jmh.annotations._
import zio.kafka.producer.Producer
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils.{ produceMany, producer }
import zio.{ ZIO, ZLayer }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ProducerBenchmark extends ZioBenchmark[Kafka with Producer] {
  val topic1                      = "topic1"
  val nrPartitions                = 6
  val nrMessages                  = 50000
  val kvs: List[(String, String)] = List.tabulate(nrMessages)(i => (s"key$i", s"msg$i"))

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer.make[Kafka with Producer](Kafka.embedded, producer).orDie

  override def initialize: ZIO[Kafka with Producer, Throwable, Any] = for {
    _ <- ZIO.succeed(EmbeddedKafka.deleteTopics(List(topic1))).ignore
    _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
  } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughput(): Any = runZIO {
    for {
      _ <- produceMany(topic1, kvs)
    } yield ()
  }
}
