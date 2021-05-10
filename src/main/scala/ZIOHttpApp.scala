package com.joprice

import zio._
import zio.stream._
import zhttp.http._
import zhttp.service.Server
import caliban.ZHttpAdapter
import zio.internal.{Platform, Tracing}

object ZIOHttpApp extends App {
  override val platform: Platform =
    // disabling tracing since it is full of errors like:
    //    couldn't find class file for lambda:cats.effect.Resource$$$Lambda$adbb9d4abf1b0ba833eeb9dd1fa672bceb70257d@109d0c3c8>
    Platform.default.withTracing(Tracing.disabled)

  private val graphiql = Http.succeed(
    Response.http(content =
      HttpData.fromStream(ZStream.fromResource("graphiql.html"))
    )
  )

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    ExampleApi.api.interpreter.flatMap { interpreter =>
      Server
        .start(
          8088,
          Http.route {
            case _ -> Root / "api" / "graphql" =>
              ZHttpAdapter.makeHttpService(interpreter)
            case _ -> Root / "ws" / "graphql" =>
              ZHttpAdapter.makeWebSocketService(interpreter)
            case _ -> Root / "graphiql" => graphiql
          }
        )
        .forever
    }.exitCode
}
