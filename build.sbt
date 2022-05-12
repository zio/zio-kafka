lazy val scala212  = "2.12.15"
lazy val scala213  = "2.13.8"
lazy val scala3    = "3.1.1"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala212, scala3, mainScala)

lazy val zioVersion           = "2.0.0-RC5"
lazy val kafkaVersion         = "3.1.1"
lazy val embeddedKafkaVersion = "3.1.1" // Should be the same as kafkaVersion, except for the patch part

lazy val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion % Test

inThisBuild(
  List(
    organization             := "dev.zio",
    homepage                 := Some(url("https://github.com/zio/zio-kafka")),
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
      scalacOptions -= "-Xlint:infer-any",
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
      libraryDependencies ++= Seq(
        "dev.zio"                   %% "zio-streams"             % zioVersion,
        "dev.zio"                   %% "zio-test"                % zioVersion % Test,
        "dev.zio"                   %% "zio-test-sbt"            % zioVersion % Test,
        "org.apache.kafka"           % "kafka-clients"           % kafkaVersion,
        "com.fasterxml.jackson.core" % "jackson-databind"        % "2.13.2",
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
