package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignmentsHub
import zio.stream.{ Stream, Take, ZStream }
import zio.{ Chunk, Hub, Queue, RIO, Ref, Scope, Task, UIO, ZIO, ZLayer }

/**
 * This [[RunloopAccess]] is here to make the [[Runloop]] boot lazy: we only starts it when the user is starting a
 * consuming session.
 *
 * This is needed because of 2 things:
 *
 *   1. A Consumer can be used to do something else than consuming (e.g. fetching Kafka topics metadata)
 *   1. The [[Runloop]] has a timeout throwing a [[zio.kafka.consumer.Consumer.RunloopTimeout]] when reached. If the
 *      Runloop is started eagerly (when we instantiate a Consumer), then a user using a Consumer to do something else
 *      than consuming will get this [[zio.kafka.consumer.Consumer.RunloopTimeout]] exception and will not be able to
 *      use its Consumer.
 */
private[consumer] final class RunloopAccess private (
  runloopRef: Ref.Synchronized[Runloop],
  hubRef: Ref.Synchronized[PartitionAssignmentsHub],
  makeRunloop: Task[Runloop],
  makeHub: UIO[PartitionAssignmentsHub]
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

  /**
   * The runloop needs to be started before to start the stream to the Hub
   */
  val partitionAssignmentsHub: Task[PartitionAssignmentsHub] =
    runloop *> hubRef.updateSomeAndGetZIO { case null => makeHub }

  def withRunloopZIO[R, A](f: Runloop => RIO[R, A]): RIO[R, A] = runloop.flatMap(f)
}

private[consumer] object RunloopAccess {
  type PartitionAssignment     = (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])
  type PartitionAssignmentsHub = Hub[Take[Throwable, Chunk[PartitionAssignment]]]

  /**
   * We must supply a queue size for the partitionAssignments hub below. Under most circumstances, a value of 1 should
   * be sufficient, as runloop.partitions is already an unbounded queue. But if there is a large skew in speed of
   * consuming partition assignments (not the speed of consuming kafka messages) between the subscriptions, there may
   * arise a situation where the faster stream is 'blocked' from getting new partition assignments by the faster stream.
   * A value of 32 should be more than sufficient to cover this situation.
   */
  private final val hubCapacity: Int = 32

  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    consumerAccess: ConsumerAccess
  ): ZIO[Scope, Throwable, RunloopAccess] =
    for {
      scope <- ZIO.scope
      partitionsQueue <- ZIO
                           .acquireRelease(Queue.unbounded[Take[Throwable, PartitionAssignment]])(_.shutdown)
                           .provide(ZLayer.succeed(scope))
      runloopRef <- Ref.Synchronized.make[Runloop](null)
      hubRef     <- Ref.Synchronized.make[PartitionAssignmentsHub](null)
      makeRunloop = Runloop
                      .make(
                        hasGroupId = settings.hasGroupId,
                        consumer = consumerAccess,
                        pollTimeout = settings.pollTimeout,
                        diagnostics = diagnostics,
                        offsetRetrieval = settings.offsetRetrieval,
                        userRebalanceListener = settings.rebalanceListener,
                        restartStreamsOnRebalancing = settings.restartStreamOnRebalancing,
                        runloopTimeout = settings.runloopTimeout,
                        partitionsQueue = partitionsQueue
                      )
                      .provide(ZLayer.succeed(scope))
      makeHub = ZStream
                  .fromQueue(partitionsQueue)
                  .map(_.exit)
                  .flattenExitOption
                  .toHub(hubCapacity)
                  .provide(ZLayer.succeed(scope))
    } yield new RunloopAccess(runloopRef, hubRef, makeRunloop, makeHub)
}
