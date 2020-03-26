package me.chadrs.json2code

import io.circe.{Json, JsonObject}

import scala.meta.Term.Param
import scala.meta.{Term, Type}
import scala.meta._
import cats.implicits._

object GenerateClass {

  type MappedFields = Vector[(String, JsonTypeTree)]
  object MappedFields {
    val empty: MappedFields = Vector()
    def apply(fields: (String, JsonTypeTree)*): MappedFields = fields.toVector
  }

  sealed trait JsonTypeTree
  sealed trait CanBeNullable extends JsonTypeTree
  sealed trait CanBeInArray extends JsonTypeTree
  sealed trait JsonTypeTreeEdge extends JsonTypeTree
  case class Nullable(t: CanBeNullable) extends JsonTypeTree
  case object StringType extends JsonTypeTree with CanBeNullable with CanBeInArray with JsonTypeTreeEdge
  case object NumericType extends JsonTypeTree with CanBeNullable with CanBeInArray with JsonTypeTreeEdge
  case object BooleanType extends JsonTypeTree with CanBeNullable with CanBeInArray with JsonTypeTreeEdge
  case object Null extends JsonTypeTree with CanBeInArray with JsonTypeTreeEdge
  case class NewType(fields: MappedFields) extends JsonTypeTree with CanBeNullable with CanBeInArray
  case class ArrayOf(of: CanBeInArray) extends JsonTypeTree

  case class ClassToGen(className: Type.Name, params: List[Param]) {
    def generate() = q"""case class $className (..$params)"""
  }

  def parse(input: JsonObject, topLevelName: String): Either[String, NewType] = {
    jsonObjToNewType(input)
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

      case (s, Null) => simpleParam(s, t"Nothing")
      case (s, Nullable(nt: NewType)) =>
        val t = Type.Name(s.capitalize)
         newTypeParam(s, nt, t"Option[$t]")
      case (s, Nullable(t: JsonTypeTreeEdge)) => simpleParam(s, t"Option[${edgeToType(t)}]")

      case (s, ArrayOf(edge: JsonTypeTreeEdge with CanBeInArray)) =>
        simpleParam(s, settings.arrayType(edgeToType(edge)))
      case (s, ArrayOf(nt: NewType)) =>
        val t = Type.Name(s.capitalize)
        newTypeParam(s, nt, settings.arrayType(t))
    }
    val params = paramsAndNewTypes.map(_._1)
    val newTypes = paramsAndNewTypes.flatMap(_._2)

    newTypes :+ ClassToGen(Type.Name(topLevelName), params.toList)
  }

  def isArrayHomogeneous(arr: Vector[Json]): Boolean =
    arr.headOption.fold(ifEmpty = true)(
      json => json.fold(
        arr.forall(_.isNull),
        _ => arr.forall(_.isBoolean),
        _ => arr.forall(_.isNumber),
        _ => arr.forall(_.isString),
        _ => arr.forall(_.isArray),
        _ => arr.forall(_.isObject))
    )

  def jsonTypeToType(item: Json): Either[String, JsonTypeTree] = {
    item.fold(
      Right(Null),
      _ => Right(BooleanType),
      _ => Right(NumericType),
      _ => Right(StringType),
      jsonArrayToType,
      jsonObjToNewType
    )
  }

  def jsonArrayToType(arr: Vector[Json]): Either[String, JsonTypeTree] = {
    if(arr.forall(_.isObject)) unifyObjList(arr.map(_.asObject.get)).map(mfs => ArrayOf(NewType(mfs)))
    else if (arr.isEmpty) Right(Null)
    else if (isArrayHomogeneous(arr)) jsonTypeToType(arr.head).flatMap {
      case canBeInArray: CanBeInArray => Right(ArrayOf(canBeInArray))
      case other => Left(s"$other cannot be in an array") // limit what can be in array for sake of simplicity
    }
    else Left(s"error: Array has different types. ${Json.fromValues(arr).noSpaces}")
  }

  def jsonObjToNewType(obj: JsonObject): Either[String, NewType] = {
    val keysWithTypes = obj.toMap.toVector.map { case (key, value) => jsonTypeToType(value).map(key -> _) }.sequence
    keysWithTypes.map(NewType)
  }

  def unifyObjList(input: Vector[JsonObject]): Either[String, MappedFields] = {
    val end = input.foldLeft(MappedFields.empty.asRight[String]) {
      case (Right(init), obj) if init.isEmpty =>
        jsonObjToNewType(obj).map(_.fields)
      case (Right(existingFields), obj) =>
        jsonObjToNewType(obj).flatMap { mapped =>
          unifyNewTypes(existingFields, mapped.fields)
        }
      // compare this object with other keys
      case (Left(error), _) => Left(error) // if we want to collect errors, here's the place to update
    }
    end.map(_.toVector)
  }

  def unifyNewTypes(a: MappedFields, b: MappedFields): Either[String, MappedFields] = {
    val objMap = b.toMap
    val newKeys = objMap.keySet -- a.view.map(_._1).to(Set)
    val unifiedParams = a.map { case (key, existingType) =>
      val thisType = objMap.getOrElse(key, Null)
      unifyTypes(existingType, thisType).map(key -> _)
    }.sequence
    // any param being introduced here is optional so unify it with Null
    val newParams = newKeys.toVector.map(key => unifyTypes(Null, objMap(key)).map(key -> _)).sequence
    unifiedParams |+| newParams
  }

  def unifyTypes(a: JsonTypeTree, b: JsonTypeTree): Either[String, JsonTypeTree] = {
    def nullify(stm: JsonTypeTree): JsonTypeTree = stm match {
      case s: CanBeNullable => Nullable(s)
      case _ => stm // shouldn't ever get here
    }
    (a, b) match {
      case (a, b) if a == b => Right(a)
      case (Nullable(ta), Nullable(tb)) => unifyTypes(ta, tb).map(nullify)
      case (Nullable(ta), tb: CanBeNullable) => unifyTypes(ta, tb).map(nullify)
      case (ta: CanBeNullable, Nullable(tb)) => unifyTypes(ta, tb).map(nullify)
      case (Nullable(ta), Null) => Right(Nullable(ta))
      case (Null, Nullable(tb)) => Right(Nullable(tb))
      case (Null, cbn: CanBeNullable) => Right(Nullable(cbn))
      case (cbn: CanBeNullable, Null) => Right(Nullable(cbn))
      case (NewType(aFields), NewType(bFields)) => unifyNewTypes(aFields, bFields).map(NewType)
      // TODO: Arrays?
      case _ => Left(s"Can't unify $a and $b yet.")
    }

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
  // TODO: scalajs-ify
  // TODO: refactor :|
  // TODO: settings for which types get mapped, unify numbers/strings
  // TODO: circe encoders / decoders
  // TODO: empty object {}
  // TODO: unify arbitrary depth

}
