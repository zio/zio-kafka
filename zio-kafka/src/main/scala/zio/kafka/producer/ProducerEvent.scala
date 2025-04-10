package zio.kafka.producer

import zio._

sealed trait ProducerEvent

/**
 * Diagnostics events for the [[Producer]].
 *
 * The producer always handles records in batches. Even single records results in batches of 1. The diagnostic events
 * are therefore always about batches.
 *
 * For each batch the [[RecordsOffered]] event is emitted first, followed by the [[RecordsSent]] event. Since batches
 * are queued in the producer and sending one batch might go faster than the previous, the two event types can come in
 * any order. However, the [[RecordsOffered]] events are always in the order the batches were offered.
 *
 * To associate events to a batch you can use the `batchId` field. This is an ever-increasing number that is unique per
 * producer and starts at 0. When a [[RecordsOffered]] and a [[RecordsSent]] event are for the same batch, the `batchId`
 * and `records` fields have the same value.
 *
 * Batches of 0 records do not result in events.
 */
object ProducerEvent {

  /**
   * Represents key data about a record that was handled by the producer.
   *
   * @param size
   *   the size of the record's value
   */
  final case class ProducedRecord(topic: String, partition: Int, size: Int)

  /**
   * Diagnostic event that is emitted when a batch of records is offered to the producer, and added to the internal send
   * queue.
   *
   * @param batchId
   *   a unique identifier (for this producer, starting at 0) for this batch of records, can be used to match this
   *   diagnostics event with the other diagnostics events for the same batch
   * @param records
   *   the records that were offered
   */
  final case class RecordsOffered(batchId: Long, records: Chunk[ProducedRecord]) extends ProducerEvent

  /**
   * Diagnostic event that is emitted after all records of a batch were sent to the broker.
   *
   * The exact time this event is emitted depends on the
   * [[https://kafka.apache.org/documentation/#producerconfigs_acks `acks` producer configuration]]. If `acks` is set to
   * `0`, the event is emitted immediately after all records were added to the socket buffer. Other values for `acks`
   * cause this event to be emitted after the broker has acknowledged the records.
   *
   * @param batchId
   *   a unique identifier (for this producer, starting at 0) for this batch of records, can be used to match this
   *   diagnostics event with the other diagnostics events for the same batch
   * @param records
   *   the records that were attempted to be sent
   * @param failed
   *   indices of the records in `records` of which sending resulted in an error, and those that were not sent because
   *   of an earlier error. Records for which the index is not in this set succeeded. When all records succeeded, this
   *   set is empty.
   */
  final case class RecordsSent(batchId: Long, records: Chunk[ProducedRecord], failed: Set[Int]) extends ProducerEvent

  /** Diagnostic event that is emitted when the producer closes. */
  case object ProducerFinalized extends ProducerEvent

}
