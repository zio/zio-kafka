package zio.kafka.consumer.internal

import zio.metrics.MetricKeyType.Histogram
import zio.metrics._
import zio.{ Chunk, UIO, ZIO }

final case class ConsumerMetrics(metricsConsumerId: String) {

  private val streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048))

  private val pendingRequestsHistogram =
    Metric
      .histogram(
        "consumer_pending_requests",
        "The number of streams that are awaiting new data.",
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
        "The number of records in stream queues.",
        MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0, 128, 256, 512, 1024, 2048, 4096, 8192))
      )
      .tagged("consumer_id", metricsConsumerId)

  def observeMetrics(state: Runloop.State): UIO[Unit] =
    ZIO
      .when(state.subscriptionState.isSubscribed) {
        (ZIO.succeed(state.pendingRequests.size.toDouble) @@ pendingRequestsHistogram) *>
          (ZIO.succeed(state.pendingCommits.size.toDouble) @@ pendingCommitsHistogram) *>
          ZIO.foreachDiscard(state.assignedStreams)(_.queueSize.map(_.toDouble) @@ queueSizeHistogram)
      }
      .unit

}
