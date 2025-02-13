// format: off
// Manually formatted for the small width of readme
package zio.kafka.example

import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{ Producer, ProducerSettings }
import zio.kafka.serde._
import zio.stream.ZStream

object ReadmeExample extends ZIOAppDefault {

  private val producerRun: ZIO[Any, Throwable, Unit] =
    ZIO.scoped {
      for {
        producer <-
          Producer.make(
            ProducerSettings(List("localhost:9092"))
          )
        _ <- ZStream
          .fromSchedule(Schedule.fixed(2.seconds))
          .mapZIO(_ => Random.nextIntBetween(0, Int.MaxValue))
          .mapZIO { random =>
            producer.produce[Any, Long, String](
              topic = "random-topic",
              key = (random % 4).toLong,
              value = random.toString,
              keySerializer = Serde.long,
              valueSerializer = Serde.string
            )
          }
          .runDrain
      } yield ()
    }

  private val consumerRun: ZIO[Any, Throwable, Unit] =
    ZIO.scoped {
      for {
        consumer <-
          Consumer.make(
            ConsumerSettings(List("localhost:9092"))
              .withGroupId("group")
          )
        _ <- consumer
          .plainStream(Subscription.topics("random"), Serde.long, Serde.string)
          .tap(r => Console.printLine(r.value))
          .map(_.offset)
          .aggregateAsync(Consumer.offsetBatches)
          .mapZIO(_.commit)
          .runDrain
      } yield ()
    }

  override def run: ZIO[Any, Throwable, Unit] =
    ZIO.raceFirst(producerRun, List(consumerRun))
}
