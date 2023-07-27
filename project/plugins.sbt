val zioSbtVersion = "0.4.0-alpha.12"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)

addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"        % "0.11.0")
addSbtPlugin("org.typelevel"  % "sbt-tpolecat"        % "0.5.0")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

resolvers ++= Resolver.sonatypeOssRepos("public")
