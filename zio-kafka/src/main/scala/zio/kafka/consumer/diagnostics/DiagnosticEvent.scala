package zio.kafka.consumer.diagnostics

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

/**
 * An event in the life-cycle of a [[zio.kafka.consumer.Consumer]]. Events are reported to the callback that is set with
 * [[zio.kafka.consumer.ConsumerSettings.withDiagnostics]].
 *
 * {{{
 * import zio._
 * import zio.kafka.consumer._
 * import zio.kafka.diagnostics.Diagnostics
 *
 * val logRebalances: Consumer.ConsumerDiagnostics = new Diagnostics[DiagnosticEvent] {
 *   override def emit(event: => DiagnosticEvent): UIO[Unit] =
 *     event match {
 *       case rebalance: DiagnosticEvent.Rebalance => ZIO.logInfo("Rebalance: " + rebalance)
 *       case _                                    => ZIO.unit
 *     }
 * }
 *
 * val settings: ConsumerSettings = ConsumerSettings(List("localhost:9092")).withDiagnostics(logRebalances)
 * }}}
 *
 * @see
 *   [[https://zio.dev/zio-kafka/diagnostics/ Diagnostics]]
 */
sealed trait DiagnosticEvent

/** The events of a consumer. */
object DiagnosticEvent {

  /**
   * Emitted after every poll of the Kafka consumer.
   *
   * @param tpRequested
   *   the partitions whose streams had an outstanding request for records
   * @param tpWithData
   *   the partitions that the poll returned records for
   * @param tpWithoutData
   *   the requested partitions that the poll returned no records for
   */
  final case class Poll(
    tpRequested: Set[TopicPartition],
    tpWithData: Set[TopicPartition],
    tpWithoutData: Set[TopicPartition]
  ) extends DiagnosticEvent

  /** Emitted when the stream of a partition requests more records from the consumer. */
  final case class Request(partition: TopicPartition) extends DiagnosticEvent

  /** An event about a commit. The offsets in these events are the committed next offsets. */
  sealed trait Commit extends DiagnosticEvent

  /** The events about commits. */
  object Commit {

    /** Emitted when a commit is requested, for example by `Offset#commit`. `offsets` are the offsets of that commit. */
    final case class Started(offsets: Map[TopicPartition, OffsetAndMetadata]) extends Commit

    /**
     * Emitted when the Kafka broker confirmed a commit. The consumer combines the commits that are waiting into one
     * commit, so `offsets` holds the combined offsets.
     */
    final case class Success(offsets: Map[TopicPartition, OffsetAndMetadata]) extends Commit

    /**
     * Emitted when a commit to the Kafka broker failed with `cause`. `offsets` holds the combined offsets. A commit
     * that fails with a `RebalanceInProgressException` is retried and does not emit this event.
     */
    final case class Failure(offsets: Map[TopicPartition, OffsetAndMetadata], cause: Throwable) extends Commit
  }

  /**
   * Emitted after the consumer handled a poll during which Kafka called the consumer's rebalance listener. The sets can
   * all be empty when the assignment of this consumer did not change.
   *
   * @param revoked
   *   the partitions that were revoked
   * @param assigned
   *   the partitions that were newly assigned
   * @param lost
   *   the partitions that were lost
   * @param ended
   *   the partitions whose streams were ended
   */
  final case class Rebalance(
    revoked: Set[TopicPartition],
    assigned: Set[TopicPartition],
    lost: Set[TopicPartition],
    ended: Set[TopicPartition]
  ) extends DiagnosticEvent

  /** Emitted when the scope of a subscription ends, after the consumer unsubscribed it. */
  case object SubscriptionFinalized extends DiagnosticEvent

  /** Emitted when the run loop of the consumer, which polls Kafka, shuts down. */
  case object RunloopFinalized extends DiagnosticEvent

  /** Emitted when the consumer closes. It is the last event of the consumer. */
  case object ConsumerFinalized extends DiagnosticEvent

}
