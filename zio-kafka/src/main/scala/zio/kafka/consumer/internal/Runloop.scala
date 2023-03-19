package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.{ OffsetRetrieval, RunloopTimeout }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.Command.{ Commit, Request }
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
  pollFrequency: Duration,
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
    for {
      wasShutdown <- shutdownRef.getAndSet(true)
      state       <- currentState.get
      _           <- partitions.offer(Take.end).when(!wasShutdown)
      _           <- ZIO.foreachDiscard(state.assignedStreams) { case (_, control) => control.end() }.when(!wasShutdown)
    } yield ()

  /** Wait until a graceful shutdown was completed. */
  def awaitShutdown: UIO[Unit] =
    for {
      _     <- shutdownRef.get.filterOrDie(identity)(new IllegalStateException("Was not shutdown"))
      state <- currentState.get
      _     <- ZIO.foreachDiscard(state.assignedStreams) { case (_, control) => control.awaitCompleted().ignore }
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
        ZIO.logDebug(s"Rebalance in progress, retrying commit for offsets ${offsets}") *>
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
    currentAssignedStreams: Map[TopicPartition, PartitionStreamControl],
    isRevoked: TopicPartition => Boolean
  ): UIO[Runloop.RevokeResult] = {
    val (revokedStreams, assignedStreams) =
      currentAssignedStreams.partition(es => isRevoked(es._1))

    val revokeAction: UIO[Unit] =
      ZIO.foreachDiscard(revokedStreams) { case (tp, control) =>
        val remaining = bufferedRecords.recs.getOrElse(tp, Chunk.empty)

        ZIO.logDebug(s"Revoking topic-partition ${tp} with ${remaining.size} remaining records") *>
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
                  Map[TopicPartition, PartitionStreamControl]()
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
      updatedPendingCommits    <- ZIO.filter(state.pendingCommits)(_.isPending)
      completedTopicPartitions <- ZIO.filter(pollResult.assignedStreams.values)(_.isCompleted).map(_.map(_.tp))
      updatedAssignedStreams =
        pollResult.assignedStreams -- completedTopicPartitions ++
          newAssignedStreams.map(control => control.tp -> control)
    } yield State(
      pendingRequests = pollResult.unfulfilledRequests,
      pendingCommits = updatedPendingCommits,
      bufferedRecords = pollResult.bufferedRecords,
      assignedStreams = updatedAssignedStreams,
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
    }

  private def handleOperational(state: State, cmd: Command): Task[State] =
    cmd match {
      case req: Request =>
        if (state.isSubscribed) {
          ZIO.succeed(state.addRequest(req))
        } else {
          ZIO.succeed(state)
        }
      case cmd @ Command.Commit(_, _) =>
        doCommit(cmd).as(state.addCommit(cmd))
      case cmd @ Command.ChangeSubscription(_, _, _) =>
        handleChangeSubscription(state, cmd).flatMap { state =>
          if (state.isSubscribed) {
            ZIO.succeed(state)
          } else {
            // End pending requests
            endRevoked(state.pendingRequests, state.bufferedRecords, state.assignedStreams, _ => true).as(
              state.copy(
                pendingRequests = Chunk.empty,
                assignedStreams = Map.empty,
                bufferedRecords = BufferedRecords.empty
              )
            )
          }
        }
    }

  private def handleChangeSubscription(
    state: State,
    command: Command.ChangeSubscription
  ): Task[State] =
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
    }.foldZIO(
      e => ZIO.logErrorCause("Error subscribing", Cause.fail(e)) *> command.fail(e).as(state),
      _ => command.succeed.as(state.copy(subscription = command.subscription))
    )

  def run: ZIO[Scope, Nothing, Fiber.Runtime[Throwable, Any]] = {
    def processCommands(state: State, wait: Boolean): Task[State] =
      for {
        commands <-
          if (wait) commandQueue.takeBetween(1, commandQueueSize).timeoutTo(Chunk.empty)(ZIO.identityFn)(pollFrequency)
          else commandQueue.takeAll // Gather available commands or return immediately if nothing in the queue

        isShutdown <- isShutdown
        handleCommand = if (isShutdown) handleShutdown _ else handleOperational _
        updatedState <- ZIO.foldLeft(commands)(state)(handleCommand)
      } yield updatedState

    def doPollIfPendingActions(state: State): Task[(State, Boolean)] = {
      def logPollStart: UIO[Unit] =
        ZIO
          .logTrace(
            s"Starting poll with ${state.pendingRequests.size} pending requests and ${state.pendingCommits.size} pending commits"
          )

      val shouldPoll =
        state.isSubscribed && (state.pendingRequests.nonEmpty || state.pendingCommits.nonEmpty || state.assignedStreams.isEmpty)
      if (shouldPoll) logPollStart *> handlePoll(state).map(_ -> false) else ZIO.succeed(state -> true)
    }

    def loop(state: State, wait: Boolean): ZIO[Any, Throwable, Nothing] =
      processCommands(state, wait)
        .flatMap(doPollIfPendingActions)
        .timeoutFail(RunloopTimeout)(runloopTimeout)
        .flatMap { case (state, wait) => loop(state, wait) }

    loop(State.initial, wait = true)
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitions.offer(Take.failCause(cause)))
      .forkScoped
  }
}

private[consumer] object Runloop {
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]
  type ByteArrayConsumerRecord    = ConsumerRecord[Array[Byte], Array[Byte]]

  // Internal parameters, should not be necessary to tune
  val commandQueueSize = 1024

  final case class PollResult(
    newlyAssigned: Set[TopicPartition],
    unfulfilledRequests: Chunk[Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Map[TopicPartition, PartitionStreamControl]
  )
  final case class RevokeResult(
    unfulfilledRequests: Chunk[Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Map[TopicPartition, PartitionStreamControl]
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

    def remove(partition: TopicPartition): BufferedRecords =
      BufferedRecords(recs - partition)

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
    pollFrequency: Duration,
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
      shutdownRef     <- ZIO.acquireRelease(Ref.make(false))(_.set(true) *> ZIO.logTrace("Set shutdownRef to true"))
      currentStateRef <- Ref.make(State.initial)
      runtime         <- ZIO.runtime[Any]
      runloop = new Runloop(
                  runtime,
                  hasGroupId,
                  consumer,
                  pollTimeout,
                  pollFrequency,
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
      _ <- ZIO.addFinalizer(ZIO.logDebug("Shut down Runloop"))
      _ <- runloop.run
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[Request],
  pendingCommits: Chunk[Commit],
  bufferedRecords: BufferedRecords,
  assignedStreams: Map[TopicPartition, PartitionStreamControl],
  subscription: Option[Subscription]
) {
  def addCommit(c: Commit): State   = copy(pendingCommits = pendingCommits :+ c)
  def addRequest(r: Request): State = copy(pendingRequests = pendingRequests :+ r)

  def addBufferedRecords(recs: BufferedRecords): State =
    copy(bufferedRecords = bufferedRecords ++ recs)

  def isSubscribed: Boolean = subscription.isDefined
}

object State {
  val initial: State = State(Chunk.empty, Chunk.empty, BufferedRecords.empty, Map.empty, None)
}
