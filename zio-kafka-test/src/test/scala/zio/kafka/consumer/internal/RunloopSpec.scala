package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ ConsumerRecord, MockConsumer, OffsetResetStrategy }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.AuthorizationException
import zio._
import zio.kafka.consumer.{ ConsumerSettings, Subscription }
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.metrics.MetricState
import zio.stream.{ Take, ZStream }
import zio.test.TestAspect.withLiveClock
import zio.test._

import scala.jdk.CollectionConverters._

object RunloopSpec extends ZIOSpecDefault {

  private type BinaryMockConsumer = MockConsumer[Array[Byte], Array[Byte]]
  private type PartitionsHub      = Hub[Take[Throwable, PartitionAssignment]]

  private val tp10   = new TopicPartition("t1", 0)
  private val key123 = "123".getBytes

  private val consumerSettings = ConsumerSettings(List("bootstrap"))

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunloopSpec")(
      test("runloop creates a new partition stream and polls for new records") {
        withRunloop { (mockConsumer, partitionsHub, runloop) =>
          mockConsumer.schedulePollTask { () =>
            mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L)).asJava)
            mockConsumer.rebalance(Seq(tp10).asJava)
            mockConsumer.addRecord(makeConsumerRecord(tp10, key123))
          }
          for {
            streamStream <- ZStream.fromHubScoped(partitionsHub)
            _            <- runloop.addSubscription(Subscription.Topics(Set("t1")))
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
      test("runloop retries poll upon AuthorizationException") {
        withRunloop { (mockConsumer, partitionsHub, runloop) =>
          mockConsumer.schedulePollTask { () =>
            mockConsumer.updateEndOffsets(Map(tp10 -> Long.box(0L)).asJava)
            mockConsumer.rebalance(Seq(tp10).asJava)
          }
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(new AuthorizationException("~~test~~"))
          }
          mockConsumer.schedulePollTask { () =>
            mockConsumer.setPollException(new AuthorizationException("~~test~~"))
          }
          mockConsumer.schedulePollTask { () =>
            mockConsumer.addRecord(makeConsumerRecord(tp10, key123))
          }
          for {
            streamStream <- ZStream.fromHubScoped(partitionsHub)
            _            <- runloop.addSubscription(Subscription.Topics(Set("t1")))
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
            metrics <- ZIO.metrics
            authErrorCount = metrics.metrics
                               .find(_.metricKey.name == "ziokafka_consumer_poll_auth_errors")
                               .map(_.metricState)
                               .flatMap {
                                 case MetricState.Counter(count) => Some(count)
                                 case _                          => Option.empty[Double]
                               }
          } yield assertTrue(
            record.key sameElements key123,
            authErrorCount.contains(2d)
          )
        }
      }
    ) @@ withLiveClock

  private def withRunloop(
    f: (BinaryMockConsumer, PartitionsHub, Runloop) => ZIO[Scope, Throwable, TestResult]
  ): ZIO[Scope, Throwable, TestResult] =
    ZIO.scoped {
      val mockConsumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST)
      for {
        consumerAccess <- ConsumerAccess.make(mockConsumer)
        consumerScope  <- ZIO.scope
        partitionsHub <- ZIO
                           .acquireRelease(Hub.unbounded[Take[Throwable, PartitionAssignment]])(_.shutdown)
                           .provide(ZLayer.succeed(consumerScope))
        runloop <- Runloop.make(
                     consumerSettings,
                     100.millis,
                     100.millis,
                     Diagnostics.NoOp,
                     consumerAccess,
                     partitionsHub
                   )
        result <- f(mockConsumer, partitionsHub, runloop)
      } yield result
    }

  private def makeConsumerRecord(tp: TopicPartition, key: Array[Byte]): ConsumerRecord[Array[Byte], Array[Byte]] =
    new ConsumerRecord[Array[Byte], Array[Byte]](tp.topic(), tp.partition(), 0L, key, "value".getBytes)

}
