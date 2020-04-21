addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.32") // scalameta not yet published for 1.0
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

resolvers += Resolver.bintrayRepo("oyvindberg", "ScalablyTyped")
addSbtPlugin("org.scalablytyped" % "sbt-scalablytyped" % "202001240947")
