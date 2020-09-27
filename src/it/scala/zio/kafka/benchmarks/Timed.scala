package zio.kafka.benchmarks

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import com.codahale.metrics._
import com.typesafe.scalalogging.LazyLogging
import zio.kafka.benchmarks.commands.RunTestCommand
import zio.kafka.benchmarks.fixtures.FixtureGen

import scala.concurrent.duration._

object Timed extends LazyLogging {
  private val benchmarkReportBasePath = Paths.get("target")

  def reporter(metricRegistry: MetricRegistry): ScheduledReporter =
    Slf4jReporter
      .forRegistry(metricRegistry)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build()

  def csvReporter(metricRegistry: MetricRegistry): ScheduledReporter =
    CsvReporter
      .forRegistry(metricRegistry)
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .build(benchmarkReportBasePath.toFile)

  def runPerfTest[F](command: RunTestCommand, fixtureGen: FixtureGen[F], testBody: (F, Meter) => Unit): Unit = {
    val name     = command.testName
    val msgCount = command.msgCount
    logger.info(s"Generating fixture for $name ${command.filledTopic}")
    val fixture = fixtureGen.generate(msgCount)
    val metrics = new MetricRegistry()
    val meter   = metrics.meter(name)
    logger.info(s"Running benchmarks for $name")
    val now = System.nanoTime()
    testBody(fixture, meter)
    val after = System.nanoTime()
    val took  = (after - now).nanos
    logger.info(s"Test $name took ${took.toMillis} ms")
    reporter(metrics).report()
    csvReporter(metrics).report()
  }

}
