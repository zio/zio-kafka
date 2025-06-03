package zio.kafka.consumer.internal
import zio._

private[internal] class DummyMetrics extends ConsumerMetrics {
  override def observePoll(resumedCount: Int, pausedCount: Int, latency: zio.Duration, pollSize: Int)(implicit
    trace: Trace
  ): UIO[Unit] =
    ZIO.unit

  override def observeCommit(latency: zio.Duration)(implicit trace: Trace): UIO[Unit] = ZIO.unit
  override def observeAggregatedCommit(latency: zio.Duration, commitSize: NanoTime)(implicit trace: Trace): UIO[Unit] =
    ZIO.unit
  override def observeRebalance(
    currentlyAssignedCount: Int,
    assignedCount: Int,
    revokedCount: Int,
    lostCount: Int
  )(implicit trace: Trace): UIO[Unit] = ZIO.unit
  override def observeRunloopMetrics(
    state: Runloop.State,
    commandQueueSize: Int,
    commitQueueSize: Int,
    pendingCommits: Int
  )(implicit trace: Trace): UIO[Unit] = ZIO.unit
  override def observePollAuthError()(implicit trace: Trace): UIO[Unit] = ZIO.unit
}
