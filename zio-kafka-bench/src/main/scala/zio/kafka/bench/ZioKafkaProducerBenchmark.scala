package zio.kafka.bench

import org.apache.kafka.clients.producer.ProducerRecord
import org.openjdk.jmh.annotations._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.producer.Producer
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

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer.make[Kafka with Producer](Kafka.embedded, KafkaTestUtils.producer).orDie

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
  def produceSingleRecordSeqAsync(): Any = runZIO {
    // Produce 100 records
    for {
      producer      <- ZIO.service[Producer]
      continuations <- ZIO.replicateZIO(100)(producer.produceAsync(topic1, "key", "value", Serde.string, Serde.string))
      _             <- ZIO.foreachDiscard(continuations)(identity)
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkSeq(): Any = runZIO {
    // Produce 30 chunks sequentially
    for {
      producer <- ZIO.service[Producer]
      _        <- producer.produceChunk(records, Serde.string, Serde.string).schedule(Schedule.recurs(30))
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkSeqAsync(): Any = runZIO {
    // Produce 30 chunks sequentially
    for {
      producer      <- ZIO.service[Producer]
      continuations <- ZIO.replicateZIO(30)(producer.produceChunkAsync(records, Serde.string, Serde.string))
      _             <- ZIO.foreachDiscard(continuations)(identity)
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceChunkPar(): Any = runZIO {
    // Produce 30 chunks of which 4 run in parallel
    for {
      producer <- ZIO.service[Producer]
      _        <- ZStream
             .range(0, 30, 1)
             .mapZIOParUnordered(4) { _ =>
               producer.produceChunk(records, Serde.string, Serde.string)
             }
             .runDrain
    } yield ()
  }
}
