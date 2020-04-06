package me.chadrs.json2code

import io.circe.JsonObject
import me.chadrs.json2code.JsonTypeTree._

import scala.meta.Term.Param
import scala.meta.{Term, Type, _}

object GenerateClass {


  case class ClassToGen(className: Type.Name, params: List[Param]) {
    def generate() = q"""case class $className (..$params)"""
  }

  def render(topLevelName: String, newType: NewType, settings: RenderSettings): Vector[ClassToGen] = {
    def fieldName(s: String) = Term.Name(settings.fieldNameFormatter(s))
    def simpleParam(name: String, t: Type) =
      (Param(Nil, fieldName(name), Some(t), None), Vector.empty)
    def newTypeParam(name: String, nt: NewType, t: Type) =
      (Param(Nil, fieldName(name), Some(t), None), render(settings.classNameFormatter(name), nt, settings))
    def edgeToType(edge: JsonTypeTreeEdge): Type = edge match {
      case StringType => settings.stringType
      case NumericType => settings.numericType
      case BooleanType => settings.booleanType
      case Null => t"Nothing"
    }

    val paramsAndNewTypes: Vector[(Param, Vector[ClassToGen])] = newType.fields.map {
      case (s, edge: JsonTypeTreeEdge) => simpleParam(s, edgeToType(edge))
      case (s, nt: NewType) =>
        newTypeParam(s, nt, Type.Name(settings.classNameFormatter(s)))

      case (s, Nullable(nt: NewType)) =>
        val t = Type.Name(settings.classNameFormatter(s))
        newTypeParam(s, nt, t"Option[$t]")
      case (s, Nullable(t: JsonTypeTreeEdge)) => simpleParam(s, t"Option[${edgeToType(t)}]")

      case (s, ArrayOf(edge: JsonTypeTreeEdge with CanBeInArray)) =>
        simpleParam(s, settings.arrayType(edgeToType(edge)))
      case (s, ArrayOf(nt: NewType)) =>
        val t = Type.Name(settings.classNameFormatter(s))
        newTypeParam(s, nt, settings.arrayType(t))
      case (s, EmptyArray) =>
        val t = t"Nothing"
        simpleParam(s, settings.arrayType(t))
    }
    val params = paramsAndNewTypes.map(_._1)
    val newTypes = paramsAndNewTypes.flatMap(_._2)

    newTypes :+ ClassToGen(Type.Name(topLevelName), params.toList)
  }

  def generate(className: String, input: JsonObject, renderSettings: RenderSettings = RenderSettings()): Either[String, String] = {
    parse(input, className).map { nt =>
      val classes = render(className, nt, renderSettings)
      val packageTerm = renderSettings.packageName.split('.').map(Term.Name.apply).reduceLeft(Term.Select.apply)
      q"""package $packageTerm {
          ..${classes.toList.map(_.generate())}
           }""".syntax
    }
  }

  case class RenderSettings(stringType: Type = t"String",
                            booleanType: Type = t"Boolean",
                            numericType: Type = t"BigDecimal",
                            arrayType: Type => Type = RenderSettings.vectorOf,
                            fieldNameFormatter: String => String = RenderSettings.snakeToCamel,
                            classNameFormatter: String => String = RenderSettings.snakeToPascal,
                            generateCirceDecoders: Boolean = false,
                            generateCirceEncoders: Boolean = false,
                            packageName: String = "com.example")

  object RenderSettings {

    val vectorOf: Type => Type = (t: Type) => t"Vector[$t]"

    val snakeToCamel: String => String = (input) => {
      val parts = input.split("_")
      parts.head + parts.tail.map(_.capitalize).mkString
    }
    val snakeToPascal: String => String = (input) => {
      input.split("_").map(_.capitalize).mkString
    }
  }

  // TODO: circe encoders / decoders

}
