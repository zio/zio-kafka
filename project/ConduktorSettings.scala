import sbt.*
import sbt.Keys.*
import zio.sbt.ZioSbtCiPlugin.autoImport.ciEnabledBranches
import scala.sys.process._

import scala.util.Try

object ConduktorSettings {

  val GITHUB_OWNER   = "conduktor"
  val GITHUB_PROJECT = "zio-kafka"

  def env(v: String): Option[String] = sys.env.get(v)

  val zioKafkaSettingsOverrides =
    Seq(
      ciEnabledBranches := Seq.empty
    )

  val conduktorSettings =
    Seq(
      publishMavenStyle := true,
      publishTo := Some(
        s"GitHub $GITHUB_OWNER Apache Maven Packages of $GITHUB_PROJECT" at s"https://maven.pkg.github.com/$GITHUB_OWNER/$GITHUB_PROJECT"
      ),
      resolvers += s"GitHub $GITHUB_OWNER Apache Maven Packages" at s"https://maven.pkg.github.com/$GITHUB_OWNER/_/",
      credentials += Credentials(
        "GitHub Package Registry",
        "maven.pkg.github.com",
        GITHUB_OWNER,
        (env("GH_PACKAGES_TOKEN") orElse env("GH_READ_PACKAGES") orElse env("GITHUB_TOKEN"))
          .orElse(Try(s"git config github.token".!!).map(_.trim).toOption)
          .getOrElse(
            throw new RuntimeException(
              "Missing env variable: `GH_PACKAGES_TOKEN` or `GH_READ_PACKAGES` or `GITHUB_TOKEN` or git config option: `github.token`"
            )
          )
      )
    ) ++ zioKafkaSettingsOverrides

}
