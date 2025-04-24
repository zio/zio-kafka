//> using dep com.lihaoyi::os-lib:0.11.4
//> using dep com.lihaoyi::upickle:4.1.0

import java.time.{LocalDate, ZoneId, ZonedDateTime}
import scala.math.Ordered.orderingToOrdered
import upickle.default.*

val branch = os.proc("git rev-parse --abbrev-ref HEAD".split(' ')).call().out.trim()

if (branch != "gh-pages") {
  println("Not on gh-pages branch")
  System.exit(1)
}

val masterCommitCutOfDateTime = ZonedDateTime.now.minusMonths(6)
val prCommitCutOfDateTime = ZonedDateTime.now.minusDays(14)

val dataPrefix = "window.BENCHMARK_DATA = "

val flameDir = os.pwd / "dev" / "bench"
val dataPath = flameDir / "data.js"
val dataContent = os.read(dataPath)
val jsonContent = ujson.read(dataContent.stripPrefix(dataPrefix))
val benchmarks = jsonContent("entries")("JMH Benchmark")
val benchmarksToKeep = benchmarks.arr.filter { benchmark =>
  val datetime = ZonedDateTime.parse(benchmark("commit")("timestamp").str)
  val isPrCommit = benchmark("commit")("url").str.startsWith("https://github.com/zio/zio-kafka/pull")
  if (isPrCommit) datetime >= prCommitCutOfDateTime
  else datetime >= masterCommitCutOfDateTime
}
benchmarksToKeep.arr.value.foreach { benchmark =>
  // Remove standard commit message from scala steward
  val message = benchmark("commit")("message").str
  if (message.contains("\r\n\r\n## Usage")) {
    val newMessage = message.take(message.indexOf("\r\n\r\n## Usage"))
    benchmark("commit")("message") = newMessage
  }
}
jsonContent("entries").update("JMH Benchmark", benchmarksToKeep)
val newDataContent = dataPrefix + jsonContent.render(indent = 2) 

if (dataContent == newDataContent) {
  println("There are no old benchmarks to remove")
} else {
  println(s"::group::Removing old benchmarks")
  os.write.over(dataPath, newDataContent)
  val dataFileStr = dataPath.relativeTo(os.pwd).toString
  os.proc("git", "add", dataFileStr).call()
  os.proc("git", "commit", "-m", s"Remove old benchmarks").call()
  println("Committed to git")
  println("::endgroup::")
}
