package zio.kafka.benchmarks.fixtures

import zio.kafka.benchmarks.commands.RunTestCommand

case class FixtureGen[F](command: RunTestCommand, generate: Int => F)
