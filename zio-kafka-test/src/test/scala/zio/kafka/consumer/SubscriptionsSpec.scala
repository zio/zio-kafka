package zio.kafka.consumer
import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._
import zio.kafka.KafkaRandom
import zio.kafka.KafkaTestUtils._
import zio.kafka.TestLogger.logger
import zio.kafka.embedded.Kafka
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.stream.{ ZSink, ZStream }
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object SubscriptionsSpec extends ZIOSpecDefault with KafkaRandom {
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

        result <-
          (Consumer
            .plainStream(Subscription.topics(topic1), Serde.string, Serde.string)
            .runCollect zipPar
            Consumer
              .plainStream(
                Subscription.manual(topic2, 1),
                Serde.string,
                Serde.string
              )
              .runCollect)
            .provideSomeLayer[Kafka & Scope](consumer(client, Some(group)))
            .unit
            .exit
      } yield assert(result)(fails(isSubtype[InvalidSubscriptionUnion](anything)))
    },
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
      Kafka.embedded,
      Runtime.removeDefaultLoggers,
      Runtime.addLogger(logger())
    ) @@ withLiveClock @@ TestAspect.sequential @@ timeout(5.minutes)
}
