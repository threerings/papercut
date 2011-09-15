import sbt._
import Keys._

// allows projects to be symlinked into the current directory for a direct dependency,
// or fall back to obtaining the project from Maven otherwise
class Locals (locals :(String, String, ModuleID)*) {
  def addDeps (p :Project) = (locals collect {
    case (id, subp, dep) if (file(id).exists) => symproj(file(id), subp)
  }).foldLeft(p) { _ dependsOn _ }
  def libDeps = locals collect {
    case (id, subp, dep) if (!file(id).exists) => dep
  }
  private def symproj (dir :File, subproj :String = null) =
    if (subproj == null) RootProject(dir) else ProjectRef(dir, subproj)
}

object PapercutBuild extends Build {
  val locals = new Locals(
    ("flashbang-playn", null, "com.threerings" % "flashbang-playn" % "1.0-SNAPSHOT")
  )

  lazy val papercut = locals.addDeps(Project(
    "papercut", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "papercut",
      crossPaths   := false,

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,

      autoScalaLibrary := false // no scala-library dependency
    )
  ))
}
