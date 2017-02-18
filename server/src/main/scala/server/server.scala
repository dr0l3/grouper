package server

import java.net.URLClassLoader

import akka.actor.{Actor, ActorContext}
import shared.Teacher
import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import shared.TeacherJsonProtocol.JsonFormat

class server extends Actor with GrouperServer{
  def actorRefFactory: ActorContext = context

  def receive: Receive = runRoute(routes)
}

trait GrouperServer extends HttpService{
  def routes: Route = {
    val cl = ClassLoader.getSystemClassLoader
    val urls = cl.asInstanceOf[URLClassLoader].getURLs
    urls.foreach(url => if (!url.getFile.contains(".ivy")) println(url.getFile))
    get{
      path("") {
        println("got request")
        respondWithMediaType(`text/html`){
          complete (HttpEntity(Page.skeleton.render))
        }
      }
    } ~
    get {
      path("ajax" / "students") {
        parameter("classId") { query =>
          complete (Teacher("name").toJson.prettyPrint)
        }
      }
    } ~
    getFromResourceDirectory("public")
  }
}