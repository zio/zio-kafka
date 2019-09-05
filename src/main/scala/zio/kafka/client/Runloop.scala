package zio.kafka.client

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.consumer.OffsetCommitCallback
import org.apache.kafka.common.TopicPartition
import zio._
import zio.duration._
import zio.stream._
import zio.blocking.Blocking
import zio.clock.Clock

import scala.collection.mutable
import scala.collection.JavaConverters._

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

  def deps[K, V](consumer: ConsumerAccess[K, V], pollFrequency: Duration, pollTimeout: Duration) =
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

  case class State[K, V](
    pendingRequests: List[Command.Request[K, V]],
    pendingCommits: List[Command.Commit[K, V]]
  ) {
    def addCommit(c: Command.Commit[K, V])             = copy(pendingCommits = c :: pendingCommits)
    def setCommits(reqs: List[Command.Commit[K, V]])   = copy(pendingCommits = reqs)
    def addRequest(c: Command.Request[K, V])           = copy(pendingRequests = c :: pendingRequests)
    def setRequests(reqs: List[Command.Request[K, V]]) = copy(pendingRequests = reqs)
    def clearCommits                                   = copy(pendingCommits = Nil)
    def clearRequests                                  = copy(pendingRequests = Nil)
  }

  object State {
    def initial[K, V]: State[K, V] = State(Nil, Nil)
  }

  def apply[K, V](deps: Deps[K, V]): ZManaged[Clock with Blocking, Throwable, Runloop[K, V]] = {
    def mergeCommits(
      l: List[Command.Commit[K, V]]
    ): (Map[TopicPartition, OffsetAndMetadata], Exit[Throwable, Unit] => UIO[Unit]) = {
      val offsets = scala.collection.mutable.Map[TopicPartition, OffsetAndMetadata]()

      l.foreach { commit =>
        commit.offsets.foreach {
          case (tp, offset) =>
            val existing = offsets.get(tp).fold(-1L)(_.offset())

            if (existing < offset)
              offsets += tp -> new OffsetAndMetadata(offset + 1)
        }
      }

      offsets.toMap -> (e => ZIO.traverse_(l)(_.cont.done(e)))
    }

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

    ZStream
      .mergeAll(3, 32)(
        deps.polls,
        deps.requests,
        deps.commits
      )
      .fold(State.initial[K, V])(_ => true) { (state, cmd) =>
        def doCommit(cmds: List[Command.Commit[K, V]]) =
          for {
            runtime <- ZIO.runtime[Any]
            _ <- deps.consumer.withConsumerM { c =>
                  val (offsets, cont) = mergeCommits(cmds)
                  ZIO {
                    c.commitAsync(
                      offsets.asJava,
                      new OffsetCommitCallback {
                        def onComplete(data: java.util.Map[TopicPartition, OffsetAndMetadata], err: Exception) =
                          if (err eq null) runtime.unsafeRun(cont(Exit.succeed(())))
                          else runtime.unsafeRun(cont(Exit.fail(err)))
                      }
                    )
                  }.catchAll(e => cont(Exit.fail(e)))
                }
          } yield state

        cmd match {
          case Command.Poll() =>
            val pendingRequests = state.pendingRequests
            for {
              pollResult <- deps.consumer.withConsumerM { c =>
                             ZIO {
                               val currentSubscription = c.subscription()
                               if (currentSubscription.isEmpty())
                                 ZIO.succeed((mutable.Set(), pendingRequests))
                               else {
                                 val prevAssigned = c.assignment().asScala

                                 val requestedPartitions = pendingRequests.map(_.tp).toSet
                                 c.resume(requestedPartitions.asJava)

                                 val pollTimeout =
                                   if (requestedPartitions.nonEmpty) deps.pollTimeout.asJava
                                   else 0.millis.asJava
                                 val records       = c.poll(pollTimeout)
                                 val tpsInResponse = records.partitions.asScala

                                 if ((tpsInResponse -- requestedPartitions).nonEmpty)
                                   ZIO.dieMessage("Received unexpected records from Kafka")
                                 else {
                                   val currentAssigned = c.assignment().asScala
                                   val newlyAssigned   = currentAssigned -- prevAssigned
                                   val revoked         = prevAssigned -- currentAssigned

                                   val (fulfilledRequests, leftoverRequests) = pendingRequests.partition(
                                     req => revoked.contains(req.tp) || records.records(req.tp).size != 0
                                   )

                                   ZIO
                                     .traverse_(fulfilledRequests) { req =>
                                       if (revoked.contains(req.tp)) req.cont.fail(None).unit
                                       else {
                                         val reqRecs = records.records(req.tp)
                                         req.cont
                                           .succeed(
                                             Chunk
                                               .fromArray(
                                                 reqRecs.toArray(Array.ofDim[ConsumerRecord[K, V]](reqRecs.size))
                                               )
                                               .map(CommittableRecord(_, commit(_)))
                                           )
                                           .unit

                                       }
                                     }
                                     .as((newlyAssigned, leftoverRequests))
                                 }
                               }
                             }.flatten
                           }
              (newlyAssigned, pendingRequests) = pollResult
              _ <- ZIO.traverse(newlyAssigned) { tp =>
                    deps.newPartition(tp, partition(tp))
                  }
              stillRebalancing <- deps.isRebalancing
              newState <- if (!stillRebalancing && state.pendingCommits.nonEmpty)
                           doCommit(state.pendingCommits)
                             .as(state.setRequests(pendingRequests).clearCommits)
                         else ZIO.succeed(state.setRequests(pendingRequests))
            } yield newState
          case req @ Command.Request(tp, cont) =>
            for {
              assignment  <- deps.consumer.withConsumer((_: KafkaConsumer[K, V]).assignment.asScala)
              rebalancing <- deps.isRebalancing
              newState <- if (!rebalancing && !assignment.contains(tp))
                           cont.fail(None).as(state)
                         else UIO.succeed(state.addRequest(req))
            } yield newState

          case cmd @ Command.Commit(_, _) =>
            for {
              rebalancing <- deps.isRebalancing
              newState <- if (rebalancing)
                           UIO.succeed(state.addCommit(cmd))
                         else doCommit(List(cmd)).as(state)
            } yield newState
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
