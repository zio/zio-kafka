val zioSbtVersion = "0.4.0-alpha.30"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)

addSbtPlugin("ch.epfl.scala"  % "sbt-scalafix"        % "0.14.0")
addSbtPlugin("org.typelevel"  % "sbt-tpolecat"        % "0.5.2")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.0")
addSbtPlugin("com.typesafe"   % "sbt-mima-plugin"     % "1.1.4")

resolvers ++= Resolver.sonatypeOssRepos("public")
