package zio.kafka.bench.comparison

import org.openjdk.jmh.annotations.{ Benchmark, BenchmarkMode, Mode, OutputTimeUnit, Scope, State }
import zio.ZIO
import zio.kafka.bench.comparison.ComparisonBenchmark.{ zAssert, LowLevelKafka }
import zio.kafka.consumer.ConsumerSettings

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
          ZIO.attemptBlocking {
            consumer.subscribe(java.util.Arrays.asList(topic1))

            var count = 0L
            while (count < numberOfMessages) {
              val records = consumer.poll(settings.pollTimeout)
              count += records.count()
            }

            consumer.unsubscribe()
            count
          }.flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
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
            ZIO.attemptBlocking {
              consumer.assign(topicPartitions.map(_.asJava).asJava)

              var count = 0L
              while (count < numberOfMessages) {
                val records = consumer.poll(settings.pollTimeout)
                count += records.count()
              }

              consumer.unsubscribe()
              count
            }.flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
          }
      }
    }

}
