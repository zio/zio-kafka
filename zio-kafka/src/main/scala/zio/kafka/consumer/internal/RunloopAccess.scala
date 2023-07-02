package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.kafka.consumer.{ ConsumerSettings, InvalidSubscriptionUnion, Subscription }
import zio.stream.{ Stream, Take, UStream, ZStream }
import zio.{ durationInt, Duration, Hub, IO, Ref, Scope, UIO, ZIO, ZLayer }

private[internal] sealed trait RunloopState
private[internal] object RunloopState {
  case object NotStarted                     extends RunloopState
  final case class Started(runloop: Runloop) extends RunloopState
  case object Finalized                      extends RunloopState
}

/**
 * This [[RunloopAccess]] is here to make the [[Runloop]] instantiation/boot lazy: we only starts it when the user is
 * starting a consuming session.
 *
 * This is needed because a Consumer can be used to do something else than consuming (e.g. fetching Kafka topics
 * metadata)
 */
private[consumer] final class RunloopAccess private (
  runloopStateRef: Ref.Synchronized[RunloopState],
  partitionHub: Hub[Take[Throwable, PartitionAssignment]],
  makeRunloop: UIO[Runloop],
  diagnostics: Diagnostics
) {

  private def withRunloopZIO[E](
    requireRunning: Boolean
  )(whenRunning: Runloop => IO[E, Unit]): IO[E, Unit] =
    runloopStateRef.updateSomeAndGetZIO {
      case RunloopState.NotStarted if requireRunning => makeRunloop.map(RunloopState.Started.apply)
    }.flatMap {
      case RunloopState.NotStarted       => ZIO.unit
      case RunloopState.Started(runloop) => whenRunning(runloop)
      case RunloopState.Finalized        => ZIO.unit
    }

  /**
   * No need to call `Runloop::stopConsumption` if the Runloop has not been started or has been stopped.
   */
  def stopConsumption: UIO[Unit] = withRunloopZIO(requireRunning = false)(_.stopConsumption)

  /**
   * We're doing all of these things in this method so that the interface of this class is as simple as possible and
   * there's no mistake possible for the caller.
   *
   * The external world (Consumer) doesn't need to know how we "subscribe", "unsubscribe", etc. internally.
   */
  def subscribe(
    subscription: Subscription
  ): ZIO[Scope, InvalidSubscriptionUnion, UStream[Take[Throwable, PartitionAssignment]]] =
    for {
      stream <- ZStream.fromHubScoped(partitionHub)
      // starts the Runloop if not already started
      _ <- withRunloopZIO(requireRunning = true)(_.addSubscription(subscription))
      _ <- ZIO.addFinalizer {
             withRunloopZIO(requireRunning = false)(_.removeSubscription(subscription)) <*
               diagnostics.emit(Finalization.SubscriptionFinalized)
           }
    } yield stream

}

private[consumer] object RunloopAccess {
  type PartitionAssignment = (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])

  def make(
    settings: ConsumerSettings,
    consumerAccess: ConsumerAccess,
    diagnostics: Diagnostics = Diagnostics.NoOp
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
                        maxPollInterval = maxPollIntervalConfig(settings),
                        commitTimeout = settings.commitTimeout,
                        diagnostics = diagnostics,
                        offsetRetrieval = settings.offsetRetrieval,
                        userRebalanceListener = settings.rebalanceListener,
                        restartStreamsOnRebalancing = settings.restartStreamOnRebalancing,
                        partitionsHub = partitionsHub,
                        runloopTimeout = settings.runloopTimeout,
                        fetchStrategy = settings.fetchStrategy
                      )
                      .withFinalizer(_ => runloopStateRef.set(RunloopState.Finalized))
                      .provide(ZLayer.succeed(consumerScope))
    } yield new RunloopAccess(runloopStateRef, partitionsHub, makeRunloop, diagnostics)

  private def maxPollIntervalConfig(settings: ConsumerSettings): Duration = {
    def defaultMaxPollInterval: AnyRef = ConsumerConfig
      .configDef()
      .defaultValues()
      .getOrDefault(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000")
    val maxPollIntervalStr = settings.properties
      .getOrElse(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, defaultMaxPollInterval)
    maxPollIntervalStr.toString.toInt.millis
  }
}
