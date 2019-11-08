package zio.kafka.client

import zio._
import zio.kafka.client.Kafka.KafkaTestEnvironment
import zio.kafka.client.serde.Serde
// import zio.kafka.client.serde.Serde
import zio.test._
import zio.test.Assertion._
import zio.test.DefaultRunnableSpec

object ConsumerTest extends DefaultRunnableSpec(
  suite("consumer test suite")(
    testM("receive messages produced on the topic") {
      val kvs = (1 to 5).toList.map(i => (s"key$i", s"msg$i"))
      for {
        kvOut <- Simplify.doIt(kvs)
      } yield assert(kvOut, equalTo(kvs))
    }
  ).provideManagedShared(KafkaTestUtils.kafkaEnvironment)
)

object Simplify {

  def doIt(kvs: List[(String, String)]): RIO[KafkaTestEnvironment, List[(String, String)]] =
    for {
      _ <- KafkaTestUtils.produceMany("topic150", kvs)

      records <- KafkaTestUtils.withConsumer("group150", "client150") { consumer =>
        consumer
          .subscribeAnd(Subscription.Topics(Set("topic150")))
          .plainStream(Serde.string, Serde.string)
          .flattenChunks
          .take(5)
          .runCollect
      }
      kvOut = records.map { r =>
        (r.record.key, r.record.value)
      }

    } yield kvOut
}