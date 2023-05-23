package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Stream, Take }
import zio.{ Queue, RIO, Ref, Scope, Task, UIO, ZIO, ZLayer }

private[consumer] final class RunloopAccess private (
  runloopRef: Ref.Synchronized[Runloop],
  makeRunloop: Task[Runloop]
) {
  private val runloop: Task[Runloop] = runloopRef.updateSomeAndGetZIO { case null => makeRunloop }
  private val isMade: UIO[Boolean]   = runloopRef.get.map(_ ne null)

  /**
   * No need to call `Runloop::gracefulShutdown` if the Runloop has not been instantiated
   *
   * Note: the `.orDie` call is only here for compilation. It cannot happen because the
   * `runloop.map(_.gracefulShutdown)` is only called if the `runloop` is already successfully instantiated.
   */
  val gracefulShutdown: UIO[Unit] = ZIO.whenZIO(isMade)(runloop.flatMap(_.gracefulShutdown).orDie).unit

  def withRunloop[A](f: Runloop => A): Task[A]                 = runloop.map(f)
  def withRunloopZIO[R, A](f: Runloop => RIO[R, A]): RIO[R, A] = runloop.flatMap(f)
}

private[consumer] object RunloopAccess {
  type PartitionAssignment = (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])

  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    wrapper: ConsumerAccess,
    partitionsQueue: Queue[Take[Throwable, PartitionAssignment]]
  ): ZIO[Scope, Throwable, RunloopAccess] =
    for {
      scope      <- ZIO.scope
      runloopRef <- Ref.Synchronized.make[Runloop](null)
      scopeLayer = ZLayer.succeed(scope)
      makeRunloop = Runloop(
                      hasGroupId = settings.hasGroupId,
                      consumer = wrapper,
                      pollTimeout = settings.pollTimeout,
                      diagnostics = diagnostics,
                      offsetRetrieval = settings.offsetRetrieval,
                      userRebalanceListener = settings.rebalanceListener,
                      restartStreamsOnRebalancing = settings.restartStreamOnRebalancing,
                      runloopTimeout = settings.runloopTimeout,
                      partitionsQueue = partitionsQueue
                    ).provide(scopeLayer)
    } yield new RunloopAccess(runloopRef, makeRunloop)
}
