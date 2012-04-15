import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object MinimalBuild extends Build {

  lazy val buildVersion =  "2.0"
  
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
  										
  lazy val root = Project(id = "pcm", base = file("."), settings = Project.defaultSettings).settings(
    version := buildVersion,
    organization := "plm",
    resolvers += typesafe,
    resolvers += typesafeSnapshot,
    libraryDependencies ++= Seq("net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.0.0-rc.3",
  							  "com.typesafe" %% "play-mini" % buildVersion),
    mainClass in (Compile, run) := Some("play.core.server.NettyServer")
  ).settings(assemblySettings: _*)
}
