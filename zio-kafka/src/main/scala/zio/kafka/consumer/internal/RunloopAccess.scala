package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.{ PartitionAssignment, PartitionAssignmentsHub }
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
import zio.stream.{ Stream, Take, UStream, ZStream }
import zio.{ Chunk, Hub, Queue, RIO, Ref, Scope, Task, UIO, ZIO, ZLayer }

private[internal] sealed trait RunloopState {
  final def withRunloop[R, E, A](f: Runloop => ZIO[R, E, A]): ZIO[R, E, A] =
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
}

/**
 * This [[RunloopAccess]] is here to make the [[Runloop]] instantiation/boot lazy: we only starts it when the user is
 * starting a consuming session.
 *
 * This is needed because of 2 things:
 *
 *   1. A Consumer can be used to do something else than consuming (e.g. fetching Kafka topics metadata)
 *   1. The [[Runloop]] has a timeout which is reached if no commands are processed for a certain amount of time. If the
 *      Runloop is started eagerly (when we instantiate a Consumer), then the timeout will be reached even if the user
 *      is still using the Consumer.
 *
 * Additional note for the future:
 *
 * This is less an issue now that we have removed the `RunloopTimeout` exception. It might be possible to remove this
 * `RunloopAccess` and start the `Runloop` eagerly. Reaching the timeout if the user does not consumer. Rebooting a new
 * `Runloop` if the user decides to finally consume with its `Consumer`. Tho, I don't know the
 * implication/complexity/feasibility of this change and it's not what I'm trying to achieve/fix here.
 */
private[consumer] final class RunloopAccess private (
  runloopStateRef: Ref.Synchronized[RunloopState],
  hubRef: Ref.Synchronized[PartitionAssignmentsHub],
  makeRunloop: Task[RunloopState.Started],
  makeHub: UIO[PartitionAssignmentsHub],
  diagnostics: Diagnostics
) {
  private def runloop(shouldStartIfNot: Boolean): Task[RunloopState] =
    runloopStateRef.updateSomeAndGetZIO { case RunloopState.NotStarted if shouldStartIfNot => makeRunloop }
  private def withRunloopZIO[R, A](shouldStartIfNot: Boolean)(f: Runloop => RIO[R, A]): RIO[R, A] =
    runloop(shouldStartIfNot).flatMap(_.withRunloop(f))

  /**
   * No need to call `Runloop::stopConsumption` if the Runloop has not been instantiated
   *
   * Note: the `.orDie` call is only here for compilation. It cannot happen because the `runloop.stopConsumption` is
   * only called if the `runloop` is already successfully instantiated.
   */
  val stopConsumption: UIO[Unit] = withRunloopZIO(shouldStartIfNot = false)(_.stopConsumption).orDie

  /**
   * We're doing all of these things in this method so that the interface of this class is as simple as possible and
   * there's no mistake possible for the caller.
   *
   * The external world (Consumer) doesn't need to know how we "subscribe", "unsubscribe", etc. internally.
   */
  def subscribe(
    subscription: Subscription
  ): ZIO[Scope, Throwable, UStream[Take[Throwable, Chunk[PartitionAssignment]]]] =
    for {
      _   <- runloop(shouldStartIfNot = true) // The runloop needs to be started before to start the stream to the Hub
      hub <- hubRef.updateSomeAndGetZIO { case null => makeHub }
      stream <- ZStream.fromHubScoped(hub)
      _      <- withRunloopZIO(shouldStartIfNot = false)(_.addSubscription(subscription))
      _ <- ZIO.addFinalizer {
             withRunloopZIO(shouldStartIfNot = false)(_.removeSubscription(subscription)).orDie <*
               diagnostics.emit(Finalization.SubscriptionFinalized)
           }
    } yield stream

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
      // This scope allows us to link the lifecycle of the Runloop and of the Hub to the lifecycle of the Consumer
      // When the Consumer is shutdown, the Runloop and the Hub will be shutdown too (before the consumer)
      scope <- ZIO.scope
      partitionsQueue <- ZIO
                           .acquireRelease(Queue.unbounded[Take[Throwable, PartitionAssignment]])(_.shutdown)
                           .provide(ZLayer.succeed(scope))
      runloopStateRef <- Ref.Synchronized.make[RunloopState](RunloopState.NotStarted)
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
                      .withFinalizer(_ => runloopStateRef.set(RunloopState.Stopped))
                      .map(RunloopState.Started.apply)
                      .provide(ZLayer.succeed(scope))
      makeHub = ZStream
                  .fromQueue(partitionsQueue)
                  .map(_.exit)
                  .flattenExitOption
                  .toHub(hubCapacity)
                  .provide(ZLayer.succeed(scope))
    } yield new RunloopAccess(runloopStateRef, hubRef, makeRunloop, makeHub, diagnostics)
}
