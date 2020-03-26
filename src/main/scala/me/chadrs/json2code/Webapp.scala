package me.chadrs.json2code
import io.circe.JsonObject
import io.circe.parser.parse
import me.chadrs.json2code.GenerateClass.generate
import org.scalajs.dom.document
import cats.implicits._

import scala.scalajs.js.annotation.JSExportTopLevel


object Webapp {

  def main(args: Array[String]): Unit = {
    document.getElementById("editor")
  }

  @JSExportTopLevel("convert")
  def convert(input: String): String = {
    parse(input)
      .flatMap(_.as[JsonObject])
      .leftMap(_.toString)
      .map(obj => generate("Response", obj))
      .fold("error: " + _, identity)
  }


}
