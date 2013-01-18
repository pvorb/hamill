package de.vorb.hamill

import akka.actor.ActorSystem
import java.nio.file.FileSystems
import akka.actor.Props
import akka.actor.Actor
import akka.routing.SmallestMailboxRouter
import akka.util.duration._
import akka.actor.PoisonPill

class FileHandler extends Actor {
  def receive = {
    case Tracer.PreDirectory(dir, attrs) =>
      println("PreDirectory(" + dir + "," + attrs.lastModifiedTime() + ")")
    case Tracer.File(file, attrs) =>
      println("File(" + file + "," + attrs.lastModifiedTime() + ")")
    case o => println(o)
  }
}

object TracerExample extends App {
  val system = ActorSystem("TracerExample")
  val tracer = system.actorOf(Props[Tracer])

  val rootDir = FileSystems.getDefault().getPath("src", "")

  val handler = system.actorOf(Props[FileHandler]
    .withRouter(SmallestMailboxRouter(5)), "handler")
  tracer ! Tracer.Start(rootDir, true, 6, handler)
}