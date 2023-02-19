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
  perPartitionChunkPrefetch: Int,
  pollQueue: Queue[Command.Poll.type]
) {
  private val isRebalancing = rebalancingRef.get
  private val isShutdown    = shutdownRef.get

  def newPartitionStream(
    tp: TopicPartition,
    waitBeforeStarting: UIO[Unit]
  ): UIO[(TopicPartition, PartitionStreamControl, ZStream[Any, Throwable, ByteArrayCommittableRecord])] =
    for {
      drainQueue <- Queue.unbounded[Take[Nothing, ByteArrayCommittableRecord]]
      completed  <- Promise.make[Nothing, Unit]
      annotations = Set(LogAnnotation("topic", tp.topic()), LogAnnotation("partition", tp.partition().toString))
      _ <- ZIO.logAnnotate(annotations) {
             ZIO.logInfo("Creating new partition stream")
           }
      control = PartitionStreamControl(tp, drainQueue, completed)
      stream =
        (ZStream.logAnnotate(annotations) *>
          ZStream.finalizer(control.completeStream) *>
          ZStream.fromZIO(waitBeforeStarting) *>
          ZStream.fromZIO(seekForNewPartitionStream(tp)) *>
          ZStream.repeatZIOChunkOption {
            for {
              request <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
              _       <- ZIO.logDebug(s"Queueing request")
              _       <- requestQueue.offer(Runloop.Request(tp, request)).unit
              _       <- diagnostics.emitIfEnabled(DiagnosticEvent.Request(tp))
              result <-
                request.await.tapError {
                  case Some(e) =>
                    ZIO.logErrorCause("Partition stream failed", Cause.fail(e))
                  case None =>
                    ZIO.logInfo(s"Partition stream ended")
                }
              _ <-
                ZIO.logDebug(
                  s"Got ${result.size} records. Last offset = ${result.map(_.offset.offset).max}"
                )
            } yield result
          }
            .viaFunction(s => if (perPartitionChunkPrefetch > 0) s.bufferChunks(perPartitionChunkPrefetch) else s)
            .concat(
              ZStream
                .fromQueue(drainQueue)
                .flattenTake
            ))
          .tapErrorCause(ZIO.logErrorCause(_))
    } yield (tp, control, stream)

  /*
  After rebalancing, the next fetch position is set to the last committed offset at that time, which may be lower than
  the last fetched offset that may still be in the previous partition stream's buffer. That is why we seek before
  requesting data for the partition.

  Edge case situation:
  - Consumer 1 gets a partition revoked but still has records buffered, consumer 2 starts processing and committing,
  meanwhile consumer 1 is also still committing, consumer 1 stops, rebalancing happens and consumer 2 resumes
  from the last committed position of consumer 1, which is lower than what it has already seen itself. This results
  in consumer 2 processing some records twice.
  The solution would be to also store our own committed offsets and take the max (broker committed, local committed).

  In case of manual offset retrieval, here is where we seek.
   */
  private def seekForNewPartitionStream(
    tp: TopicPartition
  ): Task[Any] =
    // We can get a "java.lang.IllegalStateException: You can only check the position for partitions assigned to this consumer."
    // from the position(tp) call when the partition is already revoked before starting, the `.option` handles this
    offsetRetrieval match {
      case OffsetRetrieval.Auto(_) if hasGroupId =>
        (consumer.withConsumer(_.position(tp)).option zip consumer
          .withConsumer(_.committed(Set(tp).asJava))
          .map(_.asScala.get(tp).flatMap(Option.apply).map(_.offset()))).tap {
          case (Some(nextToFetch), lastCommitted) =>
            lastCommitted match {
              case Some(lastCommitted) if lastCommitted > nextToFetch =>
                ZIO.logInfo(
                  s"Seeking to last committed offset by this consumer from partition stream before rebalancing: from ${nextToFetch} to ${lastCommitted}"
                ) *>
                  consumer.withConsumer(_.seek(tp, lastCommitted))
              case Some(lastCommitted) if lastCommitted < nextToFetch =>
                ZIO.logWarning(
                  s"Unexpected fetch position ${nextToFetch} for last committed offset ${lastCommitted}. This may happen after rebalancing."
                )
              case _ =>
                ZIO.unit
            }
          case _ =>
            // No position for this TP: we're not assigned this TP
            ZIO.unit
        }

      case OffsetRetrieval.Manual(getOffsets) =>
        getOffsets(Set(tp))
          .tap(offsets =>
            ZIO.foreachDiscard(offsets) { case (tp, offset) => consumer.withConsumer(_.seek(tp, offset)) }
          )
          .unit

      case _ =>
        ZIO.unit
    }

  def gracefulShutdown: UIO[Unit] =
    for {
      wasShutdown <- shutdownRef.getAndSet(true)
      _           <- partitions.offer(Take.end).when(!wasShutdown)
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
      onRevoked = (revoked, _) =>
        lastRebalanceEvent.updateZIO {
          case None =>
            ZIO.some(Runloop.RebalanceEvent.Revoked(revoked))
          case _ =>
            ZIO.fail(
              new IllegalStateException(
                s"onRevoked called on rebalance listener with pending assigned event"
              )
            )
        }
          .unlessZIO(isShutdown)
          .unit
    )

    trackRebalancing ++ emitDiagnostics ++ userRebalanceListener ++ revokeTopics
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
          commitQueue.offerAll(cmds).unit.delay(100.millis)
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

    var revokeAction: UIO[Unit] = ZIO.foreachDiscard(revokedStreams) { case (tp, control) =>
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
        revokeAction = revokeAction *> req.end.unit
      } else acc :+= req
    }

    revokeAction.as(Runloop.RevokeResult(acc, BufferedRecords.fromMutableMap(buf), assignedStreams, revokedStreams))
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
      pollResult <-
        consumer.withConsumerM { c =>
          ZIO.suspend {

            val prevAssigned        = c.assignment().asScala.toSet
            val requestedPartitions = state.pendingRequests.map(_.tp).toSet -- state.finishingStreams.keys

            val prevGroupGenerationId = getConsumerGroupMetadataIfAny

            resumeAndPausePartitions(c, prevAssigned, requestedPartitions)

            val records = doPoll(c, requestedPartitions)

            val newGroupGenerationId = getConsumerGroupMetadataIfAny

            // Check shutdown again after polling (which takes up to the poll timeout)
            ZIO.ifZIO(isShutdown)(
              onTrue = pauseAllPartitions(c) *>
                endRevoked(
                  state.pendingRequests,
                  state.bufferedRecords,
                  state.assignedStreams,
                  revoked = _ => true
                )
                  .as(
                    Runloop.PollResult(
                      newlyAssigned = Set(),
                      unfulfilledRequests = state.pendingRequests,
                      bufferedRecords = BufferedRecords.empty,
                      assignedStreams = Map[TopicPartition, PartitionStreamControl](),
                      finishingStreams = state.assignedStreams
                    )
                  ),
              onFalse = {
                val tpsInResponse   = records.partitions.asScala.toSet
                val currentAssigned = c.assignment().asScala.toSet

                for {
                  rebalanceEvent <- lastRebalanceEvent.getAndSet(None)

                  remainingRequestedPartitions = rebalanceEvent match {
                                                   case Some(Runloop.RebalanceEvent.Revoked(_)) |
                                                       Some(Runloop.RebalanceEvent.RevokedAndAssigned(_, _)) =>
                                                     // In case rebalancing restarted all partitions, we have to ignore
                                                     // all the requests as their promise were for the previous partition streams
                                                     Set.empty
                                                   case _ =>
                                                     requestedPartitions
                                                 }
                  // TODO what to do with buffered records in combination with seeking
                  unrequestedRecords =
                    bufferRecordsForUnrequestedPartitions(records, tpsInResponse -- remainingRequestedPartitions)

                  revokeResult <- rebalanceEvent match {
                                    case Some(Runloop.RebalanceEvent.Revoked(_)) |
                                        Some(Runloop.RebalanceEvent.RevokedAndAssigned(_, _)) =>
                                      ZIO.logDebug(
                                        s"New consumer group generation ID: ${c.groupMetadata().generationId()}"
                                      ) *>
                                        endRevoked(
                                          state.pendingRequests,
                                          state.bufferedRecords, // Discard new unrequested records, they are for the previous consumer group generation
                                          state.assignedStreams,
                                          revoked = _ => true
                                        )
                                    case Some(Runloop.RebalanceEvent.Assigned(newPartitions @ _)) =>
                                      ZIO.logDebug(
                                        s"New consumer group generation ID: ${c.groupMetadata().generationId()}"
                                      ) *>
                                        endRevoked(
                                          state.pendingRequests,
                                          state
                                            .addBufferedRecords(unrequestedRecords)
                                            .bufferedRecords,
                                          state.assignedStreams,
                                          revoked = _ => newGroupGenerationId != prevGroupGenerationId
                                        )
                                    case None =>
                                      endRevoked(
                                        state.pendingRequests,
                                        state
                                          .addBufferedRecords(unrequestedRecords)
                                          .bufferedRecords,
                                        state.assignedStreams,
                                        revoked = !currentAssigned(_)
                                      )
                                  }

                  newlyAssigned = rebalanceEvent match {
                                    case Some(Runloop.RebalanceEvent.Assigned(_)) =>
                                      // Just for newly assigned partitions
                                      currentAssigned -- prevAssigned
                                    case Some(Runloop.RebalanceEvent.RevokedAndAssigned(_, assigned @ _)) =>
                                      // Recreate all of the current assignment
                                      currentAssigned
                                    case Some(Runloop.RebalanceEvent.Revoked(_)) =>
                                      // We revoked, should recreate all of the current assignment
                                      currentAssigned
                                    case None =>
                                      // This should be an empty set, but just a catch-all
                                      currentAssigned -- prevAssigned
                                  }

                  _ <- ZIO.foreachDiscard(revokeResult.bufferedRecords.recs) { case (topicPartition, records) =>
                         ZIO.logAnnotate(
                           LogAnnotation("topic", topicPartition.topic),
                           LogAnnotation("partition", topicPartition.partition().toString)
                         ) {
                           ZIO.logInfo(
                             s"Buffered ${records.size} unrequested records with latest offset ${records.last.offset()}"
                           )
                         }
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
                  revokeResult.assignedStreams,
                  state.finishingStreams ++ revokeResult.revokedStreams
                )
              }
            )
          }
        }
      updatedFinishingStreams <-
        ZIO
          .filterNot(pollResult.finishingStreams.toSeq) { case (_, control) => control.streamCompleted.isDone }
          .map(_.toMap)
      newAssignedStreams <- createNewPartitionStreams(pollResult.newlyAssigned, updatedFinishingStreams)
      newPendingCommits <-
        ZIO.ifZIO(isRebalancing)(
          onTrue = ZIO.succeed(state.pendingCommits),
          onFalse = doCommit(state.pendingCommits).when(state.pendingCommits.nonEmpty).as(Chunk.empty)
        )
    } yield State(
      pollResult.unfulfilledRequests,
      newPendingCommits,
      pollResult.bufferedRecords,
      pollResult.assignedStreams ++ newAssignedStreams,
      updatedFinishingStreams
    )

  private def createNewPartitionStreams(
    newlyAssigned: Set[TopicPartition],
    finishingStreams: Map[TopicPartition, PartitionStreamControl]
  ): UIO[Set[(TopicPartition, PartitionStreamControl)]] =
    if (newlyAssigned.isEmpty)
      ZIO.succeed(Set[(TopicPartition, PartitionStreamControl)]())
    else {
      ZIO
        .foreach(newlyAssigned)(tp =>
          newPartitionStream(
            tp,
            finishingStreams
              .get(tp)
              .map(_.streamCompleted.await <* ZIO.logInfo(s"Done awaiting completion of previous stream"))
              .getOrElse(ZIO.unit)
          )
        )
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
    }

  /*
  When getting requests during rebalancing, end the partition stream. Otherwise allow the request unless
  we're not assigned the TP or there's a revoked stream that is not yet done draining.
   */
  private def handleRequests(state: State, reqs: Chunk[Runloop.Request]): UIO[State] =
    ZIO.ifZIO(isRebalancing)(
      onTrue = ZIO.foreachDiscard(reqs)(_.end).as(state),
      onFalse = consumer
        .withConsumer(_.assignment.asScala)
        .flatMap { assignment =>
          ZIO.foldLeft(reqs)(state) { (state, req) =>
            val partitionIsAssigned = assignment.contains(req.tp)
            val previousStreamFinished =
              state.finishingStreams.get(req.tp).map(_.streamCompleted.isDone).getOrElse(ZIO.succeed(true))
            ZIO.ifZIO(previousStreamFinished.map(_ && partitionIsAssigned))(
              onTrue = ZIO.succeed(state.addRequest(req)),
              onFalse = req.end.as(state)
            )
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
        handlePoll(state.copy(bufferedRecords = BufferedRecords.empty))
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
          // Optimization: eagerly enqueue a poll if we have pending requests instead of waiting
          // Do not execute handlePoll directly so that other queued commands get a fair treatment
          if (state.pendingRequests.nonEmpty) {
            pollQueue.offer(Command.Poll).as(state)
          } else ZIO.succeed(state)
        }
      case cmd @ Command.Commit(_, _) =>
        handleCommit(state, cmd)
    }

  def run: ZIO[Scope, Nothing, Fiber.Runtime[Throwable, Unit]] =
    (ZStream
      .repeatZIOWithSchedule(
//        ZIO.debug("Offering to poll queue") *>
        pollQueue.offer(Command.Poll),
        Schedule.spaced(pollFrequency)
      )
      .runDrain zipPar ZStream
      .mergeAll(3, 1)(
        ZStream.fromQueue(pollQueue),
        ZStream.fromQueue(requestQueue).mapChunks(c => Chunk.single(Command.Requests(c))),
        ZStream.fromQueue(commitQueue)
      )
      .runFoldZIO(State.initial) { (state, cmd) =>
        ZIO.ifZIO(isShutdown)(onTrue = handleShutdown(state, cmd), onFalse = handleOperational(state, cmd))
      }
      .onError(cause => partitions.offer(Take.failCause(cause)))
      .unit).forkScoped
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
    assignedStreams: Map[TopicPartition, PartitionStreamControl],
    finishingStreams: Map[TopicPartition, PartitionStreamControl]
  )
  final case class RevokeResult(
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: BufferedRecords,
    assignedStreams: Map[TopicPartition, PartitionStreamControl],
    revokedStreams: Map[TopicPartition, PartitionStreamControl]
  )
  final case class FulfillResult(
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: BufferedRecords
  )

  sealed trait RebalanceEvent
  object RebalanceEvent {
    final case class Revoked(revoked: Set[TopicPartition])        extends RebalanceEvent
    final case class Assigned(newlyAssigned: Set[TopicPartition]) extends RebalanceEvent
    final case class RevokedAndAssigned(revoked: Set[TopicPartition], newlyAssigned: Set[TopicPartition])
        extends RebalanceEvent
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
    perPartitionChunkPrefetch: Int
  ): ZIO[Scope, Throwable, Runloop] =
    for {
      rebalancingRef     <- Ref.make(false)
      requestQueue       <- ZIO.acquireRelease(Queue.unbounded[Runloop.Request])(_.shutdown)
      commitQueue        <- ZIO.acquireRelease(Queue.unbounded[Command.Commit])(_.shutdown)
      pollQueue          <- ZIO.acquireRelease(Queue.unbounded[Command.Poll.type])(_.shutdown)
      lastRebalanceEvent <- Ref.Synchronized.make[Option[Runloop.RebalanceEvent]](None)
      partitions <- ZIO.acquireRelease(
                      Queue
                        .unbounded[
                          Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                        ]
                    )(_.shutdown)
      // The Runloop's rebalance listener's onRevoked may be called after shutting down the runloop when closing the consumer,
      // where we check the shutdownRef
      shutdownRef   <- ZIO.acquireRelease(Ref.make(false))(_.set(true))
      subscribedRef <- Ref.make(false)
      runtime       <- ZIO.runtime[Any]
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
                  perPartitionChunkPrefetch,
                  pollQueue
                )
      _ <- runloop.run
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[Runloop.Request],
  pendingCommits: Chunk[Command.Commit],
  bufferedRecords: BufferedRecords,
  assignedStreams: Map[TopicPartition, PartitionStreamControl],
  finishingStreams: Map[TopicPartition, PartitionStreamControl]
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
  def initial: State = State(Chunk.empty, Chunk.empty, BufferedRecords.empty, Map.empty, Map.empty)
}
