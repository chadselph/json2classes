package me.chadrs.json2code

import io.circe.JsonObject
import io.circe.parser.parse
import me.chadrs.json2code.GenerateClass.generate
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TestWIthBusApiTest extends AnyFunSpec with Matchers {

  describe("Encoder + render") {
    it("should correctly render an example from a bus API") {

      val busJson = """{
                      |  "data": {
                      |    "agencyKey": "fake-bus",
                      |    "predictionsData": [
                      |      {
                      |        "destinations": [
                      |          {
                      |            "directionId": "1",
                      |            "predictions": [
                      |              {
                      |                "delayed": true,
                      |                "departure": true,
                      |                "min": 35,
                      |                "sec": 2101,
                      |                "time": 1585428278,
                      |                "tripId": "2419913",
                      |                "vehicleId": "6403"
                      |              }
                      |            ]
                      |          },
                      |          {
                      |            "directionId": "1",
                      |            "predictions": [
                      |              {
                      |                "departure": true,
                      |                "min": 14,
                      |                "sec": 890,
                      |                "time": 1585427067,
                      |                "tripId": "2419934",
                      |                "vehicleId": "6413"
                      |              },
                      |              {
                      |                "departure": true,
                      |                "min": 54,
                      |                "sec": 3262,
                      |                "time": 1585429440,
                      |                "tripId": "2419935",
                      |                "vehicleId": "4405"
                      |              }
                      |            ]
                      |          }
                      |        ],
                      |        "routeId": "61",
                      |        "routeName": "61 - Sierra & Piedmont - Good Samaritan Hospital",
                      |        "routeShortName": "61",
                      |        "stopCode": 61662,
                      |        "stopId": "1662",
                      |        "stopName": "Naglee & The Alameda"
                      |      }
                      |    ]
                      |  }
                      |}""".stripMargin

      val result = parse(busJson)
        .flatMap(_.as[JsonObject])
        .map(input => generate("Response", input))
        .fold(_.toString, identity)
      result shouldBe """package com.test
        |case class Predictions(delayed: Option[Boolean], departure: Boolean, min: BigDecimal, sec: BigDecimal, time: BigDecimal, tripId: String, vehicleId: String)
        |case class Destinations(directionId: String, predictions: Vector[Predictions])
        |case class PredictionsData(destinations: Vector[Destinations], routeId: String, routeName: String, routeShortName: String, stopCode: BigDecimal, stopId: String, stopName: String)
        |case class Data(agencyKey: String, predictionsData: Vector[PredictionsData])
        |case class Response(data: Data)""".stripMargin


    }
  }

}
