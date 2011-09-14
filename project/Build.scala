import sbt._
import Keys._

object PapercutBuild extends Build {
  lazy val papercut = Project(
    "papercut", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "papercut",
      crossPaths   := false,

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,

      autoScalaLibrary := false, // no scala-library dependency
      libraryDependencies ++= Seq(
        "com.samskivert" % "pythagoras" % "1.1-SNAPSHOT",
        "com.threerings" % "react" % "1.0-SNAPSHOT",
        "com.googlecode.playn" % "playn-core" % "1.0-SNAPSHOT",
        "com.googlecode.playn" % "playn-java" % "1.0-SNAPSHOT",
        "com.threerings" % "tripleplay" % "1.0-SNAPSHOT",
        "com.threerings" % "flashbang-playn" % "1.0-SNAPSHOT"
      )
    )
  )
}
