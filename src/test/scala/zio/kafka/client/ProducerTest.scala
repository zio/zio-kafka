package zio.kafka.client

import zio.test._
import KafkaTestUtils._
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.client.serde.Serde
import zio.stream.Take
import zio.test.Assertion._

object ProducerTest
    extends DefaultRunnableSpec(
      suite("producer test suite")(
        testM("one record") {
          withProducerStrings { producer =>
            for {
              _ <- producer.produce(new ProducerRecord("topic", "boo", "baa"))
            } yield assertCompletes
          }
        },
        testM("a non-empty chunk of records") {
          withProducerStrings {
            producer =>
              import Subscription._

              val (topic1, key1, value1) = ("topic1", "boo", "baa")
              val (topic2, key2, value2) = ("topic2", "baa", "boo")
              val chunks = Chunk.fromIterable(
                List(new ProducerRecord(topic1, key1, value1), new ProducerRecord(topic2, key2, value2))
              )
              def withConsumerSettings(subscription: Subscription, settings: ConsumerSettings) =
                Consumer
                  .make(settings)
                  .flatMap(
                    c => c.subscribe(subscription).toManaged_ *> c.plainStream(Serde.string, Serde.string).toQueue()
                  )

              for {
                outcome  <- producer.produceChunk(chunks).flatten
                settings <- consumerSettings("testGroup", "testClient")
                record1 <- withConsumerSettings(Topics(Set(topic1)), settings).use { consumer =>
                            for {
                              messages <- Take.option(consumer.take).someOrFail(new NoSuchElementException)
                              record = messages
                                .filter(rec => rec.record.key == key1 && rec.record.value == value1)
                                .toSeq
                            } yield record
                          }
                record2 <- withConsumerSettings(Topics(Set(topic2)), settings).use { consumer =>
                            for {
                              messages <- Take.option(consumer.take).someOrFail(new NoSuchElementException)
                              record   = messages.filter(rec => rec.record.key == key2 && rec.record.value == value2)
                            } yield record
                          }
              } yield {
                assert(outcome.length, equalTo(2)) &&
                assert(record1, isNonEmpty) &&
                assert(record2.length, isGreaterThan(0))
              }
          }
        },
        testM("an empty chunk of records") {
          withProducerStrings { producer =>
            val chunks = Chunk.fromIterable(List.empty)
            for {
              outcome <- producer.produceChunk(chunks).flatten
            } yield assert(outcome.length, equalTo(0))
          }
        }
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
    )
