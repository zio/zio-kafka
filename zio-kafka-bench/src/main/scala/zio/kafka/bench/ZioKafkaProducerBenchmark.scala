package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.producer.ProducerRecord
import org.openjdk.jmh.annotations._
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils.producer
import zio.stream.ZStream
import zio.{ Chunk, ZIO, ZLayer }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaProducerBenchmark extends ProducerZioBenchmark[Kafka with Producer] {
  val records: Chunk[ProducerRecord[String, String]] = Chunk.fromIterable(kvs.map { case (k, v) =>
    new ProducerRecord(topic1, k, v)
  })

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer.make[Kafka with Producer](Kafka.embedded, producer).orDie

  override def initialize: ZIO[Kafka with Producer, Throwable, Any] = for {
    _ <- ZIO.succeed(EmbeddedKafka.deleteTopics(List(topic1))).ignore
    _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = partitionCount))
  } yield ()

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkSeq(): Any = runZIO {
    // Produce 30 chunks sequentially
    Producer.produceChunk(records, Serde.string, Serde.string).repeatN(29)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkPar(): Any = runZIO {
    // Produce 30 chunks of which 4 run in parallel
    ZStream
      .range(0, 30, 1)
      .mapZIOParUnordered(4) { _ =>
        Producer.produceChunk(records, Serde.string, Serde.string)
      }
      .runDrain
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.SECONDS)
  def produceSingleRecordSeq(): Any = runZIO {
    // Produce 100 records sequentially
    Producer.produce(topic1, "key", "value", Serde.string, Serde.string).repeatN(99)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.SECONDS)
  def produceSingleRecordPar(): Any = runZIO {
    // Produce 100 records of which 4 run in parallel
    ZStream
      .range(0, 100, 1)
      .mapZIOParUnordered(4) { _ =>
        Producer.produce(topic1, "key", "value", Serde.string, Serde.string)
      }
      .runDrain
  }
}
