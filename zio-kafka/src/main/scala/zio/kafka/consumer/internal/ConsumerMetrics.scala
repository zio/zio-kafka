package zio.kafka.consumer.internal

import zio.metrics.MetricKeyType.Histogram
import zio.metrics._
import zio.{ Chunk, UIO, ZIO }

final case class ConsumerMetrics(metricLabels: Set[MetricLabel]) {

  // Chunk(0,1,3,8,21,55,149,404,1097,2981)
  private val streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // Chunk(0,100,272,739,2009,5460,14842,40343,109664,298096)
  private val streamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  private val pendingRequestsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_pending_requests",
        "The number of partition streams that are awaiting new records.",
        streamCountBoundaries
      )
      .tagged(metricLabels)

  private val pendingCommitsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_pending_commits",
        "The number of commits that are awaiting completion.",
        streamCountBoundaries
      )
      .tagged(metricLabels)

  private val queueSizeHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_queue_size",
        "The number of records queued per partition.",
        streamSizeBoundaries
      )
      .tagged(metricLabels)

  private val allQueueSizeHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_all_queue_size",
        "The number of records queued in the consumer (all partitions).",
        streamSizeBoundaries
      )
      .tagged(metricLabels)

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
