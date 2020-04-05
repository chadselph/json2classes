package me.chadrs.json2code
import io.circe.JsonObject
import io.circe.parser.parse
import me.chadrs.json2code.GenerateClass.generate
import cats.implicits._
import typings.ace.AceAjax.Editor
import typings.ace.ace

import scala.scalajs.js.annotation.JSExportTopLevel

object Webapp {

  def main(args: Array[String]): Unit = {

    val jsonEditor = ace.edit("editor")
    val output = ace.edit("output")

    def showOutput(): Unit = transform(jsonEditor, output, convert)

    jsonEditor.setTheme("ace/theme/monokai")
    jsonEditor.session.setMode("ace/mode/json")
    jsonEditor.getSession().setUseWrapMode(true)
    jsonEditor
      .getSession()
      .on("change", _ => showOutput())
    output.setTheme("ace/theme/monokai")
    output.session.setMode("ace/mode/scala")
    output.getSession().setUseWrapMode(true)
    output.setReadOnly(true)
    showOutput()
  }

  def transform(input: Editor,
                output: Editor,
                transform: String => String): Unit = {
    output.session.setValue(transform(input.session.getValue()))
  }

  def convert(input: String): String = {
    parse(input)
      .flatMap(_.as[JsonObject])
      .leftMap(_.toString)
      .map(obj => generate("Response", obj))
      .fold("error: " + _, identity)
  }

}
