package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.{ OffsetRetrieval, RunloopTimeout }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.Command.{ CommitAvailable, Request, StopAllStreams, StopRunloop }
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.{ CommittableRecord, RebalanceConsumer, RebalanceListener, Subscription }
import zio.stream._

import java.util
import scala.jdk.CollectionConverters._

private[consumer] final class Runloop private (
  sameThreadRuntime: Runtime[Any],
  hasGroupId: Boolean,
  consumer: ConsumerAccess,
  pollTimeout: Duration,
  runloopTimeout: Duration,
  commandQueue: Queue[Command],
  commitQueue: Queue[Commit],
  rebalanceListenerInvoked: Ref.Synchronized[Boolean],
  rebalanceListenerCommits: Ref.Synchronized[Chunk[Commit]],
  val partitions: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  diagnostics: Diagnostics,
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  restartStreamsOnRebalancing: Boolean,
  endRevokedStreamsBeforeRebalance: Boolean,
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

  private val rebalanceListener: RebalanceListener = {
    val emitDiagnostics = RebalanceListener(
      (assigned, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Assigned(assigned)),
      (revoked, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Revoked(revoked)),
      (lost, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Lost(lost))
    )

    val endRevokedStreamsRebalancingListener = RebalanceListener(
      onAssigned = (assigned, rebalanceConsumer) =>
        for {
          alreadyInvoked <- rebalanceListenerInvoked.get
          _              <- ZIO.logDebug(s"${assigned.size} partitions are assigned")
          state          <- currentState.get
          streamsToEnd = if (restartStreamsOnRebalancing && !alreadyInvoked) state.assignedStreams else Chunk.empty
          pendingCommits <- endRevokedStreamsAndAwaitEnd(rebalanceConsumer, streamsToEnd)
          _              <- rebalanceListenerInvoked.set(true)
          _              <- rebalanceListenerCommits.update(_ ++ pendingCommits)
          _              <- ZIO.logTrace("onAssigned done")
        } yield (),
      onRevoked = (revokedTps, rebalanceConsumer) =>
        for {
          alreadyInvoked <- rebalanceListenerInvoked.get
          _              <- ZIO.logDebug(s"${revokedTps.size} partitions are revoked")
          state          <- currentState.get
          streamsToEnd = if (restartStreamsOnRebalancing && !alreadyInvoked) state.assignedStreams
                         else state.assignedStreams.filter(control => revokedTps.contains(control.tp))
          pendingCommits <- endRevokedStreamsAndAwaitEnd(rebalanceConsumer, streamsToEnd)
          _              <- rebalanceListenerInvoked.set(true)
          _              <- rebalanceListenerCommits.update(_ ++ pendingCommits)
          _              <- ZIO.logTrace("onRevoked done")
        } yield (),
      onLost = (lostTps, _) =>
        for {
          _     <- ZIO.logDebug(s"${lostTps.size} partitions are lost, interrupting the streams")
          state <- currentState.get
          streamsToInterrupt = state.assignedStreams.filter(control => lostTps.contains(control.tp))
          _ <- ZIO.foreachDiscard(streamsToInterrupt)(_.lost())
          _ <- rebalanceListenerInvoked.set(true)
          _ <- ZIO.logTrace(s"onLost done")
        } yield ()
    )

    emitDiagnostics ++ endRevokedStreamsRebalancingListener ++ userRebalanceListener
  }

  private def endRevokedStreamsAndAwaitEnd(
    rebalanceConsumer: RebalanceConsumer,
    streamsToEnd: Chunk[PartitionStreamControl]
  ): Task[Chunk[Commit]] =
    for {
      _ <- ZIO.foreachDiscard(streamsToEnd)(_.end())
      pendingCommits <-
        if (streamsToEnd.nonEmpty && endRevokedStreamsBeforeRebalance) {
          // When the queue is empty we still need to call commit (with 0 offsets) so that earlier commits can complete.
          // We can not use ZStream.fromQueue because that will emit nothing when the queue is empty.
          // TODO: add timeout; we can't postpone the rebalance for ever
          ZStream
            .fromZIO(commitQueue.takeAll)
            .repeat(Schedule.forever)
            .tap(doCommitsFromRebalanceListener(rebalanceConsumer))
            .takeUntilZIO(_ => ZIO.forall(streamsToEnd)(_.isCompleted))
            .runCollect
            .map(_.flatten)
        } else {
          ZIO.succeed(Chunk.empty)
        }
    } yield pendingCommits

  /**
   * Handle commits while waiting for revoked streams to end.
   *
   * We need to ensure the revoked streams can end. This is only possible if the commits these streams started complete.
   * The commits complete when the callback is invoked. The callback is invoked when the underlying consumer polls the
   * broker. This can be achieved by invoking `commitAsync`. Even when we pass no offsets, the broker will be polled and
   * callbacks will be called.
   */
  private def doCommitsFromRebalanceListener(
    rebalanceConsumer: RebalanceConsumer
  )(commits: Chunk[Commit]): Task[Unit] = {
    val (offsets, callback, onFailure) = asyncCommitParameters(commits)
    ZIO.logTrace(s"Async commit of ${offsets.size} offsets for ${commits.size} commits") *>
      rebalanceConsumer
        .commitAsync(offsets, callback)
        .catchAll(onFailure)
        .unit
  }

  private def handleCommits(state: State, commits: Chunk[Commit]): Task[State] = {
    val (offsets, callback, onFailure) = asyncCommitParameters(commits)
    consumer.withConsumerZIO { c =>
      // We don't wait for the completion of the commit here, because it
      // will only complete once we poll again.
      ZIO.attempt(c.commitAsync(offsets.asJava, callback))
    }
      .catchAll(onFailure)
      .unit
      .as(state.addCommits(commits))
  }

  private def asyncCommitParameters(
    commits: Chunk[Commit]
  ): (Map[TopicPartition, OffsetAndMetadata], OffsetCommitCallback, Throwable => UIO[Unit]) = {
    val offsets = commits.foldLeft(Map.empty[TopicPartition, Long]) { case (acc, commit) =>
      commit.offsets.foldLeft(acc) { case (acc, (tp, offset)) =>
        acc.updated(tp, acc.get(tp).map(_ max offset).getOrElse(offset))
      }
    }
    val offsetsWithMetaData = offsets.map { case (tp, offset) => tp -> new OffsetAndMetadata(offset + 1) }
    val cont                = (e: Exit[Throwable, Unit]) => ZIO.foreachDiscard(commits)(_.cont.done(e))
    val onSuccess = cont(Exit.unit) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Success(offsetsWithMetaData))
    val onFailure: Throwable => UIO[Unit] = {
      case _: RebalanceInProgressException =>
        for {
          _ <- ZIO.logDebug(s"Rebalance in progress, commit for offsets $offsets will be retried")
          _ <- commitQueue.offerAll(commits)
          _ <- commandQueue.offer(CommitAvailable)
        } yield ()
      case err: Throwable =>
        cont(Exit.fail(err)) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Failure(offsetsWithMetaData, err))
    }
    val callback =
      new OffsetCommitCallback {
        override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit =
          Unsafe.unsafe { implicit u =>
            sameThreadRuntime.unsafe
              .run(if (exception eq null) onSuccess else onFailure(exception))
              .getOrThrowFiberFailure()
          }
      }
    (offsetsWithMetaData, callback, onFailure)
  }

  /** This is the implementation behind the user facing api `Offset.commit`. */
  private val commit: Map[TopicPartition, Long] => Task[Unit] =
    offsets =>
      for {
        p <- Promise.make[Throwable, Unit]
        c = Commit(offsets, p)
        _ <- commitQueue.offer(c)
        _ <- commandQueue.offer(CommitAvailable)
        _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await
      } yield ()

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
        _ <- ZIO
               .foreachDiscard(streams) { streamControl =>
                 val tp = streamControl.tp
                 val records = {
                   val records  = polledRecords.records(tp)
                   val builder  = ChunkBuilder.make[CommittableRecord[Array[Byte], Array[Byte]]](records.size())
                   val iterator = records.iterator()
                   while (iterator.hasNext) {
                     val consumerRecord = iterator.next()
                     builder += CommittableRecord[Array[Byte], Array[Byte]](
                       record = consumerRecord,
                       commitHandle = commit,
                       consumerGroupMetadata = consumerGroupMetadata
                     )
                   }
                   builder.result()
                 }

                 if (records.nonEmpty) streamControl.offerRecords(records) else ZIO.unit
               }
      } yield fulfillResult
    }
  }

  private def getConsumerGroupMetadataIfAny: UIO[Option[ConsumerGroupMetadata]] =
    if (hasGroupId) consumer.withConsumer(_.groupMetadata()).fold(_ => None, Some(_))
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

  private def doPoll(c: ByteArrayKafkaConsumer): ConsumerRecords[Array[Byte], Array[Byte]] = {
    val records = c.poll(pollTimeout)

    if (records eq null) ConsumerRecords.empty[Array[Byte], Array[Byte]]() else records
  }

  private def handlePoll(state: State): Task[State] =
    for {
      _ <-
        ZIO.logTrace(
          s"Starting poll with ${state.pendingRequests.size} pending requests and ${state.pendingCommits.size} pending commits"
        )
      _ <- currentState.set(state)
      _ <- rebalanceListenerInvoked.set(false)
      _ <- rebalanceListenerCommits.set(Chunk.empty)
      pollResult <-
        consumer.withConsumerZIO { c =>
          val prevAssigned        = c.assignment().asScala.toSet
          val requestedPartitions = state.pendingRequests.map(_.tp).toSet

          resumeAndPausePartitions(c, prevAssigned, requestedPartitions)

          val records = doPoll(c)

          ZIO.ifZIO(rebalanceListenerInvoked.get)(
            onTrue = {
              // Some partitions were revoked, lost or assigned,
              // some new commits might have been initiated.

              val currentAssigned = c.assignment().asScala.toSet
              val newlyAssigned   = currentAssigned -- prevAssigned

              // When `restartStreamsOnRebalancing == true`,
              // all already assigned streams were ended.
              // Therefore, _all_ currently assigned tps are starting,
              // either because they are restarting, or because they
              // are new.
              val startingTps =
                if (restartStreamsOnRebalancing) currentAssigned
                else newlyAssigned

              for {
                ignoreRecordsForTps <- doSeekForNewPartitions(c, newlyAssigned)

                newCommits <- rebalanceListenerCommits.get

                _ <- diagnostics.emitIfEnabled {
                       val providedTps = records.partitions().asScala.toSet
                       DiagnosticEvent.Poll(
                         tpRequested = requestedPartitions,
                         tpWithData = providedTps,
                         tpWithoutData = requestedPartitions -- providedTps
                       )
                     }

              } yield Runloop.PollResult(newCommits, startingTps, records, ignoreRecordsForTps)
            },
            onFalse =
              // No change in assigned partitions, the fast track.
              ZIO.succeed(Runloop.PollResult(records))
          )
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
      runningStreams <- ZIO.filter(state.assignedStreams)(_.acceptsData)
      updatedStreams = runningStreams ++ startingStreams
      updatedPendingRequests = {
        val streamTps = updatedStreams.map(_.tp).toSet
        state.pendingRequests.filter(req => streamTps.contains(req.tp))
      }
      fulfillResult <- offerRecordsToStreams(
                         updatedStreams,
                         updatedPendingRequests,
                         pollResult.ignoreRecordsForTps,
                         pollResult.records
                       )
      updatedPendingCommits <- ZIO.filter(state.pendingCommits ++ pollResult.newCommits)(_.isPending)
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
      case cmd @ Command.ChangeSubscription(subscription, _) =>
        val newState = state.copy(subscription = subscription)
        handleChangeSubscription(cmd).flatMap { newAssignedStreams =>
          if (subscription.isDefined) {
            ZIO.succeed(newState.copy(assignedStreams = state.assignedStreams ++ newAssignedStreams))
          } else {
            // End all streams
            ZIO
              .foreachDiscard(state.assignedStreams)(_.end())
              .as(newState.copy(pendingRequests = Chunk.empty))
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
    consumer.withConsumerZIO { c =>
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
                .attempt(c.subscribe(pattern.pattern, rebalanceListener.toKafka(sameThreadRuntime, rc)))
                .as(Chunk.empty)
            case Subscription.Topics(topics) =>
              val rc = RebalanceConsumer.Live(c)
              ZIO
                .attempt(c.subscribe(topics.asJava, rebalanceListener.toKafka(sameThreadRuntime, rc)))
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
   *   - Empty the commitQueue (otherwise it would fill up when there are no rebalances)
   *   - Poll only when subscribed (leads to exceptions from the Apache Kafka Consumer if not)
   *   - Poll continuously when there are (still) unfulfilled requests or pending commits
   *   - Poll periodically when we are subscribed but do not have assigned streams yet. This happens after
   *     initialization and rebalancing
   */
  def run: ZIO[Scope, Throwable, Any] =
    ZStream
      .fromQueue(commandQueue)
      .timeoutFail[Throwable](RunloopTimeout)(runloopTimeout)
      .takeWhile(_ != StopRunloop)
      .runFoldChunksDiscardZIO(State.initial) { (state, commands) =>
        for {
          commits <- commitQueue.takeAll
          _ <- ZIO.logTrace(s"Processing ${commits.size} commits, ${commands.size} commands: ${commands.mkString(",")}")
          stateAfterCommits  <- handleCommits(state, commits)
          stateAfterCommands <- ZIO.foldLeft(commands)(stateAfterCommits)(handleCommand)

          updatedStateAfterPoll <- if (stateAfterCommands.shouldPoll) handlePoll(stateAfterCommands)
                                   else ZIO.succeed(stateAfterCommands)
          // Immediately poll again, after processing all new queued commands
          _ <- commandQueue.offer(Command.Poll).when(updatedStateAfterPoll.shouldPoll)
        } yield updatedStateAfterPoll
      }
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitions.offer(Take.failCause(cause)))
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
        ZChannel.readWith(
          (in: Chunk[A]) => ZChannel.fromZIO(f(s, in)).flatMap(reader),
          (err: E1) => ZChannel.fail(err),
          (_: Any) => ZChannel.unit
        )

      stream.run(ZSink.fromChannel(reader(s)))
    }
  }

  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]

  // Internal parameters, should not be necessary to tune
  private val CommandQueueSize = 1024
  private val CommitQueueSize  = 1024

  private final case class PollResult(
    newCommits: Chunk[Commit],
    startingTps: Set[TopicPartition],
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    ignoreRecordsForTps: Set[TopicPartition]
  )
  private object PollResult {
    def apply(records: ConsumerRecords[Array[Byte], Array[Byte]]): PollResult =
      PollResult(Chunk.empty, Set.empty, records, Set.empty)
  }

  private final case class FulfillResult(
    pendingRequests: Chunk[Request]
  )

  sealed trait Command
  object Command {
    // Used for internal control of the runloop
    sealed trait Control extends Command

    case object Poll            extends Control
    case object StopRunloop     extends Control
    case object StopAllStreams  extends Control
    case object CommitAvailable extends Control

    final case class Request(tp: TopicPartition) extends Command

    final case class ChangeSubscription(
      subscription: Option[Subscription],
      cont: Promise[Throwable, Unit]
    ) extends Command {
      @inline def succeed: UIO[Boolean]                    = cont.succeed(())
      @inline def fail(throwable: Throwable): UIO[Boolean] = cont.fail(throwable)
    }
  }

  final case class Commit(
    offsets: Map[TopicPartition, Long],
    cont: Promise[Throwable, Unit]
  ) {
    @inline def isDone: UIO[Boolean]    = cont.isDone
    @inline def isPending: UIO[Boolean] = isDone.negate
  }

  def apply(
    hasGroupId: Boolean,
    consumer: ConsumerAccess,
    pollTimeout: Duration,
    diagnostics: Diagnostics,
    offsetRetrieval: OffsetRetrieval,
    userRebalanceListener: RebalanceListener,
    restartStreamsOnRebalancing: Boolean,
    endRevokedStreamsBeforeRebalance: Boolean,
    runloopTimeout: Duration
  ): ZIO[Scope, Throwable, Runloop] =
    for {
      commandQueue             <- ZIO.acquireRelease(Queue.bounded[Runloop.Command](CommandQueueSize))(_.shutdown)
      commitQueue              <- ZIO.acquireRelease(Queue.bounded[Runloop.Commit](CommitQueueSize))(_.shutdown)
      rebalanceListenerInvoked <- Ref.Synchronized.make[Boolean](false)
      rebalanceListenerCommits <- Ref.Synchronized.make[Chunk[Commit]](Chunk.empty)
      partitions <- ZIO.acquireRelease(
                      Queue
                        .unbounded[
                          Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                        ]
                    )(_.shutdown)
      currentStateRef   <- Ref.make(State.initial)
      sameThreadRuntime <- ZIO.runtime[Any].provideLayer(SameThreadRuntimeLayer)
      runloop = new Runloop(
                  sameThreadRuntime,
                  hasGroupId,
                  consumer,
                  pollTimeout,
                  runloopTimeout,
                  commandQueue,
                  commitQueue,
                  rebalanceListenerInvoked,
                  rebalanceListenerCommits,
                  partitions,
                  diagnostics,
                  offsetRetrieval,
                  userRebalanceListener,
                  restartStreamsOnRebalancing,
                  endRevokedStreamsBeforeRebalance,
                  currentStateRef
                )
      _ <- ZIO.logDebug("Starting Runloop")

      // Run the entire loop on the blocking thread pool to avoid executor shifts
      fib <- ZIO.blocking(runloop.run).forkScoped

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
  def addCommits(c: Chunk[Commit]): State = copy(pendingCommits = pendingCommits ++ c)
  def addRequest(r: Request): State       = copy(pendingRequests = pendingRequests :+ r)

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
