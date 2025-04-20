package zio.kafka.bench

import org.apache.kafka.clients.producer.ProducerRecord
import org.openjdk.jmh.annotations._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.producer.{ Producer, ProducerEvent }
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils
import zio.stream.ZStream
import zio.{ Scope => _, _ }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaProducerBenchmark extends ProducerZioBenchmark[Kafka with Producer] {
  val records: Chunk[ProducerRecord[String, String]] = Chunk.fromIterable(kvs.map { case (k, v) =>
    new ProducerRecord(topic1, k, v)
  })

  private val producerDiagnostics: Producer.ProducerDiagnostics = new Producer.ProducerDiagnostics {
    override def emit(event: => ProducerEvent): UIO[Unit] = ZIO.unit
  }

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer
      .make[Kafka with Producer](
        Kafka.embedded,
        ZLayer.fromZIO(KafkaTestUtils.producerSettings).update(_.withDiagnostics(producerDiagnostics)),
        Producer.live
      )
      .orDie

  override def initialize: ZIO[Kafka & Producer, Throwable, Any] =
    ZIO.scoped {
      for {
        adminClient <- KafkaTestUtils.makeAdminClient
        _           <- adminClient.deleteTopic(topic1).ignore
        _           <- adminClient.createTopic(NewTopic(topic1, partitionCount, replicationFactor = 1))
      } yield ()
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkSeq(): Any = runZIO {
    // Produce 30 chunks sequentially
    for {
      producer <- ZIO.service[Producer]
      _        <- producer.produceChunk(records, Serde.string, Serde.string).repeatN(29)
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkPar(): Any = runZIO {
    // Produce 30 chunks of which 4 run in parallel
    for {
      producer <- ZIO.service[Producer]
      _ <- ZStream
             .range(0, 30, 1)
             .mapZIOParUnordered(4) { _ =>
               producer.produceChunk(records, Serde.string, Serde.string)
             }
             .runDrain
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.SECONDS)
  def produceSingleRecordSeq(): Any = runZIO {
    // Produce 100 records sequentially
    for {
      producer <- ZIO.service[Producer]
      _        <- producer.produce(topic1, "key", "value", Serde.string, Serde.string).repeatN(99)
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  @OutputTimeUnit(TimeUnit.SECONDS)
  def produceSingleRecordPar(): Any = runZIO {
    // Produce 100 records of which 4 run in parallel
    for {
      producer <- ZIO.service[Producer]
      _ <- ZStream
             .range(0, 100, 1)
             .mapZIOParUnordered(4) { _ =>
               producer.produce(topic1, "key", "value", Serde.string, Serde.string)
             }
             .runDrain
    } yield ()
  }
}
