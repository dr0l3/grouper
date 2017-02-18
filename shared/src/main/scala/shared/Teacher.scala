package shared

import spray.json.{DefaultJsonProtocol, RootJsonFormat}


/**
  * Created by dr0l3 on 2/17/17.
  */
case class Teacher(name: String)

object TeacherJsonProtocol extends DefaultJsonProtocol {
  implicit val JsonFormat: RootJsonFormat[Teacher] = jsonFormat1(Teacher)
}

