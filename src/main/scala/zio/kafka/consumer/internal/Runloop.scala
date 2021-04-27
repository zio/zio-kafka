package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.consumer.CommittableRecord
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop.{
  ByteArrayCommittableRecord,
  ByteArrayConsumerRecord,
  Command,
  Mode,
  Request
}
import zio.stream._

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[consumer] final class Runloop(
  consumer: ConsumerAccess,
  pollFrequency: Duration,
  pollTimeout: Duration,
  requestQueue: RequestBuffer,
  commitQueue: Queue[Command.Commit],
  val partitions: Queue[Exit[Option[Throwable], (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  rebalancingRef: Ref[Boolean],
  diagnostics: Diagnostics,
  shutdownRef: Ref[Boolean],
  offsetRetrieval: OffsetRetrieval,
  mode: Runloop.Mode
) {
  private val isRebalancing = rebalancingRef.get
  private val isShutdown    = shutdownRef.get

  def newPartitionStream(tp: TopicPartition) = {
    val stream =
      ZStream {
        ZManaged.succeed {
          for {
            p      <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
            _      <- requestQueue.offer(Request(Some(tp), p)).unit
            _      <- diagnostics.emitIfEnabled(DiagnosticEvent.Request(tp))
            result <- p.await
          } yield result
        }
      }

    partitions.offer(Exit.succeed(tp -> stream)).unit
  }

  def newPlainStream() =
    ZStream {
      ZManaged.succeed {
        for {
          p      <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
          _      <- requestQueue.offer(Request(None, p)).unit
          _      <- diagnostics.emitIfEnabled(DiagnosticEvent.PlainRequest())
          result <- p.await
        } yield result
      }
    }

  def gracefulShutdown: UIO[Unit] =
    for {
      shutdown <- shutdownRef.modify((_, true))
      _        <- partitions.offer(Exit.fail(None)).when(!shutdown)
    } yield ()

  val rebalanceListener = {
    val trackRebalancing = RebalanceListener(
      onAssigned = _ => rebalancingRef.set(false),
      onRevoked = _ => rebalancingRef.set(true)
    )

    val emitDiagnostics = RebalanceListener(
      assigned => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Assigned(assigned)),
      revoked => diagnostics.emitIfEnabled(DiagnosticEvent.Rebalance.Revoked(revoked))
    )

    val pausePartitionsOnRevoke =
      RebalanceListener.onRevoked(revoked => Task(consumer.consumer.pause(revoked.asJavaCollection)))

    trackRebalancing ++ emitDiagnostics ++ pausePartitionsOnRevoke
  }

  private def commit(offsets: Map[TopicPartition, Long]): ZIO[Any, Throwable, Unit] =
    for {
      p <- Promise.make[Throwable, Unit]
      _ <- commitQueue.offer(Command.Commit(offsets, p)).unit
      _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offsets))
      _ <- p.await
    } yield ()

  private def doCommit(cmds: List[Command.Commit]): ZIO[Blocking, Nothing, Unit] = {
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
  private def aggregateOffsets(cmds: List[Command.Commit]): Map[TopicPartition, OffsetAndMetadata] = {
    val offsets = mutable.Map[TopicPartition, OffsetAndMetadata]()

    cmds.foreach { commit =>
      commit.offsets.foreach {
        case (tp, offset) =>
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
   * 1. Fail the Requests's continuation promises
   * 2. Remove from the list of pending requets
   * 3. Remove from buffered records
   *
   * @return New pending requests and new buffered records
   */
  private def endRevoked(
    reqs: List[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
    revoked: TopicPartition => Boolean
  ): UIO[
    (List[Runloop.Request], Map[TopicPartition, Chunk[ByteArrayConsumerRecord]])
  ] = {
    var acc = List[Runloop.Request]()
    val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
    buf ++= bufferedRecords

    var revokeAction: UIO[_] = UIO.unit

    val reqsIt = reqs.iterator
    while (reqsIt.hasNext) {
      val req = reqsIt.next
      req match {
        case Request(Some(tp), cont) =>
          if (revoked(tp)) {
            revokeAction = revokeAction *> cont.fail(None)
            buf -= tp
          } else acc ::= req
        case _ =>
          acc ::= req
      }
    }

    // Remove records related to revoked partitions in PlainStream mode
    if (mode == Mode.PlainStream) {
      val bufferedKeysIterator = bufferedRecords.keys.iterator
      while (bufferedKeysIterator.hasNext) {
        val tp = bufferedKeysIterator.next()
        if (revoked(tp))
          buf -= tp
      }
    }

    revokeAction.as((acc.reverse, buf.toMap))
  }

  /**
   * Fulfill pending requests with records retrieved from poll() call + buffered records
   *
   * @return Remaining pending requests and remaining/new buffered records
   */
  private def fulfillRequests(
    pendingRequests: List[Runloop.Request],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
    records: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[
    (List[Runloop.Request], Map[TopicPartition, Chunk[ByteArrayConsumerRecord]])
  ] = {
    var acc = List[Runloop.Request]()
    val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
    buf ++= bufferedRecords

    var fulfillAction: UIO[_] = UIO.unit

    val reqsIt = pendingRequests.iterator
    while (reqsIt.hasNext) {
      val req = reqsIt.next
      req match {
        case Request(Some(tp), cont) =>
          val bufferedChunk = buf.getOrElse(tp, Chunk.empty)
          val reqRecs       = records.records(tp)

          if ((bufferedChunk.length + reqRecs.size) == 0) {
            acc ::= req
          } else {
            val concatenatedChunk = bufferedChunk ++
              Chunk.fromArray(
                reqRecs.toArray(Array.ofDim[ByteArrayConsumerRecord](reqRecs.size))
              )

            fulfillAction = fulfillAction *> cont.succeed(
              concatenatedChunk.map(CommittableRecord(_, commit(_)))
            )
            buf -= tp
          }
        case Request(None, cont) =>
          val chunks = (Chunk.fromIterator(records.iterator().asScala) ++ buf.values.fold(Chunk.empty)(_ ++ _))
            .map(CommittableRecord(_, commit(_)))
          if (chunks.nonEmpty)
            fulfillAction =
              fulfillAction *> cont.succeed(chunks)
          else
            acc ::= req
      }

    }

    fulfillAction.as((acc, buf.toMap))
  }

  private def bufferRecordsForUnrequestedPartitions(
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    unrequestedTps: Iterable[TopicPartition]
  ): Map[TopicPartition, Chunk[ByteArrayConsumerRecord]] = {
    val builder = Map.newBuilder[TopicPartition, Chunk[ByteArrayConsumerRecord]]
    builder.sizeHint(unrequestedTps.size)

    val tpsIt = unrequestedTps.iterator
    while (tpsIt.hasNext) {
      val tp   = tpsIt.next
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
          .flatMap(offsets => ZIO.foreach(offsets.toList) { case (tp, offset) => ZIO(c.seek(tp, offset)) })
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

      c.poll(pollTimeout)
    } catch {
      // The consumer will throw an IllegalStateException if no call to subscribe
      // has been made yet, so we just ignore that. We have to poll even if c.subscription()
      // is empty because pattern subscriptions start out as empty.
      case _: IllegalStateException => null
    }

  private def pauseAllPartitions(c: ByteArrayKafkaConsumer) = ZIO.effectTotal {
    val currentAssigned = c.assignment().asScala.toSet
    c.pause(currentAssigned.asJava)
  }

  private def handlePoll(state: State): RIO[Blocking, State] =
    for {
      pollResult <- consumer.withConsumerM { c =>
                     Task.effectSuspend {

                       val prevAssigned = c.assignment().asScala.toSet
                       // If we are in the plain stream mode and there are pending requests we take
                       // previously assigned partitions
                       val requestedPartitions =
                         if (mode == Mode.PlainStream && state.pendingRequests.nonEmpty) prevAssigned
                         else state.pendingPartitions

                       resumeAndPausePartitions(c, prevAssigned, requestedPartitions)

                       val records = doPoll(c, requestedPartitions)

                       // Check shutdown again after polling (which takes up to the poll timeout)
                       isShutdown.flatMap { shutdown =>
                         if (shutdown) {
                           pauseAllPartitions(c) *> ZIO.succeed(
                             (Set(), (state.pendingRequests, Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()))
                           )
                         } else if (records eq null) {
                           ZIO.succeed(
                             (Set(), (state.pendingRequests, Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()))
                           )
                         } else {
                           val tpsInResponse   = records.partitions.asScala.toSet
                           val currentAssigned = c.assignment().asScala.toSet
                           val newlyAssigned   = currentAssigned -- prevAssigned
                           val revoked         = prevAssigned -- currentAssigned
                           val unrequestedRecords =
                             bufferRecordsForUnrequestedPartitions(records, tpsInResponse -- requestedPartitions)

                           doSeekForNewPartitions(c, newlyAssigned) *>
                             endRevoked(
                               state.pendingRequests,
                               state.addBufferedRecords(unrequestedRecords).bufferedRecords,
                               revoked(_)
                             ).flatMap {
                               case (pendingRequests, bufferedRecords) =>
                                 for {
                                   output                                  <- fulfillRequests(pendingRequests, bufferedRecords, records)
                                   (notFulfilledRequests, bufferedRecords) = output
                                   _ <- diagnostics.emitIfEnabled(
                                         DiagnosticEvent
                                           .Poll(
                                             requestedPartitions,
                                             bufferedRecords.keySet,
                                             notFulfilledRequests.collect {
                                               case Request(Some(tp), _) => tp
                                             }.toSet
                                           )
                                       )
                                 } yield output
                             }.map((newlyAssigned, _))
                         }
                       }
                     }
                   }
      (newlyAssigned, (unfulfilledRequests, bufferedRecords)) = pollResult
      _                                                       <- ZIO.foreach_(newlyAssigned)(tp => newPartitionStream(tp))
      stillRebalancing                                        <- isRebalancing
      newPendingCommits <- if (!stillRebalancing && state.pendingCommits.nonEmpty)
                            doCommit(state.pendingCommits).as(Nil)
                          else ZIO.succeed(state.pendingCommits)
    } yield State(unfulfilledRequests, newPendingCommits, bufferedRecords)

  private def handleRequests(state: State, reqs: List[Runloop.Request]): URIO[Blocking, State] =
    consumer
      .withConsumer(_.assignment.asScala)
      .flatMap { assignment =>
        for {
          rebalancing <- isRebalancing
          newState <- if (rebalancing) UIO.succeed(state.addRequests(reqs))
                     else
                       ZIO.foldLeft(reqs)(state) { (state, req) =>
                         req match {
                           case Request(Some(tp), cont) =>
                             if (assignment.contains(tp)) UIO.succeed(state.addRequest(req))
                             else cont.fail(None).as(state)
                           case request =>
                             UIO.succeed(state.addRequest(request))
                         }
                       }
        } yield newState
      }
      .orElse(UIO.succeed(state.addRequests(reqs)))

  private def handleCommit(state: State, cmd: Command.Commit): URIO[Blocking, State] =
    for {
      rebalancing <- isRebalancing
      newState <- if (rebalancing)
                   UIO.succeed(state.addCommit(cmd))
                 else doCommit(List(cmd)).as(state)
    } yield newState

  /**
   * After shutdown, we end all pending requests (ending their partition streams) and pause
   * all partitions, but keep executing commits and polling
   *
   * Buffered records for paused partitions will be removed to drain the stream
   * as fast as possible.
   */
  private def handleShutdown(state: State, cmd: Command): RIO[Blocking, State] = cmd match {
    case Command.Poll() =>
      // End all pending requests
      ZIO.foreach_(state.pendingRequests)(_.cont.fail(None)) *>
        handlePoll(state.copy(pendingRequests = List.empty, bufferedRecords = Map.empty))
    case Command.Requests(reqs) =>
      ZIO.foreach(reqs)(_.cont.fail(None)).as(state)
    case cmd @ Command.Commit(_, _) =>
      handleCommit(state, cmd)
  }

  private def handleOperational(state: State, cmd: Command): RIO[Blocking, State] = cmd match {
    case Command.Poll() =>
      handlePoll(state)
    case Command.Requests(reqs) =>
      handleRequests(state, reqs).flatMap { state =>
        // Optimization: eagerly poll if we have pending requests instead of waiting
        // for the next scheduled poll.
        if (state.pendingRequests.nonEmpty) handlePoll(state)
        else UIO.succeed(state)
      }
    case cmd @ Command.Commit(_, _) =>
      handleCommit(state, cmd)
  }

  def run =
    ZStream
      .mergeAll(3, 1)(
        ZStream(Command.Poll()).repeat(Schedule.spaced(pollFrequency)),
        requestQueue.stream.map(Command.Requests(_)),
        ZStream.fromQueue(commitQueue)
      )
      .foldM(State.initial) { (state, cmd) =>
        isShutdown.flatMap { shutdown =>
          if (shutdown) handleShutdown(state, cmd)
          else handleOperational(state, cmd)
        }
      }
      .onError(cause => partitions.offer(Exit.halt(cause.map(Some(_)))))
      .unit
      .toManaged_
      .fork
}

private[consumer] object Runloop {
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]
  type ByteArrayConsumerRecord    = ConsumerRecord[Array[Byte], Array[Byte]]

  def apply(
    consumer: ConsumerAccess,
    pollFrequency: Duration,
    pollTimeout: Duration,
    diagnostics: Diagnostics,
    offsetRetrieval: OffsetRetrieval,
    mode: Mode
  ): ZManaged[Blocking with Clock, Nothing, Runloop] =
    for {
      rebalancingRef <- Ref.make(false).toManaged_
      requestQueue   <- RequestBuffer.make.toManaged(_.shutdown)
      commitQueue    <- Queue.unbounded[Command.Commit].toManaged(_.shutdown)
      partitions <- Queue
                     .unbounded[
                       Exit[Option[Throwable], (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                     ]
                     .map { queue =>
                       queue.mapM {
                         case e @ Exit.Failure(cause) if cause.contains(Cause.fail(None)) => queue.shutdown.as(e)
                         case x                                                           => ZIO.succeed(x)
                       }
                     }
                     .toManaged(_.shutdown)
      shutdownRef <- Ref.make(false).toManaged_
      runloop = new Runloop(
        consumer,
        pollFrequency,
        pollTimeout,
        requestQueue,
        commitQueue,
        partitions,
        rebalancingRef,
        diagnostics,
        shutdownRef,
        offsetRetrieval,
        mode
      )
      _ <- runloop.run
    } yield runloop

  sealed trait Mode

  sealed abstract class Command

  case class Request(
    tp: Option[TopicPartition],
    cont: Promise[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
  )

  object Mode {
    case object PlainStream extends Mode

    case object PartitionStream extends Mode
  }

  object Command {
    case class Requests(requests: List[Request]) extends Command

    case class Poll() extends Command

    case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command
  }
}

private[internal] final case class State(
  pendingRequests: List[Runloop.Request],
  pendingCommits: List[Command.Commit],
  bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]
) {
  def addCommit(c: Command.Commit) = copy(pendingCommits = c :: pendingCommits)

  def addRequest(c: Runloop.Request) = copy(pendingRequests = c :: pendingRequests)

  def addRequests(c: List[Runloop.Request]) = copy(pendingRequests = c ++ pendingRequests)

  def addBufferedRecords(recs: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]) =
    copy(
      bufferedRecords = recs.foldLeft(bufferedRecords) {
        case (acc, (tp, recs)) =>
          acc.get(tp) match {
            case Some(existingRecs) => acc + (tp -> (existingRecs ++ recs))
            case None               => acc + (tp -> recs)
          }
      }
    )

  def removeBufferedRecordsFor(tp: TopicPartition) =
    copy(bufferedRecords = bufferedRecords - tp)

  def pendingPartitions = pendingRequests.collect { case Runloop.Request(Some(tp), _) => tp }.toSet
}

object State {
  def initial: State = State(Nil, Nil, Map())
}
