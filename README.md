Simple proof of concept converting JSON data into case classes.

Literally just brainstorming as I code so it's a total mess still.

Does best effort for unifying json types... i.e. ```
... "person": [{"name": "Joe", "age": 4}, {"name": "Peter"}] ...```
becomes ```case class Person(name: String, age: Option[BigDecimal]``` but it will miss some.

Rendering the `TypedJsonTree` into scala code needs to be a bit more
customizable, right you can change what string/boolean/numbers map to but
we should be able to generate circe encoders/decoders, change
snake_case keys to camelCase and some other basics.

Also the web demo should be a separate sbt project so
the main stuff can be crossbuilt, and all of the html/js
should be rewritten in scala.