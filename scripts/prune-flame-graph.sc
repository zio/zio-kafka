//> using dep com.lihaoyi::os-lib:0.9.2

import java.time.LocalDate
import math.Ordered.orderingToOrdered
import scala.util.matching.Regex

val branch = os.proc("git rev-parse --abbrev-ref HEAD".split(' ')).call().out.trim()

if (branch != "gh-pages") {
  println("Not on gh-pages branch")
  System.exit(1)
}

val flameDir = os.pwd / "dev" / "profile"
val indexPath = flameDir / "index.html"
val indexContent = os.read(indexPath)

val liPattern: Regex = """^ *<li>(\d{4}-\d{2}-\d{2}).*href="([0-9a-f]+)".*$""".r
val oneMonthAgo = LocalDate.now.minusMonths(1)

val (newIndexContentBuilder, removeDirsBuilder) = indexContent
  .linesIterator
  .foldLeft((Seq.newBuilder[String], Seq.newBuilder[String])) { case ((out, removed), line) =>
    line match {
      case liPattern(date, dir) if (LocalDate.parse(date) < oneMonthAgo) =>
        (out, removed += dir)
      case _ =>
        (out += line, removed)
    }
  }

val newIndexContent = newIndexContentBuilder.result().mkString("\n")
val removeDirs = removeDirsBuilder.result()

if (removeDirs.nonEmpty) {
  removeDirs.foreach { removeDir =>
    os.remove.all(flameDir / removeDir)
  }
  os.write.over(indexPath, newIndexContent)
}

println(s"Pruned ${removeDirs.size} old flame graph directories")
