import sbt.Keys.{ fork, parallelExecution }
import scala.sys.process._

import scala.util.Try

lazy val scala212  = "2.12.15"
lazy val scala213  = "2.13.8"
lazy val scala3    = "3.1.2"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala212, scala3, mainScala)

lazy val zioVersion           = "1.0.14"
lazy val kafkaClientsVersion  = "3.1.0-cdk"
lazy val embeddedKafkaVersion = "3.1.0" // Should be the same as kafkaVersion, except for the patch part

lazy val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion % Test

val GITHUB_OWNER   = "conduktor"
val GITHUB_PROJECT = "zio-kafka"

def env(v: String): Option[String] = sys.env.get(v)

inThisBuild(
  List(
    version := sys.env
      .getOrElse("RELEASE_VERSION", "0.0.1-SNAPSHOT"), // "RELEASE_VERSION" comes from .github/workflows/release.yml
    organization             := "dev.zio",
    homepage                 := Some(url("https://github.com/zio/zio-kafka")),
    licenses                 := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    useCoursier              := false,
    scalaVersion             := mainScala,
    crossScalaVersions       := allScala,
    Test / parallelExecution := false,
    Test / fork              := true,
    run / fork               := true,
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio/zio-kafka/"), "scm:git:git@github.com:zio/zio-kafka.git")
    ),
    developers := List(
      Developer(
        "iravid",
        "Itamar Ravid",
        "iravid@iravid.com",
        url("https://github.com/iravid")
      )
    ),
    // CDK publishing settings
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
  )
)

lazy val kafka =
  project
    .in(file("."))
    .enablePlugins(BuildInfoPlugin)
    .settings(
      name              := "zio-kafka",
      scalafmtOnCompile := true,
      Compile / compile / scalacOptions ++= {
        if (scalaBinaryVersion.value == "2.13") Seq("-Wconf:cat=unused-nowarn:s")
        else Seq()
      },
      // workaround for bad constant pool issue
      (Compile / doc) := Def.taskDyn {
        val default = (Compile / doc).taskValue
        Def.task(default.value)
      }.value
    )
    .settings(
      buildInfoKeys    := Seq[BuildInfoKey](organization, name, version, scalaVersion, sbtVersion, isSnapshot),
      buildInfoPackage := "zio.kafka"
    )
    .settings(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      libraryDependencies ++= Seq(
        "dev.zio"                   %% "zio-streams"             % zioVersion,
        "dev.zio"                   %% "zio-test"                % zioVersion % Test,
        "dev.zio"                   %% "zio-test-sbt"            % zioVersion % Test,
        "io.conduktor.kafka"         % "kafka-clients"           % kafkaClientsVersion,
        "com.fasterxml.jackson.core" % "jackson-databind"        % "2.12.6.1",
        "ch.qos.logback"             % "logback-classic"         % "1.2.11"   % Test,
        "org.scala-lang.modules"    %% "scala-collection-compat" % "2.7.0"
      ) ++ {
        if (scalaBinaryVersion.value == "3")
          Seq(
            embeddedKafka
              .cross(CrossVersion.for3Use2_13) exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
          )
        else Seq(embeddedKafka)
      },
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

lazy val docs = project
  .in(file("zio-kafka-docs"))
  .dependsOn(kafka)
  .settings(
    // Version will only appear on the generated target file replacing @VERSION@
    mdocVariables := Map(
      "VERSION" -> version.value
    )
  )
  .enablePlugins(MdocPlugin)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
