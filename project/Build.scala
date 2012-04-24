import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "pcm"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.0.0-rc.3"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
