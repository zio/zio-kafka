package zio.kafka.producer

import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zio.kafka.consumer.{Offset, OffsetBatch}
import zio.kafka.producer.TransactionalProducer.{TransactionLeaked, UserInitiatedAbort}
import zio.kafka.serde.Serializer
import zio.{Chunk, IO, RIO, Ref, UIO, ZIO}

trait Transaction {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset],
  ): RIO[R, RecordMetadata]

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset],
  ): RIO[R, RecordMetadata]

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset],
  ): RIO[R, Chunk[RecordMetadata]]

  def produceChunkBatch[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offsets: OffsetBatch,
  ): RIO[R, Chunk[RecordMetadata]]

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing]
}

final private[producer] class TransactionImpl(
  private val producer: Producer,
  private[producer] val offsetBatchRef: Ref[OffsetBatch],
  private val closed: Ref[Boolean],
) extends Transaction {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset],
  ): RIO[R, RecordMetadata] =
    produce(new ProducerRecord[K, V](topic, key, value), keySerializer, valueSerializer, offset)

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset],
  ): RIO[R, RecordMetadata] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produce[R, K, V](producerRecord, keySerializer, valueSerializer)

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset],
  ): RIO[R, Chunk[RecordMetadata]] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produceChunk[R, K, V](records, keySerializer, valueSerializer)

  def produceChunkBatch[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offsets: OffsetBatch,
  ): RIO[R, Chunk[RecordMetadata]] =
    haltIfClosed *>
      offsetBatchRef.update(_ merge offsets) *>
      producer.produceChunk[R, K, V](records, keySerializer, valueSerializer)

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing] =
    ZIO.fail(UserInitiatedAbort)

  private[producer] def markAsClosed: UIO[Unit] = closed.set(true)

  private def haltIfClosed: IO[TransactionLeaked, Unit] =
    offsetBatchRef.get
      .flatMap(offsetBatch => ZIO.fail(TransactionLeaked(offsetBatch)))
      .whenZIO(closed.get)
      .unit
}
