import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Settings {
  val name = "grouper"

  val version = "1.0.0"

  val scalacOptions = Seq(
    "-Xlint",
    "-unchecked",
    "-deprecation",
    "-feature"
  )

  object versions {
    val scala = "2.11.8"
    val scalaDom = "0.9.1"
    val uTest = "0.4.4"
    val scalajsScripts = "1.0.0"
    val dragula = "3.7.2"
    val spray = "1.3.2"
    val scalaTags = "0.6.2"
    val upickle = "0.4.4"
    val bootstrap = "2.11.0"
    val scalaj = "2.3.0"
    val scalascraper = "1.2.0"
    val akka = "2.3.9"
  }

  val sharedDependencies = Def.setting(Seq(
    "com.lihaoyi"         %%% "scalatags"       % versions.scalaTags,
    "com.lihaoyi"         %%% "upickle"         % versions.upickle,
    "io.spray"            %% "spray-json"       % versions.spray
  ))

  val jvmDependencies = Def.setting(Seq(
    "io.spray"            %% "spray-can"        % versions.spray,
    "io.spray"            %% "spray-routing"    % versions.spray,
    "io.spray"            %% "spray-testkit"    % versions.spray % "test",
    "io.spray"            %% "spray-client"     % versions.spray,
    "io.spray"            %% "spray-json"       % versions.spray,
    "com.typesafe.akka"   %% "akka-actor"       % versions.akka,
    "com.typesafe.akka"   %% "akka-testkit"     % versions.akka % "test",
    "org.scalaj"          %% "scalaj-http"      % versions.scalaj,
    "net.ruippeixotog"    %% "scala-scraper"    % versions.scalascraper
  ))

  val scalajsDependencies = Def.setting(Seq(
    "org.scala-js"        %%% "scalajs-dom"     % versions.scalaDom,
    "com.lihaoyi"         %%% "utest"           % versions.uTest % Test
  ))

  val jsDependencies = Def.setting(Seq(
    "org.webjars.bower"   % "dragula"           % versions.dragula / "dist/dragula.js"
  ))
}
