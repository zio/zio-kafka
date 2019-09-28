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

import scala.collection.JavaConverters._
import org.apache.kafka.clients.consumer.ConsumerRecords

case class Runloop(fiber: Fiber[Throwable, Unit], deps: Runloop.Deps)
object Runloop {
  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]

  sealed abstract class Command
  object Command {
    case class Request(tp: TopicPartition, cont: Promise[Option[Throwable], Chunk[ByteArrayCommittableRecord]])
        extends Command
    case class Poll()                                                                     extends Command
    case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends Command
  }

  sealed abstract class Rebalance
  object Rebalance {
    case class Revoked(previousAssignment: Set[TopicPartition]) extends Rebalance
    case class Assigned(currentAssignment: Set[TopicPartition]) extends Rebalance
  }

  case class Deps(
    consumer: ConsumerAccess,
    pollFrequency: Duration,
    pollTimeout: Duration,
    requestQueue: Queue[Command.Request],
    commitQueue: Queue[Command.Commit],
    partitions: Queue[Take[Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, ByteArrayCommittableRecord])]],
    rebalances: Queue[Rebalance],
    rebalancingRef: Ref[Boolean],
    rebalanceListener: ConsumerRebalanceListener
  ) {
    def commit(cmd: Command.Commit)   = commitQueue.offer(cmd).unit
    def commits                       = ZStream.fromQueue(commitQueue)
    def request(cmd: Command.Request) = requestQueue.offer(cmd).unit
    def requests                      = ZStream.fromQueue(requestQueue)

    val isRebalancing = rebalancingRef.get

    def polls = ZStream(Command.Poll()).repeat(Schedule.spaced(pollFrequency))
    def newPartition(tp: TopicPartition, data: StreamChunk[Throwable, ByteArrayCommittableRecord]) =
      partitions.offer(Take.Value(tp -> data)).unit
  }
  object Deps {
    def make(consumer: ConsumerAccess, pollFrequency: Duration, pollTimeout: Duration) =
      for {
        rebalancingRef <- Ref.make(false).toManaged_
        requestQueue   <- Queue.unbounded[Command.Request].toManaged(_.shutdown)
        commitQueue    <- Queue.unbounded[Command.Commit].toManaged(_.shutdown)
        partitions <- Queue
                       .unbounded[
                         Take[Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, ByteArrayCommittableRecord])]
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

  case class State(
    pendingRequests: List[Command.Request],
    pendingCommits: List[Command.Commit]
  ) {
    def addCommit(c: Command.Commit)             = copy(pendingCommits = c :: pendingCommits)
    def setCommits(reqs: List[Command.Commit])   = copy(pendingCommits = reqs)
    def addRequest(c: Command.Request)           = copy(pendingRequests = c :: pendingRequests)
    def setRequests(reqs: List[Command.Request]) = copy(pendingRequests = reqs)
    def clearCommits                             = copy(pendingCommits = Nil)
    def clearRequests                            = copy(pendingRequests = Nil)
  }

  object State {
    def initial: State = State(Nil, Nil)
  }

  def apply(deps: Deps): ZManaged[Clock with Blocking, Throwable, Runloop] = {

    def commit(offsets: Map[TopicPartition, Long]): ZIO[Any, Throwable, Unit] =
      for {
        p <- Promise.make[Throwable, Unit]
        _ <- deps.commit(Command.Commit(offsets, p))
        _ <- p.await
      } yield ()

    def partition(tp: TopicPartition): ZStreamChunk[Any, Throwable, ByteArrayCommittableRecord] =
      ZStreamChunk {
        ZStream {
          ZManaged.succeed {
            for {
              p      <- Promise.make[Option[Throwable], Chunk[ByteArrayCommittableRecord]]
              _      <- deps.request(Command.Request(tp, p))
              result <- p.await
            } yield result
          }
        }
      }

    def doCommit(cmds: List[Command.Commit]): ZIO[Blocking, Nothing, Unit] =
      for {
        runtime <- ZIO.runtime[Any]
        offsets = {
          val offsets = scala.collection.mutable.Map[TopicPartition, OffsetAndMetadata]()

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

    def handlePoll(state: State): BlockingTask[State] =
      for {
        pollResult <- deps.consumer.withConsumerM { c =>
                       def endRevoked(reqs: List[Command.Request], revoked: TopicPartition => Boolean) =
                         UIO.foldLeft(reqs)(List[Command.Request]()) { (acc, req) =>
                           if (revoked(req.tp)) req.cont.fail(None).as(acc)
                           else ZIO.succeed(req :: acc)
                         }

                       def fulfillRequests(
                         pendingRequests: List[Command.Request],
                         records: ConsumerRecords[Array[Byte], Array[Byte]]
                       ) =
                         UIO
                           .foldLeft(pendingRequests)(List[Command.Request]()) { (acc, req) =>
                             val reqRecs = records.records(req.tp)

                             if (reqRecs.size == 0) ZIO.succeed(req :: acc)
                             else
                               req.cont
                                 .succeed(
                                   Chunk
                                     .fromArray(
                                       reqRecs
                                         .toArray(Array.ofDim[ConsumerRecord[Array[Byte], Array[Byte]]](reqRecs.size))
                                     )
                                     .map(CommittableRecord(_, commit(_)))
                                 )
                                 .as(acc)
                           }

                       def actualPoll = Task.effectSuspend {
                         val prevAssigned = c.assignment().asScala

                         val requestedPartitions = state.pendingRequests.map(_.tp).toSet
                         c.resume(requestedPartitions.asJava)

                         val pollTimeout =
                           if (requestedPartitions.nonEmpty) deps.pollTimeout.asJava
                           else 0.millis.asJava
                         val records       = c.poll(pollTimeout)
                         val tpsInResponse = records.partitions.asScala
                         val unexpectedTps = tpsInResponse -- requestedPartitions

                         if (unexpectedTps.nonEmpty)
                           ZIO.dieMessage(s"Received unexpected records from Kafka for partitions: $unexpectedTps")
                         else {
                           val currentAssigned = c.assignment().asScala
                           val newlyAssigned   = currentAssigned -- prevAssigned
                           val revoked         = prevAssigned -- currentAssigned

                           endRevoked(state.pendingRequests, revoked(_))
                             .flatMap(fulfillRequests(_, records))
                             .map((newlyAssigned.toSet, _))
                         }
                       }

                       ZIO(!c.subscription().isEmpty())
                         .flatMap(
                           if (_) actualPoll
                           else ZIO.succeed((Set(), state.pendingRequests))
                         )
                     }
        (newlyAssigned, pendingRequests) = pollResult
        _                                <- ZIO.traverse_(newlyAssigned)(tp => deps.newPartition(tp, partition(tp)))
        stillRebalancing                 <- deps.isRebalancing
        newState <- if (!stillRebalancing && state.pendingCommits.nonEmpty)
                     doCommit(state.pendingCommits)
                       .as(state.setRequests(pendingRequests).clearCommits)
                   else ZIO.succeed(state.setRequests(pendingRequests))
      } yield newState

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

    ZStream
      .mergeAll(3, 32)(
        deps.polls,
        deps.requests,
        deps.commits
      )
      .foldM(State.initial) { (state, cmd) =>
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
