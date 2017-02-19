package shared

import spray.json.{DefaultJsonProtocol, RootJsonFormat}


/**
  * Created by dr0l3 on 2/17/17.
  */
case class Teacher(name: String)

object TeacherJsonProtocol extends DefaultJsonProtocol {
  implicit val JsonFormat: RootJsonFormat[Teacher] = jsonFormat1(Teacher)
}

case class School(name: String, schoolId: String)
case class Student(firstName: String, secondName: String)
case class SchoolClass(name: String, classId: String, schoolId: String)