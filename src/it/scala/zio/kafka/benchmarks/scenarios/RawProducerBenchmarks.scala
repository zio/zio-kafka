package zio.kafka.benchmarks.scenarios

import scala.concurrent.duration._

import com.typesafe.scalalogging.LazyLogging
import com.codahale.metrics.Meter
import org.apache.kafka.clients.producer.{ Callback, ProducerRecord, RecordMetadata }
import zio.kafka.benchmarks.fixtures.{ KafkaProducerTestFixture, PerfFixtureHelpers }

object RawProducerBenchmarks extends LazyLogging {

  val logStep = 100000

  def plainFlow(fixture: KafkaProducerTestFixture, meter: Meter): Unit = {
    val producer      = fixture.producer
    var lastPartStart = System.nanoTime()

    val msg = PerfFixtureHelpers.stringOfSize(fixture.msgSize)

    for (i <- 1 to fixture.msgCount) {
      val partition: Int = (i % fixture.numberOfPartitions).toInt
      producer.send(
        new ProducerRecord[Array[Byte], Array[Byte]](fixture.topic, partition, null, msg.getBytes),
        new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = meter.mark()
        }
      )

      if (i % logStep == 0) {
        val lastPartEnd = System.nanoTime()
        val took        = (lastPartEnd - lastPartStart).nanos
        logger.info(s"Sent $i, took ${took.toMillis} ms to send last $logStep")
        lastPartStart = lastPartEnd
      }
    }
    fixture.close()
  }
}
