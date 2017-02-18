package client


import org.scalajs.dom.html

import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

/**
  * Created by dr0l3 on 2/17/17.
  */
@JSExport
object client{
  @JSExport
  def main(container: html.Div): Unit = {
    container.appendChild(
      h1("hello world with brand new update").render)
  }
}
