package examples

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, parameter, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object AkkaHttpExample {

  val route: Route = path("hello") {
    get {
      parameter("id") { id =>
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello to akka-http $id</h1>"))
      }
    } ~ get {
      parameter("id") { id =>
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Say hello to akka-http $id</h1>"))
      }
    }
  }
}
