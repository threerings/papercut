import sbt._
import Keys._

object PapercutBuild extends Build {
  val locals = com.samskivert.condep.Depends(
    ("flashbang-playn", null, "com.threerings" % "flashbang-playn" % "1.0-SNAPSHOT")
  )

  lazy val papercut = locals.addDeps(Project(
    "papercut", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "papercut",
      scalaVersion := "2.9.1",
      scalacOptions ++= Seq("-deprecation"),
      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-swing" % "2.9.1"
      )
    )
  ))
}
