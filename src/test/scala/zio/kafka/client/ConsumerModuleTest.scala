package zio.kafka.client

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common.{ PartitionInfo, TopicPartition }
import zio.blocking.Blocking
import zio.duration._
import zio.Managed
import zio.random.Random
import zio.test._
import zio.test.Assertion.{ equalTo, isUnit }
import zio.test.mock.Expectation.value
import zio.test.environment.TestEnvironment
import zio.test.TestAspect.timeout

import scala.collection.compat._

object ConsumerModuleTestUtils {
  def makeEnv(managed: Managed[Nothing, Consumer]): Managed[Nothing, Consumer with Blocking] =
    for {
      testEnv <- TestEnvironment.Value
      c       <- managed
    } yield new Consumer with Blocking {
      val blocking = testEnv.blocking
      val consumer = c.consumer
    }

  val anyPosLong = Gen.anyLong.filter(_ > 0)

  val anyTopicPartition = Gen.alphaNumericString <*> Gen.int(0, 100) map {
    case (t, p) => new TopicPartition(t, p)
  }

  val anyDuration = anyPosLong.map(Duration.Finite.apply)

  val anyOffset = anyTopicPartition <*> anyPosLong

  val anyOffsetAndMetadata = anyPosLong <*> Gen.alphaNumericString map {
    case (offset, meta) => new OffsetAndMetadata(offset, meta)
  }

  val anyTopicOffsetAndMetadata = anyTopicPartition <*> anyOffsetAndMetadata

  // empty because the class is large and provides no `.equals()`
  val anyPartitionInfo = Gen.const(List.empty[PartitionInfo])

  val anyTopicInfo = Gen.alphaNumericString <*> anyPartitionInfo

  val anyOffsetAndTimestamp = anyPosLong <*> anyPosLong map {
    case (offset, ts) => new OffsetAndTimestamp(offset, ts)
  }

  val anyTopicOffsetAndTimestamp = anyTopicPartition <*> anyOffsetAndTimestamp

  val anyTopicSubscription: Gen[Random with Sized, Subscription] =
    Gen.listOf(Gen.alphaNumericString).map(x => Subscription.Topics(x.toSet))

  val anyPatternSubscription: Gen[Random with Sized, Subscription] =
    Gen.alphaNumericString.map(x => Subscription.Pattern(x.r))

  val anyManualSubscription: Gen[Random with Sized, Subscription] =
    Gen.listOf(anyTopicPartition).map(x => Subscription.Manual(x.toSet))

  val anySubscription = Gen.oneOf(
    anyTopicSubscription,
    anyPatternSubscription,
    anyManualSubscription
  )

  val topicPartitions = Gen.listOf(anyTopicPartition).map(_.toSet)

  val offsets = Gen.listOf(anyOffset).map(_.toMap)

  val topicOffsetsAndMetadata = Gen
    .listOf(
      anyTopicOffsetAndMetadata.map(t => (t._1, Option(t._2)))
    )
    .map(_.toMap)

  val topicInfo = Gen.listOf(anyTopicInfo).map(_.toMap)

  val topicOffsetAndTimestamps = Gen.listOf(anyTopicOffsetAndTimestamp).map(_.toMap)

  val topicNames = Gen.listOf(Gen.alphaNumericString).map(_.toSet)
}
import ConsumerModuleTestUtils._

object ConsumerModuleTest
    extends DefaultRunnableSpec(
      suite("Consumer module")(
        suite("delegates")(
          testM("assignment")(
            checkM(topicPartitions) { a =>
              val eff  = Consumer.assignment
              val mock = ConsumerMock.assignment returns value(a)
              val env  = makeEnv(mock)

              val result = eff.provideManaged(env)

              assertM(result, equalTo(a))
            }
          ),
          testM("beginningOffsets")(
            checkM(offsets <*> anyDuration) {
              case (off, dur) =>
                val p    = off.keySet
                val eff  = Consumer.beginningOffsets(p, dur)
                val mock = ConsumerMock.beginningOffsets(equalTo((p, dur))) returns value(off)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(off))
            }
          ),
          testM("committed")(
            checkM(topicOffsetsAndMetadata <*> anyDuration) {
              case (meta, dur) =>
                val p    = meta.keySet
                val eff  = Consumer.committed(p, dur)
                val mock = ConsumerMock.committed(equalTo((p, dur))) returns value(meta)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(meta))
            }
          ),
          testM("endOffsets")(
            checkM(offsets <*> anyDuration) {
              case (off, dur) =>
                val p    = off.keySet
                val eff  = Consumer.endOffsets(p, dur)
                val mock = ConsumerMock.endOffsets(equalTo((p, dur))) returns value(off)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(off))
            }
          ),
          testM("stopConsumption") {
            val eff  = Consumer.stopConsumption
            val mock = ConsumerMock.stopConsumption returns value(())
            val env  = makeEnv(mock)

            val result = eff.provideManaged(env)

            assertM(result, isUnit)
          },
          testM("listTopics")(
            checkM(anyDuration <*> topicInfo) {
              case (dur, topics) =>
                val eff  = Consumer.listTopics(dur)
                val mock = ConsumerMock.listTopics(equalTo(dur)) returns value(topics)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(topics))
            }
          ),
          testM("offsetsForTimes")(
            checkM(anyDuration <*> topicOffsetAndTimestamps) {
              case (dur, res) =>
                val ts   = res.view.mapValues(_.timestamp).toMap
                val eff  = Consumer.offsetsForTimes(ts, dur)
                val mock = ConsumerMock.offsetsForTimes(equalTo((ts, dur))) returns value(res)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(res))
            }
          ),
          testM("partitionsFor")(
            checkM(Gen.alphaNumericString <*> anyDuration <*> anyPartitionInfo) {
              case ((topic, dur), res) =>
                val eff  = Consumer.partitionsFor(topic, dur)
                val mock = ConsumerMock.partitionsFor(equalTo((topic, dur))) returns value(res)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(res))
            }
          ),
          testM("position")(
            checkM(anyTopicPartition <*> anyDuration <*> anyPosLong) {
              case ((tp, dur), pos) =>
                val eff  = Consumer.position(tp, dur)
                val mock = ConsumerMock.position(equalTo((tp, dur))) returns value(pos)
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, equalTo(pos))
            }
          ),
          testM("seek")(
            checkM(anyTopicPartition <*> anyPosLong) {
              case (tp, off) =>
                val eff  = Consumer.seek(tp, off)
                val mock = ConsumerMock.seek(equalTo((tp, off))) returns value(())
                val env  = makeEnv(mock)

                val result = eff.provideManaged(env)

                assertM(result, isUnit)
            }
          ),
          testM("seekToBeginning")(
            checkM(topicPartitions) { tp =>
              val eff  = Consumer.seekToBeginning(tp)
              val mock = ConsumerMock.seekToBeginning(equalTo(tp)) returns value(())
              val env  = makeEnv(mock)

              val result = eff.provideManaged(env)

              assertM(result, isUnit)
            }
          ),
          testM("seekToEnd")(
            checkM(topicPartitions) { tp =>
              val eff  = Consumer.seekToEnd(tp)
              val mock = ConsumerMock.seekToEnd(equalTo(tp)) returns value(())
              val env  = makeEnv(mock)

              val result = eff.provideManaged(env)

              assertM(result, isUnit)
            }
          ),
          testM("subscribe")(
            checkM(anySubscription) { s =>
              val eff  = Consumer.subscribe(s)
              val mock = ConsumerMock.subscribe(equalTo(s)) returns value(())
              val env  = makeEnv(mock)

              val result = eff.provideManaged(env)

              assertM(result, isUnit)
            }
          ),
          testM("subscription")(
            checkM(topicNames) { t =>
              val eff  = Consumer.subscription()
              val mock = ConsumerMock.subscription returns value(t)
              val env  = makeEnv(mock)

              val result = eff.provideManaged(env)

              assertM(result, equalTo(t))
            }
          ),
          testM("unsubscribe") {
            val eff  = Consumer.unsubscribe()
            val mock = ConsumerMock.unsubscribe returns value(())
            val env  = makeEnv(mock)

            val result = eff.provideManaged(env)

            assertM(result, isUnit)
          }
        )
      ) @@ timeout(30.seconds)
    )
