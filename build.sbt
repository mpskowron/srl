import sbt.Keys.testFrameworks

val dottyVersion = "3.1.1"

val Versions =  new {
  val munit = "0.7.29"
  val circe = "0.14.1"
  val djl   = "0.15.0"
  val cats  = "2.6.1"
  val zio   = "1.0.13"
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
      "org.scalameta"  %% "munit"               % Versions.munit % Test,
      "ai.djl"          % "api"                 % Versions.djl,
      "io.circe"       %% "circe-jawn"          % Versions.circe,
      "net.sf.supercsv" % "super-csv"           % "2.4.0",
      "org.typelevel"  %% "alleycats-core"      % Versions.cats,
      "org.typelevel"  %% "cats-core"           % Versions.cats,
      "eu.timepit"     %% "refined"             % "0.9.27",
      "dev.zio"        %% "zio"                 % Versions.zio,
      "dev.zio"        %% "zio-streams"         % Versions.zio,
      "dev.zio"        %% "zio-test"            % Versions.zio   % Test,
      "dev.zio"        %% "zio-test-sbt"        % Versions.zio   % Test,
      "dev.zio"        %% "zio-test-junit"      % Versions.zio   % Test,
      "dev.zio"        %% "zio-test-magnolia"   % Versions.zio   % Test,
      "com.github.sbt"  % "junit-interface"     % "0.13.2"       % Test,
      "org.typelevel"  %% "shapeless3-deriving" % "3.0.1"
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
