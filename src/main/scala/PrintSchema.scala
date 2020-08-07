package com.joprice

import java.nio.file.{Files, Paths}
import zio._

object PrintSchema extends App {
  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    ZIO
      .effect(
        Files.write(
          Paths.get("schema.graphql"),
          ExampleApi.api.render.getBytes("UTF-8")
        )
      )
      .as(ExitCode.success)
      .orDie
}
