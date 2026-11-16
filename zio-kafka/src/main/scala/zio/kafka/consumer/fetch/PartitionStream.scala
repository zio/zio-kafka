package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.UIO

/**
 * Key information needed by a [[FetchStrategy]]. At any given moment a consumer has at most 1 partition stream per
 * partition.
 *
 * `queueSize` gives the size of the record queue that buffers record before they are consumed by the user's program.
 * See also section 'Using metrics to tune the consumer' in the 'Consumer Tuning' documentation.
 */
abstract class PartitionStream {
  def tp: TopicPartition
  def queueSize: UIO[Int]
}
