import sbt.Keys.testFrameworks

val dottyVersion = "3.0.0-RC2"

val Versions = new {
  val munit = "0.7.23"
  val circe = "0.14.0-M5"
  val djl = "0.10.0"
  val cats = "2.5.0"
}

lazy val root = project
  .in(file("."))
  .settings(
    name := "srl",
    version := "0.1.0",
    organization := "ai.srl",
    scalaVersion := dottyVersion,
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % Versions.munit % Test,
      "ai.djl" % "api" % Versions.djl,
      "io.circe" %% "circe-jawn" % Versions.circe,
      "net.sf.supercsv" % "super-csv" % "2.4.0",
      "org.typelevel" %% "alleycats-core" % Versions.cats
  ),
    testFrameworks += new TestFramework("munit.Framework"),
  )
