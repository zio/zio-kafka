package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
import zio.stream.{ Stream, Take, UStream, ZStream }
import zio.{ Hub, Promise, Ref, Scope, UIO, URIO, ZIO, ZLayer }

private[internal] sealed trait RunloopState
private[internal] object RunloopState {

  /**
   * Why do we need a Promise here?
   *
   * If the user starts a forked consumption session and just after the fork, calls `consumer.stopConsumption`, the
   * consumption needs to be stopped even if the runloop is still booting up.
   *
   * For all the details, see discussion: https://github.com/zio/zio-kafka/pull/857#discussion_r1218434608
   */
  final case class Started(promise: Promise[Nothing, Runloop]) extends RunloopState {
    @inline def runloop: UIO[Runloop] = promise.await
  }
  case object NotStarted extends RunloopState
  case object Stopped    extends RunloopState
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
  makeRunloop: UIO[Runloop],
  diagnostics: Diagnostics
) {
  private def runloop(shouldStartIfNot: Boolean): URIO[Scope, RunloopState] =
    runloopStateRef.updateSomeAndGetZIO {
      case RunloopState.NotStarted if shouldStartIfNot =>
        for {
          promise <- Promise.make[Nothing, Runloop]
          _       <- makeRunloop.flatMap(promise.succeed).forkScoped
        } yield RunloopState.Started(promise)
    }
  private def withRunloopZIO[A](shouldStartIfNot: Boolean)(f: Runloop => UIO[A]): UIO[A] =
    ZIO.scoped {
      runloop(shouldStartIfNot).flatMap {
        case RunloopState.NotStarted => ZIO.unit.asInstanceOf[UIO[A]]
        case RunloopState.Stopped    => ZIO.unit.asInstanceOf[UIO[A]]
        case s: RunloopState.Started => s.runloop.flatMap(f)
      }
    }

  /**
   * No need to call `Runloop::stopConsumption` if the Runloop has not been started or has been stopped.
   */
  def stopConsumption: UIO[Unit] = withRunloopZIO(shouldStartIfNot = false)(_.stopConsumption)

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
             withRunloopZIO(shouldStartIfNot = false)(_.removeSubscription(subscription)) <*
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
                      .provide(ZLayer.succeed(consumerScope))
    } yield new RunloopAccess(runloopStateRef, partitionsHub, makeRunloop, diagnostics)
}
