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

  def plainFlow(fixture: KafkaProducerTestFixture, meter: Meter): Unit = {
    val msg = PerfFixtureHelpers.stringOfSize(fixture.msgSize)
    val stream = Stream
      .fromIterable(0 to fixture.msgCount) // pre-allocates, but Stream.range is very slow!
      .map { number =>
        val partition: Int = (number % fixture.numberOfPartitions).toInt
        new ProducerRecord[Array[Byte], String](fixture.topic, partition, null, msg)
      }
      .transduce {
        Producer.produceAll[Any, Array[Byte], String]
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

    // TODO extract the unsafe running part away
    val runtime         = Runtime.default
    val blockingService = Blocking.live
    val _: Unit         = runtime.unsafeRun(stream.runDrain.provideLayer(fixture.zioProducer ++ blockingService))
    logger.info("Stream finished")
  }
}
