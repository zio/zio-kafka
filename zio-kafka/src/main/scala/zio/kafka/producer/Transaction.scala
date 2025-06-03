package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.kafka.consumer.{ Offset, OffsetBatch }
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.serde.Serializer
import zio._

trait Transaction {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  )(implicit trace: Trace): RIO[R, RecordMetadata]

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  )(implicit trace: Trace): RIO[R, RecordMetadata]

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  )(implicit trace: Trace): RIO[R, Chunk[RecordMetadata]]

  def produceChunkBatch[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offsets: OffsetBatch
  )(implicit trace: Trace): RIO[R, Chunk[RecordMetadata]]

  def abort(implicit trace: Trace): IO[TransactionalProducer.UserInitiatedAbort.type, Nothing]
}

private[producer] final class TransactionImpl(
  producer: Producer,
  private[producer] val offsetBatchRef: Ref[OffsetBatch],
  closed: Ref[Boolean]
) extends Transaction {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  )(implicit trace: Trace): RIO[R, RecordMetadata] =
    produce(new ProducerRecord[K, V](topic, key, value), keySerializer, valueSerializer, offset)

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  )(implicit trace: Trace): RIO[R, RecordMetadata] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produce[R, K, V](producerRecord, keySerializer, valueSerializer)

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  )(implicit trace: Trace): RIO[R, Chunk[RecordMetadata]] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produceChunk[R, K, V](records, keySerializer, valueSerializer)

  def produceChunkBatch[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offsets: OffsetBatch
  )(implicit trace: Trace): RIO[R, Chunk[RecordMetadata]] =
    haltIfClosed *>
      offsetBatchRef.update(_ merge offsets) *>
      producer.produceChunk[R, K, V](records, keySerializer, valueSerializer)

  def abort(implicit trace: Trace): IO[TransactionalProducer.UserInitiatedAbort.type, Nothing] =
    ZIO.fail(UserInitiatedAbort)

  private[producer] def markAsClosed(implicit trace: Trace): UIO[Unit] = closed.set(true)

  private def haltIfClosed(implicit trace: Trace): IO[TransactionLeaked, Unit] =
    offsetBatchRef.get
      .flatMap(offsetBatch => ZIO.fail(TransactionLeaked(offsetBatch)))
      .whenZIO(closed.get)
      .unit
}
