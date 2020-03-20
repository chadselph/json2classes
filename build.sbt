name := "json2classes"

version := "0.1"

scalaVersion := "2.13.1"

val circeVersion = "0.12.3"


libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.4"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
