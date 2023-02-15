lazy val scala212  = "2.12.17"
lazy val scala213  = "2.13.10"
lazy val scala3    = "3.2.2"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala212, scala3, mainScala)

lazy val zioVersion           = "2.0.9"
lazy val kafkaVersion         = "3.3.2"
lazy val embeddedKafkaVersion = "3.3.2" // Should be the same as kafkaVersion, except for the patch part

lazy val kafkaClients          = "org.apache.kafka"           % "kafka-clients"           % kafkaVersion
lazy val zio                   = "dev.zio"                   %% "zio"                     % zioVersion
lazy val zioStreams            = "dev.zio"                   %% "zio-streams"             % zioVersion
lazy val zioTest               = "dev.zio"                   %% "zio-test"                % zioVersion
lazy val zioTestSbt            = "dev.zio"                   %% "zio-test-sbt"            % zioVersion
lazy val scalaCollectionCompat = "org.scala-lang.modules"    %% "scala-collection-compat" % "2.9.0"
lazy val jacksonDatabind       = "com.fasterxml.jackson.core" % "jackson-databind"        % "2.14.2"
lazy val logback               = "ch.qos.logback"             % "logback-classic"         % "1.3.5"
lazy val embeddedKafka         = "io.github.embeddedkafka"   %% "embedded-kafka"          % embeddedKafkaVersion

inThisBuild(
  List(
    organization             := "dev.zio",
    homepage                 := Some(url("https://zio.dev/zio-kafka")),
    licenses                 := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    useCoursier              := false,
    scalaVersion             := mainScala,
    crossScalaVersions       := allScala,
    Test / parallelExecution := false,
    Test / fork              := true,
    run / fork               := true,
    pgpPublicRing            := file("/tmp/public.asc"),
    pgpSecretRing            := file("/tmp/secret.asc"),
    pgpPassphrase            := sys.env.get("PGP_PASSWORD").map(_.toArray),
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
    docs
  )

def buildInfoSettings(packageName: String) =
  Seq(
    buildInfoKeys := Seq[BuildInfoKey](organization, moduleName, name, version, scalaVersion, sbtVersion, isSnapshot),
    buildInfoPackage := packageName
  )

def stdSettings(prjName: String) = Seq(
  name              := s"$prjName",
  scalafmtOnCompile := !(insideCI.value),
  Compile / compile / scalacOptions ++= {
    if (scalaBinaryVersion.value == "2.13") Seq("-Wconf:cat=unused-nowarn:s")
    else Seq()
  },
  scalacOptions -= "-Xlint:infer-any",
  // workaround for bad constant pool issue
  (Compile / doc) := Def.taskDyn {
    val default = (Compile / doc).taskValue
    Def.task(default.value)
  }.value
)

lazy val zioKafka =
  project
    .in(file("zio-kafka"))
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(
      libraryDependencies ++= Seq(
        zioStreams,
        kafkaClients,
        jacksonDatabind,
        scalaCollectionCompat
      )
    )

lazy val zioKafkaTestUtils =
  project
    .in(file("zio-kafka-test-utils"))
    .dependsOn(zioKafka)
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka-test-utils"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(
      libraryDependencies ++= Seq(
        zio,
        zioTest,
        kafkaClients,
        scalaCollectionCompat
      ) ++ {
        if (scalaBinaryVersion.value == "3")
          Seq(
            embeddedKafka
              .cross(CrossVersion.for3Use2_13) exclude ("org.scala-lang.modules", "scala-collection-compat_2.13")
          )
        else Seq(embeddedKafka)
      }
    )

lazy val zioKafkaTest =
  project
    .in(file("zio-kafka-test"))
    .dependsOn(zioKafka, zioKafkaTestUtils)
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka-test"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(publish / skip := true)
    .settings(
      libraryDependencies ++= Seq(
        zioStreams,
        zioTest    % Test,
        zioTestSbt % Test,
        kafkaClients,
        jacksonDatabind,
        logback % Test,
        scalaCollectionCompat
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

lazy val zioKafkaBench =
  project
    .in(file("zio-kafka-bench"))
    .enablePlugins(JmhPlugin)
    .settings(stdSettings("zio-kafka-bench"))
    .settings(publish / skip := true)
    .dependsOn(zioKafka)

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
    docsPublishBranch                          := "master",
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
