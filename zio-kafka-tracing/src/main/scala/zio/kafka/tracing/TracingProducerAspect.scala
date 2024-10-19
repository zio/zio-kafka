package zio.kafka.tracing

import io.opentracing.propagation.{ Format, TextMapAdapter }
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo }
import zio.kafka.producer._
import zio.telemetry.opentracing.OpenTracing
import zio.{ Chunk, RIO, Task, UIO, ZIO }

import java.nio.charset.StandardCharsets
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object TracingProducerAspect {

  /**
   * Adds open tracing headers to each outgoing record of a ZIO Kafka [[Producer]].
   *
   * WARNING: this aspect mutates the headers in the record by adding the tracing headers directly. Be careful NOT to
   * reuse the records after passing the records to the producer.
   */
  def traced: ProducerAspect[Nothing, OpenTracing] = new ProducerAspect[Nothing, OpenTracing] {
    override def apply[R >: Nothing <: OpenTracing](wrapped: ProducerWithEnv[R]): ProducerWithEnv[R] =
      new ProducerWithEnv[R] with DefaultProducer[R] {
        // noinspection YieldingZIOEffectInspection
        override def produceChunkAsyncWithFailures(
          records: Chunk[ByteRecord]
        ): RIO[R, UIO[Chunk[Either[Throwable, RecordMetadata]]]] =
          for {
            recordsWithHeaders <- ZIO.foreach(records)(withTracingHeaders)
            result             <- wrapped.produceChunkAsyncWithFailures(recordsWithHeaders)
          } yield result

        // noinspection YieldingZIOEffectInspection
        override def produceAsync(record: ByteRecord): RIO[R, Task[RecordMetadata]] =
          for {
            recordWithHeaders <- withTracingHeaders(record)
            result            <- wrapped.produceAsync(recordWithHeaders)
          } yield result

        override def partitionsFor(topic: String): RIO[R, Chunk[PartitionInfo]] =
          wrapped.partitionsFor(topic)

        override def flush: RIO[R, Unit] =
          wrapped.flush

        override def metrics: RIO[R, Map[MetricName, Metric]] =
          wrapped.metrics

        private def withTracingHeaders(record: ByteRecord): ZIO[OpenTracing, Nothing, ByteRecord] =
          kafkaTracingHeaders(record).map { headers =>
            headers.foreach(header => record.headers().add(header))
            record
          }

        private def kafkaTracingHeaders(record: ByteRecord): ZIO[OpenTracing, Nothing, Seq[Header]] =
          ZIO.serviceWithZIO[OpenTracing] { tracing =>
            import tracing.aspects._
            val headers = mutable.Map.empty[String, String]
            val buffer  = new TextMapAdapter(headers.asJava)
            tracing
              .inject(Format.Builtin.HTTP_HEADERS, buffer)
              .zipLeft(ZIO.unit @@ spanFrom(Format.Builtin.HTTP_HEADERS, buffer, s"produce to topic ${record.topic()}"))
              .as(headers.toSeq.map(PairHeader))
          }
      }
  }

  private case class PairHeader(keyValue: (String, String)) extends Header {
    override def key(): String = keyValue._1

    override def value(): Array[Byte] = keyValue._2.getBytes(StandardCharsets.UTF_8)
  }
}
