package com.joprice

import caliban._
import zio.ZIO
import scala.language.postfixOps
import caliban.GraphQL.graphQL
import caliban.schema.GenericSchema
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{
  maxDepth,
  maxFields,
  printSlowQueries,
  timeout
}
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.duration._

object ExampleApi extends GenericSchema[Any] {

  case class UserSimilarArgs(limit: Int)
  case class PostSimilarArgs(limit: Int, offset: Option[Int])

  case class User(
      similar: UserSimilarArgs => UIO[List[User]]
  )

  case class Post(
      similar: PostSimilarArgs => UIO[List[Post]]
  )

  case class Queries(
      users: UIO[List[User]],
      posts: UIO[List[Post]]
  )

  val api: GraphQL[Console with Clock] =
    graphQL(
      RootResolver(
        Queries(
          users = ZIO.succeed(Nil),
          posts = ZIO.succeed(Nil)
        )
      )
    ) @@
      maxFields(200) @@ // query analyzer that limit query fields
      maxDepth(30) @@ // query analyzer that limit query depth
      timeout(3 seconds) @@ // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      apolloTracing // wrapper for https://github.com/apollographql/apollo-tracing

}
