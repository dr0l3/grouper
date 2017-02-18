import sbt.Keys.{managedClasspath, _}
import sbt.Project.projectToRef

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedDependencies.value
  )
  .jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS = shared.js.settings(name := "sharedJS")

lazy val client : Project = (project in file("client"))
  .settings(
    name:= "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.scalajsDependencies.value,
    jsDependencies ++= Settings.jsDependencies.value,
    jsDependencies += RuntimeDOM % "test",
    skip in packageJSDependencies := false
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(sharedJS)


lazy val clients = Seq(client)

lazy val server: Project = (project in file ("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.jvmDependencies.value,
    mainClass in Compile := Some("server.Boot"),
    scalaJSProjects := clients,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    compile in Compile := {(compile in Compile) dependsOn scalaJSPipeline}.value,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .dependsOn(sharedJS, sharedJVM)
  .enablePlugins(SbtWeb, JavaAppPackaging)

//Revolver.settings

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value