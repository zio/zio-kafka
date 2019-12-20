package zio.kafka.client.internal

import java.util

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.ConsumerStream.OffsetRetrieval
import zio.kafka.client.diagnostics.DiagnosticEvent.Rebalance.{ Assigned, Revoked }
import zio.kafka.client.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.client.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.client.{ BlockingTask, CommittableRecord }
import zio.stream._

import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[client] final case class Runloop(fiber: Fiber[Throwable, Unit], deps: Runloop.Deps)
private[client] object Runloop {
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]
  type ByteArrayConsumerRecord    = ConsumerRecord[Array[Byte], Array[Byte]]

  sealed abstract class Command
  object Command {
    case class Request(tp: TopicPartition, cont: Promise[Option[Throwable], Chunk[ByteArrayCommittableRecord]])
        extends Command
    case class Poll()                                                                     extends Command
    case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command
  }

  case class Deps(
    consumer: ConsumerAccess,
    pollFrequency: Duration,
    pollTimeout: Duration,
    requestQueue: Queue[Command.Request],
    commitQueue: Queue[Command.Commit],
    partitions: Queue[Take[Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, ByteArrayCommittableRecord])]],
    rebalancingRef: Ref[Boolean],
    rebalanceListener: RebalanceListener[Any],
    diagnostics: Diagnostics,
    shutdownRef: Ref[Boolean],
    offsetRetrieval: OffsetRetrieval
  ) {
    def commit(cmd: Command.Commit)   = commitQueue.offer(cmd).unit
    def commits                       = ZStream.fromQueue(commitQueue)
    def request(cmd: Command.Request) = requestQueue.offer(cmd).unit
    def requests                      = ZStream.fromQueue(requestQueue)

    val isRebalancing = rebalancingRef.get

    def polls = ZStream(Command.Poll()).repeat(Schedule.spaced(pollFrequency))

    def newPartitionStream(tp: TopicPartition): UIO[Unit] = {
      val stream = ZStreamChunk {
        ZStream {
          ZManaged.succeed {
            for {
              p      <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
              _      <- request(Command.Request(tp, p))
              _      <- emitIfEnabledDiagnostic(DiagnosticEvent.Request(tp))
              result <- p.await
            } yield result
          }
        }
      }

      partitions.offer(Take.Value(tp -> stream)).unit
    }

    def emitIfEnabledDiagnostic(event: DiagnosticEvent) = diagnostics.emitIfEnabled(event)

    val isShutdown = shutdownRef.get

    def gracefulShutdown: UIO[Unit] =
      for {
        shutdown <- shutdownRef.modify((_, true))
        _        <- partitions.offer(Take.End).when(!shutdown)
      } yield ()
  }
  object Deps {
    def make(
      consumer: ConsumerAccess,
      pollFrequency: Duration,
      pollTimeout: Duration,
      diagnostics: Diagnostics,
      offsetRetrieval: OffsetRetrieval
    ) =
      for {
        rebalancingRef <- Ref.make(false).toManaged_
        requestQueue   <- Queue.unbounded[Command.Request].toManaged(_.shutdown)
        commitQueue    <- Queue.unbounded[Command.Commit].toManaged(_.shutdown)
        partitions <- Queue
                       .unbounded[
                         Take[Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, ByteArrayCommittableRecord])]
                       ]
                       .map { queue =>
                         queue.mapM {
                           case Take.End => queue.shutdown.as(Take.End)
                           case x        => ZIO.succeed(x)
                         }
                       }
                       .toManaged(_.shutdown)
        listener = RebalanceListener(
          tp => diagnostics.emitIfEnabled(Assigned(tp)) *> rebalancingRef.set(true),
          tp => diagnostics.emitIfEnabled(Revoked(tp)) *> rebalancingRef.set(false)
        )
        shutdownRef <- Ref.make(false).toManaged_
      } yield Deps(
        consumer,
        pollFrequency,
        pollTimeout,
        requestQueue,
        commitQueue,
        partitions,
        rebalancingRef,
        listener,
        diagnostics,
        shutdownRef,
        offsetRetrieval
      )
  }

  case class State(
    pendingRequests: List[Command.Request],
    pendingCommits: List[Command.Commit],
    bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]
  ) {
    def addCommit(c: Command.Commit)           = copy(pendingCommits = c :: pendingCommits)
    def setCommits(reqs: List[Command.Commit]) = copy(pendingCommits = reqs)
    def addRequest(c: Command.Request)         = copy(pendingRequests = c :: pendingRequests)
    def clearCommits                           = copy(pendingCommits = Nil)
//    def clearRequests                          = copy(pendingRequests = Nil)
    def addBufferedRecords(recs: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]): State =
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

    def setBufferedRecords(recs: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]) =
      copy(bufferedRecords = recs)
  }

  object State {
    def initial: State = State(Nil, Nil, Map())
  }

  def apply(deps: Deps): ZManaged[Clock with Blocking, Throwable, Runloop] = {

    def commit(offsets: Map[TopicPartition, Long]): ZIO[Any, Throwable, Unit] =
      for {
        p <- Promise.make[Throwable, Unit]
        _ <- deps.commit(Command.Commit(offsets, p))
        _ <- deps.emitIfEnabledDiagnostic(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await
      } yield ()

    def doCommit(cmds: List[Command.Commit]): ZIO[Blocking, Nothing, Unit] =
      for {
        runtime <- ZIO.runtime[Any]
        offsets = {
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
        cont = (e: Exit[Throwable, Unit]) => ZIO.traverse_(cmds)(_.cont.done(e))
        _ <- deps.consumer.withConsumerM { c =>
              // We don't wait for the completion of the commit here, because it
              // will only complete once we poll again.
              ZIO {
                c.commitAsync(
                  offsets.asJava,
                  new OffsetCommitCallback {
                    def onComplete(data: java.util.Map[TopicPartition, OffsetAndMetadata], err: Exception) =
                      if (err eq null)
                        runtime.unsafeRun(
                          cont(Exit.succeed(())) <* deps.emitIfEnabledDiagnostic(
                            DiagnosticEvent.Commit.Success(offsets)
                          )
                        )
                      else
                        runtime.unsafeRun(
                          cont(Exit.fail(err)) <* deps.emitIfEnabledDiagnostic(
                            DiagnosticEvent.Commit.Failure(offsets, err)
                          )
                        )
                  }
                )
              }
            }.catchAll(
              e => cont(Exit.fail(e)) <* deps.emitIfEnabledDiagnostic(DiagnosticEvent.Commit.Failure(offsets, e))
            )
      } yield ()

    def handlePoll(state: State): BlockingTask[State] =
      for {
        pollResult <- deps.consumer.withConsumerM { c =>
                       def endRevoked(
                         reqs: List[Command.Request],
                         bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
                         revoked: TopicPartition => Boolean
                       ): UIO[
                         (List[Command.Request], Map[TopicPartition, Chunk[ByteArrayConsumerRecord]])
                       ] = {
                         var acc = List[Command.Request]()
                         val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
                         buf ++= bufferedRecords

                         var revokeAction: UIO[_] = UIO.unit

                         val reqsIt = reqs.iterator
                         while (reqsIt.hasNext) {
                           val req = reqsIt.next
                           if (revoked(req.tp)) {
                             revokeAction = revokeAction *> req.cont.fail(None)
                             buf -= req.tp
                           } else acc ::= req
                         }

                         revokeAction.as((acc.reverse, buf.toMap))
                       }

                       def fulfillRequests(
                         pendingRequests: List[Command.Request],
                         bufferedRecords: Map[TopicPartition, Chunk[ByteArrayConsumerRecord]],
                         records: ConsumerRecords[Array[Byte], Array[Byte]]
                       ): UIO[
                         (List[Command.Request], Map[TopicPartition, Chunk[ByteArrayConsumerRecord]])
                       ] = {
                         var acc = List[Command.Request]()
                         val buf = mutable.Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()
                         buf ++= bufferedRecords

                         var fulfillAction: UIO[_] = UIO.unit

                         val reqsIt = pendingRequests.iterator
                         while (reqsIt.hasNext) {
                           val req           = reqsIt.next
                           val bufferedChunk = buf.getOrElse(req.tp, Chunk.empty)
                           val reqRecs       = records.records(req.tp)

                           if ((bufferedChunk.length + reqRecs.size) == 0) {
                             acc ::= req
                           } else {
                             val concatenatedChunk = bufferedChunk ++
                               Chunk.fromArray(
                                 reqRecs.toArray(Array.ofDim[ByteArrayConsumerRecord](reqRecs.size))
                               )

                             fulfillAction = fulfillAction *> req.cont.succeed(
                               concatenatedChunk.map(CommittableRecord(_, commit(_)))
                             )
                             buf -= req.tp
                           }
                         }

                         fulfillAction.as((acc, buf.toMap))
                       }

                       def bufferUnrequestedPartitions(
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

                       def doSeekForNewPartitions(tps: Set[TopicPartition], c: ByteArrayKafkaConsumer): Task[Unit] =
                         deps.offsetRetrieval match {
                           // For new partitions we do a seek
                           case OffsetRetrieval.Manual(getOffsets) =>
                             getOffsets(tps).flatMap { offsets =>
                               ZIO.traverse(offsets) { case (tp, offset) => ZIO(c.seek(tp, offset)) }
                             }.when(tps.nonEmpty)

                           case OffsetRetrieval.Auto(_) =>
                             ZIO.unit
                         }

                       Task.effectSuspend {
                         val prevAssigned = c.assignment().asScala.toSet

                         val requestedPartitions = state.pendingRequests.map(_.tp).toSet
                         c.resume(requestedPartitions.asJava)
                         c.pause((prevAssigned -- requestedPartitions).asJava)

                         val pollTimeout =
                           if (requestedPartitions.nonEmpty) deps.pollTimeout
                           else 0.millis
                         val records = c.poll(pollTimeout.asJava)

                         deps.isShutdown.flatMap { shutdown =>
                           if (shutdown) {
                             ZIO.effectTotal(c.pause(requestedPartitions.asJava)) *>
                               ZIO.succeed(
                                 (Set(), (state.pendingRequests, Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()))
                               )
                           } else if (records eq null) {
                             ZIO.succeed(
                               (Set(), (state.pendingRequests, Map[TopicPartition, Chunk[ByteArrayConsumerRecord]]()))
                             )
                           } else {
                             val currentAssigned = c.assignment().asScala.toSet

                             // Newly assigned partitions by automatic rebalancing only
                             // (manual partitions are not in this set)
                             val newlyAssigned = currentAssigned -- prevAssigned

                             val revoked = prevAssigned -- currentAssigned

                             val tpsInResponse = records.partitions.asScala.toSet
                             val unrequestedRecords =
                               bufferUnrequestedPartitions(records, tpsInResponse -- requestedPartitions)

                             doSeekForNewPartitions(newlyAssigned, c) *> endRevoked(
                               state.pendingRequests,
                               state.addBufferedRecords(unrequestedRecords).bufferedRecords,
                               revoked(_)
                             ).flatMap {
                               case (pendingRequests, bufferedRecords) =>
                                 for {
                                   output                    <- fulfillRequests(pendingRequests, bufferedRecords, records)
                                   (notFulfilled, fulfilled) = output
                                   _ <- deps.emitIfEnabledDiagnostic(
                                         DiagnosticEvent.Poll(
                                           requestedPartitions,
                                           fulfilled.keySet,
                                           notFulfilled.map(_.tp).toSet
                                         )
                                       )
                                 } yield (notFulfilled, fulfilled)
                             }.map((newlyAssigned, _))
                           }
                         }
                       }
                     }
        (newlyAssigned, (unfulfilledRequests, bufferedRecords)) = pollResult
        // Create new partition substreams for newly assigned partitions
        _                <- ZIO.traverse_(newlyAssigned)(tp => deps.newPartitionStream(tp))
        stillRebalancing <- deps.isRebalancing
        newPendingCommits <- if (!stillRebalancing && state.pendingCommits.nonEmpty)
                              doCommit(state.pendingCommits).as(Nil)
                            else ZIO.succeed(state.pendingCommits)
      } yield State(unfulfilledRequests, newPendingCommits, bufferedRecords)

    def handleRequest(state: State, req: Command.Request): URIO[Blocking, State] =
      deps.consumer
        .withConsumer(_.assignment.asScala)
        .flatMap { assignment =>
          for {
            rebalancing <- deps.isRebalancing
            newState <- if (!rebalancing && !assignment.contains(req.tp))
                         req.cont.fail(None).as(state)
                       else UIO.succeed(state.addRequest(req))
          } yield newState
        }
        .orElse(UIO.succeed(state.addRequest(req)))

    def handleCommit(state: State, cmd: Command.Commit): URIO[Blocking, State] =
      for {
        rebalancing <- deps.isRebalancing
        newState <- if (rebalancing)
                     UIO.succeed(state.addCommit(cmd))
                   else doCommit(List(cmd)).as(state)
      } yield newState

    def handleShutdown(state: State, cmd: Command): BlockingTask[State] = cmd match {
      case Command.Poll() =>
        state.pendingRequests match {
          case h :: t =>
            handleShutdown(state, h).flatMap { s =>
              handleShutdown(s.copy(pendingRequests = t), cmd)
            }
          case Nil => handlePoll(state)
        }
      case Command.Request(tp, cont) =>
        state.bufferedRecords.get(tp) match {
          case Some(recs) =>
            cont
              .succeed(recs.map(CommittableRecord(_, commit(_))))
              .as(state.removeBufferedRecordsFor(tp))
          case None => cont.fail(None).as(state)
        }
      case cmd @ Command.Commit(_, _) => handleCommit(state, cmd)
    }

    ZStream
      .mergeAll(3, 32)(
        deps.polls,
        deps.requests,
        deps.commits
      )
      .foldM(State.initial) { (state, cmd) =>
        deps.isShutdown.flatMap { shutdown =>
          if (shutdown) handleShutdown(state, cmd)
          else
            cmd match {
              case Command.Poll()              => handlePoll(state)
              case req @ Command.Request(_, _) => handleRequest(state, req)
              case cmd @ Command.Commit(_, _)  => handleCommit(state, cmd)
            }
        }
      }
      .onError { cause =>
        deps.partitions.offer(Take.Fail(cause))
      }
      .unit
      .toManaged_
      .ensuringFirst(deps.gracefulShutdown)
      .fork
      .map(Runloop(_, deps))
  }

  case class RebalanceListener[-R](
    onAssigned: Set[TopicPartition] => ZIO[R, Nothing, Any] = (_: Set[TopicPartition]) => UIO.unit,
    onRevoked: Set[TopicPartition] => ZIO[R, Nothing, Any] = (_: Set[TopicPartition]) => UIO.unit
  ) {
    def toConsumerRebalanceListener(runtime: Runtime[R]): ConsumerRebalanceListener = new ConsumerRebalanceListener {
      override def onPartitionsRevoked(partitions: util.Collection[TopicPartition]): Unit = {
        runtime.unsafeRun(onAssigned(partitions.asScala.toSet))
        ()
      }
      override def onPartitionsAssigned(partitions: util.Collection[TopicPartition]): Unit = {
        runtime.unsafeRun(onRevoked(partitions.asScala.toSet))
        ()
      }
    }

    def +[R1 <: R](listener2: RebalanceListener[R1]): RebalanceListener[R1] = copy(
      onAssigned = tp => listener2.onAssigned(tp) *> onAssigned(tp),
      onRevoked = tp => listener2.onRevoked(tp) *> onRevoked(tp)
    )
  }

  object RebalanceListener {
    val noop = RebalanceListener[Any]()
  }

}
