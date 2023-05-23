package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
import zio.stream.{ Stream, Take, UStream, ZStream }
import zio.{ durationInt, Hub, RIO, Ref, Scope, Task, UIO, ZIO, ZLayer }

private[internal] sealed trait RunloopState
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
  partitionHub: Hub[Take[Throwable, PartitionAssignment]],
  makeRunloop: Task[RunloopState.Started],
  diagnostics: Diagnostics
) {
  private def runloop(shouldStartIfNot: Boolean): Task[RunloopState] =
    runloopStateRef.updateSomeAndGetZIO { case RunloopState.NotStarted if shouldStartIfNot => makeRunloop }
  private def withRunloopZIO[R, A](shouldStartIfNot: Boolean)(f: Runloop => RIO[R, A]): RIO[R, A] =
    runloop(shouldStartIfNot).flatMap {
      case RunloopState.NotStarted       => ZIO.unit.asInstanceOf[RIO[R, A]]
      case RunloopState.Started(runloop) => f(runloop)
      case RunloopState.Stopped          => ZIO.unit.asInstanceOf[RIO[R, A]]
    }

  /**
   * No need to call `Runloop::stopConsumption` if the Runloop has not been started or has been stopped.
   *
   * Note:
   *   1. The `.orDie` is just here for compilation. It cannot happen.
   *   1. We do a 100 retries waiting 10ms between each to roughly take max 1s before to stop to retry. We want to avoid
   *      an infinite loop. We need this recursion because if the user calls `stopConsumption` before the Runloop is
   *      started, we need to wait for it to be started. Can happen if the user starts a consuming session in a forked
   *      fiber and immediately after forking, stops it. The Runloop will potentially not be started yet.
   */
  // noinspection SimplifyUnlessInspection
  def stopConsumption(retry: Int = 100, initialCall: Boolean = true): UIO[Unit] = {
    @inline def next: UIO[Unit] = stopConsumption(retry - 1, initialCall = false)

    runloop(shouldStartIfNot = false).orDie.flatMap {
      case RunloopState.Stopped          => ZIO.unit
      case RunloopState.Started(runloop) => runloop.stopConsumption
      case RunloopState.NotStarted =>
        if (retry <= 0) ZIO.unit
        else if (initialCall) next
        else next.delay(10.millis)
    }
  }

  /**
   * We're doing all of these things in this method so that the interface of this class is as simple as possible and
   * there's no mistake possible for the caller.
   *
   * The external world (Consumer) doesn't need to know how we "subscribe", "unsubscribe", etc. internally.
   */
  def subscribe(
    subscription: Subscription
  ): ZIO[Scope, Throwable, UStream[Take[Throwable, PartitionAssignment]]] =
    for {
      stream <- ZStream.fromHubScoped(partitionHub)
      // starts the Runloop if not already started
      _ <- withRunloopZIO(shouldStartIfNot = true)(_.addSubscription(subscription))
      _ <- ZIO.addFinalizer {
             withRunloopZIO(shouldStartIfNot = false)(_.removeSubscription(subscription)).orDie <*
               diagnostics.emit(Finalization.SubscriptionFinalized)
           }
    } yield stream

}

private[consumer] object RunloopAccess {
  type PartitionAssignment = (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])

  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    consumerAccess: ConsumerAccess,
    consumerSettings: ConsumerSettings
  ): ZIO[Scope, Throwable, RunloopAccess] =
    for {
      // This scope allows us to link the lifecycle of the Runloop and of the Hub to the lifecycle of the Consumer
      // When the Consumer is shutdown, the Runloop and the Hub will be shutdown too (before the consumer)
      consumerScope <- ZIO.scope
      partitionsHub <- ZIO
                         .acquireRelease(Hub.unbounded[Take[Throwable, PartitionAssignment]])(_.shutdown)
                         .provide(ZLayer.succeed(consumerScope))
      runloopStateRef <- Ref.Synchronized.make[RunloopState](RunloopState.NotStarted)
      makeRunloop = Runloop
                      .make(
                        hasGroupId = settings.hasGroupId,
                        consumer = consumerAccess,
                        pollTimeout = settings.pollTimeout,
                        diagnostics = diagnostics,
                        offsetRetrieval = settings.offsetRetrieval,
                        userRebalanceListener = settings.rebalanceListener,
                        restartStreamsOnRebalancing = settings.restartStreamOnRebalancing,
                        partitionsHub = partitionsHub,
                        consumerSettings = consumerSettings
                      )
                      .withFinalizer(_ => runloopStateRef.set(RunloopState.Stopped))
                      .map(RunloopState.Started.apply)
                      .provide(ZLayer.succeed(consumerScope))
    } yield new RunloopAccess(runloopStateRef, partitionsHub, makeRunloop, diagnostics)
}
