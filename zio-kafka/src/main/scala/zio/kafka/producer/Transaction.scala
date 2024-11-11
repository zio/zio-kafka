package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.kafka.consumer.{ Offset, OffsetBatch }
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.serde.Serializer
import zio.{ Chunk, IO, Ref, Task, UIO, ZIO }

trait Transaction {
  def produce[K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offset: Option[Offset]
  ): Task[RecordMetadata]

  def produce[K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offset: Option[Offset]
  ): Task[RecordMetadata]

  def produceChunk[K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offset: Option[Offset]
  ): Task[Chunk[RecordMetadata]]

  def produceChunkBatch[K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offsets: OffsetBatch
  ): Task[Chunk[RecordMetadata]]

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing]
}

private[producer] final class TransactionImpl(
  producer: Producer,
  private[producer] val offsetBatchRef: Ref[OffsetBatch],
  closed: Ref[Boolean]
) extends Transaction {
  def produce[K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offset: Option[Offset]
  ): Task[RecordMetadata] =
    produce(new ProducerRecord[K, V](topic, key, value), keySerializer, valueSerializer, offset)

  def produce[K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offset: Option[Offset]
  ): Task[RecordMetadata] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produce[K, V](producerRecord, keySerializer, valueSerializer)

  def produceChunk[K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offset: Option[Offset]
  ): Task[Chunk[RecordMetadata]] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produceChunk[K, V](records, keySerializer, valueSerializer)

  def produceChunkBatch[K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[K],
    valueSerializer: Serializer[V],
    offsets: OffsetBatch
  ): Task[Chunk[RecordMetadata]] =
    haltIfClosed *>
      offsetBatchRef.update(_ merge offsets) *>
      producer.produceChunk[K, V](records, keySerializer, valueSerializer)

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing] =
    ZIO.fail(UserInitiatedAbort)

  private[producer] def markAsClosed: UIO[Unit] = closed.set(true)

  private def haltIfClosed: IO[TransactionLeaked, Unit] =
    offsetBatchRef.get
      .flatMap(offsetBatch => ZIO.fail(TransactionLeaked(offsetBatch)))
      .whenZIO(closed.get)
      .unit
}
