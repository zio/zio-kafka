package zio.kafka.client

import org.apache.kafka.clients.consumer.{
  ConsumerRebalanceListener,
  ConsumerRecord,
  OffsetAndMetadata,
  OffsetCommitCallback
}
import org.apache.kafka.common.TopicPartition
import zio._
import zio.duration._
import zio.stream._
import zio.blocking.Blocking
import zio.clock.Clock

import scala.collection.mutable
import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.ConsumerRecords

case class Runloop[K, V](fiber: Fiber[Throwable, Unit], deps: Runloop.Deps[K, V])
object Runloop {
  sealed abstract class Command[K, V]
  object Command {
    case class Request[K, V](tp: TopicPartition, cont: Promise[Option[Throwable], Chunk[CommittableRecord[K, V]]])
        extends Command[K, V]
    case class Poll[K, V]()                                                                     extends Command[K, V]
    case class Commit[K, V](offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command[K, V]
  }

  sealed abstract class Rebalance
  object Rebalance {
    case class Revoked(previousAssignment: Set[TopicPartition]) extends Rebalance
    case class Assigned(currentAssignment: Set[TopicPartition]) extends Rebalance
  }

  case class Deps[K, V](
    consumer: ConsumerAccess[K, V],
    pollFrequency: Duration,
    pollTimeout: Duration,
    requestQueue: Queue[Command.Request[K, V]],
    commitQueue: Queue[Command.Commit[K, V]],
    partitions: Queue[Take[Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, CommittableRecord[K, V]])]],
    rebalances: Queue[Rebalance],
    rebalancingRef: Ref[Boolean],
    rebalanceListener: ConsumerRebalanceListener
  ) {
    def commit(cmd: Command.Commit[K, V])   = commitQueue.offer(cmd).unit
    def commits                             = ZStream.fromQueue(commitQueue)
    def request(cmd: Command.Request[K, V]) = requestQueue.offer(cmd).unit
    def requests                            = ZStream.fromQueue(requestQueue)

    val isRebalancing = rebalancingRef.get

    def polls = ZStream(Command.Poll[K, V]()).repeat(Schedule.spaced(pollFrequency))
    def newPartition(tp: TopicPartition, data: StreamChunk[Throwable, CommittableRecord[K, V]]) =
      partitions.offer(Take.Value(tp -> data)).unit
  }
  object Deps {
    def make[K, V](consumer: ConsumerAccess[K, V], pollFrequency: Duration, pollTimeout: Duration) =
      for {
        rebalancingRef <- Ref.make(false).toManaged_
        requestQueue   <- Queue.unbounded[Command.Request[K, V]].toManaged(_.shutdown)
        commitQueue    <- Queue.unbounded[Command.Commit[K, V]].toManaged(_.shutdown)
        partitions <- Queue
                       .unbounded[
                         Take[Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, CommittableRecord[K, V]])]
                       ]
                       .toManaged(_.shutdown)
        rebalances <- Queue.unbounded[Rebalance].toManaged(_.shutdown)
        listener <- ZIO
                     .runtime[Blocking]
                     .map { runtime =>
                       new ConsumerRebalanceListener {
                         override def onPartitionsRevoked(partitions: java.util.Collection[TopicPartition]): Unit = {
                           runtime.unsafeRun(
                             rebalancingRef
                               .set(true) *> rebalances.offer(Rebalance.Revoked(partitions.asScala.toSet)).fork
                           )
                           ()
                         }

                         override def onPartitionsAssigned(partitions: java.util.Collection[TopicPartition]): Unit = {
                           runtime.unsafeRun(
                             rebalancingRef
                               .set(false) *> rebalances.offer(Rebalance.Assigned(partitions.asScala.toSet)).fork
                           )
                           consumer.consumer.pause(partitions)
                           ()
                         }
                       }
                     }
                     .toManaged_
      } yield Deps(
        consumer,
        pollFrequency,
        pollTimeout,
        requestQueue,
        commitQueue,
        partitions,
        rebalances,
        rebalancingRef,
        listener
      )
  }

  case class State[K, V](
    pendingRequests: List[Command.Request[K, V]],
    pendingCommits: List[Command.Commit[K, V]],
    bufferedRecords: Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]
  ) {
    def addCommit(c: Command.Commit[K, V])             = copy(pendingCommits = c :: pendingCommits)
    def setCommits(reqs: List[Command.Commit[K, V]])   = copy(pendingCommits = reqs)
    def addRequest(c: Command.Request[K, V])           = copy(pendingRequests = c :: pendingRequests)
    def setRequests(reqs: List[Command.Request[K, V]]) = copy(pendingRequests = reqs)
    def clearCommits                                   = copy(pendingCommits = Nil)
    def clearRequests                                  = copy(pendingRequests = Nil)
    def addBufferedRecords(recs: Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]) =
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

    def setBufferedRecords(recs: Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]) =
      copy(bufferedRecords = recs)
  }

  object State {
    def initial[K, V]: State[K, V] = State(Nil, Nil, Map())
  }

  def apply[K, V](deps: Deps[K, V]): ZManaged[Clock with Blocking, Throwable, Runloop[K, V]] = {

    def commit(offsets: Map[TopicPartition, Long]): ZIO[Any, Throwable, Unit] =
      for {
        p <- Promise.make[Throwable, Unit]
        _ <- deps.commit(Command.Commit[K, V](offsets, p))
        _ <- p.await
      } yield ()

    def partition(tp: TopicPartition): ZStreamChunk[Any, Throwable, CommittableRecord[K, V]] =
      ZStreamChunk {
        ZStream {
          ZManaged.succeed {
            for {
              p      <- Promise.make[Option[Throwable], Chunk[CommittableRecord[K, V]]]
              _      <- deps.request(Command.Request(tp, p))
              result <- p.await
            } yield result
          }
        }
      }

    def doCommit(cmds: List[Command.Commit[K, V]]): ZIO[Blocking, Nothing, Unit] =
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
                      if (err eq null) runtime.unsafeRun(cont(Exit.succeed(())))
                      else runtime.unsafeRun(cont(Exit.fail(err)))
                  }
                )
              }
            }.catchAll(e => cont(Exit.fail(e)))
      } yield ()

    def handlePoll(state: State[K, V]): BlockingTask[State[K, V]] =
      for {
        pollResult <- deps.consumer.withConsumerM { c =>
                       def endRevoked(
                         reqs: List[Command.Request[K, V]],
                         bufferedRecords: Map[TopicPartition, Chunk[ConsumerRecord[K, V]]],
                         revoked: TopicPartition => Boolean
                       ): UIO[(List[Command.Request[K, V]], Map[TopicPartition, Chunk[ConsumerRecord[K, V]]])] = {
                         var acc = List[Command.Request[K, V]]()
                         val buf = mutable.Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]()
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
                         pendingRequests: List[Command.Request[K, V]],
                         bufferedRecords: Map[TopicPartition, Chunk[ConsumerRecord[K, V]]],
                         records: ConsumerRecords[K, V]
                       ): UIO[(List[Command.Request[K, V]], Map[TopicPartition, Chunk[ConsumerRecord[K, V]]])] = {
                         var acc = List[Command.Request[K, V]]()
                         val buf = mutable.Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]()
                         buf ++= bufferedRecords

                         var fulfillAction: UIO[_] = UIO.unit

                         val reqsIt = pendingRequests.iterator
                         while (reqsIt.hasNext) {
                           val req           = reqsIt.next
                           val bufferedChunk = buf.getOrElse(req.tp, Chunk.empty)
                           val reqRecs       = records.records(req.tp)

                           if ((bufferedChunk.length + reqRecs.size) == 0)
                             acc ::= req
                           else {
                             val concatenatedChunk = bufferedChunk ++
                               Chunk.fromArray(
                                 reqRecs.toArray(Array.ofDim[ConsumerRecord[K, V]](reqRecs.size))
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
                         records: ConsumerRecords[K, V],
                         unrequestedTps: Iterable[TopicPartition]
                       ): Map[TopicPartition, Chunk[ConsumerRecord[K, V]]] = {
                         val builder = Map.newBuilder[TopicPartition, Chunk[ConsumerRecord[K, V]]]
                         builder.sizeHint(unrequestedTps.size)

                         val tpsIt = unrequestedTps.iterator
                         while (tpsIt.hasNext) {
                           val tp   = tpsIt.next
                           val recs = records.records(tp)

                           if (recs.size > 0)
                             builder += (tp -> Chunk.fromArray(
                               recs.toArray(Array.ofDim[ConsumerRecord[K, V]](recs.size))
                             ))
                         }

                         builder.result()
                       }

                       def actualPoll = Task.effectSuspend {
                         val prevAssigned = c.assignment().asScala

                         val requestedPartitions = state.pendingRequests.map(_.tp).toSet
                         c.resume(requestedPartitions.asJava)

                         val pollTimeout =
                           if (requestedPartitions.nonEmpty) deps.pollTimeout.asJava
                           else 0.millis.asJava
                         val records = c.poll(pollTimeout)

                         val tpsInResponse   = records.partitions.asScala
                         val currentAssigned = c.assignment().asScala
                         val newlyAssigned   = currentAssigned -- prevAssigned
                         val revoked         = prevAssigned -- currentAssigned
                         val unrequestedRecords =
                           bufferUnrequestedPartitions(records, tpsInResponse -- requestedPartitions)

                         endRevoked(
                           state.pendingRequests,
                           state.addBufferedRecords(unrequestedRecords).bufferedRecords,
                           revoked(_)
                         ).flatMap {
                           case (pendingRequests, bufferedRecords) =>
                             fulfillRequests(pendingRequests, bufferedRecords, records)
                         }.map((newlyAssigned.toSet, _))
                       }

                       ZIO(!c.subscription().isEmpty())
                         .flatMap(
                           if (_) actualPoll
                           else
                             ZIO.succeed(
                               (Set(), (state.pendingRequests, Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]()))
                             )
                         )
                     }
        (newlyAssigned, (pendingRequests, bufferedRecords)) = pollResult
        _                                                   <- ZIO.traverse_(newlyAssigned)(tp => deps.newPartition(tp, partition(tp)))
        stillRebalancing                                    <- deps.isRebalancing
        newPendingCommits <- if (!stillRebalancing && state.pendingCommits.nonEmpty)
                              doCommit(state.pendingCommits).as(Nil)
                            else ZIO.succeed(state.pendingCommits)
      } yield State(pendingRequests, newPendingCommits, bufferedRecords)

    def handleRequest(state: State[K, V], req: Command.Request[K, V]): URIO[Blocking, State[K, V]] =
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

    def handleCommit(state: State[K, V], cmd: Command.Commit[K, V]): URIO[Blocking, State[K, V]] =
      for {
        rebalancing <- deps.isRebalancing
        newState <- if (rebalancing)
                     UIO.succeed(state.addCommit(cmd))
                   else doCommit(List(cmd)).as(state)
      } yield newState

    ZStream
      .mergeAll(3, 32)(
        deps.polls,
        deps.requests,
        deps.commits
      )
      .foldM(State.initial[K, V]) { (state, cmd) =>
        cmd match {
          case Command.Poll()              => handlePoll(state)
          case req @ Command.Request(_, _) => handleRequest(state, req)
          case cmd @ Command.Commit(_, _)  => handleCommit(state, cmd)
        }
      }
      .onError { cause =>
        deps.partitions.offer(Take.Fail(cause))
      }
      .unit
      .toManaged_
      .fork
      .map(Runloop(_, deps))
  }
}
