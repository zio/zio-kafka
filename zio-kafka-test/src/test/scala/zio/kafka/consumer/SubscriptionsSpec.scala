package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.serde.Serde
import zio.kafka.testkit.KafkaTestUtils
import zio.kafka.testkit.{ Kafka, KafkaRandom }
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.util.concurrent.atomic.AtomicInteger

//noinspection SimplifyAssertInspection
object SubscriptionsSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {
  override val kafkaPrefix: String = "subscriptionsspec"

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("Consumer subscriptions")(
      test("consumes from two topic subscriptions") {
        val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- KafkaTestUtils.createCustomTopic(topic1)
          _ <- KafkaTestUtils.createCustomTopic(topic2)

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)
          _        <- KafkaTestUtils.produceMany(producer, topic2, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          records <-
            consumer
              .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
              .take(5)
              .runCollect <&>
              consumer
                .plainStream(Subscription.topics(topic2), Serde.string, Serde.string)
                .take(5)
                .runCollect
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

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)
          _        <- KafkaTestUtils.produceMany(producer, topic2, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          records <-
            consumer
              .plainStream(Subscription.Pattern(s"$topic1".r), Serde.string, Serde.string)
              .take(5)
              .runCollect <&>
              consumer
                .plainStream(Subscription.Pattern(s"$topic2".r), Serde.string, Serde.string)
                .take(5)
                .runCollect
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

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)
          _        <- KafkaTestUtils.produceMany(producer, topic2, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          consumer0 =
            consumer
              .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
              .runCollect

          consumer1 =
            consumer
              .plainStream(
                Subscription.manual(topic2, 1), // invalid with the previous subscription
                Serde.string,
                Serde.string
              )
              .runCollect

          result <- (consumer0 <&> consumer1).unit.exit
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

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)

          counter = new AtomicInteger(1)

          firstMessagesRef <- Ref.make(("", ""))
          finalizersRef    <- Ref.make(Chunk.empty[String])

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))

          c1Fib <- consumer
                     .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                     // Here we delay each message to be sure that `consumer1` will fail while `consumer0` is still running
                     .mapZIO { r =>
                       firstMessagesRef.updateSome { case ("", v) => ("First consumer0 message", v) } *>
                         ZIO
                           .logDebug(s"Consumed ${counter.getAndIncrement()} records")
                           .delay(10.millis)
                           .as(r)
                     }
                     .take(numberOfMessages.toLong)
                     .runCollect
                     .exit
                     .zipLeft(finalizersRef.update(_ :+ "consumer0 finalized"))
                     .fork

          _ <- ZIO.sleep(100.millis) // Wait to be sure that `consumer0` is running

          c2Fib <- consumer
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
                     .fork

          result1 <- c1Fib.join
          result2 <- c2Fib.join

          finalizingOrder <- finalizersRef.get
          firstMessages   <- firstMessagesRef.get
        } yield assert(result1)(succeeds(hasSize(equalTo(numberOfMessages)))) &&
          assert(result2)(fails(isSubtype[InvalidSubscriptionUnion](anything))) &&
          // Here we check that `consumer0` was running when `consumer1` failed
          assert(firstMessages)(equalTo(("First consumer0 message", "consumer1 error"))) &&
          assert(finalizingOrder)(equalTo(Chunk("consumer1 finalized", "consumer0 finalized")))
      } @@ nonFlaky(5),
      test("distributes records (randomly) from overlapping subscriptions over all subscribers") {
        for {
          topic1   <- randomTopic
          client   <- randomClient
          group    <- randomGroup
          _        <- KafkaTestUtils.createCustomTopic(topic1)
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.scheduledProduce(producer, topic1, Schedule.spaced(5.millis)).runDrain.forkScoped

          consumer1GotMessage <- Promise.make[Nothing, Unit]
          consumer2GotMessage <- Promise.make[Nothing, Unit]
          consumer <- KafkaTestUtils.makeConsumer(
                        client,
                        Some(group),
                        properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1")
                      )
          _ <- ZStream
                 .mergeAllUnbounded()(
                   consumer
                     .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                     .tap(_ => consumer1GotMessage.succeed(())),
                   consumer
                     .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                     .tap(_ => consumer2GotMessage.succeed(()))
                 )
                 .interruptWhen(consumer1GotMessage.await *> consumer2GotMessage.await)
                 .runDrain
        } yield assertCompletes
      },
      test("can handle unsubscribing during the lifetime of other streams") {
        val kvs = (1 to 50).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          topic2 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)
          _        <- KafkaTestUtils.produceMany(producer, topic2, kvs)

          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          _ <-
            consumer
              .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
              .take(100)
              .merge(
                consumer
                  .plainStream(Subscription.topics(topic2), Serde.string, Serde.string)
                  .take(50) *> ZStream.fromZIO(KafkaTestUtils.produceMany(producer, topic1, kvs))
              )
              .runCollect
        } yield assertCompletes
      },
      test("can restart a stream for the same subscription") {
        val kvs = (1 to 100).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)

          errored  <- Ref.make(false)
          consumer <- KafkaTestUtils.makeConsumer(client, Some(group))
          _ <- consumer
                 .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                 .take(5)
                 .tap(_ => ZIO.unlessZIO(errored.getAndSet(true))(ZIO.fail(new RuntimeException("Stream failure 1"))))
                 .runCollect
                 .retryN(1)
        } yield assertCompletes
      },
      test("can resume a stream for the same subscription") {
        val kvs = (1 to 1000).toList.map(i => (s"key$i", s"msg$i"))
        for {
          topic1 <- randomTopic
          client <- randomClient
          group  <- randomGroup

          _ <- KafkaTestUtils.createCustomTopic(topic1, partitionCount = 48) // Large number of partitions

          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic1, kvs)

          recordsConsumed <- Ref.make(Chunk.empty[CommittableRecord[String, String]])
          consumer        <- KafkaTestUtils.makeConsumer(client, Some(group))
          _ <- ZIO.scoped {
                 consumer
                   .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
                   .take(40)
                   .transduce(
                     Consumer.offsetBatches.contramap[CommittableRecord[String, String]](_.offset) <&>
                       ZSink.collectAll[CommittableRecord[String, String]]
                   )
                   .mapZIO { case (offsetBatch, records) => offsetBatch.commit.as(records) }
                   .flattenChunks
                   .runCollect
                   .tap(records => recordsConsumed.update(_ ++ records))
               }
                 .repeatN(24)
          consumed <- recordsConsumed.get
        } yield assert(consumed.map(r => r.value))(hasSameElements(Chunk.fromIterable(kvs.map(_._2))))
      } @@ TestAspect.nonFlaky(2)
    )
      .provideSomeShared[Scope](
        Kafka.embedded
      ) @@ withLiveClock @@ timeout(2.minutes)
}
