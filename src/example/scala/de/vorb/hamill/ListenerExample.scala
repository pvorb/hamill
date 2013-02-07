package de.vorb.hamill

import akka.actor.ActorSystem
import java.nio.file.FileSystems
import akka.actor.Props
import akka.actor.Actor
import akka.pattern.ask
import akka.routing.SmallestMailboxRouter
import scala.concurrent.duration._
import akka.actor.PoisonPill
import scala.concurrent.Await
import akka.util.Timeout
import java.nio.file.attribute.BasicFileAttributes

object ListenerExample extends App {
  implicit val system = ActorSystem("Filewalker")

  implicit val timeout = Timeout(5 seconds)
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

  val future = Tracing.walkFileTree(root, system.actorOf(Props[Listener]),
    Configuration.Default, timeout)

  val result = Await.result(future, 5 minutes)
  result match {
    case Tracer.Result(None)    => println("success")
    case Tracer.Result(Some(a)) => println("error: " + a)
  }

  system.shutdown
}
