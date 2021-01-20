package examples

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import io.circe.Printer
import ru.tinkoff.tschema.akkaHttp.MkRoute
import io.circe.syntax._
import ru.tinkoff.tschema.swagger._

object ExampleDefinition {
  import ru.tinkoff.tschema.syntax._
  def api =
    get |> operation("hello") |> capture[Int]("name") |> $$[String]
}


object Example extends App {
  import ExampleDefinition.api
  import akka.http.scaladsl.server.Directives._

  // building service
  object handler {
    def hello(name: Int): String = s"Hello, $name"
  }

  val apiRoute: Route = MkRoute(api)(handler)

  //building swagger
  val apiSwagger: OpenApi = MkSwagger(api).make(OpenApiInfo("example"))
  val printer      = Printer.spaces2.copy(dropNullValues = true)
  val swaggerRoute = path("swagger")(complete(apiSwagger.asJson.pretty(printer)))

  //typical akka http boilerplate
  implicit val system: ActorSystem        = ActorSystem("tschema-example")
  implicit val materializer: Materializer = ActorMaterializer()

  //run the server
  Http().bindAndHandle(apiRoute ~ swaggerRoute, "localhost", 8080)
}