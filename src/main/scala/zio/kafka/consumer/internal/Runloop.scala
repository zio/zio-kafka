package zio.kafka.consumer.internal

import java.util
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.CommittableRecord
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.{ ByteArrayCommittableRecord, ByteArrayConsumerRecord, Command }
import zio.stream._

import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[consumer] final class Runloop(
  consumer: ConsumerAccess,
  pollFrequency: Duration,
  pollTimeout: Duration,
  requestQueue: Queue[Runloop.Request],
  commitQueue: Queue[Command.Commit],
  val partitions: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  rebalancingRef: Ref[Boolean],
  diagnostics: Diagnostics,
  shutdownRef: Ref[Boolean],
  offsetRetrieval: OffsetRetrieval
) {
  private val isRebalancing = rebalancingRef.get
  private val isShutdown    = shutdownRef.get

  def newPartitionStream(
    tp: TopicPartition
  ): UIO[(TopicPartition, Promise[Throwable, Unit], ZStream[Any, Throwable, ByteArrayCommittableRecord])] =
    for {
      interruptionPromise <- Promise.make[Throwable, Unit]
      stream               = ZStream.repeatEffectChunkOption {
                               for {
                                 request <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
                                 _       <- requestQueue.offer(Runloop.Request(tp, request)).unit
                                 _       <- diagnostics.emitIfEnabled(DiagnosticEvent.Request(tp))
                                 result  <- request.await
                               } yield result
                             }.interruptWhen(interruptionPromise)
    } yield (tp, interruptionPromise, stream)

  def gracefulShutdown: UIO[Unit] =
    for {
      wasShutdown <- shutdownRef.getAndSet(true)
      _           <- partitions.offer(Take.end).when(!wasShutdown)
    } yield ()

  val rebalanceListener: RebalanceListener = {
    val trackRebalancing = RebalanceListener(
      onAssigned = _ => rebalancingRef.set(false),
      onRevoked = _ => rebalancingRef.set(true)
    )

    val emitDiagnostics = RebalanceListener(
      assigned => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Assigned(assigned)),
      revoked => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Revoked(revoked))
    )

    trackRebalancing ++ emitDiagnostics
  }

  private def commit(offsets: Map[TopicPartition, Long]): Task[Unit] =
    for {
      p <- Promise.make[Throwable, Unit]
      _ <- commitQueue.offer(Command.Commit(offsets, p)).unit
      _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
      _ <- p.await
    } yield ()

  private def doCommit(cmds: Chunk[Command.Commit]): URIO[Blocking, Unit] = {
    val offsets   = aggregateOffsets(cmds)
    val cont      = (e: Exit[Throwable, Unit]) => ZIO.foreach_(cmds)(_.cont.done(e))
    val onSuccess = cont(Exit.succeed(())) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Success(offsets))
    val onFailure = (err: Throwable) =>
      cont(Exit.fail(err)) <* diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Failure(offsets, err))

    ZIO
      .runtime[Any]
      .map(makeOffsetCommitCallback(onSuccess, onFailure))
      .flatMap { callback =>
        consumer.withConsumerM { c =>
          // We don't wait for the completion of the commit here, because it
          // will only complete once we poll again.
          ZIO(c.commitAsync(offsets.asJava, callback))
        }
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

  private def makeOffsetCommitCallback(onSuccess: Task[Unit], onFailure: Exception => Task[Unit])(
    runtime: Runtime[Any]
  ): OffsetCommitCallback = new OffsetCommitCallback {
    override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit =
      runtime.unsafeRun(if (exception eq null) onSuccess else onFailure(exception))
  }

  /**
   * Does all needed to end revoked partitions:
   * 1. Complete the revoked assigned streams
   * 2. Remove from the list of pending requests
   * 3. Remove from buffered records
   * @return New pending requests, new buffered records and active assigned streams
   */
  private def endRevoked(
    reqs: Chunk[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
    currentAssignedStreams: Map[TopicPartition, Promise[Throwable, Unit]],
    revoked: TopicPartition => Boolean
  ): UIO[Runloop.RevokeResult] = {
    var acc = Chunk[Runloop.Request]()
    val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
    buf ++= bufferedRecords

    val (revokedStreams, assignedStreams) = currentAssignedStreams.partition(es => revoked(es._1))

    val revokeAction: UIO[Unit] = ZIO.foreach_(revokedStreams) { case (_, p) => p.succeed(()) }

    val reqsIt = reqs.iterator
    while (reqsIt.hasNext) {
      val req = reqsIt.next()
      if (revoked(req.tp)) {
        buf -= req.tp
      } else acc :+= req
    }

    revokeAction.as(Runloop.RevokeResult(acc, buf.toMap, assignedStreams))
  }

  /**
   * Fulfill pending requests with records retrieved from poll() call + buffered records
   *
   * @return Remaining pending requests and remaining/new buffered records
   */
  private def fulfillRequests(
    pendingRequests: Chunk[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
    records: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[Runloop.FulfillResult] = {
    var acc = Chunk[Runloop.Request]()
    val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
    buf ++= bufferedRecords

    var fulfillAction: UIO[_] = UIO.unit

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

        fulfillAction = fulfillAction *> req.cont.succeed(
          concatenatedChunk.map(CommittableRecord(_, commit(_), consumer.consumer.groupMetadata()))
        )
        buf -= req.tp
      }
    }

    fulfillAction.as(Runloop.FulfillResult(acc, buf.toMap))
  }

  private def bufferRecordsForUnrequestedPartitions(
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    unrequestedTps: Iterable[TopicPartition]
  ): Map[TopicPartition, Chunk[ByteArrayConsumerRecord]] = {
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

    builder.result()
  }

  private def doSeekForNewPartitions(c: ByteArrayKafkaConsumer, tps: Set[TopicPartition]): Task[Unit] =
    offsetRetrieval match {
      case OffsetRetrieval.Manual(getOffsets) =>
        getOffsets(tps)
          .tap(offsets => ZIO.foreach_(offsets) { case (tp, offset) => ZIO(c.seek(tp, offset)) })
          .when(tps.nonEmpty)

      case OffsetRetrieval.Auto(_) =>
        ZIO.unit
    }

  // Pause partitions for which there is no demand and resume those for which there is now demand
  private def resumeAndPausePartitions(
    c: ByteArrayKafkaConsumer,
    assignment: Set[TopicPartition],
    requestedPartitions: Set[TopicPartition]
  ) = {
    val toResume = assignment intersect requestedPartitions
    val toPause  = assignment -- requestedPartitions

    if (toResume.nonEmpty) c.resume(toResume.asJava)
    if (toPause.nonEmpty) c.pause(toPause.asJava)
  }

  private def doPoll(c: ByteArrayKafkaConsumer, requestedPartitions: Set[TopicPartition]) =
    try {
      val pollTimeout =
        if (requestedPartitions.nonEmpty) this.pollTimeout.asJava
        else 0.millis.asJava

      val records = c.poll(pollTimeout)

      if (records eq null) ConsumerRecords.empty[Array[Byte], Array[Byte]]() else records
    } catch {
      // The consumer will throw an IllegalStateException if no call to subscribe
      // has been made yet, so we just ignore that. We have to poll even if c.subscription()
      // is empty because pattern subscriptions start out as empty.
      case _: IllegalStateException => ConsumerRecords.empty[Array[Byte], Array[Byte]]()
    }

  private def pauseAllPartitions(c: ByteArrayKafkaConsumer) = ZIO.effectTotal {
    val currentAssigned = c.assignment()
    c.pause(currentAssigned)
  }

  private def handlePoll(state: State): RIO[Blocking, State] =
    for {
      pollResult         <-
        consumer.withConsumerM { c =>
          Task.effectSuspend {

            val prevAssigned        = c.assignment().asScala.toSet
            val requestedPartitions = state.pendingRequests.map(_.tp).toSet

            resumeAndPausePartitions(c, prevAssigned, requestedPartitions)

            val records = doPoll(c, requestedPartitions)

            // Check shutdown again after polling (which takes up to the poll timeout)
            ZIO.ifM(isShutdown)(
              pauseAllPartitions(c).as(
                Runloop.PollResult(
                  Set(),
                  state.pendingRequests,
                  Map[TopicPartition, Chunk[ByteArrayConsumerRecord]](),
                  Map[TopicPartition, Promise[Throwable, Unit]]()
                )
              ), {
                val tpsInResponse      = records.partitions.asScala.toSet
                val currentAssigned    = c.assignment().asScala.toSet
                val newlyAssigned      = currentAssigned -- prevAssigned
                val unrequestedRecords =
                  bufferRecordsForUnrequestedPartitions(records, tpsInResponse -- requestedPartitions)

                for {
                  _             <- doSeekForNewPartitions(c, newlyAssigned)
                  revokeResult  <- endRevoked(
                                     state.pendingRequests,
                                     state.addBufferedRecords(unrequestedRecords).bufferedRecords,
                                     state.assignedStreams,
                                     !currentAssigned(_)
                                   )
                  fulfillResult <-
                    fulfillRequests(revokeResult.unfulfilledRequests, revokeResult.bufferedRecords, records)
                  _             <- diagnostics.emitIfEnabled(
                                     DiagnosticEvent.Poll(
                                       requestedPartitions,
                                       fulfillResult.bufferedRecords.keySet,
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
          ZIO.succeed(Set[(TopicPartition, Promise[Throwable, Unit])]())
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
            .map(_.map { case (tp, interruptionPromise, _) =>
              tp -> interruptionPromise
            })
      newPendingCommits  <-
        ZIO.ifM(isRebalancing)(
          UIO.succeed(state.pendingCommits),
          doCommit(state.pendingCommits).when(state.pendingCommits.nonEmpty).as(Chunk.empty)
        )
    } yield State(
      pollResult.unfulfilledRequests,
      newPendingCommits,
      pollResult.bufferedRecords,
      pollResult.assignedStreams ++ newAssignedStreams
    )

  private def handleRequests(state: State, reqs: Chunk[Runloop.Request]): URIO[Blocking, State] =
    ZIO.ifM(isRebalancing)(
      UIO.succeed(state.addRequests(reqs)),
      consumer
        .withConsumer(_.assignment.asScala)
        .flatMap { assignment =>
          ZIO.foldLeft(reqs)(state) { (state, req) =>
            if (assignment.contains(req.tp))
              UIO.succeed(state.addRequest(req))
            else
              req.cont.fail(None).as(state)
          }
        }
        .orElseSucceed(state.addRequests(reqs))
    )

  private def handleCommit(state: State, cmd: Command.Commit): URIO[Blocking, State] =
    ZIO.ifM(isRebalancing)(
      UIO.succeed(state.addCommit(cmd)),
      doCommit(Chunk(cmd)).as(state)
    )

  /**
   * After shutdown, we end all pending requests (ending their partition streams) and pause
   * all partitions, but keep executing commits and polling
   *
   * Buffered records for paused partitions will be removed to drain the stream
   * as fast as possible.
   */
  private def handleShutdown(state: State, cmd: Command): RIO[Blocking, State] =
    cmd match {
      case Command.Poll()             =>
        // End all pending requests
        ZIO.foreach_(state.pendingRequests)(_.cont.fail(None)) *>
          handlePoll(state.copy(pendingRequests = Chunk.empty, bufferedRecords = Map.empty))
      case Command.Requests(reqs)     =>
        ZIO.foreach_(reqs)(_.cont.fail(None)).as(state)
      case cmd @ Command.Commit(_, _) =>
        handleCommit(state, cmd)
    }

  private def handleOperational(state: State, cmd: Command): RIO[Blocking, State] =
    cmd match {
      case Command.Poll()             =>
        handlePoll(state)
      case Command.Requests(reqs)     =>
        handleRequests(state, reqs).flatMap { state =>
          // Optimization: eagerly poll if we have pending requests instead of waiting
          // for the next scheduled poll.
          if (state.pendingRequests.nonEmpty) handlePoll(state)
          else UIO.succeed(state)
        }
      case cmd @ Command.Commit(_, _) =>
        handleCommit(state, cmd)
    }

  def run: URManaged[Blocking with Clock, Fiber.Runtime[Throwable, Unit]] =
    ZStream
      .mergeAll(3, 1)(
        ZStream(Command.Poll()).repeat(Schedule.spaced(pollFrequency)),
        ZStream.fromQueue(requestQueue).mapChunks(c => Chunk.single(Command.Requests(c))),
        ZStream.fromQueue(commitQueue)
      )
      .foldM(State.initial) { (state, cmd) =>
        RIO.ifM(isShutdown)(handleShutdown(state, cmd), handleOperational(state, cmd))
      }
      .onError(cause => partitions.offer(Take.halt(cause)))
      .unit
      .toManaged_
      .fork
}

private[consumer] object Runloop {
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]
  type ByteArrayConsumerRecord    = ConsumerRecord[Array[Byte], Array[Byte]]

  case class Request(tp: TopicPartition, cont: Promise[Option[Throwable], Chunk[ByteArrayCommittableRecord]])
  case class PollResult(
    newlyAssigned: Set[TopicPartition],
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
    assignedStreams: Map[TopicPartition, Promise[Throwable, Unit]]
  )
  case class RevokeResult(
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
    assignedStreams: Map[TopicPartition, Promise[Throwable, Unit]]
  )
  case class FulfillResult(
    unfulfilledRequests: Chunk[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]
  )

  sealed abstract class Command
  object Command {
    case class Requests(requests: Chunk[Request])                                         extends Command
    case class Poll()                                                                     extends Command
    case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command
  }

  def apply(
    consumer: ConsumerAccess,
    pollFrequency: Duration,
    pollTimeout: Duration,
    diagnostics: Diagnostics,
    offsetRetrieval: OffsetRetrieval
  ): RManaged[Blocking with Clock, Runloop] =
    for {
      rebalancingRef <- Ref.make(false).toManaged_
      requestQueue   <- Queue.unbounded[Runloop.Request].toManaged(_.shutdown)
      commitQueue    <- Queue.unbounded[Command.Commit].toManaged(_.shutdown)
      partitions     <- Queue
                          .unbounded[
                            Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                          ]
                          .map { queue =>
                            queue
                              .mapM(
                                _.fold(
                                  queue.shutdown.as(Take.end),
                                  cause => UIO.succeed(Take.halt(cause)),
                                  chunk => UIO.succeed(Take.chunk(chunk))
                                )
                              )
                          }
                          .toManaged(_.shutdown)
      shutdownRef    <- Ref.make(false).toManaged_
      runloop         = new Runloop(
                          consumer,
                          pollFrequency,
                          pollTimeout,
                          requestQueue,
                          commitQueue,
                          partitions,
                          rebalancingRef,
                          diagnostics,
                          shutdownRef,
                          offsetRetrieval
                        )
      _              <- runloop.run
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[Runloop.Request],
  pendingCommits: Chunk[Command.Commit],
  bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
  assignedStreams: Map[TopicPartition, Promise[Throwable, Unit]]
) {
  def addCommit(c: Command.Commit)                                                  = copy(pendingCommits = c +: pendingCommits)
  def addRequest(c: Runloop.Request)                                                = copy(pendingRequests = c +: pendingRequests)
  def addRequests(c: Chunk[Runloop.Request])                                        = copy(pendingRequests = c ++ pendingRequests)
  def addBufferedRecords(recs: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]) =
    copy(
      bufferedRecords = recs.foldLeft(bufferedRecords) { case (acc, (tp, recs)) =>
        acc.get(tp) match {
          case Some(existingRecs) => acc + (tp -> (existingRecs ++ recs))
          case None               => acc + (tp -> recs)
        }
      }
    )

  def removeBufferedRecordsFor(tp: TopicPartition) =
    copy(bufferedRecords = bufferedRecords - tp)
}

object State {
  def initial: State = State(Chunk.empty, Chunk.empty, Map.empty, Map.empty)
}
