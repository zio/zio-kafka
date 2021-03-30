lazy val scala211  = "2.11.12"
lazy val scala212  = "2.12.11"
lazy val scala213  = "2.13.3"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala211, scala212, mainScala)

lazy val zioVersion   = "1.0.4"
lazy val kafkaVersion = "2.6.0"

// Allows to silence scalac compilation warnings selectively by code block or file path
// This is only compile time dependency, therefore it does not affect the generated bytecode
// https://github.com/ghik/silencer
lazy val silencer = {
  val Version = "1.7.0"
  Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % Version cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % Version % Provided cross CrossVersion.full
  )
}

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://github.com/zio/zio-kafka")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    useCoursier := false,
    scalaVersion := mainScala,
    crossScalaVersions := allScala,
    parallelExecution in Test := false,
    fork in Test := true,
    fork in run := true,
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
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
    )
  )
)

lazy val kafka =
  project
    .in(file("."))
    .enablePlugins(BuildInfoPlugin)
    .settings(
      name := "zio-kafka",
      scalafmtOnCompile := true,
      Compile / compile / scalacOptions ++= {
        if (scalaBinaryVersion.value == "2.13") Seq("-P:silencer:globalFilters=[import scala.collection.compat._]")
        else if (scalaBinaryVersion.value == "2.11") Seq("-Xmax-classfile-name", "242")
        else Seq.empty
      },
      // workaround for bad constant pool issue
      (Compile / doc) := Def.taskDyn {
        val default = (Compile / doc).taskValue
        if (scalaBinaryVersion.value == "2.11") {
          (Compile / doc / target).toTask
        } else {
          Def.task(default.value)
        }
      }.value,
      Compile / doc / scalacOptions ++= {
        if (scalaBinaryVersion.value == "2.13") Seq("-P:silencer:globalFilters=[import scala.collection.compat._]")
        else Seq.empty
      }
    )
    .settings(
      buildInfoKeys := Seq[BuildInfoKey](organization, name, version, scalaVersion, sbtVersion, isSnapshot),
      buildInfoPackage := "zio.kafka"
    )
    .settings(
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      libraryDependencies ++= Seq(
        "dev.zio"                    %% "zio-streams"             % zioVersion,
        "dev.zio"                    %% "zio-test"                % zioVersion % "test",
        "dev.zio"                    %% "zio-test-sbt"            % zioVersion % "test",
        "org.apache.kafka"           % "kafka-clients"            % kafkaVersion,
        "com.fasterxml.jackson.core" % "jackson-databind"         % "2.12.2",
        "ch.qos.logback"             % "logback-classic"          % "1.2.3" % "test",
        "org.scala-lang.modules"     %% "scala-collection-compat" % "2.4.3",
        compilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full)
      ) ++ {
        if (scalaBinaryVersion.value == "2.13") silencer
        else if (scalaBinaryVersion.value == "2.12") silencer
        else Seq.empty
      } ++ {
        if (scalaBinaryVersion.value == "2.11") Seq.empty
        else Seq("io.github.embeddedkafka" %% "embedded-kafka" % kafkaVersion % "test")
      },
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
    )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
