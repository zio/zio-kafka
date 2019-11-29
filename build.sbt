lazy val scala211 = "2.11.12"
lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.1"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala211, scala212, mainScala)

lazy val zioVersion = "1.0.0-RC17"
lazy val kafkaVersion = "2.4.0"
lazy val embeddedKafkaVersion = kafkaVersion

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

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / resolvers ++= Seq(
  // for Embedded Kafka 2.4.0
  Resolver.bintrayRepo("seglo", "maven"),
  // for release candidate builds of Apache Kafka
  MavenRepository("Apache Staging", "https://repository.apache.org/content/groups/staging/")
)

name := "zio-kafka"
scalafmtOnCompile := true

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot)
buildInfoPackage := "zio.kafka"
buildInfoObject := "BuildInfo"

libraryDependencies ++= Seq(
  "dev.zio"          %% "zio-streams"  % zioVersion,
  "dev.zio"          %% "zio-test"     % zioVersion % Test,
  "dev.zio"          %% "zio-test-sbt" % zioVersion % Test,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
//  "org.scalatest"           %% "scalatest"      % "3.0.5" % "test",
  "io.github.seglo"  %% "embedded-kafka" % embeddedKafkaVersion, // "io.github.embeddedkafka" %% "embedded-kafka" % "2.3.1" % "test",
  "ch.qos.logback"   % "logback-classic" % "1.2.3" % Test,
)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
