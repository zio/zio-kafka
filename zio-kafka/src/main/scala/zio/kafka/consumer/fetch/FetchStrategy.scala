package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.PartitionStream
import zio.{ Chunk, Trace, ZIO }

import scala.collection.mutable

/**
 * A fetch strategy determined which stream are allowed to fetch data in the next poll.
 */
trait FetchStrategy {

  /**
   * Selects which partitions can fetch data in the next poll.
   *
   * @param streams
   *   all stream for this consumer
   * @return
   *   the partitions that may fetch in the next poll
   */
  def selectPartitionsToFetch(streams: Chunk[PartitionStream])(implicit
    trace: Trace
  ): ZIO[Any, Nothing, Set[TopicPartition]]
}

/**
 * A fetch strategy that allows a stream to fetch data when its queue size is at or below
 * `partitionPreFetchBufferLimit`.
 *
 * @param partitionPreFetchBufferLimit
 *   The queue size at or below which more records are fetched and buffered (per partition). This buffer improves
 *   throughput and supports varying downstream message processing time, while maintaining some backpressure. Large
 *   values effectively disable backpressure at the cost of high memory usage, low values will effectively disable
 *   prefetching in favor of low memory consumption. The number of records that are fetched on every poll is controlled
 *   by the `max.poll.records` setting, the number of records fetched for every partition is somewhere between 0 and
 *   `max.poll.records`.
 *
 * The default value for this parameter is 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.
 *
 * The value `0` disables pre-fetching.
 */
final case class QueueSizeBasedFetchStrategy(partitionPreFetchBufferLimit: Int = 1024) extends FetchStrategy {
  require(
    partitionPreFetchBufferLimit >= 0,
    s"partitionPreFetchBufferLimit must be at least 0, got $partitionPreFetchBufferLimit"
  )

  override def selectPartitionsToFetch(
    streams: Chunk[PartitionStream]
  )(implicit trace: Trace): ZIO[Any, Nothing, Set[TopicPartition]] =
    ZIO
      .foldLeft(streams)(mutable.ArrayBuilder.make[TopicPartition]) { case (acc, stream) =>
        stream.queueSize.map { queueSize =>
          if (queueSize <= partitionPreFetchBufferLimit) acc += stream.tp else acc
        }
      }
      .map(_.result().toSet)
}
