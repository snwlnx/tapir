package tapir

import cats.effect.{ContextShift, IO, Timer}
import cats.syntax.semigroupk._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, endpoint, oneOf}
import sttp.tapir.generic.auto._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.openapi.circe.yaml._
import org.http4s.syntax.kleisli._
import sttp.model.StatusCode
import sttp.tapir._

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.ExecutionContext

object TapirHttp4s extends App {

  case class Book(author: String, year: Int, price: Int)

  val books: AtomicReference[List[Book]] = new AtomicReference(
    List(
      Book("alex", 2020, 100),
      Book("alex", 2020, 100)
    )
  )
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val timer: Timer[IO] = IO.timer(ec)
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  def booksResult: Either[ErrorInfo, List[Book]] = Right[ErrorInfo, List[Book]](books.get())

  sealed trait ErrorInfo

  case class NotFound(what: String) extends ErrorInfo

  case class Unauthorized(realm: String) extends ErrorInfo

  case class Unknown(code: Int, msg: String) extends ErrorInfo

  case object NoContent extends ErrorInfo

  // list of books
  val listOfBooks: Endpoint[Unit, ErrorInfo, List[Book], Any] = endpoint
    .get
    .in("books")
    .out(jsonBody[List[Book]])
    .errorOut(
      oneOf[ErrorInfo](
        statusMapping(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
        statusMapping(StatusCode.Unauthorized, jsonBody[Unauthorized].description("unauthorized")),
        statusMapping(StatusCode.NoContent, emptyOutput.map(_ => NoContent)(_ => ())),
        statusDefaultMapping(jsonBody[Unknown].description("unknown"))
      ))


  val listOfBooksRoute: HttpRoutes[IO] = Http4sServerInterpreter.toRoutes(listOfBooks)(_ => IO.pure(booksResult))

  // put list of books
  val putListOfBooks: Endpoint[List[Book], ErrorInfo, Unit, Any] = endpoint
    .put
    .in("books")
    .in(jsonBody[List[Book]])
    .errorOut(
      oneOf[ErrorInfo](
        statusMapping(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
        statusMapping(StatusCode.Unauthorized, jsonBody[Unauthorized].description("unauthorized")),
        statusMapping(StatusCode.NoContent, emptyOutput.map(_ => NoContent)(_ => ())),
        statusDefaultMapping(jsonBody[Unknown].description("unknown"))
      ))


  val respL: Either[ErrorInfo, Unit] = Right[ErrorInfo, Unit]()

  val putListOfBooksRoute: HttpRoutes[IO] =
    Http4sServerInterpreter.toRoutes(putListOfBooks)(booksInput =>
      IO {
        books.accumulateAndGet(booksInput,
          (t: List[Book], u: List[Book]) => t ++ u)
      }.as(respL)
    )

  val docs= OpenAPIDocsInterpreter.toOpenAPI(List(listOfBooks, putListOfBooks), "Books store", version = "0.1")

  val swaggerRoute: HttpRoutes[IO] = new SwaggerHttp4s(docs.toYaml).routes[IO]

  val routes:  HttpRoutes[IO] = listOfBooksRoute <+> swaggerRoute <+> putListOfBooksRoute

  val router = Router("/" -> routes).orNotFound

  BlazeServerBuilder[IO](ec)
    .bindHttp(8080, "localhost")
    .withHttpApp(router)
    .resource
    .use { _ =>
      IO {
        println("exit")
        scala.io.StdIn.readLine()
      }
    }.unsafeRunSync()

}