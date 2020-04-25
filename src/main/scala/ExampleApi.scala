package com.joprice

import caliban._
import zio.ZIO
import scala.language.postfixOps
import caliban.GraphQL.graphQL
import caliban.schema.Annotations.GQLDescription
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{ maxDepth, maxFields, printSlowQueries, timeout }
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.duration._

object ExampleApi extends GenericSchema[Any] {

  case class Queries(
    @GQLDescription("Return all characters from a given origin")
    characters: UIO[List[String]]
  )

  val api: GraphQL[Console with Clock] =
    graphQL(
      RootResolver(
        Queries(
          characters = ZIO.succeed(List("a"))
        )
      )
    ) @@
      maxFields(200) @@               // query analyzer that limit query fields
      maxDepth(30) @@                 // query analyzer that limit query depth
      timeout(3 seconds) @@           // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      apolloTracing // wrapper for https://github.com/apollographql/apollo-tracing

}
