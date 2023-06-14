val zioSbtVersion = "0.4.0-alpha.12"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)

addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"        % "0.11.0")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"        % "0.4.2")
addSbtPlugin("com.github.sbt"            % "sbt-native-packager" % "1.9.16")

resolvers ++= Resolver.sonatypeOssRepos("public")
