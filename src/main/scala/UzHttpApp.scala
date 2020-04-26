package com.joprice

import java.net.InetSocketAddress
import _root_.uzhttp.server._
import caliban._
import zio.console.putStrLn
import zio.{ App, ZEnv, ZIO }

object UzHttpApp extends App {

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    (for {
      interpreter <- ExampleApi.api.interpreter
      address     = new InetSocketAddress(8088)
      route       = UzHttpAdapter.makeHttpService("/api/graphql", interpreter)
      wsRoute     = UzHttpAdapter.makeWebSocketService("/ws/graphql", interpreter)
      server      = Server.builder(address).handleSome(route orElse wsRoute)
      result           <- server.serve.useForever.as(0)//.provideCustomLayer(ExampleService.make(sampleCharacters))
    } yield result).catchAll(err => putStrLn(err.toString).as(1))

}
