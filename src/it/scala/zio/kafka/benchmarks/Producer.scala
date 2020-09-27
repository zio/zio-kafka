package zio.kafka.benchmarks

import zio.kafka.benchmarks.BenchmarksBase.{ topic_2000_100, topic_2000_500, topic_2000_5000, topic_2000_5000_8 }
import zio.kafka.benchmarks.Timed.runPerfTest
import zio.kafka.benchmarks.commands.RunTestCommand
import zio.kafka.benchmarks.fixtures.KafkaProducerFixtures
import zio.kafka.benchmarks.scenarios.{ RawProducerBenchmarks, ZioProducerBenchmarks }

class ProducerBenchmarks extends BenchmarksBase {
  val prefix = "apache-kafka-plain-producer"

  it should "bench with small messages" in {
    val cmd = RunTestCommand(prefix, bootstrapServers, topic_2000_100.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), RawProducerBenchmarks.plainFlow)
  }

  it should "bench with 500b messages" in {
    val cmd = RunTestCommand(prefix + "-500b", bootstrapServers, topic_2000_500.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), RawProducerBenchmarks.plainFlow)
  }

  it should "bench with normal messages" in {
    val cmd = RunTestCommand(prefix + "-normal-msg", bootstrapServers, topic_2000_5000.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), RawProducerBenchmarks.plainFlow)
  }

  it should "bench with normal messages written to 8 partitions" in {
    val cmd =
      RunTestCommand(prefix + "-normal-msg-8-partitions", bootstrapServers, topic_2000_5000_8.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), RawProducerBenchmarks.plainFlow)
  }
}

class ZioProducerBenchmarks extends BenchmarksBase {
  val prefix = "zio-plain-producer"

  it should "bench with small messages" in {
    val cmd = RunTestCommand(prefix, bootstrapServers, topic_2000_100.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), ZioProducerBenchmarks.plainFlow)
  }

  it should "bench with 500b messages" in {
    val cmd = RunTestCommand(prefix + "-500b", bootstrapServers, topic_2000_500.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), ZioProducerBenchmarks.plainFlow)
  }

  it should "bench with normal messages" in {
    val cmd = RunTestCommand(prefix + "-normal-msg", bootstrapServers, topic_2000_5000.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), ZioProducerBenchmarks.plainFlow)
  }

  it should "bench with normal messages written to 8 partitions" in {
    val cmd =
      RunTestCommand(prefix + "-normal-msg-8-partitions", bootstrapServers, topic_2000_5000_8.freshTopic)
    runPerfTest(cmd, KafkaProducerFixtures.initializedProducer(cmd), ZioProducerBenchmarks.plainFlow)
  }
}
