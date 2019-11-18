val mainScala = "2.12.10"
val allScala  = Seq("2.11.12", mainScala)

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

name := "zio-kafka"
scalafmtOnCompile := true

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot)
buildInfoPackage := "zio.kafka"
buildInfoObject := "BuildInfo"

libraryDependencies ++= Seq(
  "dev.zio"          %% "zio-streams"  % "1.0.0-RC16",
  "dev.zio"          %% "zio-test"     % "1.0.0-RC16" % "test",
  "dev.zio"          %% "zio-test-sbt" % "1.0.0-RC16" % "test",
  "org.apache.kafka" % "kafka-clients" % "2.3.0",
//  "org.scalatest"           %% "scalatest"      % "3.0.5" % "test",
  "io.github.embeddedkafka" %% "embedded-kafka" % "2.3.1" % "test",
  "ch.qos.logback"          % "logback-classic" % "1.2.3" % "test",
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.10")
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
