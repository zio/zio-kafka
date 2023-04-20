import sbt.Def

lazy val kafkaVersion         = "3.4.0"
lazy val embeddedKafkaVersion = "3.4.0.1" // Should be the same as kafkaVersion, except for the patch part

lazy val kafkaClients          = "org.apache.kafka"           % "kafka-clients"           % kafkaVersion
lazy val scalaCollectionCompat = "org.scala-lang.modules"    %% "scala-collection-compat" % "2.9.0"
lazy val jacksonDatabind       = "com.fasterxml.jackson.core" % "jackson-databind"        % "2.14.2"
lazy val logback               = "ch.qos.logback"             % "logback-classic"         % "1.3.7"

enablePlugins(ZioSbtEcosystemPlugin, ZioSbtCiPlugin)

inThisBuild(
  List(
    name       := "ZIO Kafka",
    zioVersion := "2.0.13",
    crossScalaVersions -= scala211.value,
    ciEnabledBranches        := Seq("master", "series/0.x"),
    useCoursier              := false,
    Test / parallelExecution := false,
    Test / fork              := true,
    run / fork               := true,
    ciJvmOptions ++= Seq("-Xms6G", "-Xmx6G", "-Xss4M", "-XX:+UseG1GC"),
    developers := List(
      Developer(
        "iravid",
        "Itamar Ravid",
        "iravid@iravid.com",
        url("https://github.com/iravid")
      ),
      Developer(
        "svroonland",
        "svroonland",
        "",
        url("https://github.com/svroonland")
      ),
      Developer(
        "guizmaii",
        "Jules Ivanic",
        "",
        url("https://github.com/guizmaii")
      ),
      Developer(
        "erikvanoosten",
        "Erik van Oosten",
        "",
        url("https://github.com/erikvanoosten")
      )
    )
  )
)

val excludeInferAny = { options: Seq[String] => options.filterNot(Set("-Xlint:infer-any")) }

lazy val root = project
  .in(file("."))
  .settings(
    name               := "zio-kafka",
    publish / skip     := true,
    crossScalaVersions := Nil // https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Cross+building+a+project+statefully
  )
  .aggregate(
    zioKafka,
    zioKafkaTestUtils,
    zioKafkaTest,
    zioKafkaBench,
    zioKafkaExample,
    docs
  )

def stdSettings(prjName: String) = Seq(
  name              := s"$prjName",
  scalafmtOnCompile := !insideCI.value,
  Compile / compile / scalacOptions ++=
    optionsOn("2.13")("-Wconf:cat=unused-nowarn:s").value,
  scalacOptions -= "-Xlint:infer-any",
  // workaround for bad constant pool issue
  (Compile / doc) := Def.taskDyn {
    val default = (Compile / doc).taskValue
    Def.task(default.value)
  }.value,
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => Seq(compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"))
      case _            => List.empty
    }
  }
) ++ scalafixSettings

lazy val zioKafka =
  project
    .in(file("zio-kafka"))
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(enableZIO(enableStreaming = true))
    .settings(
      libraryDependencies ++= Seq(
        kafkaClients,
        jacksonDatabind,
        scalaCollectionCompat
      )
    )

lazy val `embedded-kafka`: Def.Initialize[Seq[sbt.ModuleID]] = {
  val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion
  dependenciesOnOrElse("3")(
    embeddedKafka
      .cross(CrossVersion.for3Use2_13) exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
  )(embeddedKafka)
}

lazy val zioKafkaTestUtils =
  project
    .in(file("zio-kafka-test-utils"))
    .dependsOn(zioKafka)
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka-test-utils"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"      % zioVersion.value,
        "dev.zio" %% "zio-test" % zioVersion.value,
        kafkaClients,
        scalaCollectionCompat
      ) ++ `embedded-kafka`.value
    )

lazy val zioKafkaTest =
  project
    .in(file("zio-kafka-test"))
    .dependsOn(zioKafka, zioKafkaTestUtils)
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka-test"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(enableZIO(enableStreaming = true))
    .settings(publish / skip := true)
    .settings(
      libraryDependencies ++= Seq(
        kafkaClients,
        jacksonDatabind,
        logback % Test,
        scalaCollectionCompat
      ) ++ `embedded-kafka`.value
    )

lazy val zioKafkaBench =
  project
    .in(file("zio-kafka-bench"))
    .enablePlugins(JmhPlugin)
    .settings(stdSettings("zio-kafka-bench"))
    .settings(publish / skip := true)
    .settings(libraryDependencies += logback)
    .dependsOn(zioKafka, zioKafkaTestUtils)

lazy val zioKafkaExample =
  project
    .in(file("zio-kafka-example"))
    .enablePlugins(JavaAppPackaging)
    .settings(stdSettings("zio-kafka-example"))
    .settings(publish / skip := true)
    .settings(run / fork := false)
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio"                 %% "zio"                % "2.0.13",
        "dev.zio"                 %% "zio-kafka"          % "2.2",
        "ch.qos.logback"           % "logback-classic"    % "1.4.6",
        "dev.zio"                 %% "zio-logging-slf4j2" % "2.1.12",
        "io.github.embeddedkafka" %% "embedded-kafka"     % embeddedKafkaVersion
      ),
      // Scala 3 compiling fails with:
      // [error] Modules were resolved with conflicting cross-version suffixes in ProjectRef(uri("file:/home/runner/work/zio-kafka/zio-kafka/"), "zioKafkaExample"):
      // [error]    org.scala-lang.modules:scala-collection-compat _3, _2.13
      crossScalaVersions -= scala3.value
    )

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

lazy val docs = project
  .in(file("zio-kafka-docs"))
  .settings(
    moduleName := "zio-kafka-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    projectName                                := "ZIO Kafka",
    mainModuleName                             := (zioKafka / moduleName).value,
    projectStage                               := ProjectStage.ProductionReady,
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(zioKafka),
    readmeCredits :=
      "This library is heavily inspired and made possible by the research and implementation done in " +
        "[Alpakka Kafka](https://github.com/akka/alpakka-kafka), a library maintained by the Akka team and originally " +
        "written as Reactive Kafka by SoftwareMill.",
    readmeLicense +=
      "\n\n" + """|Copyright 2021 Itamar Ravid and the zio-kafka contributors. All rights reserved.
                  |<!-- TODO: not all rights reserved, rather Apache 2... -->""".stripMargin
  )
  .enablePlugins(WebsitePlugin)
