package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.PartitionStreamControl
import zio.{ Chunk, ZIO }

import scala.collection.mutable

/**
 * A fetch strategy determined which stream are allowed to fetch data in the next poll.
 *
 * WARNING: this is an EXPERIMENTAL API and may change in an incompatible way without notice in any zio-kafka version.
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
  def selectPartitionsToFetch(streams: Chunk[PartitionStreamControl]): ZIO[Any, Nothing, Set[TopicPartition]]
}

/**
 * A fetch strategy that allows a stream to fetch data when its queue size is below `maxPartitionQueueSize`.
 *
 * @param maxPartitionQueueSize
 *   Maximum number of records to be buffered per partition. This buffer improves throughput and supports varying
 *   downstream message processing time, while maintaining some backpressure. Large values effectively disable
 *   backpressure at the cost of high memory usage, low values will effectively disable prefetching in favour of low
 *   memory consumption. The number of records that is fetched on every poll is controlled by the `max.poll.records`
 *   setting, the number of records fetched for every partition is somewhere between 0 and `max.poll.records`. A value
 *   that is a power of 2 offers somewhat better queueing performance.
 *
 * The default value for this parameter is 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.
 */
final case class QueueSizeBasedFetchStrategy(maxPartitionQueueSize: Int = 1024) extends FetchStrategy {
  override def selectPartitionsToFetch(streams: Chunk[PartitionStreamControl]): ZIO[Any, Nothing, Set[TopicPartition]] =
    ZIO
      .foldLeft(streams)(mutable.ArrayBuilder.make[TopicPartition]) { case (acc, stream) =>
        stream.queueSize.map { queueSize =>
          if (queueSize < maxPartitionQueueSize) acc += stream.tp else acc
        }
      }
      .map(_.result().toSet)
}
