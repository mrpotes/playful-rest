name := "core"

organisation := "playful-rest"

version := "1.0"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "play" %% "play" % "2.1.1" intransitive(),
  "play" %% "play-iteratees" % "2.1.1" intransitive(),
  "joda-time" % "joda-time" % "2.1" intransitive(),
  "org.joda" % "joda-convert" % "1.2" intransitive(),
  "org.scala-lang" % "scala-reflect" % "2.10.1"
)

publishTo := Some(Resolver.file("file", new File("../playful-rest-repo")))

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
