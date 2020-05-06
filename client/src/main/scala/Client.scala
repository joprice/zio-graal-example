import caliban.client.FieldBuilder._
import caliban.client.SelectionBuilder._
import caliban.client._
import caliban.client.Operations._

object Client {

  type Post
  object Post {
    def similar[A](limit: Int, offset: Option[Int] = None)(
        innerSelection: SelectionBuilder[Post, A]
    ): SelectionBuilder[Post, List[A]] =
      Field(
        "similar",
        ListOf(Obj(innerSelection)),
        arguments = List(Argument("limit", limit), Argument("offset", offset))
      )
  }

  type User
  object User {
    def similar[A](limit: Int)(
        innerSelection: SelectionBuilder[User, A]
    ): SelectionBuilder[User, List[A]] =
      Field(
        "similar",
        ListOf(Obj(innerSelection)),
        arguments = List(Argument("limit", limit))
      )
  }

  type Queries = RootQuery
  object Queries {
    def users[A](
        innerSelection: SelectionBuilder[User, A]
    ): SelectionBuilder[RootQuery, List[A]] =
      Field("users", ListOf(Obj(innerSelection)))
    def posts[A](
        innerSelection: SelectionBuilder[Post, A]
    ): SelectionBuilder[RootQuery, List[A]] =
      Field("posts", ListOf(Obj(innerSelection)))
  }

}

