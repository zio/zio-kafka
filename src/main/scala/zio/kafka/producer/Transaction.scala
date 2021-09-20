package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.kafka.consumer.{ Offset, OffsetBatch }
import zio.kafka.producer.TransactionalProducer.UserInitiatedAbort
import zio.{ Cause, Chunk, IO, RIO, Ref, ZIO }
import zio.kafka.serde.Serializer

trait Transaction {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata]

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata]

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, Chunk[RecordMetadata]]

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing]
}

final private[producer] class TransactionImpl(
  private val producer: Producer,
  private[producer] val offsetBatchRef: Ref[OffsetBatch]
) extends Transaction {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata] =
    produce(new ProducerRecord[K, V](topic, key, value), keySerializer, valueSerializer, offset)

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata] =
    ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ merge offset) } *>
      producer.produce[R, K, V](producerRecord, keySerializer, valueSerializer)

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, Chunk[RecordMetadata]] =
    ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ merge offset) } *>
      producer.produceChunk[R, K, V](records, keySerializer, valueSerializer)

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing] =
    ZIO.haltWith(t => Cause.traced(Cause.fail(UserInitiatedAbort), t()))
}
