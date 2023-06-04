package zio.kafka.bench.comparison

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.openjdk.jmh.annotations._
import zio.ZIO
import zio.kafka.bench.comparison.ComparisonBenchmark.{zAssert, LowLevelKafka}
import zio.kafka.consumer.ConsumerSettings

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters._

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class KafkaClientBenchmarks extends ComparisonBenchmark {

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def kafkaClients(): Any =
    runZIO {
      ZIO.service[ConsumerSettings].flatMap { settings =>
        ZIO.serviceWithZIO[LowLevelKafka] { consumer =>
          ZIO
            .attemptBlocking {
              consumer.subscribe(java.util.Arrays.asList(topic1))

              var count                                                               = 0L
              var first: ConsumerRecord[Array[Byte], Array[Byte]]                     = null
              var lasts: java.util.Iterator[ConsumerRecord[Array[Byte], Array[Byte]]] = null
              while (count < numberOfMessages) {
                val records  = consumer.poll(settings.pollTimeout)
                val iterator = records.iterator()
                val hasNext  = iterator.hasNext
                if (count == 0L && hasNext) {
                  first = iterator.next()
                }
                if (hasNext) {
                  lasts = iterator
                }
                count += records.count()
              }

              consumer.unsubscribe()
              val last: ConsumerRecord[Array[Byte], Array[Byte]] = lasts.next()
              (count, first, last)
            }
            .flatMap { case (count, first, last) =>
              val firstKey = new String(first.key(), StandardCharsets.UTF_8)
              val lastKey  = new String(last.key(), StandardCharsets.UTF_8)
              zAssert(count == numberOfMessages, s"Consumed $count messages instead of $numberOfMessages") &&
              zAssert(
                firstKey != lastKey,
                s"The first consumed record key ('$firstKey') shouldn't be the same as one of the last consumed record ('$lastKey')",
              )
            }
        }
      }
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def manualKafkaClients(): Any =
    runZIO {
      ZIO.service[ConsumerSettings].flatMap { settings =>
        ZIO
          .serviceWithZIO[LowLevelKafka] { consumer =>
            ZIO
              .attemptBlocking {
                consumer.assign(topicPartitions.map(_.asJava).asJava)

                var count                                                               = 0L
                var first: ConsumerRecord[Array[Byte], Array[Byte]]                     = null
                var lasts: java.util.Iterator[ConsumerRecord[Array[Byte], Array[Byte]]] = null
                while (count < numberOfMessages) {
                  val records  = consumer.poll(settings.pollTimeout)
                  val iterator = records.iterator()
                  val hasNext  = iterator.hasNext
                  if (count == 0L && hasNext) {
                    first = iterator.next()
                  }
                  if (hasNext) {
                    lasts = iterator
                  }
                  count += records.count()
                }

                consumer.unsubscribe()
                val last: ConsumerRecord[Array[Byte], Array[Byte]] = lasts.next()
                (count, first, last)
              }
              .flatMap { case (count, first, last) =>
                val firstKey = new String(first.key(), StandardCharsets.UTF_8)
                val lastKey  = new String(last.key(), StandardCharsets.UTF_8)
                zAssert(count == numberOfMessages, s"Consumed $count messages instead of $numberOfMessages") &&
                zAssert(
                  firstKey != lastKey,
                  s"The first consumed record key ('$firstKey') shouldn't be the same as one of the last consumed record ('$lastKey')",
                )
              }
          }
      }
    }

}
