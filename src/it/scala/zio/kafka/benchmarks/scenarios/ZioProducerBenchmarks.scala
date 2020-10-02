package zio.kafka.benchmarks.scenarios

import com.codahale.metrics.Meter
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.blocking.Blocking
import zio.kafka.benchmarks.fixtures.{ KafkaProducerTestFixture, PerfFixtureHelpers }
import zio.kafka.producer.Producer
import zio.stream._

import scala.concurrent.duration._

object ZioProducerBenchmarks extends LazyLogging {
  @volatile var lastPartStart = System.nanoTime()
  val logStep                 = 100000

  def plainFlow(
    fixture: KafkaProducerTestFixture,
    meter: Meter
  ): ZIO[Blocking, Throwable, Unit] = {
    val msg = PerfFixtureHelpers.stringOfSize(fixture.msgSize)
    Stream
      .range(0, fixture.msgCount - 1)
      .chunkN(1000)
      .map { number =>
        val partition: Int = (number % fixture.numberOfPartitions).toInt
        new ProducerRecord[Array[Byte], Array[Byte]](fixture.topic, partition, null, msg.getBytes)
      }
      .transduce {
        Producer.produceAll[Any, Array[Byte], Array[Byte]]
      }
      .map { msg: RecordMetadata =>
        meter.mark()
        if (msg.offset % logStep == 0) {
          val lastPartEnd = System.nanoTime()
          val took        = (lastPartEnd - lastPartStart).nanos
          logger.info(s"Sent ${msg.offset}, took ${took.toMillis} ms to send last $logStep")
          lastPartStart = lastPartEnd
        }
        msg
      }
      .runDrain
      .provideSomeLayer[Blocking](ZLayer.succeed(fixture.producer: Producer.Service[Any, Array[Byte], Array[Byte]]))
  }
}
