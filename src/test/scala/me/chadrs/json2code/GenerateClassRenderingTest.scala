package me.chadrs.json2code

import me.chadrs.json2code.GenerateClass._
import org.scalatest.matchers.should.Matchers
import scala.meta._


class GenerateClassRenderingTest extends org.scalatest.funspec.AnyFunSpec with Matchers {

  def newtype(fields: (String, JsonTypeTree)*): NewType = NewType(fields.toVector)

  describe("rendering a JsonTypeTree") {
    it("should work for simple types") {
      val testType = NewType(MappedFields(
        "myString" -> StringType,
        "myBool" -> BooleanType,
        "myNull" -> Null,
        "myNumber" -> NumericType)
      )
      val renderResult = render("SomeType", testType, RenderSettings(arrayType = t => t"Array[$t]", numericType = t"java.lang.Integer"))
      renderResult.length shouldBe 1
      renderResult.head.className.value shouldBe "SomeType"
      renderResult.head.params.length shouldBe 4
      renderResult.head.params.head.name.value shouldBe "myString"
      renderResult.head.params(1).name.value shouldBe "myBool"
      renderResult.head.params(2).name.value shouldBe "myNull"
      renderResult.head.params(3).name.value shouldBe "myNumber"

      renderResult.head.generate().structure shouldEqual q"""case class SomeType(myString: String, myBool: Boolean, myNull: Nothing, myNumber: java.lang.Integer)""".structure
    }

    it("should work with nested types") {
      val testType = newtype(
        "request" -> newtype(
          "method" -> StringType, "queryParams" -> ArrayOf(StringType)
        ),
        "response" -> newtype(
          "code" -> NumericType
        ),
        "metrics" -> ArrayOf(newtype("name" -> StringType, "data" -> Nullable(newtype("kb" -> NumericType))))
      )
      val results = render("Http", testType, RenderSettings())
      results.length shouldBe 5
      val rendered = q"""package com.test {
          ..${results.toList.map(_.generate())}
           }""".structure shouldBe q"""package com.test {
              case class Request(method: String, queryParams: Vector[String])
              case class Response(code: BigDecimal)
              case class Data(kb: BigDecimal)
              case class Metrics(name: String, data: Option[Data])
              case class Http(request: Request, response: Response, metrics: Vector[Metrics])}""".structure
    }
  }

}
