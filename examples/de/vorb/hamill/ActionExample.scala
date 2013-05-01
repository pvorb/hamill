package de.vorb.hamill

import java.nio.file.FileSystems
import java.nio.file.attribute.BasicFileAttributes

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.implicitConversions
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.util.Timeout

object ActionExample extends App {
  implicit val system = ActorSystem("Filewalker")

  implicit val timeout = Timeout(5 seconds)
  implicit def toString(a: BasicFileAttributes): String =
    "(" + (if (a.isDirectory()) "Directory" else "File") + ", " +
      a.lastModifiedTime + ")"

  val root = FileSystems.getDefault().getPath("src", "")
  
  val tracing = new Tracing

  val future = tracing.walkFileTree(root,
    (path: PathContainer) => {
      path match {
        case File(f, Left(err)) =>
          println("error in file " + f)
        case File(f, Right(attrs)) =>
          println(f + ", " + toString(attrs))
        case Directory(d, attrs) =>
          println(d + ", " + toString(attrs))
      }
    }, timeout)

  val result = Await.result(future, 5 minutes)
  result match {
    case Tracer.Result(None) => println("success")
    case Tracer.Result(Some(a)) => println("error: " + a)
  }

  system.shutdown
}
