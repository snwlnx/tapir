package tapir

sealed trait Event

case class Foo(i: Int) extends Event
case class Bar(s: String) extends Event
case class Baz(c: Char) extends Event
case class Qux(values: List[String]) extends Event

import cats.syntax.functor._
import io.circe.{ Decoder, Encoder }, io.circe.generic.auto._
import io.circe.syntax._

object GenericDerivation {
  implicit val encodeEvent: Encoder[Event] = Encoder.instance {
    case foo @ Foo(_) => foo.asJson
    case bar @ Bar(_) => bar.asJson
    case baz @ Baz(_) => baz.asJson
    case qux @ Qux(_) => qux.asJson
  }

  implicit val decodeEvent: Decoder[Event] =
    List[Decoder[Event]](
      Decoder[Foo].widen,
      Decoder[Bar].widen,
      Decoder[Baz].widen,
      Decoder[Qux].widen
    ).reduceLeft(_ or _)
}

object A extends App {

  import GenericDerivation._
  import io.circe.parser.decode

  println(decode[Event]("""{ "i": 1000 }"""))
  // res0: Either[io.circe.Error, Event] = Right(Foo(1000))

  println((Foo(100): Event).asJson.noSpaces)
  // res1: String = "{\"i\":100}"

  println(decode[Event]("""{ "values": ["1", "2"] }"""))
  // res0: Either[io.circe.Error, Event] = Right(Foo(1000))

  println((Foo(100): Event).asJson.noSpaces)

}