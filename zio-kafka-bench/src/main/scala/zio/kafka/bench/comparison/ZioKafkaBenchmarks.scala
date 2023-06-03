package zio.kafka.bench.comparison

import org.openjdk.jmh.annotations._
import zio.kafka.bench.comparison.ComparisonBenchmark.zAssert
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.serde.Serde

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaBenchmarks extends ComparisonBenchmark {

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
          Serde.byteArray
        )
        .take(numberOfMessages.toLong)
        .runCount
        .flatMap(r => zAssert(r == numberOfMessages, s"Consumed $r messages instead of $numberOfMessages"))
    }

}
