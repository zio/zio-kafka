package zio.kafka.bench

import org.apache.kafka.clients.producer.ProducerRecord
import org.openjdk.jmh.annotations._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.stream.ZStream
import zio.{ Scope => _, _ }

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaSeqProducerBenchmark extends ProducerZioBenchmark[Kafka with Producer] {
  val records: Chunk[ProducerRecord[String, String]] = Chunk.fromIterable(kvs.map { case (k, v) =>
    new ProducerRecord(topic1, k, v)
  })

  override protected def bootstrap: ZLayer[Any, Nothing, Kafka with Producer] =
    ZLayer
      .make[Kafka with Producer](
        Kafka.embedded,
        // Producing record by record does not play well with linger: set it to zero.
        ZLayer.fromZIO(KafkaTestUtils.producerSettings.map(_.withLinger(0.millis))),
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
  def produceSingleRecordSeq(): Any = runZIO {
    // Produce 100 records sequentially
    for {
      producer <- ZIO.service[Producer]
      _        <- producer.produce(topic1, "key", "value", Serde.string, Serde.string).schedule(Schedule.recurs(100))
    } yield ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def produceSingleRecordPar(): Any = runZIO {
    // Produce 100 records of which 4 run in parallel
    for {
      producer <- ZIO.service[Producer]
      _        <- ZStream
             .range(0, 100, 1)
             .mapZIOParUnordered(4) { _ =>
               producer.produce(topic1, "key", "value", Serde.string, Serde.string)
             }
             .runDrain
    } yield ()
  }

}
