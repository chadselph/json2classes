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
    def simpleParam(name: String, t: Type) =
      (Param(Nil, Term.Name(name), Some(t), None), Vector.empty)
    def newTypeParam(name: String, nt: NewType, t: Type) =
      (Param(Nil, Term.Name(name), Some(t), None), render(name.capitalize, nt, settings))
    def edgeToType(edge: JsonTypeTreeEdge): Type = edge match {
      case StringType => settings.stringType
      case NumericType => settings.numericType
      case BooleanType => settings.booleanType
      case Null => t"Nothing"
    }

    val paramsAndNewTypes: Vector[(Param, Vector[ClassToGen])] = newType.fields.map {
      case (s, edge: JsonTypeTreeEdge) => simpleParam(s, edgeToType(edge))
      case (s, nt: NewType) =>
        newTypeParam(s, nt, Type.Name(s.capitalize))

      case (s, Nullable(nt: NewType)) =>
        val t = Type.Name(s.capitalize)
        newTypeParam(s, nt, t"Option[$t]")
      case (s, Nullable(t: JsonTypeTreeEdge)) => simpleParam(s, t"Option[${edgeToType(t)}]")

      case (s, ArrayOf(edge: JsonTypeTreeEdge with CanBeInArray)) =>
        simpleParam(s, settings.arrayType(edgeToType(edge)))
      case (s, ArrayOf(nt: NewType)) =>
        val t = Type.Name(s.capitalize)
        newTypeParam(s, nt, settings.arrayType(t))
      case (s, EmptyArray) =>
        val t = t"Nothing"
        simpleParam(s, settings.arrayType(t))
    }
    val params = paramsAndNewTypes.map(_._1)
    val newTypes = paramsAndNewTypes.flatMap(_._2)

    newTypes :+ ClassToGen(Type.Name(topLevelName), params.toList)
  }

  def generate(className: String, input: JsonObject): String = {
    parse(input, className).fold(identity, { nt =>
      val classes = render(className, nt, RenderSettings())
      q"""package com.test {
          ..${classes.toList.map(_.generate())}
           }""".syntax
    })
  }

  case class RenderSettings(stringType: Type = t"String",
                            booleanType: Type = t"Boolean",
                            numericType: Type = t"BigDecimal",
                            arrayType: Type => Type = (t: Type) => t"Vector[$t]",
                            fieldNameFormatter: String => String = identity,
                            generateCirceDecoders: Boolean = false,
                            generateCirceEncoders: Boolean = false,
                            packageName: String = "com.test")

  // TODO: camelCase snake_case conversion
  // TODO: settings for which types get mapped, unify numbers/strings
  // TODO: circe encoders / decoders
  // TODO: unify arbitrary depth?

}
