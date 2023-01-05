import sbt.Keys.testFrameworks

val dottyVersion = "3.2.1"

val Versions =  new {
  val munit = "0.7.29"
  val circe = "0.14.2"
  val circeConfig = "0.8.0-148-g173bd6e-SNAPSHOT"
  val djl   = "0.20.0"
  val cats  = "2.8.0"
  val zio   = "2.0.5"
  val zioConfig = "3.0.7"
}

lazy val srl = project
  .in(file("."))
  .settings(
    name := "srl",
    version := "0.1.0",
    organization := "ai.srl",
    scalaVersion := dottyVersion,
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Seq(
      "io.circe"           %% "circe-config"     % Versions.circeConfig,
      "org.scalameta"  %% "munit"               % Versions.munit % Test,
      "ai.djl"          % "api"                 % Versions.djl,
      "io.circe"       %% "circe-jawn"          % Versions.circe,
      "net.sf.supercsv" % "super-csv"           % "2.4.0",
      "org.typelevel"  %% "alleycats-core"      % Versions.cats,
      "org.typelevel"  %% "cats-core"           % Versions.cats,
      "eu.timepit"     %% "refined"             % "0.10.1",
      "dev.zio"        %% "zio"                 % Versions.zio,
      "dev.zio"        %% "zio-streams"         % Versions.zio,
      "dev.zio"        %% "zio-test"            % Versions.zio   % Test,
      "dev.zio"        %% "zio-test-sbt"        % Versions.zio   % Test,
      "dev.zio"        %% "zio-test-junit"      % Versions.zio   % Test,
      "dev.zio"        %% "zio-test-magnolia"   % Versions.zio   % Test,
      "com.github.sbt"  % "junit-interface"     % "0.13.3"       % Test,
      "org.typelevel"  %% "shapeless3-deriving" % "3.1.0",
      "dev.zio" %% "zio-config" % Versions.zioConfig,
      "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
      "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig,
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
