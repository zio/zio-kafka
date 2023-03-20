val zioSbtVersion = "0.3.10+95-bfadedb2-SNAPSHOT"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.2")

resolvers ++= Resolver.sonatypeOssRepos("public")
