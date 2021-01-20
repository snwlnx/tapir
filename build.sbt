name := "otus-tapir-200121"

version := "0.1"

scalaVersion := "2.13.4"


val tapirVersion = "0.17.6"

val typedSchema = List(
  "ru.tinkoff" %% "typed-schema-swagger"   % "0.12.7",
  "ru.tinkoff" %% "typed-schema-akka-http" % "0.12.7",
)

libraryDependencies ++= Seq(
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1",
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-akka-http" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-redoc-akka-http" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % "0.17.6"
) ++ typedSchema

