package zio.kafka.consumer

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils._
import zio.kafka.testkit.{ Kafka, KafkaRandom }
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.util.concurrent.atomic.AtomicInteger

//noinspection SimplifyAssertInspection
object SubscriptionsSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "subscriptionsspec"

  override def spec: Spec[TestEnvironment with Scope, Throwable] = suite("Consumer subscriptions")(
    test("consumes from two topic subscriptions") {
      val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        topic2 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)
        _ <- produceMany(topic2, kvs)

        records <-
          (Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .take(5)
            .runCollect zipPar
            Consumer
              .plainStream(Subscription.topics(topic2), Serde.string, Serde.string)
              .take(5)
              .runCollect)
            .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
        (records1, records2) = records
        kvOut1               = records1.map(r => (r.record.key, r.record.value)).toList
        kvOut2               = records2.map(r => (r.record.key, r.record.value)).toList
      } yield assertTrue(kvOut1 == kvs) &&
        assertTrue(kvOut2 == kvs) &&
        assertTrue(records1.map(_.record.topic()).forall(_ == topic1)) &&
        assertTrue(records2.map(_.record.topic()).forall(_ == topic2))
    },
    test("consumes from two pattern subscriptions") {
      val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        topic2 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)
        _ <- produceMany(topic2, kvs)

        records <-
          (Consumer
            .plainStream(Subscription.Pattern(s"$topic1".r), Serde.string, Serde.string)
            .take(5)
            .runCollect zipPar
            Consumer
              .plainStream(Subscription.Pattern(s"$topic2".r), Serde.string, Serde.string)
              .take(5)
              .runCollect)
            .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
        (records1, records2) = records
        kvOut1               = records1.map(r => (r.record.key, r.record.value)).toList
        kvOut2               = records2.map(r => (r.record.key, r.record.value)).toList
      } yield assertTrue(kvOut1 == kvs) &&
        assertTrue(kvOut2 == kvs) &&
        assertTrue(records1.map(_.record.topic()).forall(_ == topic1)) &&
        assertTrue(records2.map(_.record.topic()).forall(_ == topic2))
    },
    test(
      "gives an error when attempting to subscribe using a manual subscription when there is already a topic subscription"
    ) {
      val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        topic2 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)
        _ <- produceMany(topic2, kvs)

        consumer0 =
          Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .runCollect

        consumer1 =
          Consumer
            .plainStream(
              Subscription.manual(topic2, 1), // invalid with the previous subscription
              Serde.string,
              Serde.string
            )
            .runCollect

        result <- (consumer0 zipPar consumer1)
                    .provideSomeLayer[Kafka & Scope](consumer(client, Some(group)))
                    .unit
                    .exit
      } yield assert(result)(fails(isSubtype[InvalidSubscriptionUnion](anything)))
    },
    test(
      "gives an error when attempting to subscribe using a manual subscription when there is already a topic subscription and doesn't fail the already running consuming session"
    ) {
      val numberOfMessages = 20
      val kvs              = (0 to numberOfMessages).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)

        counter = new AtomicInteger(1)

        firstMessagesRef <- Ref.make(("", ""))
        finalizersRef    <- Ref.make(Chunk.empty[String])

        consumer0 =
          Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            // Here we delay each message to be sure that `consumer1` will fail while `consumer0` is still running
            .mapZIO { r =>
              firstMessagesRef.updateSome { case ("", v) =>
                ("First consumer0 message", v)
              } *>
                ZIO
                  .logDebug(s"Consumed ${counter.getAndIncrement()} records")
                  .delay(10.millis)
                  .as(r)
            }
            .take(numberOfMessages.toLong)
            .runCollect
            .exit
            .zipLeft(finalizersRef.update(_ :+ "consumer0 finalized"))

        consumer1 =
          Consumer
            .plainStream(
              Subscription.manual(topic1, 1), // invalid with the previous subscription
              Serde.string,
              Serde.string
            )
            .tapError { _ =>
              firstMessagesRef.updateSome { case (v, "") =>
                (v, "consumer1 error")
              }
            }
            .runCollect
            .exit
            .zipLeft(finalizersRef.update(_ :+ "consumer1 finalized"))

        consumerInstance <- consumer(client, Some(group)).build

        fiber0 <- consumer0.provideEnvironment(consumerInstance).fork
        _      <- ZIO.unit.delay(100.millis) // Wait to be sure that `consumer0` is running
        fiber1 <- consumer1.provideEnvironment(consumerInstance).fork

        result0 <- fiber0.join
        result1 <- fiber1.join

        finalizingOrder <- finalizersRef.get
        firstMessages   <- firstMessagesRef.get
      } yield assert(result0)(succeeds(hasSize(equalTo(numberOfMessages)))) &&
        assert(result1)(fails(isSubtype[InvalidSubscriptionUnion](anything))) &&
        // Here we check that `consumer0` was running when `consumer1` failed
        assert(firstMessages)(equalTo(("First consumer0 message", "consumer1 error"))) &&
        assert(finalizingOrder)(equalTo(Chunk("consumer1 finalized", "consumer0 finalized")))
    } @@ nonFlaky(5),
    test("distributes records (randomly) from overlapping subscriptions over all subscribers") {
      val kvs = (1 to 500).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)

        consumer1GotMessage <- Promise.make[Nothing, Unit]
        consumer2GotMessage <- Promise.make[Nothing, Unit]
        _ <-
          (Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .tap(_ => consumer1GotMessage.succeed(()))
            .merge(
              Consumer
                .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                .tap(_ => consumer2GotMessage.succeed(()))
            )
            .interruptWhen(consumer1GotMessage.await *> consumer2GotMessage.await)
            .runCollect)
            .provideSomeLayer[Kafka & Scope](
              consumer(client, Some(group), properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "10"))
            )
      } yield assertCompletes
    } @@ TestAspect.nonFlaky(5),
    test("can handle unsubscribing during the lifetime of other streams") {
      val kvs = (1 to 50).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        topic2 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)
        _ <- produceMany(topic2, kvs)

        _ <-
          (Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .take(100)
            .merge(
              Consumer
                .plainStream(Subscription.topics(topic2), Serde.string, Serde.string)
                .take(50) *> ZStream.fromZIO(produceMany(topic1, kvs))
            )
            .runCollect)
            .provideSomeLayer[Kafka with Scope with Producer](consumer(client, Some(group)))
      } yield assertCompletes
    },
    test("can restart a stream for the same subscription") {
      val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- produceMany(topic1, kvs)

        errored <- Ref.make(false)
        _ <-
          Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .take(5)
            .tap(_ => ZIO.unlessZIO(errored.getAndSet(true))(ZIO.fail(new RuntimeException("Stream failure 1"))))
            .runCollect
            .retryN(1)
            .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
      } yield assertCompletes
    },
    test("can resume a stream for the same subscription") {
      val kvs = (1 to 1000).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic1 <- randomTopic
        client <- randomClient
        group  <- randomGroup

        _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic1, partitions = 48)) // Large number of partitions

        _ <- produceMany(topic1, kvs)

        recordsConsumed <- Ref.make(Chunk.empty[CommittableRecord[String, String]])
        _ <-
          Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .take(40)
            .transduce(
              Consumer.offsetBatches.contramap[CommittableRecord[String, String]](_.offset) <&> ZSink
                .collectAll[CommittableRecord[String, String]]
            )
            .mapZIO { case (offsetBatch, records) => offsetBatch.commit.as(records) }
            .flattenChunks
            .runCollect
            .tap(records => recordsConsumed.update(_ ++ records))
            .repeatN(24)
            .provideSomeLayer[Kafka with Scope](consumer(client, Some(group)))
        consumed <- recordsConsumed.get
      } yield assert(consumed.map(r => r.value))(hasSameElements(Chunk.fromIterable(kvs.map(_._2))))
    } @@ TestAspect.nonFlaky(3)
  )
    .provideSome[Scope & Kafka](producer)
    .provideSomeShared[Scope](
      Kafka.embedded
    ) @@ withLiveClock @@ TestAspect.sequential @@ timeout(2.minutes)
}
