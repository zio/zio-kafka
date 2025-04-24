//> using dep com.lihaoyi::os-lib:0.9.2

import java.time.LocalDate
import scala.math.Ordered.orderingToOrdered
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

// Flame graphs from before this date are removed.
val flameGraphCutoffDate = LocalDate.now.minusMonths(6)

val (newIndexContentBuilder, removeDirsBuilder) = indexContent
  .linesIterator
  .foldLeft((Seq.newBuilder[String], Seq.newBuilder[String])) { case ((out, removed), line) =>
    line match {
      case liPattern(date, dir) if (LocalDate.parse(date) < flameGraphCutoffDate) =>
        (out, removed += dir)
      case _ =>
        (out += line, removed)
    }
  }

val newIndexContent = newIndexContentBuilder.result().mkString("\n")
val removeDirs = removeDirsBuilder.result()

if (removeDirs.isEmpty) {
  println("There are no old flame graph directories to remove")
} else {
  println(s"::group::Removing ${removeDirs.size} old flame graph directories")
  removeDirs.foreach { removeDir =>
    println(s"git rm -r $removeDir")
    os.proc("git", "rm", "-r", removeDir).call(cwd = flameDir)
  }
  os.write.over(indexPath, newIndexContent)
  val indexFileStr = indexPath.relativeTo(os.pwd).toString
  os.proc("git", "add", indexFileStr).call()
  os.proc("git", "commit", "-m", s"Remove old flame graph directories").call()
  println("Committed to git")
  println("::endgroup::")
}
