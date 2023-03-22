package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.{ OffsetRetrieval, RunloopTimeout }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.Command.{ Commit, Request, StopRunloop }
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.{ CommittableRecord, RebalanceConsumer, RebalanceListener, Subscription }
import zio.stream._

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

private[consumer] final class Runloop(
  runtime: Runtime[Any],
  hasGroupId: Boolean,
  consumer: ConsumerAccess,
  pollTimeout: Duration,
  runloopTimeout: Duration,
  commandQueue: Queue[Command],
  lastRebalanceEvent: Ref.Synchronized[Option[Runloop.RebalanceEvent]],
  val partitions: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  diagnostics: Diagnostics,
  shutdownRef: Ref[Boolean],
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  restartStreamsOnRebalancing: Boolean,
  currentState: Ref[State]
) {
  private val isShutdown = shutdownRef.get

  private def newPartitionStream(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, commandQueue, diagnostics)

  /** Initiate a graceful shutdown. */
  def gracefulShutdown: UIO[Unit] =
    ZIO
      .whenZIO(shutdownRef.getAndSet(true).negate) {
        for {
          state <- currentState.get
          _     <- ZIO.foreachDiscard(state.assignedStreams)(_.end())
          _     <- partitions.offer(Take.end)
        } yield ()
      }
      .unit

  /** Wait until graceful shutdown completes. */
  def awaitShutdown: UIO[Unit] =
    for {
      state <- currentState.get
      _     <- ZIO.foreachDiscard(state.assignedStreams)(_.awaitCompleted().ignore)
    } yield ()

  def changeSubscription(
    subscription: Option[Subscription],
    offsetRetrieval: OffsetRetrieval
  ): Task[Unit] =
    Promise
      .make[Throwable, Unit]
      .flatMap { cont =>
        commandQueue.offer(Command.ChangeSubscription(subscription, offsetRetrieval, cont)) *>
          cont.await
      }
      .unlessZIO(isShutdown)
      .unit
      .uninterruptible

  val rebalanceListener: RebalanceListener = {
    val emitDiagnostics = RebalanceListener(
      (assigned, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Assigned(assigned)),
      (revoked, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Revoked(revoked)),
      (lost, _) => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Lost(lost))
    )

    def revokeTopics = RebalanceListener(
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
            endRevoked(
              state.pendingRequests,
              state.bufferedRecords,
              state.assignedStreams,
              _ => true
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
              }.unlessZIO(isShutdown).unit
            }
          }
    )

    if (restartStreamsOnRebalancing) {
      emitDiagnostics ++ revokeTopics ++ userRebalanceListener
    } else {
      emitDiagnostics ++ userRebalanceListener
    }
  }

  private def commit(offsets: Map[TopicPartition, Long]): Task[Unit] =
    for {
      p <- Promise.make[Throwable, Unit]
      _ <- commandQueue.offer(Commit(offsets, p)).unit
      _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
      _ <- p.await
    } yield ()

  private def doCommit(cmd: Commit): UIO[Unit] = {
    val offsets   = cmd.offsets.map { case (tp, offset) => tp -> new OffsetAndMetadata(offset + 1) }
    val cont      = (e: Exit[Throwable, Unit]) => cmd.cont.done(e).asInstanceOf[UIO[Unit]]
    val onSuccess = cont(Exit.succeed(())) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Success(offsets))
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

    consumer.withConsumerM { c =>
      // We don't wait for the completion of the commit here, because it
      // will only complete once we poll again.
      ZIO.attempt(c.commitAsync(offsets.asJava, callback))
    }
      .catchAll(onFailure)
  }

  /**
   * Does all needed to end revoked partitions:
   *   1. Complete the revoked assigned streams 2. Remove from the list of pending requests 3. Remove from buffered
   *      records
   * @return
   *   New pending requests, new buffered records and active assigned streams
   */
  private def endRevoked(
    requests: Chunk[Request],
    bufferedRecords: BufferedRecords,
    currentAssignedStreams: Seq[PartitionStreamControl],
    isRevoked: TopicPartition => Boolean
  ): UIO[Runloop.RevokeResult] = {
    val (revokedStreams, assignedStreams) =
      currentAssignedStreams.partition(control => isRevoked(control.tp))

    val revokeAction: UIO[Unit] =
      ZIO.foreachDiscard(revokedStreams) { control =>
        val remaining = bufferedRecords.recs.getOrElse(control.tp, Chunk.empty)

        ZIO.logDebug(s"Revoking topic-partition ${control.tp} with ${remaining.size} remaining records") *>
          control.endWith(
            remaining.map(
              CommittableRecord(_, commit, getConsumerGroupMetadataIfAny)
            )
          )
      }

    val acc = ChunkBuilder.make[Request]()
    val buf = mutable.Map.empty[TopicPartition, Chunk[ByteArrayConsumerRecord]]
    buf ++= bufferedRecords.recs
    requests.foreach(req => if (isRevoked(req.tp)) buf -= req.tp else acc += req)
    val unfulfilledRequests = acc.result()
    val newBufferedRecords  = BufferedRecords.fromMutableMap(buf)

    revokeAction *>
      ZIO.succeed(Runloop.RevokeResult(unfulfilledRequests, newBufferedRecords, assignedStreams))
  }

  /**
   * Fulfill pending requests with records retrieved from poll() call + buffered records
   *
   * @return
   *   Remaining pending requests and remaining/new buffered records
   */
  private def fulfillRequests(
    pendingRequests: Chunk[Request],
    bufferedRecords: BufferedRecords,
    polledRecords: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[Runloop.FulfillResult] = {
    val acc = ChunkBuilder.make[Request]()
    val buf = mutable.Map.empty[TopicPartition, Chunk[ByteArrayConsumerRecord]]
    buf ++= bufferedRecords.recs

    var fulfillAction: UIO[_] = ZIO.unit

    pendingRequests.foreach { req =>
      val bufferedChunk = buf.getOrElse(req.tp, Chunk.empty)
      val reqRecs       = polledRecords.records(req.tp)

      if (bufferedChunk.isEmpty && reqRecs.isEmpty) {
        acc += req
      } else {
        val consumerGroupMetadata = getConsumerGroupMetadataIfAny
        val concatenatedChunk =
          (bufferedChunk ++ Chunk.fromJavaIterable(reqRecs)).map { record =>
            CommittableRecord(
              record = record,
              commitHandle = commit,
              consumerGroupMetadata = consumerGroupMetadata
            )
          }

        fulfillAction = fulfillAction <* ZIO
          .logTrace(s"Fulfilling ${bufferedChunk.size} buffered records")
          .when(bufferedChunk.nonEmpty) *> req.succeed(concatenatedChunk)
        buf -= req.tp
      }
    }
    val unfulfilledRequests = acc.result()
    val newBufferedRecords  = BufferedRecords.fromMutableMap(buf)

    fulfillAction.as(Runloop.FulfillResult(unfulfilledRequests, newBufferedRecords))
  }

  private def getConsumerGroupMetadataIfAny: Option[ConsumerGroupMetadata] =
    if (hasGroupId)
      try Some(consumer.consumer.groupMetadata())
      catch { case NonFatal(_) => None }
    else None

  private def bufferRecordsForUnrequestedPartitions(
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    unrequestedTps: Iterable[TopicPartition]
  ): BufferedRecords = {
    val builder = Map.newBuilder[TopicPartition, Chunk[ByteArrayConsumerRecord]]
    builder.sizeHint(unrequestedTps.size)

    val tpsIt = unrequestedTps.iterator
    while (tpsIt.hasNext) {
      val tp   = tpsIt.next()
      val recs = records.records(tp)

      if (recs.size > 0) {
        builder += (tp -> Chunk.fromJavaIterable(recs))
      }
    }

    BufferedRecords.fromMap(builder.result())
  }

  private def doSeekForNewPartitions(c: ByteArrayKafkaConsumer, tps: Set[TopicPartition]): Task[Unit] =
    offsetRetrieval match {
      case OffsetRetrieval.Manual(getOffsets) =>
        getOffsets(tps)
          .tap(offsets => ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) })
          .when(tps.nonEmpty)
          .unit

      case OffsetRetrieval.Auto(_) =>
        ZIO.unit
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

  private def pauseAllPartitions(c: ByteArrayKafkaConsumer) = ZIO.succeed {
    val currentAssigned = c.assignment()
    c.pause(currentAssigned)
  }

  private def handlePoll(state: State): Task[State] =
    for {
      _ <- currentState.set(state)
      pollResult <-
        consumer.withConsumerM { c =>
          ZIO.suspend {

            val prevAssigned        = c.assignment().asScala.toSet
            val requestedPartitions = state.pendingRequests.map(_.tp).toSet

            resumeAndPausePartitions(c, prevAssigned, requestedPartitions)

            val records = doPoll(c)

            // Check shutdown again after polling (which takes up to the poll timeout)
            ZIO.ifZIO(isShutdown)(
              onTrue = pauseAllPartitions(c).as(
                Runloop.PollResult(
                  Set.empty,
                  state.pendingRequests,
                  BufferedRecords.empty,
                  Seq.empty
                )
              ),
              onFalse = {
                val tpsInResponse   = records.partitions.asScala.toSet
                val currentAssigned = c.assignment().asScala.toSet

                for {
                  rebalanceEvent <- lastRebalanceEvent.getAndSet(None)

                  newlyAssigned = rebalanceEvent match {
                                    case Some(Runloop.RebalanceEvent.Assigned(assigned)) =>
                                      assigned
                                    case Some(
                                          Runloop.RebalanceEvent.RevokedAndAssigned(_, assigned)
                                        ) =>
                                      assigned
                                    case Some(Runloop.RebalanceEvent.Revoked(_)) =>
                                      currentAssigned -- prevAssigned
                                    case None =>
                                      currentAssigned -- prevAssigned
                                  }
                  remainingRequestedPartitions = rebalanceEvent match {
                                                   case Some(Runloop.RebalanceEvent.Revoked(_)) | Some(
                                                         Runloop.RebalanceEvent
                                                           .RevokedAndAssigned(_, _)
                                                       ) =>
                                                     // In case rebalancing restarted all partitions, we have to ignore
                                                     // all the requests as their promise were for the previous partition streams
                                                     Set.empty
                                                   case Some(Runloop.RebalanceEvent.Assigned(_)) =>
                                                     requestedPartitions
                                                   case None =>
                                                     requestedPartitions
                                                 }

                  // Discard unrequested records for newly assigned partitions when doing manual offset seeking
                  unrequestedRecords = offsetRetrieval match {
                                         case OffsetRetrieval.Manual(_) =>
                                           BufferedRecords.empty
                                         case _ =>
                                           bufferRecordsForUnrequestedPartitions(
                                             records,
                                             tpsInResponse -- remainingRequestedPartitions
                                           )
                                       }

                  _ <- doSeekForNewPartitions(c, newlyAssigned)

                  revokeResult <- rebalanceEvent match {
                                    case Some(Runloop.RebalanceEvent.Revoked(result)) =>
                                      ZIO.succeed(
                                        result.copy(
                                          bufferedRecords = result.bufferedRecords ++ unrequestedRecords
                                        )
                                      )
                                    case Some(
                                          Runloop.RebalanceEvent.RevokedAndAssigned(result, _)
                                        ) =>
                                      ZIO.succeed(
                                        result.copy(
                                          bufferedRecords = result.bufferedRecords ++ unrequestedRecords
                                        )
                                      )
                                    case Some(Runloop.RebalanceEvent.Assigned(_)) =>
                                      endRevoked(
                                        state.pendingRequests,
                                        state
                                          .addBufferedRecords(unrequestedRecords)
                                          .bufferedRecords,
                                        state.assignedStreams,
                                        _ => false // not treating any partitions as revoked, as endRevoked was called previously in the rebalance listener
                                      )
                                    case None =>
                                      endRevoked(
                                        state.pendingRequests,
                                        state
                                          .addBufferedRecords(unrequestedRecords)
                                          .bufferedRecords,
                                        state.assignedStreams,
                                        tp => !currentAssigned(tp)
                                      )
                                  }

                  fulfillResult <- fulfillRequests(
                                     revokeResult.unfulfilledRequests,
                                     revokeResult.bufferedRecords,
                                     records
                                   )
                  _ <- diagnostics.emitIfEnabled(
                         DiagnosticEvent.Poll(
                           requestedPartitions,
                           fulfillResult.bufferedRecords.partitions,
                           fulfillResult.unfulfilledRequests.map(_.tp).toSet
                         )
                       )
                } yield Runloop.PollResult(
                  newlyAssigned,
                  fulfillResult.unfulfilledRequests,
                  fulfillResult.bufferedRecords,
                  revokeResult.assignedStreams
                )
              }
            )
          }
        }
      newAssignedStreams <-
        if (pollResult.newlyAssigned.isEmpty)
          ZIO.succeed(Seq.empty[PartitionStreamControl])
        else
          ZIO
            .foreach(pollResult.newlyAssigned)(newPartitionStream)
            .tap { newStreams =>
              ZIO.logTrace(s"Offering partition assignment ${pollResult.newlyAssigned}") *>
                partitions.offer(Take.chunk(Chunk.fromIterable(newStreams.map(_.tpStream))))
            }
      runningStreams <- ZIO.filter(pollResult.assignedStreams)(_.isRunning)
      updatedStreams = runningStreams ++ newAssignedStreams
      updatedPendingCommits <- ZIO.filter(state.pendingCommits)(_.isPending)
    } yield State(
      pendingRequests = pollResult.unfulfilledRequests,
      pendingCommits = updatedPendingCommits,
      bufferedRecords = pollResult.bufferedRecords,
      assignedStreams = updatedStreams,
      subscription = state.subscription
    )

  /**
   * After shutdown, we end all pending requests (ending their partition streams) and pause all partitions, but keep
   * executing commits and polling
   *
   * Buffered records for paused partitions will be removed to drain the stream as fast as possible.
   */
  private def handleShutdown(state: State, cmd: Command): Task[State] =
    cmd match {
      case _: Request                    => /* Ignore requests during shutdown. */ ZIO.succeed(state)
      case r: Command.ChangeSubscription => r.succeed.as(state)
      case cmd: Command.Commit           => doCommit(cmd).as(state.addCommit(cmd))
      case _: Command.Control            => ZIO.succeed(state)
    }

  private def handleOperational(state: State, cmd: Command): Task[State] =
    cmd match {
      case req: Request =>
        ZIO.succeed(state.addRequest(req))
      case cmd @ Command.Commit(_, _) =>
        doCommit(cmd).as(state.addCommit(cmd))
      case cmd @ Command.ChangeSubscription(subscription, _, _) =>
        handleChangeSubscription(cmd)
          .as(state.copy(subscription = subscription))
          .flatMap { state =>
            if (state.isSubscribed) {
              ZIO.succeed(state)
            } else {
              // End pending requests
              endRevoked(state.pendingRequests, state.bufferedRecords, state.assignedStreams, _ => true).as(
                state.copy(
                  pendingRequests = Chunk.empty,
                  assignedStreams = Seq.empty,
                  bufferedRecords = BufferedRecords.empty
                )
              )
            }
          }
          .tapBoth(e => cmd.fail(e), _ => cmd.succeed)
          .uninterruptible
      case _: Command.Control => ZIO.succeed(state)
    }

  private def handleChangeSubscription(
    command: Command.ChangeSubscription
  ): Task[Unit] =
    consumer.withConsumerM { c =>
      command.subscription match {
        case None =>
          ZIO.attempt(c.unsubscribe())

        case Some(subscription) =>
          subscription match {
            case Subscription.Pattern(pattern) =>
              val rc = RebalanceConsumer.Live(c)
              ZIO.attempt(c.subscribe(pattern.pattern, rebalanceListener.toKafka(runtime, rc)))
            case Subscription.Topics(topics) =>
              val rc = RebalanceConsumer.Live(c)
              ZIO.attempt(c.subscribe(topics.asJava, rebalanceListener.toKafka(runtime, rc)))
            case Subscription.Manual(topicPartitions) =>
              // For manual subscriptions we have to do some manual work before starting the run loop
              ZIO.attempt(c.assign(topicPartitions.asJava)) *> {
                offsetRetrieval match {
                  case OffsetRetrieval.Manual(getOffsets) =>
                    getOffsets(topicPartitions).flatMap { offsets =>
                      ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) }
                    }
                  case OffsetRetrieval.Auto(_) => ZIO.unit
                }
              } *>
                ZIO.foreach(topicPartitions)(newPartitionStream).flatMap { partitionStreams =>
                  partitions.offer(Take.chunk(Chunk.fromIterable(partitionStreams.map(_.tpStream))))
                }
          }
      }
    }.unit

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
      .aggregateAsync(ZSink.collectAllN[Command](commandQueueSize))
      .runFoldZIO(State.initial) { case (state, commands) =>
        for {
          _          <- ZIO.logTrace(s"Processing ${commands.size} commands: ${commands.mkString(",")}")
          isShutdown <- isShutdown
          handleCommand = if (isShutdown) handleShutdown _ else handleOperational _
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
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]
  type ByteArrayConsumerRecord    = ConsumerRecord[Array[Byte], Array[Byte]]

  // Internal parameters, should not be necessary to tune
  private val commandQueueSize = 1024

  final case class PollResult(
    newlyAssigned: Set[TopicPartition],
    unfulfilledRequests: Chunk[Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Seq[PartitionStreamControl]
  )
  final case class RevokeResult(
    unfulfilledRequests: Chunk[Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Seq[PartitionStreamControl]
  )
  final case class FulfillResult(
    unfulfilledRequests: Chunk[Request],
    bufferedRecords: BufferedRecords
  )

  sealed trait RebalanceEvent
  object RebalanceEvent {
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

    case object Poll        extends Control
    case object StopRunloop extends Control

    final case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command {
      @inline def isDone: UIO[Boolean]    = cont.isDone
      @inline def isPending: UIO[Boolean] = isDone.negate
    }

    final case class Request(
      tp: TopicPartition,
      private val dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]]
    ) extends Command {
      @inline def succeed(data: Chunk[ByteArrayCommittableRecord]): UIO[Boolean] = dataQueue.offer(Take.chunk(data))
    }

    final case class ChangeSubscription(
      subscription: Option[Subscription],
      offsetRetrieval: OffsetRetrieval,
      cont: Promise[Throwable, Unit]
    ) extends Command {
      @inline def succeed: UIO[Boolean]                    = cont.succeed(())
      @inline def fail(throwable: Throwable): UIO[Boolean] = cont.fail(throwable)
    }
  }

  final case class BufferedRecords(recs: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]) {
    def partitions: Set[TopicPartition] = recs.keySet

    def ++(newRecs: BufferedRecords): BufferedRecords =
      BufferedRecords(newRecs.recs.foldLeft(recs) { case (acc, (tp, recs)) =>
        acc.get(tp) match {
          case Some(existingRecs) => acc + (tp -> (existingRecs ++ recs))
          case None               => acc + (tp -> recs)
        }
      })
  }

  object BufferedRecords {
    val empty: BufferedRecords = BufferedRecords(Map.empty)

    def fromMap(map: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]): BufferedRecords =
      BufferedRecords(map)

    def fromMutableMap(
      map: mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]
    ): BufferedRecords =
      BufferedRecords(map.toMap)
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
      shutdownRef     <- Ref.make(false)
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
                  shutdownRef,
                  offsetRetrieval,
                  userRebalanceListener,
                  restartStreamsOnRebalancing,
                  currentStateRef
                )
      _ <- ZIO.logDebug("Starting Runloop")

      // Run the entire loop on the blocking thread pool to avoid executor shifts
      fib <- ZIO.blocking(runloop.run).forkScoped

      _ <- ZIO.addFinalizer(
             ZIO.logTrace("Shutting down Runloop") *>
               shutdownRef.set(true) *>
               commandQueue.offer(StopRunloop) *>
               fib.join.orDie *>
               ZIO.logDebug("Shut down Runloop")
           )
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[Request],
  pendingCommits: Chunk[Commit],
  bufferedRecords: BufferedRecords,
  assignedStreams: Seq[PartitionStreamControl],
  subscription: Option[Subscription]
) {
  def addCommit(c: Commit): State   = copy(pendingCommits = pendingCommits :+ c)
  def addRequest(r: Request): State = copy(pendingRequests = pendingRequests :+ r)

  def addBufferedRecords(recs: BufferedRecords): State =
    copy(bufferedRecords = bufferedRecords ++ recs)

  def isSubscribed: Boolean = subscription.isDefined

  def shouldPoll: Boolean =
    isSubscribed && (pendingRequests.nonEmpty || pendingCommits.nonEmpty || assignedStreams.isEmpty)
}

object State {
  val initial: State = State(Chunk.empty, Chunk.empty, BufferedRecords.empty, Seq.empty, None)
}
