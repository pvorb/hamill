package de.vorb.hamill

import java.nio.file.FileSystems
import java.nio.file.attribute.BasicFileAttributes

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.implicitConversions
import scala.language.postfixOps

import akka.actor.{ Actor, ActorSystem, Props }
import akka.util.Timeout

object ListenerExample extends App {
  implicit def toString(a: BasicFileAttributes): String =
    "(" + (if (a.isDirectory()) "Directory" else "File") + ", " +
      a.lastModifiedTime + ")"

  val root = FileSystems.getDefault().getPath("src", "")

  class Listener extends Actor {
    def receive = {
      case File(f, Left(err))    => println("error in file " + f)
      case File(f, Right(attrs)) => println(f + ", " + ListenerExample.toString(attrs))
      case Directory(d, attrs)   => println(d + ", " + ListenerExample.toString(attrs))
    }
  }

  val tracing = new Tracing

  val future = tracing.walkFileTree(root, tracing.system.actorOf(Props[Listener]))

  val result = Await.result(future, 5 minutes)
  result match {
    case Tracer.Result(None)    => println("success")
    case Tracer.Result(Some(a)) => println("error: " + a)
  }

  tracing.system.shutdown()
}
