package client


import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.raw.Element
import shared.{School, SchoolClass, Student}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSGlobalScope}
import scalatags.JsDom.all._

/**
  * Created by dr0l3 on 2/17/17.
  */

@js.native
@JSGlobalScope
object globalC extends js.Object{
  def dragula(containers: js.Array[Element], options: js.Object): Unit = js.native
}

@JSExport
object client{
  @JSExport
  def main(container: html.Div): Unit = {
    val schoolInput = input(list:="schools").render
    val classInput = input(list:="classes").render
    val studentDiv = div(id:="studentdiv").render
    var schools: Seq[School] = Seq()
    var classes: Seq[SchoolClass] = Seq()
    var students: Seq[Student] = Seq()

    def updateSchools() = Ajax.get("/ajax/schools").foreach { xhr =>
      schools = upickle.default.read[Seq[School]](xhr.responseText)
      val schoolOptions = for {
        school <- schools
      } yield option(value:=school.name)
      schoolInput.appendChild(datalist(id:="schools", schoolOptions).render)
    }

    def updateClasses(school: School) = Ajax.get("/ajax/classes?schoolId="+school.schoolId).foreach { xhr =>
      classes = upickle.default.read[Seq[SchoolClass]](xhr.responseText)
      val classOptions = for {
        clazz <- classes
      } yield option(value:=clazz.name)
      classInput.appendChild(datalist(id:="classes", classOptions).render)
    }

    def updateStudents(schoolClass: SchoolClass) =
      Ajax.get("/ajax/students?classId=" +schoolClass.classId + "&schoolId="+ schoolClass.schoolId).foreach {xhr =>
        students = upickle.default.read[Seq[Student]](xhr.responseText)
        val studentDivs = for {
          student <- students
        } yield div(studentToShortenedName(student))
        studentDivs.foreach(sDiv => studentDiv.appendChild(sDiv.render))
        import js.JSConverters._
        globalC.dragula(Seq(dom.document.getElementById("studentdiv")).toJSArray, Nil)
      }

    updateSchools()

    schoolInput.onselect = (e: dom.Event) => {
      val selectedSchoolName = schoolInput.value
      val selectedSchool = schools.find(school => school.name == selectedSchoolName)
      println(selectedSchool.getOrElse(School("Not found", "Not found").schoolId))
      updateClasses(selectedSchool.getOrElse(School("No School found", "No id for non-existing school")))
    }

    classInput.onselect = (_: dom.Event) => {
      val selectedClassName = classInput.value
      val selectedClass = classes.find(clazz => clazz.name == selectedClassName)
      println(selectedClass.getOrElse(SchoolClass("No class found", "no id", "no id")))
      updateStudents(selectedClass.getOrElse(SchoolClass("No class found", "no id", "no id")))
    }

    container.appendChild(div(
      h1("Pick a school"),
      schoolInput,
      h1("Pick a class"),
      classInput,
      h1("Students in chosen class"),
      studentDiv
    ).render)
  }

  @JSExport
  def studentToShortenedName(student: Student): String = {
    student.firstName.split(" ").head + " " + student.secondName.charAt(0) + "."
  }
}
