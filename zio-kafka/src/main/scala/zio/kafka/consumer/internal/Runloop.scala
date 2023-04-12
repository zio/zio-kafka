package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.{ OffsetRetrieval, RunloopTimeout }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.Command.{ Commit, Request, StopAllStreams, StopRunloop }
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.{ CommittableRecord, RebalanceConsumer, RebalanceListener, Subscription }
import zio.stream._

import java.util
import scala.jdk.CollectionConverters._

private[consumer] final class Runloop private (
  runtime: Runtime[Any],
  hasGroupId: Boolean,
  consumer: ConsumerAccess,
  pollTimeout: Duration,
  runloopTimeout: Duration,
  commandQueue: Queue[Command],
  lastRebalanceEvent: Ref.Synchronized[Option[Runloop.RebalanceEvent]],
  val partitions: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  diagnostics: Diagnostics,
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  restartStreamsOnRebalancing: Boolean,
  currentState: Ref[State]
) {

  private def newPartitionStream(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, commandQueue, diagnostics)

  /** Initiate a graceful shutdown. */
  def gracefulShutdown: UIO[Unit] =
    commandQueue.offer(Command.StopAllStreams).unit

  /** Wait until graceful shutdown completes. */
  def awaitShutdown: UIO[Unit] =
    for {
      state <- currentState.get
      _     <- ZIO.foreachDiscard(state.assignedStreams)(_.awaitCompleted())
    } yield ()

  def changeSubscription(
    subscription: Option[Subscription]
  ): Task[Unit] =
    Promise
      .make[Throwable, Unit]
      .flatMap { cont =>
        commandQueue.offer(Command.ChangeSubscription(subscription, cont)) *>
          cont.await
      }
      .unit
      .uninterruptible

  val rebalanceListener: RebalanceListener = {
    val emitDiagnostics = RebalanceListener(
      (assigned, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Assigned(assigned)),
      (revoked, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Revoked(revoked)),
      (lost, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Lost(lost))
    )

    def restartStreamsRebalancingListener = RebalanceListener(
      onAssigned = (assigned, _) =>
        ZIO.logDebug("Rebalancing completed") *>
          lastRebalanceEvent.updateZIO {
            case None =>
              ZIO.some(Runloop.RebalanceEvent.Assigned(assigned))
            case Some(Runloop.RebalanceEvent.Revoked(revokeResult)) =>
              ZIO.some(Runloop.RebalanceEvent.RevokedAndAssigned(revokeResult, assigned))
            case Some(_) =>
              ZIO.fail(new IllegalStateException(s"Multiple onAssigned calls on rebalance listener"))
          },
      onRevoked = (_, _) =>
        ZIO.logDebug("Rebalancing started") *>
          currentState.get.flatMap { state =>
            // End all streams
            endRevokedPartitions(
              state.pendingRequests,
              state.assignedStreams,
              isRevoked = _ => true
            ).flatMap { result =>
              lastRebalanceEvent.updateZIO {
                case None =>
                  ZIO.some(Runloop.RebalanceEvent.Revoked(result))
                case _ =>
                  ZIO.fail(
                    new IllegalStateException(
                      s"onRevoked called on rebalance listener with pending assigned event"
                    )
                  )
              }
            }
          }
    )

    if (restartStreamsOnRebalancing) {
      emitDiagnostics ++ restartStreamsRebalancingListener ++ userRebalanceListener
    } else {
      emitDiagnostics ++ userRebalanceListener
    }
  }

  private val commit: Map[TopicPartition, Long] => Task[Unit] =
    offsets =>
      for {
        p <- Promise.make[Throwable, Unit]
        _ <- commandQueue.offer(Commit(offsets, p)).unit
        _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await
      } yield ()

  private def doCommit(cmd: Commit): UIO[Unit] = {
    val offsets   = cmd.offsets.map { case (tp, offset) => tp -> new OffsetAndMetadata(offset + 1) }
    val cont      = (e: Exit[Throwable, Unit]) => cmd.cont.done(e).asInstanceOf[UIO[Unit]]
    val onSuccess = cont(Exit.unit) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Success(offsets))
    val onFailure: Throwable => UIO[Unit] = {
      case _: RebalanceInProgressException =>
        ZIO.logDebug(s"Rebalance in progress, retrying commit for offsets $offsets") *>
          commandQueue.offer(cmd).unit
      case err =>
        cont(Exit.fail(err)) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Failure(offsets, err))
    }
    val callback =
      new OffsetCommitCallback {
        override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit =
          Unsafe.unsafe { implicit u =>
            runtime.unsafe.run(if (exception eq null) onSuccess else onFailure(exception)).getOrThrowFiberFailure()
          }
      }

    // We don't wait for the completion of the commit here, because it
    // will only complete once we poll again.
    consumer.runloopAccess { c =>
      ZIO
        .attempt(c.commitAsync(offsets.asJava, callback))
        .catchAll(onFailure)
    }
  }

  /**
   * Does all needed to end revoked partitions:
   *   1. Complete the revoked assigned streams 2. Remove from the list of pending requests
   * @return
   *   New pending requests, new active assigned streams
   */
  private def endRevokedPartitions(
    pendingRequests: Chunk[Request],
    assignedStreams: Chunk[PartitionStreamControl],
    isRevoked: TopicPartition => Boolean
  ): UIO[Runloop.RevokeResult] = {
    val (revokedStreams, newAssignedStreams) =
      assignedStreams.partition(control => isRevoked(control.tp))

    ZIO
      .foreachDiscard(revokedStreams)(_.end())
      .as(
        Runloop.RevokeResult(
          pendingRequests = pendingRequests.filter(req => !isRevoked(req.tp)),
          assignedStreams = newAssignedStreams
        )
      )
  }

  /**
   * Offer records retrieved from poll() call to the streams.
   *
   * @return
   *   Remaining pending requests
   */
  private def offerRecordsToStreams(
    partitionStreams: Chunk[PartitionStreamControl],
    pendingRequests: Chunk[Request],
    ignoreRecordsForTps: Set[TopicPartition],
    polledRecords: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[Runloop.FulfillResult] = {
    type Record = CommittableRecord[Array[Byte], Array[Byte]]

    // The most efficient way to get the records from [[ConsumerRecords]] per
    // topic-partition, is by first getting the set of topic-partitions, and
    // then requesting the records per topic-partition.
    val tps           = polledRecords.partitions().asScala.toSet -- ignoreRecordsForTps
    val fulfillResult = Runloop.FulfillResult(pendingRequests = pendingRequests.filter(req => !tps.contains(req.tp)))
    val streams =
      if (tps.isEmpty) Chunk.empty else partitionStreams.filter(streamControl => tps.contains(streamControl.tp))

    if (streams.isEmpty) ZIO.succeed(fulfillResult)
    else {
      for {
        consumerGroupMetadata <- getConsumerGroupMetadataIfAny
        committableRecords = {
          val acc             = ChunkBuilder.make[(PartitionStreamControl, Chunk[Record])](streams.size)
          val streamsIterator = streams.iterator
          while (streamsIterator.hasNext) {
            val streamControl = streamsIterator.next()
            val tp            = streamControl.tp
            val records       = polledRecords.records(tp)
            if (!records.isEmpty) {
              val builder  = ChunkBuilder.make[Record](records.size())
              val iterator = records.iterator()
              while (iterator.hasNext) {
                val consumerRecord = iterator.next()
                builder +=
                  CommittableRecord[Array[Byte], Array[Byte]](
                    record = consumerRecord,
                    commitHandle = commit,
                    consumerGroupMetadata = consumerGroupMetadata
                  )
              }
              acc += (streamControl -> builder.result())
            }
          }
          acc.result()
        }
        _ <- ZIO
               .foreachDiscard(committableRecords) { case (streamControl, records) =>
                 streamControl.offerRecords(records)
               }
      } yield fulfillResult
    }
  }

  private def getConsumerGroupMetadataIfAny: UIO[Option[ConsumerGroupMetadata]] =
    if (hasGroupId) consumer.runloopAccess(c => ZIO.attempt(c.groupMetadata())).fold(_ => None, Some(_))
    else ZIO.none

  private def doSeekForNewPartitions(c: ByteArrayKafkaConsumer, tps: Set[TopicPartition]): Task[Set[TopicPartition]] =
    offsetRetrieval match {
      case OffsetRetrieval.Manual(getOffsets) =>
        getOffsets(tps)
          .tap(offsets => ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) })
          .when(tps.nonEmpty)
          .as(tps)

      case OffsetRetrieval.Auto(_) =>
        ZIO.succeed(Set.empty)
    }

  // Pause partitions for which there is no demand and resume those for which there is now demand
  private def resumeAndPausePartitions(
    c: ByteArrayKafkaConsumer,
    assignment: Set[TopicPartition],
    requestedPartitions: Set[TopicPartition]
  ): Unit = {
    val toResume = assignment intersect requestedPartitions
    val toPause  = assignment -- requestedPartitions

    if (toResume.nonEmpty) c.resume(toResume.asJava)
    if (toPause.nonEmpty) c.pause(toPause.asJava)
  }

  private def doPoll(c: ByteArrayKafkaConsumer) = {
    val records = c.poll(pollTimeout)

    if (records eq null) ConsumerRecords.empty[Array[Byte], Array[Byte]]() else records
  }

  private def handlePoll(state: State): Task[State] =
    for {
      _ <- currentState.set(state)
      pollResult <-
        consumer.runloopAccess { c =>
          ZIO.suspend {

            val prevAssigned        = c.assignment().asScala.toSet
            val requestedPartitions = state.pendingRequests.map(_.tp).toSet

            resumeAndPausePartitions(c, prevAssigned, requestedPartitions)

            val records = doPoll(c)

            val currentAssigned = c.assignment().asScala.toSet
            val newlyAssigned   = currentAssigned -- prevAssigned

            for {
              ignoreRecordsForTps <- doSeekForNewPartitions(c, newlyAssigned)

              rebalanceEvent <- lastRebalanceEvent.getAndSet(None)

              revokeResult <- rebalanceEvent match {
                                case Some(Runloop.RebalanceEvent.Revoked(result)) =>
                                  // If we get here, `restartStreamsOnRebalancing == true`
                                  // Use revoke result from endRevokedPartitions that was called previously in the rebalance listener
                                  ZIO.succeed(result)
                                case Some(Runloop.RebalanceEvent.RevokedAndAssigned(result, _)) =>
                                  // If we get here, `restartStreamsOnRebalancing == true`
                                  // Use revoke result from endRevokedPartitions that was called previously in the rebalance listener
                                  ZIO.succeed(result)
                                case Some(Runloop.RebalanceEvent.Assigned(_)) =>
                                  // If we get here, `restartStreamsOnRebalancing == true`
                                  // endRevokedPartitions was not called yet in the rebalance listener,
                                  // and all partitions should be revoked
                                  endRevokedPartitions(
                                    state.pendingRequests,
                                    state.assignedStreams,
                                    isRevoked = _ => true
                                  )
                                case None =>
                                  // End streams for partitions that are no longer assigned
                                  endRevokedPartitions(
                                    state.pendingRequests,
                                    state.assignedStreams,
                                    isRevoked = (tp: TopicPartition) => !currentAssigned.contains(tp)
                                  )
                              }

              startingTps = rebalanceEvent match {
                              case Some(_) =>
                                // If we get here, `restartStreamsOnRebalancing == true`,
                                // some partitions were revoked and/or assigned and
                                // all already assigned streams were ended.
                                // Therefore, all currently assigned tps are starting,
                                // either because they are restarting, or because they
                                // are new.
                                currentAssigned
                              case None =>
                                newlyAssigned
                            }

              _ <- diagnostics.emitIfEnabled {
                     val providedTps = records.partitions().asScala.toSet
                     DiagnosticEvent.Poll(
                       tpRequested = requestedPartitions,
                       tpWithData = providedTps,
                       tpWithoutData = requestedPartitions -- providedTps
                     )
                   }

            } yield Runloop.PollResult(
              startingTps = startingTps,
              pendingRequests = revokeResult.pendingRequests,
              assignedStreams = revokeResult.assignedStreams,
              records = records,
              ignoreRecordsForTps = ignoreRecordsForTps
            )
          }
        }
      startingStreams <-
        if (pollResult.startingTps.isEmpty) {
          ZIO.succeed(Chunk.empty[PartitionStreamControl])
        } else {
          ZIO
            .foreach(Chunk.fromIterable(pollResult.startingTps))(newPartitionStream)
            .tap { newStreams =>
              ZIO.logTrace(s"Offering partition assignment ${pollResult.startingTps}") *>
                partitions.offer(Take.chunk(Chunk.fromIterable(newStreams.map(_.tpStream))))
            }
        }
      runningStreams <- ZIO.filter(pollResult.assignedStreams)(_.isRunning)
      updatedStreams = runningStreams ++ startingStreams
      fulfillResult <- offerRecordsToStreams(
                         updatedStreams,
                         pollResult.pendingRequests,
                         pollResult.ignoreRecordsForTps,
                         pollResult.records
                       )
      updatedPendingCommits <- ZIO.filter(state.pendingCommits)(_.isPending)
    } yield State(
      pendingRequests = fulfillResult.pendingRequests,
      pendingCommits = updatedPendingCommits,
      assignedStreams = updatedStreams,
      subscription = state.subscription
    )

  private def handleCommand(state: State, cmd: Command): Task[State] =
    cmd match {
      case req: Request =>
        ZIO.succeed(state.addRequest(req))
      case cmd @ Command.Commit(_, _) =>
        doCommit(cmd).as(state.addCommit(cmd))
      case cmd @ Command.ChangeSubscription(subscription, _) =>
        handleChangeSubscription(cmd).flatMap { newAssignedStreams =>
          val newState = state.copy(
            assignedStreams = state.assignedStreams ++ newAssignedStreams,
            subscription = subscription
          )
          if (subscription.isDefined) ZIO.succeed(newState)
          else {
            // End all streams and pending requests
            endRevokedPartitions(
              newState.pendingRequests,
              newState.assignedStreams,
              isRevoked = _ => true
            ).map { revokeResult =>
              newState.copy(
                pendingRequests = revokeResult.pendingRequests,
                assignedStreams = revokeResult.assignedStreams
              )
            }
          }
        }
          .tapBoth(e => cmd.fail(e), _ => cmd.succeed)
          .uninterruptible
      case Command.StopAllStreams =>
        {
          for {
            _ <- ZIO.logDebug("Graceful shutdown")
            _ <- ZIO.foreachDiscard(state.assignedStreams)(_.end())
            _ <- partitions.offer(Take.end)
            _ <- ZIO.logTrace("Graceful shutdown initiated")
          } yield ()
        }.as(state.copy(pendingRequests = Chunk.empty))

      case _: Command.Control => ZIO.succeed(state)
    }

  /**
   * @return
   *   any created streams
   */
  private def handleChangeSubscription(
    command: Command.ChangeSubscription
  ): Task[Chunk[PartitionStreamControl]] =
    consumer.runloopAccess { c =>
      command.subscription match {
        case None =>
          ZIO
            .attempt(c.unsubscribe())
            .as(Chunk.empty)
        case Some(subscription) =>
          subscription match {
            case Subscription.Pattern(pattern) =>
              val rc = RebalanceConsumer.Live(c)
              ZIO
                .attempt(c.subscribe(pattern.pattern, rebalanceListener.toKafka(runtime, rc)))
                .as(Chunk.empty)
            case Subscription.Topics(topics) =>
              val rc = RebalanceConsumer.Live(c)
              ZIO
                .attempt(c.subscribe(topics.asJava, rebalanceListener.toKafka(runtime, rc)))
                .as(Chunk.empty)
            case Subscription.Manual(topicPartitions) =>
              // For manual subscriptions we have to do some manual work before starting the run loop
              for {
                _ <- ZIO.attempt(c.assign(topicPartitions.asJava))
                _ <- offsetRetrieval match {
                       case OffsetRetrieval.Manual(getOffsets) =>
                         getOffsets(topicPartitions).flatMap { offsets =>
                           ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) }
                         }
                       case OffsetRetrieval.Auto(_) => ZIO.unit
                     }
                partitionStreams <- ZIO.foreach(Chunk.fromIterable(topicPartitions))(newPartitionStream)
                _                <- partitions.offer(Take.chunk(partitionStreams.map(_.tpStream)))
              } yield partitionStreams
          }
      }
    }

  /**
   * Poll behavior:
   *   - Run until stop is set to true
   *   - Process commands as soon as they are queued, unless in the middle of polling
   *   - Process all currently queued commands before polling instead of one by one
   *   - Immediately after polling, if there are available commands, process them instead of waiting until some periodic
   *     trigger
   *   - Poll only when subscribed (leads to exceptions from the Apache Kafka Consumer if not)
   *   - Poll continuously when there are (still) unfulfilled requests or pending commits
   *   - Poll periodically when we are subscribed but do not have assigned streams yet. This happens after
   *     initialization and rebalancing
   */
  def run: ZIO[Scope, Throwable, Any] = {
    def logPollStart(state: State): UIO[Unit] =
      ZIO
        .logTrace(
          s"Starting poll with ${state.pendingRequests.size} pending requests and ${state.pendingCommits.size} pending commits"
        )

    ZStream
      .fromQueue(commandQueue)
      .timeoutFail[Throwable](RunloopTimeout)(runloopTimeout)
      .takeWhile(_ != StopRunloop)
      .runFoldChunksDiscardZIO(State.initial) { (state, commands) =>
        for {
          _            <- ZIO.logTrace(s"Processing ${commands.size} commands: ${commands.mkString(",")}")
          updatedState <- ZIO.foldLeft(commands)(state)(handleCommand)

          updatedStateAfterPoll <- if (updatedState.shouldPoll)
                                     logPollStart(updatedState) *> handlePoll(updatedState)
                                   else ZIO.succeed(updatedState)
          // Immediately poll again, after processing all new queued commands
          _ <- commandQueue.offer(Command.Poll).when(updatedStateAfterPoll.shouldPoll)
        } yield updatedStateAfterPoll
      }
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitions.offer(Take.failCause(cause)))
  }
}

private[consumer] object Runloop {
  private implicit final class StreamOps[R, E, A](private val stream: ZStream[R, E, A]) extends AnyVal {

    /**
     * Inlined, simplified and specialized for our needs version of [[ZSink.foldChunksZIO]]
     *
     * Code initially inspired by the implementation of [[ZStream.runFoldZIO]] with everything we don't need removed and
     * with chunking added
     */
    def runFoldChunksDiscardZIO[R1 <: R, E1 >: E, S](s: S)(f: (S, Chunk[A]) => ZIO[R1, E1, S]): ZIO[R1, E1, Unit] = {
      def reader(s: S): ZChannel[R1, E1, Chunk[A], Any, E1, Nothing, Unit] =
        ZChannel.readWithCause(
          (in: Chunk[A]) => ZChannel.fromZIO(f(s, in)).flatMap(reader),
          (err: Cause[E1]) => ZChannel.refailCause(err),
          (_: Any) => ZChannel.unit
        )

      stream.run(ZSink.fromChannel(reader(s)))
    }
  }

  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]

  // Internal parameters, should not be necessary to tune
  private val commandQueueSize = 1024

  private final case class PollResult(
    startingTps: Set[TopicPartition],
    pendingRequests: Chunk[Request],
    assignedStreams: Chunk[PartitionStreamControl],
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    ignoreRecordsForTps: Set[TopicPartition]
  )
  private final case class RevokeResult(
    pendingRequests: Chunk[Request],
    assignedStreams: Chunk[PartitionStreamControl]
  )
  private final case class FulfillResult(
    pendingRequests: Chunk[Request]
  )

  private sealed trait RebalanceEvent
  private object RebalanceEvent {
    final case class Revoked(revokeResult: Runloop.RevokeResult)  extends RebalanceEvent
    final case class Assigned(newlyAssigned: Set[TopicPartition]) extends RebalanceEvent
    final case class RevokedAndAssigned(
      revokeResult: Runloop.RevokeResult,
      newlyAssigned: Set[TopicPartition]
    ) extends RebalanceEvent
  }

  sealed trait Command
  object Command {
    // Used for internal control of the runloop
    sealed trait Control extends Command

    case object Poll           extends Control
    case object StopRunloop    extends Control
    case object StopAllStreams extends Control

    final case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command {
      @inline def isDone: UIO[Boolean]    = cont.isDone
      @inline def isPending: UIO[Boolean] = isDone.negate
    }

    final case class Request(tp: TopicPartition) extends Command

    final case class ChangeSubscription(
      subscription: Option[Subscription],
      cont: Promise[Throwable, Unit]
    ) extends Command {
      @inline def succeed: UIO[Boolean]                    = cont.succeed(())
      @inline def fail(throwable: Throwable): UIO[Boolean] = cont.fail(throwable)
    }
  }

  def apply(
    hasGroupId: Boolean,
    consumer: ConsumerAccess,
    pollTimeout: Duration,
    diagnostics: Diagnostics,
    offsetRetrieval: OffsetRetrieval,
    userRebalanceListener: RebalanceListener,
    restartStreamsOnRebalancing: Boolean,
    runloopTimeout: Duration
  ): ZIO[Scope, Throwable, Runloop] =
    for {
      commandQueue       <- ZIO.acquireRelease(Queue.bounded[Runloop.Command](commandQueueSize))(_.shutdown)
      lastRebalanceEvent <- Ref.Synchronized.make[Option[Runloop.RebalanceEvent]](None)
      partitions <- ZIO.acquireRelease(
                      Queue
                        .unbounded[
                          Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                        ]
                    )(_.shutdown)
      currentStateRef <- Ref.make(State.initial)
      runtime         <- ZIO.runtime[Any]
      runloop = new Runloop(
                  runtime,
                  hasGroupId,
                  consumer,
                  pollTimeout,
                  runloopTimeout,
                  commandQueue,
                  lastRebalanceEvent,
                  partitions,
                  diagnostics,
                  offsetRetrieval,
                  userRebalanceListener,
                  restartStreamsOnRebalancing,
                  currentStateRef
                )
      _ <- ZIO.logDebug("Starting Runloop")

      // Run the entire loop on the a dedicated thread to avoid executor shifts
      executor <- RunloopExecutor.newInstance
      fib      <- ZIO.onExecutor(executor)(runloop.run).forkScoped

      _ <- ZIO.addFinalizer(
             ZIO.logTrace("Shutting down Runloop") *>
               commandQueue.offer(StopAllStreams) *>
               commandQueue.offer(StopRunloop) *>
               fib.join.orDie <*
               ZIO.logDebug("Shut down Runloop")
           )
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[Request],
  pendingCommits: Chunk[Commit],
  assignedStreams: Chunk[PartitionStreamControl],
  subscription: Option[Subscription]
) {
  def addCommit(c: Commit): State   = copy(pendingCommits = pendingCommits :+ c)
  def addRequest(r: Request): State = copy(pendingRequests = pendingRequests :+ r)

  def isSubscribed: Boolean = subscription.isDefined

  def shouldPoll: Boolean =
    isSubscribed && (pendingRequests.nonEmpty || pendingCommits.nonEmpty || assignedStreams.isEmpty)
}

object State {
  val initial: State = State(
    pendingRequests = Chunk.empty,
    pendingCommits = Chunk.empty,
    assignedStreams = Chunk.empty,
    subscription = None
  )
}
