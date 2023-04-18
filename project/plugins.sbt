val zioSbtVersion = "0.3.10+80-6c029fcb-SNAPSHOT"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)

addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"        % "0.4.2")
addSbtPlugin("com.github.sbt"            % "sbt-native-packager" % "1.9.16")

resolvers ++= Resolver.sonatypeOssRepos("public")
