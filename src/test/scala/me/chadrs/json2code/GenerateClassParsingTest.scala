package me.chadrs.json2code

import io.circe.{Json, JsonObject}
import me.chadrs.json2code.GenerateClass.{ArrayOf, BooleanType, JsonTypeTree, NewType, Null, Nullable, NumericType, StringType}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers


class GenerateClassParsingTest extends org.scalatest.funspec.AnyFunSpec with Matchers {

  protected def parseJson(json: (String, Json)*): Either[String, NewType] = GenerateClass.parse(JsonObject(json: _*), "MyClass")

  protected implicit class ParseResultShouldHelpers(val result: Either[String, NewType]) {
    def shouldHaveFields(fields: (String, JsonTypeTree)*): Assertion = {
      result.map(_.fields) shouldBe Right(fields.toVector)
    }
    def shouldFail: Assertion = assert(result.isLeft)
    def shouldFailWithString(error: String): Assertion = result shouldBe Left(error)
  }

  describe("parse from JsonObject") {
    describe("MapToExisting") {
      it("should map json strings to scala strings") {
        parseJson("someString" -> Json.fromString("")) shouldHaveFields ("someString" -> StringType)
      }
      it("should map json numbers to scala BigDouble") {
        parseJson("someDub" -> Json.fromInt(3)) shouldHaveFields ("someDub" -> NumericType)
      }
      it("should map json booleans to scala boolean") {
        parseJson("b" -> Json.False) shouldHaveFields ("b" -> BooleanType)
      }
      it("should map json null to scala null") {
        parseJson("b" -> Json.Null) shouldHaveFields ("b" -> Null)
      }
    }

    describe("ArrayOf") {
      it("should work with existing types") {
        parseJson("a" -> Json.arr(Json.False, Json.True, Json.False)) shouldHaveFields(
          "a" -> ArrayOf(BooleanType)
        )
      }

      it("should work with homogeneous new types") {
        parseJson("a" -> Json.arr(
          Json.obj("id" -> Json.fromInt(1), "name" -> Json.fromString("Jo")),
          Json.obj("id" -> Json.fromInt(2), "name" -> Json.fromString("Mo"))),
        ) shouldHaveFields(
          "a" -> ArrayOf(NewType(Vector(
            "id" -> NumericType, "name" -> StringType)))
          )
      }

      it("should fail with heterogeneous fields") {
        parseJson("a" -> Json.arr(Json.False, Json.True, Json.fromInt(3))).shouldFail
      }

      it("should unify new types with missing or null fields") {
        parseJson("arrOfObj" -> Json.arr(
          Json.obj("kittens" -> Json.fromInt(5), "puppies" -> Json.fromInt(10)),
          Json.obj("kittens" -> Json.Null, "puppies" -> Json.fromInt(101)),
          Json.obj("puppies" -> Json.fromInt(40)),
          Json.obj("puppies" -> Json.fromInt(40), "type" -> Json.fromString("bear")))
        ) shouldHaveFields(
          "arrOfObj" -> ArrayOf(NewType(Vector(
            "kittens" -> Nullable(NumericType),
            "puppies" -> NumericType,
            "type" -> Nullable(StringType)
          )))
        )
      }

      it("should unify new types inside new types") {
        parseJson("arrOfObj" -> Json.arr(
          Json.obj("animals" -> Json.obj("kittens" -> Json.fromInt(5), "puppies" -> Json.fromInt(10))),
          Json.obj("animals" -> Json.obj("kittens" -> Json.Null, "puppies" -> Json.fromInt(101))),
          Json.obj("animals" -> Json.obj("puppies" -> Json.fromInt(40))),
          Json.obj("animals" -> Json.obj("puppies" -> Json.fromInt(40), "type" -> Json.fromString("bear"))))
        ) shouldHaveFields(
          "arrOfObj" -> ArrayOf(NewType(Vector(
            "animals" -> NewType(Vector(
              "kittens" -> Nullable(NumericType),
              "puppies" -> NumericType,
              "type" -> Nullable(StringType)
            ))
          )))
        )

      }

      it("should fail if there is a null in the array") {
        parseJson("a" -> Json.arr(Json.False, Json.True, Json.Null)).shouldFail
      }

      it("should fail on nested arrays") {
        parseJson("a" -> Json.arr(Json.arr(Json.True))).shouldFail
      }
    }

    describe("NewType") {
      it("should be able to nest many levels of new types") {
        parseJson(
          "nest0" -> Json.obj("nest1" -> Json.obj("nest2" -> Json.obj("nest3" -> Json.obj("nest4" -> Json.fromInt(1)))))
        ) shouldHaveFields(
          "nest0" -> NewType(Vector(
            "nest1" -> NewType(Vector(
              "nest2" -> NewType(Vector(
                "nest3" -> NewType(Vector(
                  "nest4" -> NumericType)))))))))
      }

      it("should unify two newtypes when one has an empty array") {
        parseJson("objects" -> Json.arr(
          Json.obj("projects" -> Json.arr(Json.obj("name" -> Json.fromString("proj1")))),
          Json.obj("projects" -> Json.arr())
        )) shouldHaveFields(
          "objects" -> ArrayOf(NewType(Vector(
            "projects" -> ArrayOf(NewType(Vector("name" -> StringType)))
          )))
        )
      }
    }

    /*
    GenerateClass.parse(
     JsonObject(
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
      )), "SomeClass")

     */


  }

}
