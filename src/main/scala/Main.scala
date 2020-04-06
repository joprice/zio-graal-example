package hello

import zio.{App, ZIO, Managed, IO}
import zio.console.{ putStrLn , Console }
import java.io.IOException
import scala.io.Source

object Main extends App {

  def run(args: List[String]) =
    myAppLogic.fold(_ => 1, _ => 0)

  def openFile(name: String) =
    IO.effect(Source.fromFile(name)).refineToOrDie[IOException]

  def closeFile(source: Source) = IO.effectTotal(source.close())

  // see https://zio.dev/docs/datatypes/datatypes_managed#managed-with-zio-environment
  // see https://github.com/zio/zio/blob/43013ecd42a65f8cea575d408ec9178b0374f251/docs/datatypes/io.md#brackets
  val managedFile: Managed[IOException, Source] = Managed.make(openFile("data.json"))(closeFile)

  val myAppLogic: ZIO[Console, Throwable, Unit] =
    managedFile.use { file =>
      for {
      _ <- putStrLn(file.mkString)
      } yield ()
    }
}
