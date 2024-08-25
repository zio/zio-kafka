package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.producer.ProducerRecord
import org.openjdk.jmh.annotations._
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils.producer
import zio.{ Chunk, ZIO, ZLayer }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ProducerBenchmark extends ZioBenchmark[Kafka with Producer] {
  val topic1                      = "topic1"
  val nrPartitions                = 6
  val nrMessages                  = 500
  val kvs: List[(String, String)] = List.tabulate(nrMessages)(i => (s"key$i", s"msg$i"))
  val records: Chunk[ProducerRecord[String, String]] = Chunk.fromIterable(kvs.map { case (k, v) =>
    new ProducerRecord(topic1, k, v)
  })

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer.make[Kafka with Producer](Kafka.embedded, producer).orDie

  override def initialize: ZIO[Kafka with Producer, Throwable, Any] = for {
    _ <- ZIO.succeed(EmbeddedKafka.deleteTopics(List(topic1))).ignore
    _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = nrPartitions))
  } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunk(): Any = runZIO {
    for {
      _ <- Producer.produceChunk(records, Serde.string, Serde.string).repeatN(100)
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.SECONDS)
  def produceSingleRecord(): Any = runZIO {
    Producer.produce(topic1, "key", "value", Serde.string, Serde.string)
  }
}
