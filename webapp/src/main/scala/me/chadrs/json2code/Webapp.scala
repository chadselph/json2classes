package me.chadrs.json2code

import cats.implicits._
import io.circe.parser.parse
import io.circe.{Json, JsonObject}
import me.chadrs.json2code.GenerateClass.{RenderSettings, generate}
import mhtml._
import org.scalajs.dom.document
import org.scalajs.dom.html.Input
import org.scalajs.dom.raw.Event
import typings.ace.ace
import typings.std.Infinity

import scala.scalajs.js

object Webapp {

  val sample = Json.obj(
    "name" -> Json.fromString("chad"),
    "age" -> Json.fromInt(100),
    "languages" -> Json.arr(
      Json.obj(
        "name" -> Json.fromString("scala"),
        "years" -> Json.fromInt(6),
        "functional" -> Json.True,
        "projects" -> Json.arr(Json.fromString("json2classes"), Json.fromString("akka-smpp"))
      ),
      Json.obj(
        "name" -> Json.fromString("elm"),
        "years" -> Json.fromInt(2),
        "functional" -> Json.True,
        "projects" -> Json.arr(Json.fromString("elm-yahtzee"))
      ),
      Json.obj(
        "name" -> Json.fromString("python"),
        "years" -> Json.fromInt(6),
        "functional" -> Json.False,
        "projects" -> Json.arr()
      )
    ),
    "favorite_players" -> Json.arr(
      Json.obj(
        "name" -> Json.fromString("Naomi Osaka"),
        "last_grand_slam" -> Json.obj(
          "tournament" -> Json.fromString("Australian Open"),
          "year" -> Json.fromInt(2019)
        )
      ),
      Json.obj(
        "name" -> Json.fromString("Nick Kyrgios"),
        "lastGrandSlam" -> Json.Null
      )
    )
  )

  def settingsView(encoders: Var[Boolean], decoders: Var[Boolean], packageName: Var[String], fieldNamesToCamel: Var[Boolean]) = {
    <div>
      <div>
        <label for="packageInput">Package name</label>
        <input id="packageInput" type="text" value={packageName}
               oninput={event: Event => packageName := event.target.asInstanceOf[Input].value } />
      </div>
      <div>
        { checkbox("Generate encoders", encoders) }
      </div>
      <div>
        { checkbox("Generate decoders", decoders) }
      </div>
      <div>
        { checkbox("Convert field names to camelCase", fieldNamesToCamel) }
      </div>
    </div>
  }

  def checkbox(label: String, value: Var[Boolean]) = {
    <div>
        <input type="checkbox" checked={value} onchange={e: js.Dynamic => value := e.target.checked.asInstanceOf[Boolean] } id={label} />
        <label for={label}>{label}</label>
      </div>
  }

  /**
    *
    */
  def main(args: Array[String]): Unit = {

    val jsonEditor = ace.edit("editor")
    val output = ace.edit("output")

    Seq(jsonEditor, output).foreach { e =>
      e.setTheme("ace/theme/monokai")
      e.$blockScrolling = Infinity
      e.getSession().setUseWrapMode(true)
    }
    jsonEditor.session.setMode("ace/mode/json")
    output.session.setMode("ace/mode/scala")
    output.setReadOnly(true)

    val inputCode = Var(sample.spaces2)
    jsonEditor.session.on("change", _ => inputCode := jsonEditor.session.getValue())
    jsonEditor.session.setValue(sample.spaces2)

    val encoders = Var(false)
    val decoders = Var(false)
    val packageName = Var("com.example")
    val convertFieldNamesToCamel = Var(true)

    val settings = for {
      dec <- decoders
      enc <- encoders
      packageN <- packageName
      camelField <- convertFieldNamesToCamel
    } yield
      RenderSettings(
        generateCirceDecoders = dec,
        generateCirceEncoders = enc,
        packageName = if (packageN.isEmpty) "com.example" else packageN,
        fieldNameFormatter = if (camelField) RenderSettings.snakeToCamel else identity

      )

    val outputCode = for {
      json <- inputCode
      s <- settings
    } yield {
      parse(json)
        .flatMap(_.as[JsonObject])
        .leftMap("Error: " + _.toString)
        .flatMap(obj => generate("Response", obj, s))
        .merge
    }
    outputCode.impure.run { scalaCode =>
      output.session.setValue(scalaCode)
    }

    mount(document.getElementById("checkboxes"), settingsView(encoders, decoders, packageName, convertFieldNamesToCamel))
  }

}
