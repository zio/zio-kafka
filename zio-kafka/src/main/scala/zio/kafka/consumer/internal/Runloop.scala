package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.{
  BufferedRecords,
  ByteArrayCommittableRecord,
  ByteArrayConsumerRecord,
  Command
}
import zio.kafka.consumer.{ CommittableRecord, RebalanceListener }
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
  requestQueue: Queue[Runloop.Request],
  commitQueue: Queue[Command.Commit],
  lastRebalanceEvent: Ref.Synchronized[Option[Runloop.RebalanceEvent]],
  val partitions: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  rebalancingRef: Ref[Boolean],
  diagnostics: Diagnostics,
  shutdownRef: Ref[Boolean],
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  subscribedRef: Ref[Boolean],
  restartStreamsOnRebalancing: Boolean,
  currentState: Ref[State]
) {
  private val isRebalancing = rebalancingRef.get
  private val isShutdown    = shutdownRef.get

  def newPartitionStream(
    tp: TopicPartition
  ): UIO[(TopicPartition, PartitionStreamControl, ZStream[Any, Throwable, ByteArrayCommittableRecord])] =
    for {
      interruptionPromise <- Promise.make[Throwable, Unit]
      drainQueue          <- Queue.unbounded[Take[Nothing, ByteArrayCommittableRecord]]
      stream = ZStream.repeatZIOChunkOption {
                 for {
                   request <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
                   _       <- requestQueue.offer(Runloop.Request(tp, request)).unit
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

    lazy val revokeTopics = RebalanceListener(
      onAssigned = (assigned, _) =>
        lastRebalanceEvent.updateZIO {
          case None =>
            ZIO.some(Runloop.RebalanceEvent.Assigned(assigned))
          case Some(Runloop.RebalanceEvent.Revoked(revokeResult)) =>
            ZIO.some(Runloop.RebalanceEvent.RevokedAndAssigned(revokeResult, assigned))
          case Some(_) =>
            ZIO.fail(new IllegalStateException(s"Multiple onAssigned calls on rebalance listener"))
        },
      onRevoked = (_, _) =>
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
            }
          }
        }
    )

    if (restartStreamsOnRebalancing) {
      trackRebalancing ++ emitDiagnostics ++ revokeTopics ++ userRebalanceListener
    } else {
      trackRebalancing ++ emitDiagnostics ++ userRebalanceListener
    }
  }

  def markSubscribed: UIO[Unit] = subscribedRef.set(true)

  def markUnsubscribed: UIO[Unit] = subscribedRef.set(false)

  private def commit(offsets: Map[TopicPartition, Long]): Task[Unit] =
    for {
      p <- Promise.make[Throwable, Unit]
      _ <- commitQueue.offer(Command.Commit(offsets, p)).unit
      _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
      _ <- p.await
    } yield ()

  private def doCommit(cmds: Chunk[Command.Commit]): UIO[Unit] = {
    val offsets   = aggregateOffsets(cmds)
    val cont      = (e: Exit[Throwable, Unit]) => ZIO.foreachDiscard(cmds)(_.cont.done(e))
    val onSuccess = cont(Exit.succeed(())) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Success(offsets))
    val onFailure: Throwable => UIO[Unit] = {
      case _: RebalanceInProgressException =>
        ZIO.logInfo(s"Rebalance in progress, retrying ${cmds.size.toString} commits") *>
          commitQueue.offerAll(cmds).unit
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
  private def aggregateOffsets(cmds: Chunk[Command.Commit]): Map[TopicPartition, OffsetAndMetadata] = {
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
    reqs: Chunk[Runloop.Request],
    bufferedRecords: BufferedRecords,
    currentAssignedStreams: Map[TopicPartition, PartitionStreamControl],
    revoked: TopicPartition => Boolean
  ): UIO[Runloop.RevokeResult] = {
    var acc = Chunk[Runloop.Request]()
    val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
    buf ++= bufferedRecords.recs

    val (revokedStreams, assignedStreams) =
      currentAssignedStreams.partition(es => revoked(es._1))

    val revokeAction: UIO[Unit] = ZIO.foreachDiscard(revokedStreams) { case (tp, control) =>
      val remaining = bufferedRecords.recs.getOrElse(tp, Chunk.empty)
      for {
        _ <- control.finishWith(
               remaining.map(
                 CommittableRecord(_, commit, getConsumerGroupMetadataIfAny)
               )
             )
      } yield ()
    }

    val reqsIt = reqs.iterator
    while (reqsIt.hasNext) {
      val req = reqsIt.next()
      if (revoked(req.tp)) {
        buf -= req.tp
      } else acc :+= req
    }

    revokeAction.as(Runloop.RevokeResult(acc, BufferedRecords.fromMutableMap(buf), assignedStreams))
  }

  /**
   * Fulfill pending requests with records retrieved from poll() call + buffered records
   *
   * @return
   *   Remaining pending requests and remaining/new buffered records
   */
  private def fulfillRequests(
    pendingRequests: Chunk[Runloop.Request],
    bufferedRecords: BufferedRecords,
    records: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[Runloop.FulfillResult] = {
    var acc = Chunk[Runloop.Request]()
    val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
    buf ++= bufferedRecords.recs

    var fulfillAction: UIO[_] = ZIO.unit

    val reqsIt = pendingRequests.iterator
    while (reqsIt.hasNext) {
      val req           = reqsIt.next()
      val bufferedChunk = buf.getOrElse(req.tp, Chunk.empty)
      val reqRecs       = records.records(req.tp)

      if (bufferedChunk.isEmpty && reqRecs.isEmpty) {
        acc +:= req
      } else {
        val concatenatedChunk = bufferedChunk ++
          Chunk.fromArray(
            reqRecs.toArray[ByteArrayConsumerRecord](Array.ofDim[ByteArrayConsumerRecord](reqRecs.size))
          )

        fulfillAction = fulfillAction *> req.succeed(concatenatedChunk.map { record =>
          CommittableRecord(
            record = record,
            commitHandle = commit,
            consumerGroupMetadata = getConsumerGroupMetadataIfAny
          )
        })
        buf -= req.tp
      }
    }

    fulfillAction.as(Runloop.FulfillResult(acc, BufferedRecords.fromMutableMap(buf)))
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

      if (recs.size > 0)
        builder += (tp -> Chunk.fromArray(
          recs.toArray(Array.ofDim[ByteArrayConsumerRecord](recs.size))
        ))
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

  private def doPoll(c: ByteArrayKafkaConsumer, requestedPartitions: Set[TopicPartition]) = {
    val pollTimeout =
      if (requestedPartitions.nonEmpty) this.pollTimeout.asJava
      else 0.millis.asJava

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

            val records = doPoll(c, requestedPartitions)

            // Check shutdown again after polling (which takes up to the poll timeout)
            ZIO.ifZIO(isShutdown)(
              onTrue = pauseAllPartitions(c).as(
                Runloop.PollResult(
                  Set(),
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
                  unrequestedRecords = bufferRecordsForUnrequestedPartitions(
                                         records,
                                         tpsInResponse -- remainingRequestedPartitions
                                       )

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
          ZIO.succeed(Set[(TopicPartition, PartitionStreamControl)]())
        else
          ZIO
            .foreach(pollResult.newlyAssigned)(newPartitionStream)
            .tap { newStreams =>
              partitions.offer(
                Take.chunk(
                  Chunk.fromIterable(newStreams.map { case (tp, _, stream) => tp -> stream })
                )
              )
            }
            .map(_.map { case (tp, control, _) =>
              tp -> control
            })
      newPendingCommits <-
        ZIO.ifZIO(isRebalancing)(
          onTrue = ZIO.succeed(state.pendingCommits),
          onFalse = doCommit(state.pendingCommits).when(state.pendingCommits.nonEmpty).as(Chunk.empty)
        )
    } yield State(
      pollResult.unfulfilledRequests,
      newPendingCommits,
      pollResult.bufferedRecords,
      pollResult.assignedStreams ++ newAssignedStreams
    )

  private def handleRequests(state: State, reqs: Chunk[Runloop.Request]): UIO[State] =
    ZIO.ifZIO(isRebalancing)(
      onTrue = if (restartStreamsOnRebalancing) {
        ZIO.foreachDiscard(reqs)(_.end).as(state)
      } else {
        ZIO.succeed(state.addRequests(reqs))
      },
      onFalse = consumer
        .withConsumer(_.assignment.asScala)
        .flatMap { assignment =>
          ZIO.foldLeft(reqs)(state) { (state, req) =>
            if (assignment.contains(req.tp))
              ZIO.succeed(state.addRequest(req))
            else
              req.end.as(state)
          }
        }
        .orElseSucceed(state.addRequests(reqs))
    )

  private def handleCommit(state: State, cmd: Command.Commit): UIO[State] =
    ZIO.ifZIO(isRebalancing)(
      onTrue = ZIO.succeed(state.addCommit(cmd)),
      onFalse = doCommit(Chunk(cmd)).as(state)
    )

  /**
   * After shutdown, we end all pending requests (ending their partition streams) and pause all partitions, but keep
   * executing commits and polling
   *
   * Buffered records for paused partitions will be removed to drain the stream as fast as possible.
   */
  private def handleShutdown(state: State, cmd: Command): Task[State] =
    cmd match {
      case Command.Poll =>
        // End all pending requests
        ZIO.foreachDiscard(state.pendingRequests)(_.end) *>
          handlePoll(state.copy(pendingRequests = Chunk.empty, bufferedRecords = BufferedRecords.empty))
      case Command.Requests(reqs) =>
        ZIO.foreachDiscard(reqs)(_.end).as(state)
      case cmd @ Command.Commit(_, _) =>
        handleCommit(state, cmd)
    }

  private def handleOperational(state: State, cmd: Command): Task[State] =
    cmd match {
      case Command.Poll =>
        // The consumer will throw an IllegalStateException if no call to subscribe
        ZIO.ifZIO(subscribedRef.get)(onTrue = handlePoll(state), onFalse = ZIO.succeed(state))
      case Command.Requests(reqs) =>
        handleRequests(state, reqs).flatMap { state =>
          // Optimization: eagerly poll if we have pending requests instead of waiting
          // for the next scheduled poll.
          if (state.pendingRequests.nonEmpty) handlePoll(state)
          else ZIO.succeed(state)
        }
      case cmd @ Command.Commit(_, _) =>
        handleCommit(state, cmd)
    }

  def run: ZIO[Scope, Nothing, Fiber.Runtime[Throwable, Unit]] =
    ZStream
      .mergeAll(3, 1)(
        ZStream(Command.Poll).repeat(Schedule.spaced(pollFrequency)),
        ZStream.fromQueue(requestQueue).mapChunks(c => Chunk.single(Command.Requests(c))),
        ZStream.fromQueue(commitQueue)
      )
      .runFoldZIO(State.initial) { (state, cmd) =>
        ZIO.ifZIO(isShutdown)(onTrue = handleShutdown(state, cmd), onFalse = handleOperational(state, cmd))
      }
      .onError(cause => partitions.offer(Take.failCause(cause)))
      .unit
      .forkScoped
}

private[consumer] object Runloop {
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]
  type ByteArrayConsumerRecord    = ConsumerRecord[Array[Byte], Array[Byte]]

  final case class Request(
    tp: TopicPartition,
    private val cont: Promise[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
  ) {
    @inline def succeed(data: Chunk[ByteArrayCommittableRecord]): UIO[Boolean] = cont.succeed(data)
    @inline def end: UIO[Boolean]                                              = cont.fail(None)
    @inline def fail(throwable: Throwable): UIO[Boolean]                       = cont.fail(Some(throwable))
  }
  final case class PollResult(
    newlyAssigned: Set[TopicPartition],
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Map[TopicPartition, PartitionStreamControl]
  )
  final case class RevokeResult(
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Map[TopicPartition, PartitionStreamControl]
  )
  final case class FulfillResult(
    unfulfilledRequests: Chunk[Runloop.Request],
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

  sealed abstract class Command
  object Command {
    final case class Requests(requests: Chunk[Request])                                         extends Command
    case object Poll                                                                            extends Command
    final case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command
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
      requestQueue       <- ZIO.acquireRelease(Queue.unbounded[Runloop.Request])(_.shutdown)
      commitQueue        <- ZIO.acquireRelease(Queue.unbounded[Command.Commit])(_.shutdown)
      lastRebalanceEvent <- Ref.Synchronized.make[Option[Runloop.RebalanceEvent]](None)
      partitions <- ZIO.acquireRelease(
                      Queue
                        .unbounded[
                          Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                        ]
                    )(_.shutdown)
      shutdownRef     <- Ref.make(false)
      currentStateRef <- Ref.make(State.initial)
      subscribedRef   <- Ref.make(false)
      runtime         <- ZIO.runtime[Any]
      runloop = new Runloop(
                  runtime,
                  hasGroupId,
                  consumer,
                  pollFrequency,
                  pollTimeout,
                  requestQueue,
                  commitQueue,
                  lastRebalanceEvent,
                  partitions,
                  rebalancingRef,
                  diagnostics,
                  shutdownRef,
                  offsetRetrieval,
                  userRebalanceListener,
                  subscribedRef,
                  restartStreamsOnRebalancing,
                  currentStateRef
                )
      _ <- runloop.run
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[Runloop.Request],
  pendingCommits: Chunk[Command.Commit],
  bufferedRecords: BufferedRecords,
  assignedStreams: Map[TopicPartition, PartitionStreamControl]
) {
  def addCommit(c: Command.Commit): State           = copy(pendingCommits = c +: pendingCommits)
  def addRequest(c: Runloop.Request): State         = copy(pendingRequests = c +: pendingRequests)
  def addRequests(c: Chunk[Runloop.Request]): State = copy(pendingRequests = c ++ pendingRequests)
  def addBufferedRecords(recs: BufferedRecords): State =
    copy(bufferedRecords = bufferedRecords ++ recs)

  def removeBufferedRecordsFor(tp: TopicPartition): State =
    copy(bufferedRecords = bufferedRecords.remove(tp))
}

object State {
  def initial: State = State(Chunk.empty, Chunk.empty, BufferedRecords.empty, Map.empty)
}
