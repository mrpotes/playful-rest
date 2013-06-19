import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "playful-rest-example"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "se.radley" %% "play-plugins-salat" % "1.2",
    "playful-rest" %% "playful-rest" % "1.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= Seq(
        "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
        "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
        "playful-rest on GitHub" at "https://raw.github.com/mrpotes/playful-rest/repository"
        )
    // Add your own project settings here 
  )
}
