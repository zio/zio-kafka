import com.typesafe.tools.mima.core.*
import com.typesafe.tools.mima.core.ProblemFilters.*
import com.typesafe.tools.mima.plugin.MimaKeys.*
import sbt.*
import sbt.Keys.{ name, organization }

object MimaSettings {

  def mimaSettings(binCompatVersionToCompare: Option[String], failOnProblem: Boolean): Seq[Def.Setting[?]] =
    binCompatVersionToCompare match {
      case None =>
        Seq(mimaPreviousArtifacts := Set.empty)
      case Some(binCompatVersion) =>
        Seq(
          mimaPreviousArtifacts := Set(organization.value %% name.value % binCompatVersion),
          mimaBinaryIssueFilters ++= Seq(
            exclude[Problem]("zio.kafka.consumer.internal.*"),
            exclude[Problem]("zio.kafka.diagnostics.internal.*")
          ),
          mimaFailOnProblem := failOnProblem
        )

    }

}
