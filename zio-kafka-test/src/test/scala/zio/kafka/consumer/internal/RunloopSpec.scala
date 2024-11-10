package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{
  ConsumerRebalanceListener,
  ConsumerRecord,
  MockConsumer,
  OffsetResetStrategy
}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException }
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
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
        Diagnostics.SlidingQueue.make(100).flatMap { diagnostics =>
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
        Diagnostics.SlidingQueue.make(100).flatMap { diagnostics =>
          var rebalanceListener: ConsumerRebalanceListener = null

          // Catches the rebalance listener so we can use it
          val mockConsumer: BinaryMockConsumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {
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
      }
    ) @@ withLiveClock

  private def withRunloop(
    diagnostics: Diagnostics = Diagnostics.NoOp,
    mockConsumer: BinaryMockConsumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST)
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
        runloop <- Runloop.make(
                     consumerSettings,
                     100.millis,
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
