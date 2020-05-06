package com.joprice

import caliban._
import cats.data.Kleisli
import cats.effect.Blocker
import org.http4s.StaticFile
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.console.putStrLn
import zio.{ZEnv, ZIO}
import zio.interop.catz._
import zio._
import zio.blocking.Blocking
import org.http4s.implicits._
import scala.concurrent.ExecutionContext

object Http4sApp extends CatsApp {
  type ExampleTask[A] = RIO[ZEnv, A]
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      blocker <- ZIO
        .access[Blocking](_.get.blockingExecutor.asEC)
        .map(Blocker.liftExecutionContext)
      interpreter <- ExampleApi.api.interpreter
      result <- BlazeServerBuilder[ExampleTask](ExecutionContext.global)
        .bindHttp(8088, "0.0.0.0")
        .withHttpApp(
          Router[ExampleTask](
            "/api/graphql" -> CORS(Http4sAdapter.makeHttpService(interpreter)),
            "/ws/graphql" -> CORS(
              Http4sAdapter.makeWebSocketService(interpreter)
            ),
            "/graphiql" -> Kleisli.liftF(
              StaticFile.fromResource("/graphiql.html", blocker, None)
            )
          ).orNotFound
        )
        .resource
        .toManaged
        .useForever
        .as(0)
    } yield result).catchAll(err => putStrLn(err.toString).as(1))

}
