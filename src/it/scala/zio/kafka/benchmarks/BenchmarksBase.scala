package zio.kafka.benchmarks

import com.codahale.metrics.Meter
import org.scalatest.flatspec.AnyFlatSpecLike
import zio.kafka.benchmarks.Timed.runTimed
import zio.kafka.benchmarks.commands.RunTestCommand
import zio.kafka.benchmarks.fixtures.PerfFixtureHelpers.FilledTopic
import zio.kafka.benchmarks.fixtures.{ KafkaProducerFixtures, KafkaProducerTestFixture }
import zio.{ ZEnv, ZIO }

object BenchmarksBase {
  // Message count multiplier to adapt for shorter local testing
  val factor = 1000

  val numBrokers = 1 // TODO config

  lazy val topic_50_100 = FilledTopic(50 * factor, 100, replicationFactor = numBrokers)

  lazy val topic_100_100  = FilledTopic(100 * factor, 100, replicationFactor = numBrokers)
  lazy val topic_100_5000 = FilledTopic(100 * factor, 5000, replicationFactor = numBrokers)

  lazy val topic_1000_100  = FilledTopic(1000 * factor, 100, replicationFactor = numBrokers)
  lazy val topic_1000_5000 = FilledTopic(1000 * factor, 5 * 1000, replicationFactor = numBrokers)
  lazy val topic_1000_5000_8 =
    FilledTopic(msgCount = 1000 * factor, msgSize = 5 * 1000, numberOfPartitions = 8, replicationFactor = numBrokers)
  lazy val topic_1000_5000_100 =
    FilledTopic(msgCount = 1000 * factor, msgSize = 5 * 1000, numberOfPartitions = 100, replicationFactor = numBrokers)

  lazy val topic_2000_100    = FilledTopic(2000 * factor, 100, replicationFactor = numBrokers)
  lazy val topic_2000_500    = FilledTopic(2000 * factor, 500, replicationFactor = numBrokers)
  lazy val topic_2000_5000   = FilledTopic(2000 * factor, 5000, replicationFactor = numBrokers)
  lazy val topic_2000_5000_8 = FilledTopic(2000 * factor, 5000, numberOfPartitions = 8, replicationFactor = numBrokers)
}

abstract class BenchmarksBase extends AnyFlatSpecLike {
  val bootstrapServers = "localhost:9092" // TODO support containers, clustering
  val runtime          = zio.Runtime.default

  def runWithProducer[R](
    cmd: RunTestCommand,
    testCase: (KafkaProducerTestFixture, Meter) => ZIO[ZEnv, Throwable, Unit]
  ): Unit = {

    val program = KafkaProducerFixtures
      .initializedProducer(cmd)
      .use(runTimed(cmd, testCase))

    runtime.unsafeRun(program)
  }
}
