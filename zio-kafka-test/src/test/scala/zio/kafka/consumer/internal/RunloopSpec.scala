package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ ConsumerRebalanceListener, ConsumerRecord, MockConsumer }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException, TopicAuthorizationException }
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.Consumer.ConsumerDiagnostics
import zio.kafka.consumer.diagnostics.DiagnosticEvent
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.kafka.consumer.metrics.ConsumerMetricsObserver
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
import zio.kafka.diagnostics.{ Diagnostics, SlidingDiagnostics }
import zio.metrics.{ MetricState, Metrics }
import zio.stream.{ Take, ZStream }
import zio.test.TestAspect.withLiveClock
import zio.test._

import java.util
import scala.jdk.CollectionConverters._

object RunloopSpec extends ZIOSpecDefaultSlf4j {

  private type BinaryMockConsumer = MockConsumer[Array[Byte], Array[Byte]]
  private type PartitionsHub      = Hub[Take[Throwable, PartitionAssignment]]

  private val tp10   = new TopicPartition("t1", 0)
  private val tp11   = new TopicPartition("t1", 1)
  private val key123 = "123".getBytes

  private val consumerSettings = ConsumerSettings(List("bootstrap"))

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunloopSpec")(
      test("runloop creates a new partition stream and polls for new records") {
        withRunloop() { (mockConsumer, partitionsHub, runloop) =>
          mockConsumer.schedulePollTask { () =>
            mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L)).asJava)
            mockConsumer.rebalance(Seq(tp10).asJava)
            mockConsumer.addRecord(makeConsumerRecord(tp10, key123))
          }
          for {
            streamStream <- ZStream.fromHubScoped(partitionsHub)
            _            <- runloop.addSubscription(Subscription.Topics(Set(tp10.topic())))
            record <- streamStream
                        .map(_.exit)
                        .flattenExitOption
                        .flattenChunks
                        .take(1)
                        .mapZIO { case (_, stream) =>
                          stream.runHead
                            .someOrFail(new AssertionError("Expected at least 1 record"))
                        }
                        .runHead
                        .someOrFail(new AssertionError("Expected at least 1 record from the streams"))
          } yield assertTrue(
            record.key sameElements key123
          )
        }
      },
      test(
        "runloop does not starts a new stream for partition which being revoked right after assignment within the same RebalanceEvent"
      ) {
        SlidingDiagnostics.make[DiagnosticEvent](100).flatMap { diagnostics =>
          withRunloop(diagnostics) { (mockConsumer, partitionsHub, runloop) =>
            mockConsumer.schedulePollTask { () =>
              mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L), tp11 -> Long.box(0L)).asJava)
              mockConsumer.rebalance(Seq(tp10, tp11).asJava)
              mockConsumer.rebalance(Seq(tp10).asJava)
              mockConsumer.addRecord(makeConsumerRecord(tp10, key123))
            }
            for {
              streamStream <- ZStream.fromHubScoped(partitionsHub)
              _            <- runloop.addSubscription(Subscription.Topics(Set(tp10, tp11).map(_.topic())))
              _ <- streamStream
                     .map(_.exit)
                     .flattenExitOption
                     .flattenChunks
                     .take(1)
                     .mapZIO { case (_, stream) =>
                       stream.runHead
                     }
                     .runDrain
              diagnosticEvents <- diagnostics.queue.takeAll
              rebalanceEvents =
                diagnosticEvents.collect { case rebalanceEvent: DiagnosticEvent.Rebalance =>
                  rebalanceEvent
                }
            } yield assertTrue(
              rebalanceEvents.length == 1,
              rebalanceEvents.head == DiagnosticEvent.Rebalance(
                revoked = Set(tp11),
                assigned = Set(tp10),
                lost = Set.empty,
                ended = Set.empty
              )
            )
          }
        }
      },
      test(
        "runloop continues polling after a lost partition"
      ) {
        SlidingDiagnostics.make[DiagnosticEvent](100).flatMap { diagnostics =>
          var rebalanceListener: ConsumerRebalanceListener = null

          // Catches the rebalance listener so we can use it
          val mockConsumer: BinaryMockConsumer = new BinaryMockConsumer("latest") {
            override def subscribe(
              topics: util.Collection[String],
              listener: ConsumerRebalanceListener
            ): Unit = {
              rebalanceListener = listener
              super.subscribe(topics, listener)
            }
          }

          withRunloop(diagnostics, mockConsumer) { (mockConsumer, partitionsHub, runloop) =>
            mockConsumer.schedulePollTask { () =>
              mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L), tp11 -> Long.box(0L)).asJava)
              mockConsumer.rebalance(Seq(tp10, tp11).asJava)
            }
            mockConsumer.schedulePollTask { () =>
              rebalanceListener.onPartitionsLost(Seq(tp10, tp11).asJava)
            }
            mockConsumer.schedulePollTask { () =>
              mockConsumer.rebalance(Seq.empty.asJava)
              mockConsumer.rebalance(Seq(tp10, tp11).asJava)
            }

            for {
              streamStream <- ZStream.fromHubScoped(partitionsHub)
              _            <- runloop.addSubscription(Subscription.Topics(Set(tp10, tp11).map(_.topic())))
              result <- streamStream
                          .map(_.exit)
                          .flattenExitOption
                          .flattenChunks
                          .take(3)
                          .mapZIO { case (_, stream) =>
                            stream.runHead
                          }
                          .runDrain
                          .timeout(10.seconds)
            } yield assertTrue(
              result.isDefined
            ) // Test will not finish if polling did not continue after partitions lost
          }
        }
      },
      test("runloop retries poll upon AuthorizationException and AuthenticationException") {
        withRunloop() { (mockConsumer, partitionsHub, runloop) =>
          mockConsumer.schedulePollTask { () =>
            mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L)).asJava)
            mockConsumer.rebalance(Seq(tp10).asJava)
          }
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(new AuthorizationException("~~test~~"))
          }
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(new AuthenticationException("~~test~~"))
          }
          mockConsumer.schedulePollTask { () =>
            mockConsumer.addRecord(makeConsumerRecord(tp10, key123))
          }
          for {
            streamStream <- ZStream.fromHubScoped(partitionsHub)
            _            <- runloop.addSubscription(Subscription.Topics(Set(tp10.topic())))
            record <- streamStream
                        .map(_.exit)
                        .flattenExitOption
                        .flattenChunks
                        .take(1)
                        .mapZIO { case (_, stream) =>
                          stream.runHead
                            .someOrFail(new AssertionError("Expected at least 1 record"))
                        }
                        .runHead
                        .someOrFail(new AssertionError("Expected at least 1 record from the streams"))
            authErrorCount <- ZIO.metrics.map(counterValue("ziokafka_consumer_poll_auth_errors"))
          } yield assertTrue(
            record.key sameElements key123,
            authErrorCount.contains(2d)
          )
        }
      },
      test("partition stream fails when Runloop crashes after auth retries exhaust with empty dataQueue") {
        val authException = new TopicAuthorizationException("acl-denied")
        // recurs(0): no retries, so the Runloop crashes on the first auth exception.
        // NoOp metricsObserver: this test does not pollute the global auth-error counter that
        // other tests asserts on, so tests stay independent and can run concurrently.
        val settings = consumerSettings
          .withAuthErrorRetrySchedule(Schedule.recurs(0))
          .withMetricsObserver(ConsumerMetricsObserver.NoOp)
        withRunloop(settings = settings) { (mockConsumer, partitionsHub, runloop) =>
          // 1st poll: assign the partition with no records, so the per-partition stream parks on dataQueue.take.
          mockConsumer.schedulePollTask { () =>
            mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L)).asJava)
            mockConsumer.rebalance(Seq(tp10).asJava)
          }
          // 2nd poll: throw an auth exception. With `recurs(0)` the Runloop crashes immediately.
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(authException)
          }
          for {
            streamStream <- ZStream.fromHubScoped(partitionsHub)
            _            <- runloop.addSubscription(Subscription.Topics(Set(tp10.topic())))
            exit <- streamStream
                      .map(_.exit)
                      .flattenExitOption
                      .flattenChunks
                      .flatMapPar(Int.MaxValue) { case (_, stream) => stream }
                      .runDrain
                      .timeout(10.seconds)
                      .exit
          } yield assertTrue(
            // Since runloop informs the partition streams when it crashes, `exit` contains the auth exception.
            // Without informing the partition streams, this would time out (and not get an auth exception).
            exit.causeOption.exists(_.squash == authException)
          )
        }
      },
      test("removeSubscription does not hang after Runloop crashes") {
        val authException = new TopicAuthorizationException("acl-denied")
        val settings = consumerSettings
          .withAuthErrorRetrySchedule(Schedule.recurs(0))
          .withMetricsObserver(ConsumerMetricsObserver.NoOp)
        withRunloop(settings = settings) { (mockConsumer, _, runloop) =>
          val subscription = Subscription.Topics(Set(tp10.topic()))
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(authException)
          }
          for {
            _ <- runloop.addSubscription(subscription)
            // Wait for the runloop to crash
            _ <- ZIO.sleep(2.seconds)
            // removeSubscription calls offerAndAwaitCommand; without runloopDone it would hang forever
            exit <- runloop.removeSubscription(subscription).timeout(5.seconds).exit
          } yield assertTrue(
            // Should complete promptly (not time out) now that runloopDone unblocks the command promise.
            // When the runloop crashes, removeSubscription may fail with the runloop's error — that is
            // acceptable: the key property is that it does NOT hang (i.e. the timeout does not fire).
            exit match {
              case Exit.Success(None) => false // timed out — the bug we are fixing
              case _                  => true  // completed (success or failure) — not hung
            }
          )
        }
      },
      test("removeSubscription does not hang in uninterruptible context after Runloop crashes") {
        // Simulates ZIO.addFinalizer: removeSubscription is called inside ZIO.uninterruptible.
        // A raceFirst-based fix would hang here because raceFirst tries to interrupt the losing
        // fiber, but the interrupt is masked in uninterruptible context.
        val authException = new TopicAuthorizationException("acl-denied")
        val settings = consumerSettings
          .withAuthErrorRetrySchedule(Schedule.recurs(0))
          .withMetricsObserver(ConsumerMetricsObserver.NoOp)
        withRunloop(settings = settings) { (mockConsumer, _, runloop) =>
          val subscription = Subscription.Topics(Set(tp10.topic()))
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(authException)
          }
          for {
            _ <- runloop.addSubscription(subscription)
            _ <- ZIO.sleep(2.seconds)
            exit <- ZIO.uninterruptible {
                      runloop.removeSubscription(subscription)
                    }.timeout(5.seconds).exit
          } yield assertTrue(
            exit match {
              case Exit.Success(None) => false
              case _                  => true
            }
          )
        }
      },
      test("endStreamsBySubscription does not hang in uninterruptible context after Runloop crashes") {
        // Simulates StreamControl.end called from ZIO.addFinalizer (uninterruptible context).
        val authException = new TopicAuthorizationException("acl-denied")
        val settings = consumerSettings
          .withAuthErrorRetrySchedule(Schedule.recurs(0))
          .withMetricsObserver(ConsumerMetricsObserver.NoOp)
        withRunloop(settings = settings) { (mockConsumer, _, runloop) =>
          val subscription = Subscription.Topics(Set(tp10.topic()))
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(authException)
          }
          for {
            _ <- runloop.addSubscription(subscription)
            _ <- ZIO.sleep(2.seconds)
            exit <- ZIO.uninterruptible {
                      runloop.endStreamsBySubscription(subscription)
                    }.timeout(5.seconds).exit
          } yield assertTrue(
            exit match {
              case Exit.Success(None) => false
              case _                  => true
            }
          )
        }
      }
    ) @@ withLiveClock

  private def withRunloop(
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    mockConsumer: BinaryMockConsumer = new BinaryMockConsumer("latest"),
    settings: ConsumerSettings = consumerSettings
  )(
    f: (BinaryMockConsumer, PartitionsHub, Runloop) => ZIO[Scope, Throwable, TestResult]
  ): ZIO[Scope, Throwable, TestResult] =
    ZIO.scoped {
      for {
        access <- Semaphore.make(1)
        consumerAccess = new ConsumerAccess(mockConsumer, access)
        consumerScope <- ZIO.scope
        partitionsHub <- ZIO
                           .acquireRelease(Hub.unbounded[Take[Throwable, PartitionAssignment]])(_.shutdown)
                           .provide(ZLayer.succeed(consumerScope))
        runloopConfig <- RunloopConfig(settings)
        runloop <- Runloop.make(
                     settings,
                     runloopConfig,
                     diagnostics,
                     consumerAccess,
                     partitionsHub
                   )
        result <- f(mockConsumer, partitionsHub, runloop)
      } yield result
    }

  private def makeConsumerRecord(tp: TopicPartition, key: Array[Byte]): ConsumerRecord[Array[Byte], Array[Byte]] =
    new ConsumerRecord[Array[Byte], Array[Byte]](tp.topic(), tp.partition(), 0L, key, "value".getBytes)

  private def counterValue(counterName: String)(metrics: Metrics): Option[Double] =
    metrics.metrics
      .find(_.metricKey.name == counterName)
      .map(_.metricState)
      .flatMap {
        case MetricState.Counter(count) => Some(count)
        case _                          => Option.empty[Double]
      }
}
