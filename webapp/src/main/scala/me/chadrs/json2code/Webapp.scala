package me.chadrs.json2code

import cats.implicits._
import io.circe.JsonObject
import io.circe.parser.parse
import me.chadrs.json2code.GenerateClass.{RenderSettings, generate}
import typings.ace.AceAjax.Editor
import typings.ace.ace


object Webapp {

  def main(args: Array[String]): Unit = {

    val jsonEditor = ace.edit("editor")
    jsonEditor.setTheme("ace/theme/monokai")
    jsonEditor.session.setMode("ace/mode/json")
    jsonEditor.getSession().setUseWrapMode(true)
    val output = ace.edit("output")
    output.setTheme("ace/theme/monokai")
    output.getSession().setUseWrapMode(true)
    output.setReadOnly(true)

    def showOutput(): Unit = transform(jsonEditor, output, convert)

    jsonEditor
      .getSession()
      .on("change", _ => showOutput())
    showOutput()
  }

  def transform(input: Editor,
                output: Editor,
                transform: String => Either[String, String]): Unit = {
    output.session.setValue(
      transform(input.session.getValue()) match {
        case Left(errorMsg) =>
          output.session.setMode("ace/mode/text")
          "error: " + errorMsg
        case Right(scalaCode) =>
          output.session.setMode("ace/mode/scala")
          scalaCode
      }
    )
  }

  def convert(input: String): Either[String, String] = {
    val settings = RenderSettings(generateCirceDecoders = true)
    parse(input)
      .flatMap(_.as[JsonObject])
      .leftMap(_.toString)
      .flatMap(obj => generate("Response", obj, settings))
  }

}
