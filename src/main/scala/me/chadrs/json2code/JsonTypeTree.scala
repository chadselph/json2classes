package me.chadrs.json2code

import cats.Show
import cats.implicits._
import io.circe.{Json, JsonObject}

sealed trait JsonTypeTree

object JsonTypeTree {

  type MappedFields = Vector[(String, JsonTypeTree)]
  object MappedFields {
    val empty: MappedFields = Vector()
    def apply(fields: (String, JsonTypeTree)*): MappedFields = fields.toVector
  }


  sealed trait CanBeNullable extends JsonTypeTree
  sealed trait CanBeInArray extends JsonTypeTree
  sealed trait JsonTypeTreeEdge extends JsonTypeTree
  case class Nullable(t: JsonTypeTree with CanBeNullable) extends JsonTypeTree
  case object StringType extends JsonTypeTree with CanBeNullable with CanBeInArray with JsonTypeTreeEdge
  case object NumericType extends JsonTypeTree with CanBeNullable with CanBeInArray with JsonTypeTreeEdge
  case object BooleanType extends JsonTypeTree with CanBeNullable with CanBeInArray with JsonTypeTreeEdge
  case object Null extends JsonTypeTree with JsonTypeTreeEdge
  case object EmptyArray extends JsonTypeTree
  case class NewType(fields: MappedFields) extends JsonTypeTree with CanBeNullable with CanBeInArray
  case class ArrayOf(of: JsonTypeTree with CanBeInArray) extends JsonTypeTree

  implicit val showTree: Show[JsonTypeTree] = Show.show[JsonTypeTree] {
    case Nullable(t) => s"${(t: JsonTypeTree).show}?"
    case NewType(fields) => fields.map({ case (key, tree: JsonTypeTree) => s"${qs(key)} -> ${tree.show}" }).mkString("{", ", ", "}")
    case ArrayOf(of: JsonTypeTree) => s"[${(of: JsonTypeTree).show}]"
    case other => other.toString
  }
  
  def parse(input: JsonObject, topLevelName: String): Either[String, NewType] = {
    jsonObjToNewType(input)
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
    if (arr.isEmpty) Right(EmptyArray)
    else if(arr.forall(_.isObject)) unifyObjList(arr.map(_.asObject.get)).map(mfs => ArrayOf(NewType(mfs)))
    else if (isArrayHomogeneous(arr)) jsonTypeToType(arr.head).flatMap {
      case canBeInArray: CanBeInArray => Right(ArrayOf(canBeInArray))
      case other => Left(s"${other.show} cannot be in an array") // limit what can be in array for sake of simplicity
    }
    else Left(s"error: Array has different types. ${Json.fromValues(arr).noSpaces}")
  }

  def jsonObjToNewType(obj: JsonObject): Either[String, NewType] = {
    val keysWithTypes = obj.toVector.map { case (key, value) => jsonTypeToType(value).map(key -> _) }.sequence
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
      case (EmptyArray, ArrayOf(a)) => Right(ArrayOf(a))
      case (ArrayOf(a), EmptyArray) => Right(ArrayOf(a))
      case (ArrayOf(a), ArrayOf(b)) => unifyTypes(a, b).flatMap {
        case unified: CanBeInArray => Right(ArrayOf(unified))
        case not => Left(s"Can't have an ArrayOf ${not.show}")
      }
      case _ => Left(s"Can't unify ${a.show} and ${b.show}.")
    }

  }

  // workaround for https://github.com/scala/bug/issues/6476
  private def qs(s: String): String = s""""$s""""


}
