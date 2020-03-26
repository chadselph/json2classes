package me.chadrs.json2code

import io.circe.JsonObject
import io.circe.parser.parse
import me.chadrs.json2code.GenerateClass.generate
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class TestWIthWeatherApiTest extends AnyFunSpec with Matchers {

  describe("Encoder + render") {
    it("should correctly render an example from a weather API") {

      val weatherJson = """
                          |{
                          |    "cod": "200",
                          |    "count": 25,
                          |    "list": [
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.77,
                          |                "lon": -122.42
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5391959,
                          |            "main": {
                          |                "feels_like": 7.48,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.67,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "San Francisco",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.77,
                          |                "lon": -122.45
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5391997,
                          |            "main": {
                          |                "feels_like": 7.52,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.7,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "San Francisco County",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.71,
                          |                "lon": -122.46
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5341430,
                          |            "main": {
                          |                "feels_like": 7.48,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.67,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Daly City",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.68,
                          |                "lon": -122.4
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5330810,
                          |            "main": {
                          |                "feels_like": 7.28,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.49,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "Brisbane",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.69,
                          |                "lon": -122.48
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5330854,
                          |            "main": {
                          |                "feels_like": 7.5,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.68,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Broadmoor",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.68,
                          |                "lon": -122.46
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5338703,
                          |            "main": {
                          |                "feels_like": 7.48,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.67,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Colma",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.86,
                          |                "lon": -122.49
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5393611,
                          |            "main": {
                          |                "feels_like": 7.6,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.77,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Sausalito",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.87,
                          |                "lon": -122.46
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5402535,
                          |            "main": {
                          |                "feels_like": 7.6,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.77,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Tiburon",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.87,
                          |                "lon": -122.46
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5327490,
                          |            "main": {
                          |                "feels_like": 7.6,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.77,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Belvedere",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.65,
                          |                "lon": -122.41
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5397765,
                          |            "main": {
                          |                "feels_like": 7.23,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.45,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "South San Francisco",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.87,
                          |                "lon": -122.51
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5370464,
                          |            "main": {
                          |                "feels_like": 7.6,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.77,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Marin City",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.8,
                          |                "lon": -122.27
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5378538,
                          |            "main": {
                          |                "feels_like": 7.29,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.5,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "Oakland",
                          |            "rain": {
                          |                "1h": 0.25
                          |            },
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "light rain",
                          |                    "icon": "10d",
                          |                    "id": 500,
                          |                    "main": "Rain"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.83,
                          |                "lon": -122.29
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5346462,
                          |            "main": {
                          |                "feels_like": 7.45,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.64,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Emeryville",
                          |            "rain": {
                          |                "1h": 0.25
                          |            },
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "light rain",
                          |                    "icon": "10d",
                          |                    "id": 500,
                          |                    "main": "Rain"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.63,
                          |                "lon": -122.41
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5391749,
                          |            "main": {
                          |                "feels_like": 7.22,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.44,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "San Bruno",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.77,
                          |                "lon": -122.24
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5322737,
                          |            "main": {
                          |                "feels_like": 7.24,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.46,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "Alameda",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 1
                          |            },
                          |            "coord": {
                          |                "lat": 37.88,
                          |                "lon": -122.54
                          |            },
                          |            "dt": 1584407873,
                          |            "id": 7262659,
                          |            "main": {
                          |                "feels_like": 7.57,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.75,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Tamalpais-Homestead Valley",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "clear sky",
                          |                    "icon": "01d",
                          |                    "id": 800,
                          |                    "main": "Clear"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 120,
                          |                "speed": 2.6
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 1
                          |            },
                          |            "coord": {
                          |                "lat": 37.9,
                          |                "lon": -122.51
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5399319,
                          |            "main": {
                          |                "feels_like": 7.58,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.76,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Strawberry",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "clear sky",
                          |                    "icon": "01d",
                          |                    "id": 800,
                          |                    "main": "Clear"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 120,
                          |                "speed": 2.6
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.89,
                          |                "lon": -122.3
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5322850,
                          |            "main": {
                          |                "feels_like": 7.18,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.72,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Albany",
                          |            "rain": {
                          |                "1h": 0.25
                          |            },
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "light rain",
                          |                    "icon": "10d",
                          |                    "id": 500,
                          |                    "main": "Rain"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 3.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.87,
                          |                "lon": -122.27
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5327684,
                          |            "main": {
                          |                "feels_like": 7.11,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.66,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Berkeley",
                          |            "rain": {
                          |                "1h": 0.25
                          |            },
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "light rain",
                          |                    "icon": "10d",
                          |                    "id": 500,
                          |                    "main": "Rain"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 3.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.82,
                          |                "lon": -122.23
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5382514,
                          |            "main": {
                          |                "feels_like": 7.28,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.49,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "Piedmont",
                          |            "rain": {
                          |                "1h": 0.25
                          |            },
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "light rain",
                          |                    "icon": "10d",
                          |                    "id": 500,
                          |                    "main": "Rain"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.61,
                          |                "lon": -122.49
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5380420,
                          |            "main": {
                          |                "feels_like": 7.28,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.49,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "Pacifica",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 1
                          |            },
                          |            "coord": {
                          |                "lat": 37.91,
                          |                "lon": -122.55
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5373121,
                          |            "main": {
                          |                "feels_like": 7.63,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.8,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Mill Valley",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "clear sky",
                          |                    "icon": "01d",
                          |                    "id": 800,
                          |                    "main": "Clear"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 120,
                          |                "speed": 2.6
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.92,
                          |                "lon": -122.31
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5345623,
                          |            "main": {
                          |                "feels_like": 7.19,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.73,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "El Cerrito",
                          |            "rain": {
                          |                "1h": 0.25
                          |            },
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "light rain",
                          |                    "icon": "10d",
                          |                    "id": 500,
                          |                    "main": "Rain"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 3.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 75
                          |            },
                          |            "coord": {
                          |                "lat": 37.6,
                          |                "lon": -122.39
                          |            },
                          |            "dt": 1584407771,
                          |            "id": 5373129,
                          |            "main": {
                          |                "feels_like": 7.2,
                          |                "humidity": 54,
                          |                "pressure": 1012,
                          |                "temp": 10.42,
                          |                "temp_max": 12.22,
                          |                "temp_min": 8.33
                          |            },
                          |            "name": "Millbrae",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "broken clouds",
                          |                    "icon": "04d",
                          |                    "id": 803,
                          |                    "main": "Clouds"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 50,
                          |                "speed": 2.1
                          |            }
                          |        },
                          |        {
                          |            "clouds": {
                          |                "all": 1
                          |            },
                          |            "coord": {
                          |                "lat": 37.94,
                          |                "lon": -122.35
                          |            },
                          |            "dt": 1584407772,
                          |            "id": 5387428,
                          |            "main": {
                          |                "feels_like": 7.56,
                          |                "humidity": 62,
                          |                "pressure": 1012,
                          |                "temp": 10.74,
                          |                "temp_max": 12.78,
                          |                "temp_min": 8.89
                          |            },
                          |            "name": "Richmond",
                          |            "rain": null,
                          |            "snow": null,
                          |            "sys": {
                          |                "country": "US"
                          |            },
                          |            "weather": [
                          |                {
                          |                    "description": "clear sky",
                          |                    "icon": "01d",
                          |                    "id": 800,
                          |                    "main": "Clear"
                          |                }
                          |            ],
                          |            "wind": {
                          |                "deg": 120,
                          |                "speed": 2.6
                          |            }
                          |        }
                          |    ],
                          |    "message": "accurate"
                          |}
                          |
                          |""".stripMargin

      val result = parse(weatherJson)
        .flatMap(_.as[JsonObject])
        .map(input => generate("Response", input))
        .fold(_.toString, identity)
      result shouldBe """package com.test
        |case class Clouds(all: BigDecimal)
        |case class Coord(lat: BigDecimal, lon: BigDecimal)
        |case class Main(feels_like: BigDecimal, humidity: BigDecimal, pressure: BigDecimal, temp: BigDecimal, temp_max: BigDecimal, temp_min: BigDecimal)
        |case class Rain(`1h`: BigDecimal)
        |case class Sys(country: String)
        |case class Weather(description: String, icon: String, id: BigDecimal, main: String)
        |case class Wind(deg: BigDecimal, speed: BigDecimal)
        |case class List(clouds: Clouds, coord: Coord, dt: BigDecimal, id: BigDecimal, main: Main, name: String, rain: Option[Rain], snow: Nothing, sys: Sys, weather: Vector[Weather], wind: Wind)
        |case class Response(cod: String, count: BigDecimal, list: Vector[List], message: String)""".stripMargin


    }
  }

}
