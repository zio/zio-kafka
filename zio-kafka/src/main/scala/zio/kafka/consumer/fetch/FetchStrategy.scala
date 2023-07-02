package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.PartitionStreamControl
import zio.{ Chunk, ZIO }

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
      .foldLeft(streams)(Chunk.empty[TopicPartition]) { case (acc, stream) =>
        stream.queueSize.map { queueSize =>
          if (queueSize < maxPartitionQueueSize) acc ++ Chunk.single(stream.tp) else acc
        }
      }
      .map(_.toSet)
}

/**
 * A fetch strategy that allows a stream to fetch data when its queue size is below `maxPartitionQueueSize`, as long as
 * the total queue size is below `maxTotalQueueSize`. This strategy is suitable when [[QueueSizeBasedFetchStrategy]]
 * requires too much heap space, particularly when a lot of partitions are being consumed.
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
 * @param maxTotalQueueSize
 *   Maximum number of records to be buffered over all partitions together. This can be used to limit memory usage when
 *   consuming a large number of partitions.
 *
 * The default value is 20 * the default for `maxTotalQueueSize`, allowing approximately 20 partitions to do
 * pre-fetching in each poll.
 */
final case class ManyPartitionsQueueSizeBasedFetchStrategy(
  maxPartitionQueueSize: Int = 1024,
  maxTotalQueueSize: Int = 20480
) extends FetchStrategy {
  override def selectPartitionsToFetch(
    streams: Chunk[PartitionStreamControl]
  ): ZIO[Any, Nothing, Set[TopicPartition]] = {
    // By shuffling the streams we prevent read-starvation for streams at the end of the list.
    val shuffledStreams = scala.util.Random.shuffle(streams)
    ZIO
      .foldLeft(shuffledStreams)((Chunk.empty[TopicPartition], maxTotalQueueSize)) {
        case (acc @ (partitions, queueBudget), stream) =>
          stream.queueSize.map { queueSize =>
            if (queueSize < maxPartitionQueueSize && queueSize < queueBudget) {
              (partitions ++ Chunk.single(stream.tp), queueBudget - queueSize)
            } else acc
          }
      }
      .map { case (tps, _) => tps.toSet }
  }
}
