package com.joprice

import zio._
import zio.stream._
import zio.console._

object Main extends App {
  def run(args: List[String]) =
    ZStream
      .range(0, 100)
      .grouped(10)
      .mapM(i => putStrLn(i.size.toString))
      .runDrain
      .exitCode
}
