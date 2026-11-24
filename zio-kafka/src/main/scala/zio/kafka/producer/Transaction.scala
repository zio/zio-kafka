package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.kafka.consumer.{ Offset, OffsetBatch }
import zio.kafka.producer.TransactionalProducer.{ TransactionLeaked, UserInitiatedAbort }
import zio.kafka.serde.Serializer
import zio._

/**
 * A Kafka transaction in progress. A transaction is created with [[TransactionalProducer.createTransaction]] and lives
 * in the `Scope` that was given to that method.
 *
 * The records produced with a transaction become visible to consumers that read committed records only when the scope
 * closes successfully. When the scope closes with a failure or an interruption, the transaction is aborted. A
 * transaction cannot be used after its scope has closed: the produce methods then fail with
 * [[TransactionalProducer.TransactionLeaked]].
 *
 * The produce methods wait until the broker has acknowledged the records. Together with the records, they take the
 * offsets of the consumed records that are committed as part of the transaction.
 *
 * {{{
 * import org.apache.kafka.clients.producer.ProducerRecord
 * import zio._
 * import zio.kafka.consumer.CommittableRecord
 * import zio.kafka.serde.Serde
 *
 * def forward(producer: TransactionalProducer, record: CommittableRecord[String, String]): Task[Unit] =
 *   ZIO.scoped {
 *     for {
 *       tx <- producer.createTransaction
 *       _ <- tx.produce(
 *              new ProducerRecord("output-topic", record.key, record.value),
 *              Serde.string,
 *              Serde.string,
 *              Some(record.offset)
 *            )
 *     } yield ()
 *   }
 * }}}
 *
 * @see
 *   [[https://zio.dev/zio-kafka/transactions/ Transactions Tutorial]]
 */
trait Transaction {

  /**
   * Produces a record to `topic` and waits for the broker's acknowledgement.
   *
   * @param offset
   *   the offset of the consumed record that this record is produced for. It is committed when the transaction
   *   commits. Use `None` when there is no such offset.
   */
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata]

  /**
   * Produces a record and waits for the broker's acknowledgement.
   *
   * @param offset
   *   the offset of the consumed record that this record is produced for. It is committed when the transaction
   *   commits. Use `None` when there is no such offset.
   */
  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata]

  /**
   * Produces a chunk of records and waits for the broker's acknowledgement. When producing any of the records fails,
   * the effect fails, like `Producer#produceChunk`.
   *
   * @param offset
   *   a single offset for the whole chunk. It is committed when the transaction commits. Use `None` when there is no
   *   such offset. To give offsets of more than one partition, use `produceChunkBatch`.
   */
  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, Chunk[RecordMetadata]]

  /**
   * Produces a chunk of records and waits for the broker's acknowledgement. When producing any of the records fails,
   * the effect fails, like `Producer#produceChunk`.
   *
   * @param offsets
   *   the offsets of the consumed records that the produced records are for. They are merged with the offsets that
   *   this transaction already has, and are committed when the transaction commits.
   */
  def produceChunkBatch[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offsets: OffsetBatch
  ): RIO[R, Chunk[RecordMetadata]]

  /**
   * Fails with [[TransactionalProducer.UserInitiatedAbort]]. The transaction is aborted when this failure reaches the
   * end of the transaction's scope. When the failure is handled inside the scope, the scope closes successfully and the
   * transaction commits.
   */
  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing]
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
  ): RIO[R, RecordMetadata] =
    produce(new ProducerRecord[K, V](topic, key, value), keySerializer, valueSerializer, offset)

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, RecordMetadata] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produce[R, K, V](producerRecord, keySerializer, valueSerializer)

  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offset: Option[Offset]
  ): RIO[R, Chunk[RecordMetadata]] =
    haltIfClosed *>
      ZIO.whenCase(offset) { case Some(offset) => offsetBatchRef.update(_ add offset) } *>
      producer.produceChunk[R, K, V](records, keySerializer, valueSerializer)

  def produceChunkBatch[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V],
    offsets: OffsetBatch
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
