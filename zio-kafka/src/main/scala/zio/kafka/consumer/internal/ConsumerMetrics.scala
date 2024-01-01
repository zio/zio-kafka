package zio.kafka.consumer.internal

import zio.metrics.MetricKeyType.Histogram
import zio.metrics._
import zio._

final case class ConsumerMetrics(metricLabels: Set[MetricLabel]) {

  // -----------------------------------------------------
  //
  // Rebalance metrics
  //

  private val rebalanceCounter: Metric.Counter[Int] =
    Metric.counterInt("ziokafka_consumer_rebalances", "The number of rebalances").tagged(metricLabels)

  private val partitionsCurrentlyAssignedGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_currently_assigned",
        "The number of partitions currently assigned to the consumer"
      )
      .tagged(metricLabels)
      .contramap[Int](_.toDouble)

  private def partitionsToStateCounter(state: String): Metric.Counter[Int] =
    Metric
      .counterInt(
        s"ziokafka_consumer_partitions_$state",
        s"The number of partitions $state to the consumer"
      )
      .tagged(metricLabels)

  private val partitionsAssignedCounter = partitionsToStateCounter("assigned")
  private val partitionsRevokedCounter  = partitionsToStateCounter("revoked")
  private val partitionsLostCounter     = partitionsToStateCounter("lost")

  def observeRebalance(currentlyAssignedCount: Int, assignedCount: Int, revokedCount: Int, lostCount: Int): UIO[Unit] =
    for {
      _ <- rebalanceCounter.increment
      _ <- partitionsCurrentlyAssignedGauge.update(currentlyAssignedCount)
      _ <- partitionsAssignedCounter.incrementBy(assignedCount)
      _ <- partitionsRevokedCounter.incrementBy(revokedCount)
      _ <- partitionsLostCounter.incrementBy(lostCount)
    } yield ()

  // -----------------------------------------------------
  //
  // Partition stream metrics
  //

  // Chunk(0,1,3,8,21,55,149,404,1097,2981)
  private val streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // Chunk(0,100,272,739,2009,5460,14842,40343,109664,298096)
  private val streamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  private val pollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk[Double](0, 1, 2, 3, 4, 5, 6, 7, 8, 9))

  private val pendingRequestsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_pending_requests",
        "The number of partition streams that are awaiting new records.",
        streamCountBoundaries
      )
      .tagged(metricLabels)
      .contramap[Int](_.toDouble)

  private val pendingCommitsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_pending_commits",
        "The number of commits that are awaiting completion.",
        streamCountBoundaries
      )
      .tagged(metricLabels)
      .contramap[Int](_.toDouble)

  private val queueSizeHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_queue_size",
        "The number of records queued per partition.",
        streamSizeBoundaries
      )
      .tagged(metricLabels)
      .contramap[Int](_.toDouble)

  private val queuePollsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_queue_polls",
        "The number of polls records are idling in the queue for a partition.",
        pollSizeBoundaries
      )
      .tagged(metricLabels)
      .contramap[Int](_.toDouble)

  private val allQueueSizeHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_all_queue_size",
        "The number of records queued in the consumer (all partitions).",
        streamSizeBoundaries
      )
      .tagged(metricLabels)
      .contramap[Int](_.toDouble)

  def observeMetrics(state: Runloop.State): UIO[Unit] =
    ZIO
      .when(state.subscriptionState.isSubscribed) {
        for {
          _          <- ZIO.foreachDiscard(state.assignedStreams)(_.outstandingPolls @@ queuePollsHistogram)
          queueSizes <- ZIO.foreach(state.assignedStreams)(_.queueSize)
          _          <- ZIO.foreachDiscard(queueSizes)(qs => queueSizeHistogram.update(qs))
          _          <- allQueueSizeHistogram.update(queueSizes.sum)
          _          <- pendingRequestsHistogram.update(state.pendingRequests.size)
          _          <- pendingCommitsHistogram.update(state.pendingCommits.size)
        } yield ()
      }
      .unit

}
