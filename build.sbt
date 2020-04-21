import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / organization := "me.chadrs"
ThisBuild / version := "0.1.0-SNAPSHOT"

val sharedSettings = Seq(name := "json2classes")

val scalaV = "2.13.1"
val circeVersion = "0.13.0"

lazy val parent = (project in file(".")).aggregate(codegen.jvm, webapp)

lazy val codegen =
  (crossProject(JSPlatform, JVMPlatform)
    .withoutSuffixFor(JVMPlatform)
    .crossType(CrossType.Pure) in file("codegen")).settings(
    moduleName := "json2classes-codegen",
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies += "org.scalameta" %%% "scalameta" % "4.3.7",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.1" % "test"
  )

lazy val webapp = (project in file("webapp"))
  .dependsOn(codegen.js)
  .settings(
    moduleName := "json2classes-webapp",
    scalaVersion := scalaV,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0",
    libraryDependencies += "in.nvilla" %%% "monadic-html" % "0.4.0",
      libraryDependencies += "org.scalablytyped" %%% "ace" % "0.0-unknown-dt-20190322Z-2f839f",
    scalacOptions := Seq("-Ymacro-annotations")
  )
  .enablePlugins(ScalaJSPlugin)
