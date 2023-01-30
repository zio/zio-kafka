addSbtPlugin("org.scalameta"             % "sbt-scalafmt"    % "2.5.0")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"    % "0.4.2")
addSbtPlugin("com.eed3si9n"              % "sbt-buildinfo"   % "0.11.0")
addSbtPlugin("org.scalameta"             % "sbt-mdoc"        % "2.3.7")

resolvers ++= Resolver.sonatypeOssRepos("public")
