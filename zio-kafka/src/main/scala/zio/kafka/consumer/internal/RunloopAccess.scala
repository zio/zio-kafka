package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Stream, Take }
import zio.{ Queue, RIO, Ref, Scope, Task, UIO, ZIO, ZLayer }

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

  def withRunloopZIO[R, A](f: Runloop => RIO[R, A]): RIO[R, A] = runloop.flatMap(f)
}

private[consumer] object RunloopAccess {
  type PartitionAssignment = (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])

  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    consumerAccess: ConsumerAccess,
    partitionsQueue: Queue[Take[Throwable, PartitionAssignment]],
    scope: Scope
  ): Task[RunloopAccess] =
    for {
      runloopRef <- Ref.Synchronized.make[Runloop](null)
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
    } yield new RunloopAccess(runloopRef, makeRunloop)
}
