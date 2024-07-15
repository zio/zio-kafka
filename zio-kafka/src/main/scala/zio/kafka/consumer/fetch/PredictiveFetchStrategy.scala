package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.PartitionStream
import zio.{ Chunk, ZIO }

import scala.collection.mutable

/**
 * A fetch strategy that predicts when a stream needs more data by analyzing its history.
 *
 * The prediction is based on the average number of polls the stream needed to process data in the recent past. In
 * addition, a stream can always fetch when it is out of data.
 *
 * This fetch strategy is suitable when processing takes at a least a few polls. It is especially suitable when
 * different streams (partitions) have different processing times, but each stream has consistent processing time.
 *
 * Note: this strategy has mutable state; a separate instance is needed for each consumer.
 *
 * @param maxEstimatedPollCountsToFetch
 *   The maximum number of estimated polls before the stream may fetch data. The default (and minimum) is 1 which means
 *   that data is fetched 1 poll before it is needed. Setting this higher trades higher memory usage for a lower chance
 *   a stream needs to wait for data.
 */
final class PredictiveFetchStrategy(maxEstimatedPollCountsToFetch: Int = 1) extends FetchStrategy {
  require(maxEstimatedPollCountsToFetch >= 1, s"`pollCount` must be at least 1, got $maxEstimatedPollCountsToFetch")
  private val CleanupPollCount = 10
  private var cleanupCountDown = CleanupPollCount
  private val pollHistories    = mutable.Map.empty[PartitionStream, PollHistory]

  override def selectPartitionsToFetch(
    streams: Chunk[PartitionStream]
  ): ZIO[Any, Nothing, Set[TopicPartition]] =
    ZIO.succeed {
      if (cleanupCountDown == 0) {
        pollHistories --= (pollHistories.keySet.toSet -- streams)
        cleanupCountDown = CleanupPollCount
      } else {
        cleanupCountDown -= 1
      }
    } *>
      ZIO
        .foldLeft(streams)(mutable.ArrayBuilder.make[TopicPartition]) { case (acc, stream) =>
          stream.queueSize.map { queueSize =>
            val outOfData        = queueSize == 0
            val pollHistory      = pollHistories.getOrElseUpdate(stream, PollHistory.Empty)
            val predictiveResume = pollHistory.estimatedPollCountToResume <= maxEstimatedPollCountsToFetch
            pollHistories += (stream -> pollHistory.addPollHistory(outOfData))
            if (outOfData || predictiveResume) acc += stream.tp else acc
          }
        }
        .map(_.result().toSet)
}
