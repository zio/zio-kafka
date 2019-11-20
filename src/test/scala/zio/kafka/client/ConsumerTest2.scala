package zio.kafka.client

import zio.kafka.client.serde.Serde
import zio.stream.ZSink
import zio.test._
import zio.test.Assertion._
import zio.test.DefaultRunnableSpec
import KafkaTestUtils._
import zio._
//import zio.test.environment.Live
import zio.duration._

/**
 * Health warning - if extending these tests, be aware that consumer requires the live clock.
 * For this reason, we provide kafkaEnvrionment and use the consumer wrapper methods in KafkaTestUtils.
 * Or you can can use Live.live here but it's probably easier to use the wrappers
 */
object ConsumerTest2
    extends DefaultRunnableSpec(
      suite("consumer test suit2")(
//        testM("commit offsets for all consumed messages") {
//          val topic        = "consumeWith2"
//          val subscription = Subscription.Topics(Set(topic))
//          val nrMessages   = 50
//          val messages     = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))
//
//          // contents of main for extracted to avoid name mangling issues with CI
//
//          def consumeIt(messagesReceived: Ref[List[(String, String)]], done: Promise[Nothing, Unit]) =
//            consumeWithStrings("group3", "client3", subscription)({ (key, value) =>
//              (for {
//                messagesSoFar <- messagesReceived.update(_ :+ (key -> value))
//                _             <- Task.when(messagesSoFar.size == nrMessages)(done.succeed(()))
//              } yield ()).orDie
//            }).fork
//
//          for {
//            done             <- Promise.make[Nothing, Unit]
//            messagesReceived <- Ref.make(List.empty[(String, String)])
//            _                <- produceMany(topic, messages)
//            fib              <- consumeIt(messagesReceived, done)
//            _ <- done.await *> Live
//                  .live(ZIO.sleep(3.seconds)) // TODO the sleep is necessary for the outstanding commits to be flushed. Maybe we can fix that another way
//            _                <- fib.interrupt
//            _                <- produceOne(topic, "key-new", "msg-new")
//            newMessage       <- TestHelper.newM(subscription)
//            consumedMessages <- messagesReceived.get
//          } yield assert(consumedMessages, contains(newMessage).negate)
//        },
        testM("deliver messages only once") {
          val kvs = (1 to 50000).toList.map(i => (s"key$i", s"msg$i"))
          for {
            _ <- produceMany("topic150", kvs)

            records <- withConsumer("group150", "client150") { consumer =>
                        consumer
                          .subscribeAnd(Subscription.Topics(Set("topic150")))
                          .plainStream(Serde.string, Serde.string)
                          .flattenChunks
                          .mapM { r =>
                            ZIO(println(r)) *> ZIO.sleep(2.seconds).as(r)
                          }
                          .take(5)
                          .runCollect
                      }
            kvOut = records.map { r =>
              (r.record.key, r.record.value)
            }
          } yield assert(kvOut, equalTo(kvs))
        }
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
    )

object TestHelper {
  def newM(subscription: Subscription) =
    withConsumer("group3", "client3") { c =>
      c.subscribe(subscription) *> c
        .plainStream(Serde.string, Serde.string)
        .take(1)
        .flattenChunks
        .map(r => (r.record.key(), r.record.value()))
        .run(ZSink.collectAll[(String, String)])
        .map(_.head)
    }.orDie

}
