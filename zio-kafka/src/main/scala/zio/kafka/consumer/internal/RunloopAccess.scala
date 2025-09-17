package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer.Consumer.ConsumerDiagnostics
import zio.kafka.consumer.diagnostics.DiagnosticEvent
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.kafka.consumer._
import zio.kafka.diagnostics.Diagnostics
import zio.stream.{ Stream, Take, ZStream }

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
  diagnostics: ConsumerDiagnostics
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
   *
   * @returns
   *   A StreamControl which allows graceful shutdown of all streams created from this subscription
   */
  def subscribe(
    subscription: Subscription
  ): ZIO[Scope, InvalidSubscriptionUnion, StreamControl[Any, Nothing, Take[Throwable, PartitionAssignment]]] =
    for {
      ended                     <- Promise.make[Nothing, Unit] // For ending the stream of partition streams
      partitionAssignmentStream <- ZStream.fromHubScoped(partitionHub)
      // starts the Runloop if not already started
      _ <- withRunloopZIO(requireRunning = true)(_.addSubscription(subscription))
      _ <- ZIO.addFinalizer {
             withRunloopZIO(requireRunning = false)(_.removeSubscription(subscription).orDie) <*
               diagnostics.emit(DiagnosticEvent.SubscriptionFinalized)
           }
    } yield new StreamControl[Any, Nothing, Take[Throwable, PartitionAssignment]] {
      override def stream = partitionAssignmentStream.interruptWhen(
        ended
      ) // This also prevents any partitions assigned during a rebalance after initiating the graceful shutdown to be consumed
      override def end =
        ended.succeed(()).ignore *> withRunloopZIO(requireRunning = false)(_.endStreamsBySubscription(subscription))

    }

  def registerExternalCommits(externallyCommittedOffsets: OffsetBatch): Task[Unit] =
    withRunloopZIO(requireRunning = true)(_.registerExternalCommits(externallyCommittedOffsets))

}

private[consumer] object RunloopAccess {
  type PartitionAssignment = (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])

  def make(
    settings: ConsumerSettings,
    consumerAccess: ConsumerAccess,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp
  ): ZIO[Scope, Throwable, RunloopAccess] =
    for {
      runloopConfig <- RunloopConfig(settings)
      // This scope allows us to link the lifecycle of the Runloop and of the Hub to the lifecycle of the Consumer
      // When the Consumer is shutdown, the Runloop and the Hub will be shutdown too (before the consumer)
      consumerScope <- ZIO.scope
      partitionsHub <- ZIO
                         .acquireRelease(Hub.unbounded[Take[Throwable, PartitionAssignment]])(_.shutdown)
                         .provide(ZLayer.succeed(consumerScope))
      runloopStateRef <- Ref.Synchronized.make[RunloopState](RunloopState.NotStarted)
      makeRunloop = Runloop
                      .make(
                        settings = settings,
                        runloopConfig = runloopConfig,
                        diagnostics = diagnostics,
                        consumer = consumerAccess,
                        partitionsHub = partitionsHub
                      )
                      .interruptible
                      .withFinalizer(_ => runloopStateRef.set(RunloopState.Finalized))
                      .provide(ZLayer.succeed(consumerScope))
    } yield new RunloopAccess(runloopStateRef, partitionsHub, makeRunloop, diagnostics)

}
