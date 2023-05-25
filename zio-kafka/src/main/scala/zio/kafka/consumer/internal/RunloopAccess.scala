package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.{ PartitionAssignment, PartitionAssignmentsHub }
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
import zio.stream.{ Stream, Take, ZStream }
import zio.{ Chunk, Hub, Queue, RIO, Ref, Scope, Task, UIO, ZIO, ZLayer }

private[internal] sealed trait RunloopState {
  def withRunloop[R, E, A](f: Runloop => ZIO[R, E, A]): ZIO[R, E, A] =
    this match {
      case RunloopState.NotStarted       => ZIO.unit.asInstanceOf[ZIO[R, E, A]]
      case RunloopState.Started(runloop) => f(runloop)
      case RunloopState.Stopped          => ZIO.unit.asInstanceOf[ZIO[R, E, A]]
    }
}
private[internal] object RunloopState {
  case object NotStarted                     extends RunloopState
  final case class Started(runloop: Runloop) extends RunloopState
  case object Stopped                        extends RunloopState

  @inline def notStarted: RunloopState = NotStarted
  @inline def stopped: RunloopState    = Stopped
}

/**
 * This [[RunloopAccess]] is here to make the [[Runloop]] boot lazy: we only starts it when the user is starting a
 * consuming session.
 *
 * // TODO Jules: DOC TO UPDATE
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
  runloopStateRef: Ref.Synchronized[RunloopState],
  hubRef: Ref.Synchronized[PartitionAssignmentsHub],
  makeRunloop: Task[RunloopState.Started],
  makeHub: UIO[PartitionAssignmentsHub]
) {
  private def runloop(shouldStartIfNot: Boolean): Task[RunloopState] =
    runloopStateRef.updateSomeAndGetZIO { case RunloopState.NotStarted if shouldStartIfNot => makeRunloop }
  private def withRunloopZIO[R, A](shouldStartIfNot: Boolean)(f: Runloop => RIO[R, A]): RIO[R, A] =
    runloop(shouldStartIfNot).flatMap(_.withRunloop(f))

  /**
   * No need to call `Runloop::gracefulShutdown` if the Runloop has not been instantiated
   *
   * Note: the `.orDie` call is only here for compilation. It cannot happen because the `runloop.gracefulShutdown` is
   * only called if the `runloop` is already successfully instantiated.
   */
  val gracefulShutdown: UIO[Unit] = withRunloopZIO(shouldStartIfNot = false)(_.gracefulShutdown).orDie

  /**
   * The runloop needs to be started before to start the stream to the Hub
   */
  def subscribe(
    subscription: Subscription
  ): ZIO[Scope, Throwable, ZStream[Any, Nothing, Take[Throwable, Chunk[PartitionAssignment]]]] =
    for {
      _      <- runloop(shouldStartIfNot = true)
      hub    <- hubRef.updateSomeAndGetZIO { case null => makeHub }
      stream <- ZStream.fromHubScoped(hub)
      _      <- withRunloopZIO(shouldStartIfNot = false)(_.addSubscription(subscription))
      _      <- ZIO.logDebug("Added subscription to runloop")
    } yield stream

  /**
   * If the Runloop as not been started or has been stopped, there's no need to remove the subscription
   */
  def unsubscribe(subscription: Subscription): Task[Unit] =
    withRunloopZIO(shouldStartIfNot = false)(_.removeSubscription(subscription))

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
      runloopStateRef <- Ref.Synchronized.make[RunloopState](RunloopState.notStarted)
      hubRef          <- Ref.Synchronized.make[PartitionAssignmentsHub](null)
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
                      .withFinalizer { case (_, finalizer) => runloopStateRef.set(RunloopState.stopped) *> finalizer }
                      .map { case (runloop, _) => RunloopState.Started(runloop) }
                      .provide(ZLayer.succeed(scope))
      makeHub = ZStream
                  .fromQueue(partitionsQueue)
                  .map(_.exit)
                  .flattenExitOption
                  .toHub(hubCapacity)
                  .provide(ZLayer.succeed(scope))
    } yield new RunloopAccess(runloopStateRef, hubRef, makeRunloop, makeHub)
}
