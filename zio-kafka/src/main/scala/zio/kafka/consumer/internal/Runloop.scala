package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.errors.RebalanceInProgressException
import org.apache.kafka.common.{ KafkaException, TopicPartition }
import zio._
import zio.kafka.consumer.Consumer.OffsetRetrieval
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
  commandQueue: Queue[Command],
  lastRebalanceEvent: Ref.Synchronized[Option[Runloop.RebalanceEvent]],
  val partitions: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  rebalancingRef: Ref[Boolean],
  diagnostics: Diagnostics,
  shutdownRef: Ref[Boolean],
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  restartStreamsOnRebalancing: Boolean,
  currentState: Ref[State]
) {
  private val isRebalancing = rebalancingRef.get
  private val isShutdown    = shutdownRef.get

  def newPartitionStream(
    tp: TopicPartition
  ): UIO[(TopicPartition, PartitionStreamControl, ZStream[Any, Throwable, ByteArrayCommittableRecord])] =
    for {
      _                   <- ZIO.logTrace(s"Creating partition stream for ${tp.toString}")
      interruptionPromise <- Promise.make[Throwable, Unit]
      drainQueue          <- Queue.unbounded[Take[Nothing, ByteArrayCommittableRecord]]
      stream = ZStream.logAnnotate("topic", tp.topic()) *>
                 ZStream.logAnnotate("partition", tp.partition().toString) *>
                 ZStream.finalizer(
                   ZIO.logDebug(s"Partition stream for ${tp.toString} has ended")
                 ) *> ZStream.repeatZIOChunkOption {
                   for {
                     request <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
                     _       <- commandQueue.offer(Request(tp, request)).unit
                     _       <- diagnostics.emitIfEnabled(DiagnosticEvent.Request(tp))
                     result  <- request.await
                   } yield result
                 }.interruptWhen(interruptionPromise)
                   .concat(
                     ZStream
                       .fromQueue(drainQueue)
                       .flattenTake
                   )
    } yield (tp, PartitionStreamControl(interruptionPromise, drainQueue), stream)

  def gracefulShutdown: UIO[Unit] =
    for {
      wasShutdown <- shutdownRef.getAndSet(true)
      state       <- currentState.get
      _           <- partitions.offer(Take.end).when(!wasShutdown)
      _           <- ZIO.foreachDiscard(state.assignedStreams) { case (_, control) => control.finishWith(Chunk.empty) }
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
    val trackRebalancing = RebalanceListener(
      onAssigned = (_, _) => rebalancingRef.set(false),
      onRevoked = (_, _) => rebalancingRef.set(true)
    )

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
      trackRebalancing ++ emitDiagnostics ++ revokeTopics ++ userRebalanceListener
    } else {
      trackRebalancing ++ emitDiagnostics ++ userRebalanceListener
    }
  }

  private def commit(offsets: Map[TopicPartition, Long]): Task[Unit] =
    for {
      p <- Promise.make[Throwable, Unit]
      _ <- commandQueue.offer(Commit(offsets, p)).unit
      _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
      _ <- p.await
    } yield ()

  private def doCommit(cmds: Chunk[Commit]): UIO[Unit] = {
    val offsets   = aggregateOffsets(cmds)
    val cont      = (e: Exit[Throwable, Unit]) => ZIO.foreachDiscard(cmds)(_.cont.done(e))
    val onSuccess = cont(Exit.succeed(())) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Success(offsets))
    val onFailure: Throwable => UIO[Unit] = {
      case _: RebalanceInProgressException =>
        ZIO.logDebug(s"Rebalance in progress, retrying commit for offsets ${offsets}") *>
          commandQueue.offerAll(cmds).unit // .delay(pollFrequency)
      case err =>
        cont(Exit.fail(err)) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Failure(offsets, err))
    }
    val callback = makeOffsetCommitCallback(onSuccess, onFailure)

    consumer.withConsumerM { c =>
      // We don't wait for the completion of the commit here, because it
      // will only complete once we poll again.
      ZIO.attempt(c.commitAsync(offsets.asJava, callback))
    }
      .catchAll(onFailure)
  }

  // Returns the highest offset to commit per partition
  private def aggregateOffsets(cmds: Chunk[Commit]): Map[TopicPartition, OffsetAndMetadata] = {
    val offsets = mutable.Map[TopicPartition, OffsetAndMetadata]()

    cmds.foreach { commit =>
      commit.offsets.foreach { case (tp, offset) =>
        val existing = offsets.get(tp).fold(-1L)(_.offset())

        if (existing < offset)
          offsets += tp -> new OffsetAndMetadata(offset + 1)
      }
    }

    offsets.toMap
  }

  private def makeOffsetCommitCallback(
    onSuccess: Task[Unit],
    onFailure: Exception => Task[Unit]
  ): OffsetCommitCallback =
    new OffsetCommitCallback {
      override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit =
        Unsafe.unsafe { implicit u =>
          runtime.unsafe.run(if (exception eq null) onSuccess else onFailure(exception)).getOrThrowFiberFailure()
        }
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
          control.finishWith(
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

    val endRevokedRequests = ZIO.foreachDiscard(requests.filter(req => isRevoked(req.tp))) { req =>
      ZIO.logTrace(s"Ending request for TP ${req.tp}") *> req.end.unit
    }

    endRevokedRequests *> revokeAction.as(
      Runloop.RevokeResult(unfulfilledRequests, newBufferedRecords, assignedStreams)
    )
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
    records: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[Runloop.FulfillResult] = {
    val acc = ChunkBuilder.make[Request]()
    val buf = mutable.Map.empty[TopicPartition, Chunk[ByteArrayConsumerRecord]]
    buf ++= bufferedRecords.recs

    var fulfillAction: UIO[_] = ZIO.unit

    pendingRequests.foreach { req =>
      val bufferedChunk = buf.getOrElse(req.tp, Chunk.empty)
      val reqRecs       = records.records(req.tp)

      if (bufferedChunk.isEmpty && reqRecs.isEmpty) {
        acc += req
      } else {
        val concatenatedChunk = bufferedChunk ++ Chunk.fromJavaIterable(reqRecs)

        fulfillAction = fulfillAction <* ZIO
          .logTrace(s"Fulfilling ${bufferedChunk.size} buffered records")
          .when(bufferedChunk.nonEmpty) *> req.succeed(concatenatedChunk.map { record =>
          CommittableRecord(
            record = record,
            commitHandle = commit,
            consumerGroupMetadata = getConsumerGroupMetadataIfAny
          )
        })
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
          ZIO.succeed(Set.empty[(TopicPartition, PartitionStreamControl)])
        else
          ZIO
            .foreach(pollResult.newlyAssigned)(newPartitionStream)
            .tap { newStreams =>
              ZIO.logTrace(s"Offering partition assignment ${pollResult.newlyAssigned}") *>
                partitions.offer(
                  Take.chunk(
                    Chunk.fromIterable(newStreams.map { case (tp, _, stream) => tp -> stream })
                  )
                )
            }
            .map(_.map { case (tp, control, _) =>
              tp -> control
            })
      newPendingCommits <- ZIO.filter(state.pendingCommits)(_.isPending)
    } yield state.copy(
      pendingRequests = pollResult.unfulfilledRequests,
      pendingCommits = newPendingCommits,
      bufferedRecords = pollResult.bufferedRecords,
      assignedStreams = pollResult.assignedStreams ++ newAssignedStreams
    )

  /**
   * After shutdown, we end all pending requests (ending their partition streams) and pause all partitions, but keep
   * executing commits and polling
   *
   * Buffered records for paused partitions will be removed to drain the stream as fast as possible.
   */
  private def handleShutdown(state: State, cmd: Command): Task[State] =
    cmd match {
      case req: Request =>
        req.end.as(state)
      case r @ Command.ChangeSubscription(_, _, _) =>
        r.succeed.as(state)
      case _ @Command.Commit(_, cont) =>
        cont.fail(new KafkaException("Consumer is shutting down")).as(state) // TODO can we just allow it?

    }

  private def handleOperational(state: State, cmd: Command): Task[State] =
    cmd match {
      case req: Request =>
        if (state.isSubscribed) {
          ZIO.succeed(state.addRequest(req))
        } else {
          req.end.as(state)
        }
      case cmd @ Command.Commit(_, _) =>
        doCommit(Chunk(cmd)).as(state.addCommit(cmd))
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

            // For manual subscriptions we have to do some manual work before starting the run loop
            case Subscription.Manual(topicPartitions) =>
              ZIO.attempt(c.assign(topicPartitions.asJava)) *>
                ZIO.foreach(topicPartitions)(newPartitionStream).flatMap { partitionStreams =>
                  partitions.offer(
                    Take.chunk(
                      Chunk.fromIterable(partitionStreams.map { case (tp, _, stream) =>
                        tp -> stream
                      })
                    )
                  )
                } *> {
                  offsetRetrieval match {
                    case OffsetRetrieval.Manual(getOffsets) =>
                      getOffsets(topicPartitions).flatMap { offsets =>
                        ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) }
                      }
                    case OffsetRetrieval.Auto(_) => ZIO.unit
                  }
                }

          }
      }
    }.foldZIO(
      e => ZIO.logErrorCause("Error subscribing", Cause.fail(e)) *> command.fail(e).as(state),
      _ => command.succeed.as(state.copy(subscription = command.subscription))
    )

  def run: ZIO[Scope, Nothing, Fiber.Runtime[Throwable, Any]] = {
    def processCommands(state: State, wait: Boolean): Task[State] = for {
      commands <- if (wait)
                    commandQueue.takeBetween(1, commandQueueSize).timeoutTo(Chunk.empty)(identity)(pollFrequency)
                  else commandQueue.takeAll // Gather available commands or return immediately if nothing in the queue

      isShutdown <- isShutdown
      updatedState <- ZIO.foldLeft(commands)(state) { case (s, cmd) =>
                        (if (isShutdown) handleShutdown(s, cmd) else handleOperational(s, cmd)) <*
                          diagnostics.emitIfEnabled(DiagnosticEvent.RunloopEvent(cmd))
                      }
    } yield updatedState

    def doPollIfPendingActions(state: State): Task[(State, Boolean)] =
      for {
        isRebalancing <- isRebalancing
        isInitialized = state.initialized || state.pendingRequests.nonEmpty
        shouldPoll =
          state.isSubscribed && (state.pendingRequests.nonEmpty || state.pendingCommits.nonEmpty || !isInitialized || isRebalancing)
        _ <-
          ZIO
            .logTrace(
              s"Starting poll with ${state.pendingRequests.size} pending requests and ${state.pendingCommits.size} pending commits"
            )
            .when(shouldPoll)
        newState <-
          if (shouldPoll) handlePoll(state) else ZIO.succeed(state)
      } yield (newState.copy(initialized = isInitialized), !shouldPoll)

    def loop(state: State, wait: Boolean): ZIO[Any, Throwable, Nothing] = processCommands(state, wait)
      .flatMap(doPollIfPendingActions)
      .flatMap { case (state, wait) => loop(state, wait) }

    loop(State.initial, false)
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
      private val cont: Promise[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
    ) extends Command {
      @inline def succeed(data: Chunk[ByteArrayCommittableRecord]): UIO[Boolean] = cont.succeed(data)
      @inline def end: UIO[Boolean]                                              = cont.fail(None)
      @inline def fail(throwable: Throwable): UIO[Boolean]                       = cont.fail(Some(throwable))
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
    restartStreamsOnRebalancing: Boolean
  ): ZIO[Scope, Throwable, Runloop] =
    for {
      rebalancingRef     <- Ref.make(false)
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
                  commandQueue,
                  lastRebalanceEvent,
                  partitions,
                  rebalancingRef,
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
  subscription: Option[Subscription],
  initialized: Boolean
) {
  def addCommit(c: Commit): State   = copy(pendingCommits = pendingCommits :+ c)
  def addRequest(r: Request): State = copy(pendingRequests = r +: pendingRequests)

  def addBufferedRecords(recs: BufferedRecords): State =
    copy(bufferedRecords = bufferedRecords ++ recs)

  def isSubscribed: Boolean = subscription.isDefined
}

object State {
  val initial: State = State(Chunk.empty, Chunk.empty, BufferedRecords.empty, Map.empty, None, false)
}
