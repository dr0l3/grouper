package server

import scalatags.Text.all._

object Page{
  val boot =
    "client.client().main(document.getElementById('contents'))"
  val skeleton =
    html(
      head(
        script(src:="client-fastopt.js"),
        script(src:="client-jsdeps.js"),
        link(
          rel:="stylesheet",
          href:="https://cdnjs.cloudflare.com/ajax/libs/pure/0.5.0/pure-min.css"
        ),
        link(
          rel:="stylesheet",
          href:="https://cdnjs.cloudflare.com/ajax/libs/dragula/3.7.2/dragula.css"
        )
      ),
      body(

        onload:=boot,
        div(id:="contents")
      )
    )
}
