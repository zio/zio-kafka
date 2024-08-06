package zio.kafka.consumer

//import io.github.embeddedkafka.EmbeddedKafka
//import org.apache.kafka.clients.consumer.ConsumerConfig
//import org.apache.kafka.clients.consumer.ConsumerPartitionAssignor
//import org.apache.kafka.clients.consumer.CooperativeStickyAssignor
//import org.apache.kafka.clients.consumer.MockConsumer
//import org.apache.kafka.clients.consumer.OffsetResetStrategy
//import org.apache.kafka.common.config.ConfigException
import zio.kafka.serde.Serde
//import org.apache.kafka.clients.consumer.RangeAssignor
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.apache.kafka.common.TopicPartition
//import org.apache.kafka.common.config.ConfigException
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
//import zio.kafka.consumer.Consumer.AutoOffsetStrategy
//import zio.kafka.consumer.Consumer.CommitTimeout
//import zio.kafka.consumer.Consumer.OffsetRetrieval
//import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
//import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization.ConsumerFinalized
//import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization.RunloopFinalized
//import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization.SubscriptionFinalized
//import zio.kafka.consumer.diagnostics.DiagnosticEvent
//import zio.kafka.consumer.diagnostics.Diagnostics
//import zio.kafka.producer.Producer
//import zio.kafka.producer.TransactionalProducer
//import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils._
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaRandom
//import zio.stream.ZSink
//import zio.stream.ZStream
//import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

//import scala.reflect.ClassTag

//noinspection SimplifyAssertInspection
object ConsumerAliveSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "consumespec2"

  override def spec /*: Spec[TestEnvironment with Scope, Throwable]*/ =
    suite("Consumer isAlive")(
      test("isAlive should be true when everything is fine") {
        for {
          client <- randomClient
          group  <- randomGroup
          c      <- consumer(client, Some(group)).build

          isAlive <- c.get.isAlive
        } yield assertTrue(isAlive)
      },
      test("isAlive should turn false when consumer gets shutdown") {
        for {
          client <- randomClient
          group  <- randomGroup
          topic  <- randomTopic
          cs     <- consumerSettings(client, Some(group))

//          mock = new MockConsumer[Array[Byte], Array[Byte]](OffsetResetStrategy.NONE)

          scope <- Scope.make
          _     <- scope.addFinalizer(ZIO.debug("scope getting closed"))
          c     <- Consumer.make(cs).provide(ZLayer.succeed(scope))

          _ <- Consumer
                 .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                 .take(1)
                 .runCollect
                 .provide(ZLayer.succeed(c))
                 .fork

//          // wait for runloop to start
//          _ <- c.isAlive.repeatWhileZIO(_ => c.isAlive.map(!_)).timeout(10.seconds)

          isAliveBefore <- c.isAlive

          _ <- scope.close(Exit.unit).fork

          _            <- c.isAlive.repeatWhileZIO(_ => c.isAlive)
          _            <- ZIO.logDebug("after sleeping")
          _            <- c.isAlive.repeatWhileZIO(_ => c.isAlive).timeout(15.seconds)
          _            <- ZIO.logDebug("after repeatWhile")
          isAliveAfter <- c.isAlive
          _            <- ZIO.logDebug(s"should be down now: ${isAliveAfter}")
        } yield assertTrue(isAliveBefore) && assertTrue(!isAliveAfter)

      }
//      test("isAlive should turn false when the runloop crashes") {
//        for {
//          client <- randomClient
//          group  <- randomGroup
//          topic  <- randomTopic
//          cs     <- consumerSettings(client, Some(group))
//          mock = new MockConsumer[Array[Byte], Array[Byte]](OffsetResetStrategy.NONE)
//          c <- Consumer.fromJavaConsumer(
//                 mock,
//                 cs
//               )
//
//          _ <- Consumer
//                 .plainStream(Subscription.Topics(Set(topic)), Serde.string, Serde.string)
//                 .take(5)
//                 .runCollect
//                 .sandbox
//                 .provide(ZLayer.succeed(c))
//                 .fork
//                 .sandbox
//
//          isAliveBefore <- c.isAlive
//          _ = mock.setPollException(new ConfigException("dummy"))
//          _            <- ZIO.debug("after setting poll exception")
//          _            <- ZIO.sleep(2.seconds)
//          _            <- ZIO.debug("after sleeping")
//          _            <- c.isAlive.repeatWhileZIO(_ => c.isAlive)
//          _            <- ZIO.debug("after repeatWhile")
//          isAliveAfter <- c.isAlive
//          _            <- ZIO.debug(s"should be down now: ${isAliveAfter}")
//        } yield assertTrue(isAliveBefore) && assertTrue(!isAliveAfter)
//
//      }
    )
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ timeout(2.minutes)

}
