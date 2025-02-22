package zio.kafka.bench.comparison

import org.openjdk.jmh.annotations._
import zio.ZIO
import zio.kafka.bench.comparison.ComparisonBenchmark.zAssert
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.serde.Serde

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class ZioKafkaBenchmarks extends ComparisonBenchmark {

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def manualZioKafka(): Any =
    runZIO {
      ZIO.serviceWithZIO[Consumer] { consumer =>
        consumer
          .plainStream(
            Subscription.manual(topicPartitions.map(tp => tp.name -> tp.partition): _*),
            Serde.byteArray,
            Serde.byteArray
          )
          .take(recordCount.toLong)
          .runCount
          .flatMap(r => zAssert(r == recordCount, s"Consumed $r records instead of $recordCount"))
      }
    }

}
