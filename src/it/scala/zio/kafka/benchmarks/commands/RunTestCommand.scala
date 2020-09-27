package zio.kafka.benchmarks.commands

import zio.kafka.benchmarks.fixtures.PerfFixtureHelpers.FilledTopic

case class RunTestCommand(testName: String, kafkaHost: String, filledTopic: FilledTopic) {

  val msgCount           = filledTopic.msgCount
  val msgSize            = filledTopic.msgSize
  val numberOfPartitions = filledTopic.numberOfPartitions
  val replicationFactor  = filledTopic.replicationFactor

}
