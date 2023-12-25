package zio.kafka.consumer.internal

import zio.metrics.MetricKeyType.Histogram
import zio.metrics._
import zio.{ Chunk, UIO, ZIO }

final case class ConsumerMetrics(metricsConsumerId: String) {

  // Chunk(0,1,3,8,21,55,149,404,1097,2981)
  private val streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // Chunk(0,100,272,739,2009,5460,14842,40343,109664,298096)
  private val streamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  private val pendingRequestsHistogram =
    Metric
      .histogram(
        "consumer_pending_requests",
        "The number of partition streams that are awaiting new records.",
        streamCountBoundaries
      )
      .tagged("consumer_id", metricsConsumerId)

  private val pendingCommitsHistogram =
    Metric
      .histogram(
        "consumer_pending_commits",
        "The number of commits that are awaiting completion.",
        streamCountBoundaries
      )
      .tagged("consumer_id", metricsConsumerId)

  private val queueSizeHistogram =
    Metric
      .histogram(
        "consumer_queue_size",
        "The number of records queued per partition.",
        streamSizeBoundaries
      )
      .tagged("consumer_id", metricsConsumerId)

  private val allQueueSizeHistogram =
    Metric
      .histogram(
        "consumer_all_queue_size",
        "The number of records queued in the consumer (all partitions).",
        streamSizeBoundaries
      )
      .tagged("consumer_id", metricsConsumerId)

  def observeMetrics(state: Runloop.State): UIO[Unit] =
    ZIO
      .when(state.subscriptionState.isSubscribed) {
        for {
          queueSizes <- ZIO.foreach(state.assignedStreams)(_.queueSize.map(_.toDouble))
          _          <- ZIO.foreachDiscard(queueSizes)(qs => queueSizeHistogram.update(qs))
          _          <- allQueueSizeHistogram.update(queueSizes.sum)
          _          <- ZIO.succeed(state.pendingRequests.size.toDouble) @@ pendingRequestsHistogram
          _          <- ZIO.succeed(state.pendingCommits.size.toDouble) @@ pendingCommitsHistogram
        } yield ()
      }
      .unit

}
