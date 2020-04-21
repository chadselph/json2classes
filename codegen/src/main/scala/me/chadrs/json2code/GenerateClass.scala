package me.chadrs.json2code

import io.circe.JsonObject
import me.chadrs.json2code.JsonTypeTree._

import scala.meta.Term.Param
import scala.meta.{Term, Type, _}

object GenerateClass {

  case class ClassToGen(className: Type.Name, params: List[Param]) {
    def generate() = q"""case class $className (..$params)"""
  }

  def render(topLevelName: String,
             newType: NewType,
             settings: RenderSettings): Vector[ClassToGen] = {
    def fieldName(s: String) = Term.Name(settings.fieldNameFormatter(s))

    def simpleParam(name: String, t: Type) =
      (Param(Nil, fieldName(name), Some(t), None), Vector.empty)

    def newTypeParam(name: String, nt: NewType, t: Type) =
      (Param(Nil, fieldName(name), Some(t), None),
        render(settings.classNameFormatter(name), nt, settings))

    def edgeToType(edge: JsonTypeTreeEdge): Type = edge match {
      case StringType => settings.stringType
      case NumericType => settings.numericType
      case BooleanType => settings.booleanType
      case Null => t"Nothing"
    }

    val paramsAndNewTypes: Vector[(Param, Vector[ClassToGen])] =
      newType.fields.map {
        case (s, edge: JsonTypeTreeEdge) => simpleParam(s, edgeToType(edge))
        case (s, nt: NewType) =>
          newTypeParam(s, nt, Type.Name(settings.classNameFormatter(s)))

        case (s, Nullable(nt: NewType)) =>
          val t = Type.Name(settings.classNameFormatter(s))
          newTypeParam(s, nt, t"Option[$t]")
        case (s, Nullable(t: JsonTypeTreeEdge)) =>
          simpleParam(s, t"Option[${edgeToType(t)}]")

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

  private def traverseNewType(name: String, nt: NewType, settings: RenderSettings): Seq[(Type.Name, NewType)] = {
    nt.fields.flatMap {
      case (key, ntt: NewType) =>
        traverseNewType(key, ntt, settings)
      case (key, ArrayOf(ntt: NewType)) =>
        traverseNewType(key, ntt, settings)
      case (key, Nullable(ntt: NewType)) =>
        traverseNewType(key, ntt, settings)
      case _ =>
        Seq()
    } :+ (Type.Name(settings.classNameFormatter(name)), nt)
  }

  private def renderEncodersDecoders(topLevelName: String,
                                     topNewType: NewType,
                                     settings: RenderSettings): Seq[Defn] = {
    val newTypes = traverseNewType(topLevelName, topNewType, settings)
    val dec = (renderDecoder _).tupled
    val enc = (renderEncoder(_, _, settings)).tupled
    val (genDec, genEnc) = (settings.generateCirceDecoders, settings.generateCirceEncoders)
    newTypes.filter(_ => genDec).map(dec) ++ newTypes.filter(_ => genEnc).map(enc)
  }

  private def renderDecoder(className: Type.Name, nt: NewType): Defn = {
    val decoderName = Term.Name(s"decode$className")
    val decoderType = Type.Apply(t"Decoder", List(className))
    val forProductN = applyForProductN(nt.fields, Term.Name("Decoder"))
    val decoder =
      Term.Apply(forProductN, List(Term.Select(Term.Name(className.value), Term.Name("apply"))))
    Defn
      .Val(List(Mod.Implicit()), List(Pat.Var(decoderName)), Some(decoderType), decoder)
  }

  private def renderEncoder(topLevelName: Type.Name,
                            nt: NewType,
                            settings: RenderSettings): Defn = {
    val encoderName = Term.Name(s"encode$topLevelName")
    val encoderType = Type.Apply(t"Encoder", List(topLevelName))

    def xDot(fieldName: String) = Term.Select(Term.Name("x"), Term.Name(fieldName))

    val paramX = Param(Nil, Term.Name("x"), None, None)
    val forProductN = applyForProductN(nt.fields, Term.Name("Encoder"))
    val encoder =
      Term.Apply(forProductN,
        List(
          Term.Function(List(paramX),
            Term.Tuple(
              nt.fields.map {
                case (key, _) => xDot(settings.fieldNameFormatter(key))
              }.toList
            ))))
    Defn
      .Val(List(Mod.Implicit()), List(Pat.Var(encoderName)), Some(encoderType), encoder)
  }

  private def applyForProductN(fields: MappedFields, selectOn: Term.Name): Term.Apply = {
    Term.Apply(
      Term.Select(selectOn, Term.Name(s"forProduct${fields.size}")),
      fields.map(_._1).map(Lit.String(_)).toList
    )
  }

  def generate(className: String,
               input: JsonObject,
               renderSettings: RenderSettings = RenderSettings()): Either[String, String] = {
    parse(input, className).map { nt =>
      val classes = render(className, nt, renderSettings).toList.map(_.generate())
      val packageTerm = renderSettings.packageName
        .split('.')
        .map(Term.Name.apply)
        .reduceLeft(Term.Select.apply)
      if (renderSettings.generateCirceEncoders || renderSettings.generateCirceDecoders)
        q"""package $packageTerm {
          ..$classes
          object Json {
          import io.circe.{ Decoder, Encoder }
            ..${renderEncodersDecoders(className, nt, renderSettings).toList}
          } }""".syntax
      else
        q"""package $packageTerm {
          ..$classes
          }
          """.syntax
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

}
