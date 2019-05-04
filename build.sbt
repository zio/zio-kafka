lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "dev.zio",
        scalaVersion := "2.12.8",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "zio-kafka",
    libraryDependencies ++= Seq(
      "org.apache.kafka" % "kafka-clients" % "2.2.0",
      "org.scalaz"       %% "scalaz-zio"   % "1.0-RC4"
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
    scalafmtOnCompile := true,
    fork := true,
    Test / fork := true
  )
