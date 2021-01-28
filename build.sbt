val dottyVersion = "3.0.0-M3"

val Versions = new {
  val munit = "0.7.21"
}

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala reinforcement learning",
    version := "0.1.0",

    scalaVersion := dottyVersion,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % Versions.munit % Test,
    )
  )
