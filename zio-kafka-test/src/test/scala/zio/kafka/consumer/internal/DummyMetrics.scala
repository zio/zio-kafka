package zio.kafka.consumer.internal
import zio.{ UIO, ZIO }

private[internal] class DummyMetrics extends ConsumerMetrics {
  override def observePoll(resumedCount: Int, pausedCount: Int, latency: zio.Duration, pollSize: Int): UIO[Unit] =
    ZIO.unit

  override def observeCommit(latency: zio.Duration): UIO[Unit]                                 = ZIO.unit
  override def observeAggregatedCommit(latency: zio.Duration, commitSize: NanoTime): UIO[Unit] = ZIO.unit
  override def observeRebalance(
    currentlyAssignedCount: Int,
    assignedCount: Int,
    revokedCount: Int,
    lostCount: Int
  ): UIO[Unit] = ZIO.unit
  override def observeRunloopMetrics(
    state: Runloop.State,
    commandQueueSize: Int,
    commitQueueSize: Int,
    pendingCommits: Int
  ): UIO[Unit] = ZIO.unit
  override def observePollAuthError(): UIO[Unit] = ZIO.unit
}
