package com.joprice

import java.net.InetSocketAddress
import _root_.uzhttp.server._
import caliban._
import zio.App

object UzHttpApp extends App {

  override def run(args: List[String]) =
    (for {
      interpreter <- ExampleApi.api.interpreter
      address = new InetSocketAddress(8088)
      route = UzHttpAdapter.makeHttpService("/api/graphql", interpreter)
      wsRoute = UzHttpAdapter.makeWebSocketService("/ws/graphql", interpreter)
      server = Server.builder(address).handleSome(route orElse wsRoute)
      result <- server.serve.useForever.as(
        0
      ) //.provideCustomLayer(ExampleService.make(sampleCharacters))
    } yield result)
      .exitCode

}
