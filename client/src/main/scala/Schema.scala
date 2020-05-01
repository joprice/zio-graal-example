import Types._

object Types {
  case class SimilarArgs(limit: Int, offset: Option[Int])
  case class SimilarArgs(limit: Int)
  case class Post(similar: PostSimilarArgs => List[Post])
  case class User(similar: UserSimilarArgs => List[User])

}

object Operations {

  case class Queries(
      users: () => List[User],
      posts: () => List[Post]
  )

}

