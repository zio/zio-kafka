val mainScala = "2.12.10"
val allScala  = Seq("2.11.12", mainScala)

ThisBuild / useCoursier := false
organization := "dev.zio"
homepage := Some(url("https://github.com/zio/zio-kafka"))
name := "zio-kafka"
licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
scalaVersion := mainScala
parallelExecution in Test := false
scalafmtOnCompile := true
fork in Test := true
pgpPublicRing := file("/tmp/public.asc")
pgpSecretRing := file("/tmp/secret.asc")
scmInfo := Some(
  ScmInfo(url("https://github.com/zio/zio-kafka/"), "scm:git:git@github.com:zio/zio-kafka.git")
)
developers := List(
  Developer(
    "iravid",
    "Itamar Ravid",
    "iravid@iravid.com",
    url("https://github.com/iravid")
  )
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

libraryDependencies ++= Seq(
  "dev.zio"                 %% "zio-streams"    % "1.0.0-RC13",
  "dev.zio"                 %% "zio-test"       % "1.0.0-RC13" % "test",
  "dev.zio"                 %% "zio-test-sbt"   % "1.0.0-RC13" % "test",
  "org.apache.kafka"        % "kafka-clients"   % "2.3.0",
  "org.scalatest"           %% "scalatest"      % "3.0.5" % "test",
  "io.github.embeddedkafka" %% "embedded-kafka" % "2.3.0" % "test",
  "ch.qos.logback"          % "logback-classic" % "1.2.3" % "test",
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.10")
)

fork in run := true

crossScalaVersions := allScala

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
