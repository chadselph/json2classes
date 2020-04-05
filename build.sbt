enablePlugins(ScalaJSPlugin)

name := "json2classes"
version := "0.1"

scalaVersion := "2.13.1"

scalaJSUseMainModuleInitializer := true

val circeVersion = "0.13.0"


libraryDependencies += "org.scalameta" %%% "scalameta" % "4.3.7"
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0"
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"


libraryDependencies ++= Seq(
  "io.circe" %%% "circe-core",
  "io.circe" %%% "circe-generic",
  "io.circe" %%% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "org.scalablytyped" %%% "ace" %  "0.0-unknown-dt-20190322Z-2f839f"
