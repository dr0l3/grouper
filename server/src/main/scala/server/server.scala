package server

import java.net.URLClassLoader

import akka.actor.{Actor, ActorContext}
import net.ruippeixotog.scalascraper.model.Element
import shared.{School, SchoolClass, Student}
import spray.http.HttpEntity
import spray.http.MediaTypes._
import spray.json._
import spray.routing._


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
        respondWithMediaType(`text/html`){
          complete({
            HttpEntity(Page.skeleton.render)})
        }
      }
    } ~
    get {
      path("ajax" / "schools") {
        complete({
          import spray.json.DefaultJsonProtocol._
          implicit val teacherFormat = jsonFormat2(School)
          val schools = ProxyUtil.fetchSchools
          schools.toJson.prettyPrint})
      }
    } ~
    get {
      path("ajax" / "classes"){
        parameter("schoolId") {schoolId =>
          complete({
            import spray.json.DefaultJsonProtocol._
            implicit val classFormat = jsonFormat3(SchoolClass)
            val temp = ProxyUtil.fetchSchoolClasses(schoolId)
            temp.toJson.prettyPrint
          })
        }
      }
    } ~
    get{
      path("ajax" / "students"){
        parameters("classId", "schoolId") { (classId, schoolId) =>
          complete({
            import spray.json.DefaultJsonProtocol._
            implicit val studentFormat = jsonFormat2(Student)
            val temp = ProxyUtil.fetchStudents(classId, schoolId)
            temp.toJson.prettyPrint
          })
        }
      }
    } ~
    getFromResourceDirectory("public")
  }
}

object ProxyUtil {
  def fetchSchools: Seq[School] = {
    import net.ruippeixotog.scalascraper.browser.JsoupBrowser
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.model.Element
    val browser = JsoupBrowser()
    val doc = browser.get("http://www.lectio.dk/lectio/login_list.aspx")
    val schoolsLinks: List[Element] = doc >> elementList("#schoolsdiv > div > a")
    val schools: Seq[School] = schoolsLinks.map(link => School(link.text, getSchoolIdFromHref(link.attr("href"))))
    schools
  }

  def fetchSchoolClasses(schoolId: String): Seq[SchoolClass] = {
    import net.ruippeixotog.scalascraper.browser.JsoupBrowser
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.model.Element
    val browser = JsoupBrowser()
    val link = "http://www.lectio.dk/lectio/"+schoolId+"/FindSkema.aspx?type=stamklasse"
    val doc = browser.get(link)
    val elementsWithClasses: List[Element] = doc >> elementList("#m_Content_listecontainer > div > p > a")
    val schoolClasses: Seq[SchoolClass] = elementsWithClasses
      .map(ele => SchoolClass(ele.text, getClassIdFromHref(ele.attr("href")), getSchoolIdFromHref(ele.attr("href"))))
    schoolClasses
  }

  def getFirstName(row: Element): String = {
    //row.select(".printUpscaleFontFornavn > span a").head.text
    row.select("td > span > a").headOption.getOrElse(return "").text
  }

  def getSecondName(row: Element): String = {
    row.select("td:nth-child(3) > span").headOption.getOrElse(return "").text
  }


  def fetchStudents(classId: String, schoolId: String): Seq[Student] = {
    import net.ruippeixotog.scalascraper.browser.JsoupBrowser
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.model.Element
    val browser = JsoupBrowser()
    val link = "http://www.lectio.dk/lectio/"+schoolId+"/subnav/members.aspx?klasseid="+classId+"&showstudents=1"
    val doc = browser.get(link)
    val studentRows: List[Element] = doc >> elementList("#s_m_Content_Content_laerereleverpanel_alm_gv > tbody > tr")
    val students: Seq[Student] = studentRows
      .map(row => Student(getFirstName(row), getSecondName(row)))
      .filterNot(student => student.secondName == "" || student.firstName == "")
    students
  }

  def getSchoolIdFromHref(href: String) = {
    val link = href.split("/")
      .filterNot(part => part.isEmpty)
      .filter(part => isAllDigits(part))
    link.headOption.getOrElse("No Id found")
  }

  def getClassIdFromHref(href: String) = {
    val temp1: Array[String] = href.split("klasseid=")
    val classId: String = temp1.reverse.head
    classId
  }

  def isAllDigits(x: String) = x forall Character.isDigit
}