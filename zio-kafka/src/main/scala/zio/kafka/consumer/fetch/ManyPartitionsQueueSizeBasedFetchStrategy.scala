package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.{ Chunk, ZIO }
import zio.kafka.consumer.internal.PartitionStream

import scala.collection.mutable

/**
 * A fetch strategy that allows a stream to fetch data when its queue size is at or below `maxPartitionQueueSize`, as
 * long as the total queue size is at or below `maxTotalQueueSize`. This strategy is suitable when
 * [[QueueSizeBasedFetchStrategy]] requires too much heap space, particularly when a lot of partitions are being
 * consumed.
 *
 * @param maxPartitionQueueSize
 *   Maximum number of records to be buffered per partition. This buffer improves throughput and supports varying
 *   downstream message processing time, while maintaining some backpressure. Low values effectively disable prefetching
 *   in favour of low memory consumption. Large values leave it up to `maxTotalQueueSize` parameter to backpressure only
 *   over the buffers of all partitions together.
 *
 * The number of records that are fetched on every poll is controlled by the `max.poll.records` setting, the number of
 * records fetched for every partition is somewhere between 0 and `max.poll.records`.
 *
 * The default value for this parameter is 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.
 *
 * @param maxTotalQueueSize
 *   Maximum number of records to be buffered over all partitions together. This can be used to limit memory usage when
 *   consuming a large number of partitions.
 *
 * When multiple streams are eligible for pre-fetching (because their queue size is below `maxPartitionQueueSize`), but
 * together they exceed `maxTotalQueueSize`, then every call a random set of eligible streams is selected that stays
 * below `maxTotalQueueSize`. The randomization ensures fairness and prevents read-starvation for streams at the end of
 * the list.
 *
 * The default value is 20 * the default for `maxPartitionQueueSize`, allowing approximately 20 partitions to do
 * pre-fetching in each poll.
 */
final case class ManyPartitionsQueueSizeBasedFetchStrategy(
  maxPartitionQueueSize: Int = 1024,
  maxTotalQueueSize: Int = 20480
) extends FetchStrategy {
  override def selectPartitionsToFetch(
    streams: Chunk[PartitionStream]
  ): ZIO[Any, Nothing, Set[TopicPartition]] =
    for {
      random          <- ZIO.random
      shuffledStreams <- random.shuffle(streams)
      tps             <- ZIO
               .foldLeft(shuffledStreams)((mutable.ArrayBuilder.make[TopicPartition], maxTotalQueueSize)) {
                 case (acc @ (partitions, queueBudget), stream) =>
                   stream.queueSize.map { queueSize =>
                     if (queueSize <= maxPartitionQueueSize && queueSize <= queueBudget) {
                       (partitions += stream.tp, queueBudget - queueSize)
                     } else acc
                   }
               }
               .map { case (tps, _) => tps.result().toSet }
    } yield tps
}
