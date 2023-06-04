package zio.kafka.bench.comparison

import org.openjdk.jmh.annotations._
import zio.kafka.bench.comparison.ComparisonBenchmark.zAssert
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.{ZEnvironment, ZLayer}

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaNoOptimisticResumeBenchmarks extends ComparisonBenchmark {

  override protected def settings: ZLayer[Kafka, Nothing, ConsumerSettings] =
    super.settings.map(env => ZEnvironment(env.get.copy(enableOptimisticResume = false)))

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def zioKafka(): Any =
    runZIO {
      Consumer
        .plainStream(Subscription.topics(topic1), Serde.byteArray, Serde.byteArray)
        .take(numberOfMessages.toLong)
        .runCount
        .flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
    }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def manualZioKafka(): Any =
    runZIO {
      Consumer
        .plainStream(
          Subscription.manual(topicPartitions.map(tp => tp.name -> tp.partition): _*),
          Serde.byteArray,
          Serde.byteArray,
        )
        .take(numberOfMessages.toLong)
        .runCount
        .flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
    }

}
