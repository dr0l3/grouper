package server

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

object Constants {
  def actorSystemName = "grouper-system"
  def actorServiceName = "grouper-service"
  def localhost = "0.0.0.0"
  def port = 8084
}

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem(Constants.actorSystemName)

  // create and start our service actor
  val service = system.actorOf(Props[server], Constants.actorServiceName)

  implicit val timeout = Timeout(5 seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = Constants.localhost, port = Constants.port)
}
