package me.chadrs

import io.circe.JsonObject
import io.circe.Json

import scala.meta.Term.Param
import scala.meta._
import cats.implicits._


object GenerateClass {

  type MappedFields = Vector[(String, ScalaTypeMapping)]
  object MappedFields {
    val empty: MappedFields = Vector()
  }
  
  sealed trait ScalaTypeMapping
  sealed trait CanBeNullable extends ScalaTypeMapping
  sealed trait CanBeInArray extends ScalaTypeMapping
  case class Nullable(t: CanBeNullable) extends ScalaTypeMapping
  case class MapToExisting(t: Type) extends ScalaTypeMapping with CanBeNullable with CanBeInArray {
    override def equals(lhs: Any): Boolean = lhs match {
      case MapToExisting(t2) => t2.toString() == t.toString() // temp hack to make equals work
      case _ => false
    }
  }
  case object Null extends ScalaTypeMapping with CanBeInArray
  case class NewType(fields: MappedFields) extends ScalaTypeMapping with CanBeNullable with CanBeInArray
  case class ArrayOf(of: CanBeInArray) extends ScalaTypeMapping

  case class ClassToGen(className: Type.Name, params: List[Param]) {
    def generate() = q"""case class $className (..$params)"""
  }

  def parse(input: JsonObject, topLevelName: String): Either[String, NewType] = {
    jsonObjToNewType(input)
  }

  def render(topLevelName: String, newType: NewType): Vector[ClassToGen] = {
    def simpleParam(name: String, t: Type) =
      (Param(Nil, Term.Name(name), Some(t), None), Vector.empty)
    def newTypeParam(name: String, nt: NewType, t: Type) =
      (Param(Nil, Term.Name(name), Some(t), None), render(name.capitalize, nt))

    val paramsAndNewTypes: Vector[(Param, Vector[ClassToGen])] = newType.fields.map {
      case (s, MapToExisting(t)) => simpleParam(s, t)
      case (s, Null) => simpleParam(s, t"Nothing")
      case (s, Nullable(nt: NewType)) =>
        val t = Type.Name(s.capitalize)
         newTypeParam(s, nt, t"Option[$t]")
      case (s, Nullable(MapToExisting(t))) => simpleParam(s, t"Option[$t]")

      case (s, nt: NewType) =>
        newTypeParam(s, nt, Type.Name(s.capitalize))

      case (s, ArrayOf(MapToExisting(t))) => simpleParam(s, t"Vector[$t]")
      case (s, ArrayOf(Null)) => simpleParam(s, t"Vector[Nothing]")
      case (s, ArrayOf(nt: NewType)) =>
        val t = Type.Name(s.capitalize)
        newTypeParam(s, nt, t"Vector[$t]")
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

  def jsonTypeToType(item: Json): Either[String, ScalaTypeMapping] = {
    item.fold(
      Right(Null),
      _ => Right(MapToExisting(t"Boolean")),
      _ => Right(MapToExisting(t"Double")),
      _ => Right(MapToExisting(t"String")),
      jsonArrayToType,
      jsonObjToNewType
    )
  }

  def jsonArrayToType(arr: Vector[Json]): Either[String, ScalaTypeMapping] = {
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
      unifyTypes(thisType, existingType).map(key -> _)
    }.sequence
    // any param being introduced here is optional so unify it with Null
    val newParams = newKeys.toVector.map(key => unifyTypes(Null, objMap(key)).map(key -> _)).sequence
    unifiedParams |+| newParams
  }

  def unifyTypes(a: ScalaTypeMapping, b: ScalaTypeMapping): Either[String, ScalaTypeMapping] = {
    def nullify(stm: ScalaTypeMapping): ScalaTypeMapping = stm match {
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
      case _ => Left(s"Can't unify $a and $b yet.")
    }

  }

  def generate(className: String, input: JsonObject): String = {
    parse(input, className).fold(identity, { nt =>
      val classes = render(className, nt)
      q"""package com.test {
          ..${classes.toList.map(_.generate())}
           }""".syntax
    })
  }

  def main(args: Array[String]): Unit = {
    println(generate(
      "SomeClass", JsonObject(
        "name" -> Json.fromString("chad"),
        "integer" -> Json.fromInt(123),
        "arr" -> Json.arr(Json.fromInt(10), Json.fromInt(40)),
        "nested" -> Json.obj("field4" -> Json.fromInt(13), "field2" -> Json.obj("field22" -> Json.obj("field33" -> Json.fromInt(9)))),
        "arrOfObj" -> Json.arr(
          Json.obj("kittens" -> Json.fromInt(5), "puppies" -> Json.fromInt(10)),
          Json.obj("kittens" -> Json.Null, "puppies" -> Json.fromInt(101)),
          Json.obj("puppies" -> Json.fromInt(40)),
          Json.obj("puppies" -> Json.fromInt(40), "type" -> Json.obj("cubType" -> Json.fromString("bear"))),
          Json.obj("puppies" -> Json.fromInt(40), "type" -> Json.obj("cubType" -> Json.fromString("tiger"), "count" -> Json.fromInt(4))),
        )
      )))

  }

  // TODO: camelCase snake_case conversion
  // TODO: scalajs-ify
  // TODO: unit tests
  // TODO: refactor :|
  // TODO: guess number type
  // TODO: settings for which types get mapped
  // TODO: circe encoders / decoders

}
