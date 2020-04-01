package hello

import zio.{App, ZIO}
import zio.console.{ putStrLn , Console }

object Main extends App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  val myAppLogic: ZIO[Console, String, Unit] =
    for {
      _ <- putStrLn("Hello World")
    } yield ()
}
