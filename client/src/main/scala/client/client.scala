package client


import org.scalajs.dom
import org.scalajs.dom.ext.{Ajax, Color}
import org.scalajs.dom.html
import org.scalajs.dom.raw.Node
import shared.{School, SchoolClass, Student}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSGlobalScope}
import scala.util.Random
import scalatags.JsDom.all._

/**
  * Created by dr0l3 on 2/17/17.
  */

@js.native
@JSGlobalScope
object globalC extends js.Object{
  def dragula(containers: js.Array[Node], options: js.Object): Unit = js.native
}

@JSExport
object client{
  @JSExport
  def main(container: html.Div): Unit = {
    val schoolInput = input(list:="schools").render
    val classInput = input(list:="classes").render
    val numberOfGroupsInput = select().render
    val studentsInGroupsInput = select().render
    val studentDiv = div(id:="studentdiv").render
    var schools: Seq[School] = Seq()
    var classes: Seq[SchoolClass] = Seq()
    var students: Seq[Student] = Seq()
    var colors = Color.all

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
      }

    def addOptionsToNumberOfGroups() = {
      val numberOptions = for {
        number <- 1 to 8
      } yield option(value:=number.toString, number)
      numberOptions.foreach(option => println(option.toString()))
      numberOptions.foreach(option => numberOfGroupsInput.appendChild(option.render))
      numberOptions.foreach(option => studentsInGroupsInput.appendChild(option.render))
    }

    updateSchools()
    addOptionsToNumberOfGroups()

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

    numberOfGroupsInput.onchange = (_: dom.Event) => {
      studentDiv.innerHTML = ""
      //create the correct number of divs
      val uberDiv = div(id:="uberdiv").render
      var groupsDivs: List[html.Div] = Nil
      for {
        number <- 1 to numberOfGroupsInput.value.toInt
      } {
        println(colors.length)
        val chosenColor = randomColor(colors)

        colors = chosenColor._2
        println(colors.length)
        val temp = div(
          `class`:= "groupdiv",
          id := "group" + number.toString,
          width := "200",
          height := "200",
          backgroundColor := chosenColor._1)
        uberDiv.appendChild(temp.render)
        groupsDivs = temp.render :: groupsDivs
      }

      //divide the students
      var randomPermutation = Random.shuffle(students)
      for {
        i <- 1 to students.size
      }{
        val groupNumber = (i % numberOfGroupsInput.value.toInt) +1
        val groupDivId = "group"+groupNumber
        val assignedGroup = groupsDivs.find(g => g.id == groupDivId).get
        assignedGroup.appendChild(div(randomPermutation.head.firstName).render)
        randomPermutation = randomPermutation.drop(1)
      }

      uberDiv.innerHTML = ""
      for {
        gdiv <- groupsDivs
      } {
        uberDiv.appendChild(gdiv.render)
      }

      studentDiv.innerHTML = ""
      studentDiv.appendChild(uberDiv.render)
      import js.JSConverters._
      val elementList = dom.document.getElementsByClassName("groupdiv")
      val groupNodes = for {
        i <- 0 until elementList.length
      } yield elementList(i)
      globalC.dragula(groupNodes.toJSArray, Nil)
      colors = Color.all
    }

    studentsInGroupsInput.onchange = (_: dom.Event) => {
      println(studentsInGroupsInput.value)
    }

    container.appendChild(div(
      h1("Pick a school"),
      schoolInput,
      h1("Pick a class"),
      classInput,
      h1("Select the number of groups"),
      numberOfGroupsInput,
      h1("... or the number of people in a group"),
      studentsInGroupsInput,
      h1("Students in chosen class"),
      studentDiv
    ).render)
  }

  @JSExport
  def studentToShortenedName(student: Student): String = {
    student.firstName.split(" ").head + " " + student.secondName.charAt(0) + "."
  }

  @JSExport
  def randomColor(palette: Seq[Color]): (String, Seq[Color]) = {
    val randomColors = Random.shuffle(palette)
    (randomColors.head.toString(), palette.drop(1))
  }
}
