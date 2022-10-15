lazy val scala212  = "2.12.17"
lazy val scala213  = "2.13.8"
lazy val scala3    = "3.1.3"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala212, scala3, mainScala)

lazy val zioVersion           = "2.0.2"
lazy val kafkaVersion         = "3.2.0"
lazy val embeddedKafkaVersion = "3.2.0" // Should be the same as kafkaVersion, except for the patch part

lazy val embeddedKafka = "io.github.embeddedkafka" %% "embedded-kafka" % embeddedKafkaVersion

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

lazy val root = project
  .in(file("."))
  .settings(
    name           := "zio-kafka",
    publish / skip := true
  )
  .aggregate(
    zioKafka,
    zioKafkaTestUtils,
    zioKafkaTest
  )

def buildInfoSettings(packageName: String) =
  Seq(
    buildInfoKeys := Seq[BuildInfoKey](organization, moduleName, name, version, scalaVersion, sbtVersion, isSnapshot),
    buildInfoPackage := packageName
  )

def stdSettings(prjName: String) = Seq(
  name              := s"$prjName",
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

lazy val zioKafka =
  project
    .in(file("zio-kafka"))
    .enablePlugins(BuildInfoPlugin)
    .settings(stdSettings("zio-kafka"))
    .settings(buildInfoSettings("zio.kafka"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio"                   %% "zio-streams"             % zioVersion,
        "org.apache.kafka"           % "kafka-clients"           % kafkaVersion,
        "com.fasterxml.jackson.core" % "jackson-databind"        % "2.13.3",
        "org.scala-lang.modules"    %% "scala-collection-compat" % "2.7.0"
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
        "dev.zio"                %% "zio"                     % zioVersion,
        "org.apache.kafka"        % "kafka-clients"           % kafkaVersion,
        "org.scala-lang.modules" %% "scala-collection-compat" % "2.7.0"
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
        "dev.zio"                   %% "zio-streams"             % zioVersion,
        "dev.zio"                   %% "zio-test"                % zioVersion % Test,
        "dev.zio"                   %% "zio-test-sbt"            % zioVersion % Test,
        "org.apache.kafka"           % "kafka-clients"           % kafkaVersion,
        "com.fasterxml.jackson.core" % "jackson-databind"        % "2.13.4",
        "ch.qos.logback"             % "logback-classic"         % "1.4.4"    % Test,
        "org.scala-lang.modules"    %% "scala-collection-compat" % "2.8.1"
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
  .dependsOn(zioKafka)
  .settings(
    // Version will only appear on the generated target file replacing @VERSION@
    mdocVariables := Map(
      "VERSION" -> version.value
    )
  )
  .enablePlugins(MdocPlugin)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
