package zio.kafka.consumer.internal

import zio.metrics.MetricKeyType.Histogram
import zio.metrics._
import zio._

final case class ConsumerMetrics(metricLabels: Set[MetricLabel]) {

  // -----------------------------------------------------
  //
  // Poll metrics
  //

  // Chunk(0.01,0.02,0.04,0.08,0.16,0.32,0.64,1.28,2.56) in seconds
  // Chunk(10,20,40,80,160,320,640,1280,2560) in milliseconds
  private val pollLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.exponential(0.01, 2.0, 9)

  // Chunk(1,3,8,21,55,149,404,1097,2981,8104)
  private val pollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  private val pollCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_polls", "The number of polls.")
      .tagged(metricLabels)

  private val partitionsCurrentlyResumedGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_currently_resumed",
        "The number of partitions currently resumed."
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val partitionsCurrentlyPausedGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_currently_paused",
        "The number of partitions currently paused because of backpressure."
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val pollLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_consumer_poll_latency",
        "The duration of a single poll in seconds.",
        pollLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  private val pollSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_poll_size",
        "The number of records fetched by a single poll.",
        pollSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  def observePoll(resumedCount: Int, pausedCount: Int, latency: Duration, pollSize: Int): UIO[Unit] =
    for {
      _ <- pollCounter.increment
      _ <- partitionsCurrentlyResumedGauge.update(resumedCount)
      _ <- partitionsCurrentlyPausedGauge.update(pausedCount)
      _ <- pollLatencyHistogram.update(latency)
      _ <- pollSizeHistogram.update(pollSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Commit metrics
  //

  // Chunk(0.01,0.02,0.04,0.08,0.16,0.32,0.64,1.28,2.56) in seconds
  // Chunk(10,20,40,80,160,320,640,1280,2560) in milliseconds
  private val commitLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.exponential(0.01, 2.0, 9)

  // Chunk(1,3,8,21,55,149,404,1097,2981,8104)
  private val commitSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  private val commitCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_commits", "The number of aggregated commits.")
      .tagged(metricLabels)

  private val commitLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_consumer_commit_latency",
        "The duration of a single aggregated commit in seconds.",
        commitLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  // Note: the metric is an approximation because the first commit to a partition is not included.
  private val commitSizeHistogram: Metric.Histogram[Long] =
    Metric
      .histogram(
        "ziokafka_consumer_commit_size",
        "An approximation of the number of records (offsets) per aggregated commit.",
        commitSizeBoundaries
      )
      .contramap[Long](_.toDouble)
      .tagged(metricLabels)

  def observeCommit(latency: Duration, commitSize: Long): UIO[Unit] =
    for {
      _ <- commitCounter.increment
      _ <- commitLatencyHistogram.update(latency)
      _ <- commitSizeHistogram.update(commitSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Rebalance metrics
  //

  private val rebalanceCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_rebalances", "The number of rebalances")
      .tagged(metricLabels)

  private val partitionsCurrentlyAssignedGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_currently_assigned",
        "The number of partitions currently assigned to the consumer"
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

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
  // Runloop metrics
  //

  // Chunk(0,1,3,8,21,55,149,404,1097,2981)
  private val streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // Chunk(0,100,272,739,2009,5460,14842,40343,109664,298096)
  private val streamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  private val queuePollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk[Double](0, 1, 2, 3, 4, 5, 6, 7, 8, 9))

  private val pendingRequestsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_pending_requests",
        "The number of partition streams that are awaiting new records.",
        streamCountBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val pendingCommitsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_pending_commits",
        "The number of commits that are awaiting completion.",
        streamCountBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val queueSizeHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_queue_size",
        "The number of records queued per partition.",
        streamSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val queuePollsHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_queue_polls",
        "The number of polls records are idling in the queue for a partition.",
        queuePollSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val allQueueSizeHistogram =
    Metric
      .histogram(
        "ziokafka_consumer_all_queue_size",
        "The number of records queued in the consumer (all partitions).",
        streamSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val subscriptionStateGauge: Metric.Gauge[SubscriptionState] =
    Metric
      .gauge(
        "ziokafka_consumer_subscription_state",
        "Whether the consumer is subscribed (1) or not (0)."
      )
      .contramap[SubscriptionState](s => if (s.isSubscribed) 1 else 0)
      .tagged(metricLabels)

  // Chunk(0,1,3,8,21,55,149,404,1097,2981)
  private val commandAndCommitQueueSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  private val commandQueueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_command_queue_size",
        "The number of commands queued in the consumer.",
        commandAndCommitQueueSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val commitQueueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_commit_queue_size",
        "The number of commits queued in the consumer.",
        commandAndCommitQueueSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  def observeRunloopMetrics(state: Runloop.State, commandQueueSize: Int, commitQueueSize: Int): UIO[Unit] =
    ZIO
      .when(state.subscriptionState.isSubscribed) {
        for {
          _          <- ZIO.foreachDiscard(state.assignedStreams)(_.outstandingPolls @@ queuePollsHistogram)
          queueSizes <- ZIO.foreach(state.assignedStreams)(_.queueSize)
          _          <- ZIO.foreachDiscard(queueSizes)(qs => queueSizeHistogram.update(qs))
          _          <- allQueueSizeHistogram.update(queueSizes.sum)
          _          <- pendingRequestsHistogram.update(state.pendingRequests.size)
          _          <- pendingCommitsHistogram.update(state.pendingCommits.size)
          _          <- subscriptionStateGauge.update(state.subscriptionState)
          _          <- commandQueueSizeHistogram.update(commandQueueSize)
          _          <- commitQueueSizeHistogram.update(commitQueueSize)
        } yield ()
      }
      .unit

}
