package zio.kafka.tracing

import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo }
import zio.kafka.producer._
import zio.telemetry.opentelemetry.tracing.Tracing
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator
import zio._
import zio.telemetry.opentelemetry.context.OutgoingContextCarrier

import java.nio.charset.StandardCharsets
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object TracingProducerAspect {

  /**
   * Adds open tracing headers to each outgoing record of a ZIO Kafka [[Producer]].
   */
  def traced: ProducerAspect[Nothing, Tracing & TraceContextPropagator] =
    new ProducerAspect[Nothing, Tracing & TraceContextPropagator] {
      override def apply[R >: Nothing <: Tracing & TraceContextPropagator](
        wrapped: ProducerWithEnv[R]
      ): ProducerWithEnv[R] =
        new ProducerWithEnv[R] with DefaultProducer[R] {
          // noinspection YieldingZIOEffectInspection
          override def produceChunkAsyncWithFailures(
            records: Chunk[ByteRecord]
          ): RIO[R, UIO[Chunk[Either[Throwable, RecordMetadata]]]] =
            for {
              recordWithTraceHeaders <- ZIO.foreach(records)(withTraceHeaders)
              result                 <- wrapped.produceChunkAsyncWithFailures(recordWithTraceHeaders)
            } yield result

          // noinspection YieldingZIOEffectInspection
          override def produceAsync(record: ByteRecord): RIO[R, Task[RecordMetadata]] =
            for {
              recordWithTraceHeaders <- withTraceHeaders(record)
              result                 <- wrapped.produceAsync(recordWithTraceHeaders)
            } yield result

          override def partitionsFor(topic: String): RIO[R, Chunk[PartitionInfo]] =
            wrapped.partitionsFor(topic)

          override def flush: RIO[R, Unit] =
            wrapped.flush

          override def metrics: RIO[R, Map[MetricName, Metric]] =
            wrapped.metrics

          private def withTraceHeaders(record: ByteRecord): ZIO[Tracing & TraceContextPropagator, Nothing, ByteRecord] =
            traceKafkaHeaders.map { extraHeaders =>
              new ByteRecord(
                record.topic(),
                record.partition(),
                record.timestamp(),
                record.key(),
                record.value(),
                new RecordHeaders((record.headers().asScala ++ extraHeaders).asJava)
              )
            }

          private def traceKafkaHeaders: ZIO[Tracing & TraceContextPropagator, Nothing, Seq[Header]] =
            for {
              tracing                <- ZIO.service[Tracing]
              traceContextPropagator <- ZIO.service[TraceContextPropagator]
              headers = mutable.Map.empty[String, String]
              _ <- tracing.injectSpan(traceContextPropagator, OutgoingContextCarrier.default(headers))
            } yield headers.toSeq.map(PairHeader)
        }
    }

  private case class PairHeader(keyValue: (String, String)) extends Header {
    override def key(): String = keyValue._1

    override def value(): Array[Byte] = keyValue._2.getBytes(StandardCharsets.UTF_8)
  }
}
