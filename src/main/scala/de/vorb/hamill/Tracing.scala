package de.vorb.hamill

import java.nio.file.Path

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

/**
 * Main component.
 */
object Tracing {

  /**
   * Walks a file tree recursively (depth-first) starting at `root` and applies
   * an action on each file and/or directory depending on the given
   * configuration.
   */
  def walkFileTree(root: Path, action: PathContainer => Unit,
    config: Configuration = Configuration.Default,
    timeout: Timeout = new Timeout(1 minute))(
      implicit system: ActorSystem): Future[Any] =
    {
      implicit val t = timeout
      system.actorOf(Props[Tracer]) ? Tracer.StartAction(root, config, action)
    }

  /**
   * Walks a file tree recursively (depth-first) starting at `root` and sends
   * either an [[de.vorb.hamill.Directory]] or an [[de.vorb.hamill.File]] to the
   * specified listener actor for each directory/file passed.
   */
  def walkFileTree(root: Path, listener: ActorRef, config: Configuration,
    timeout: Timeout)(implicit system: ActorSystem): Future[Any] =
    {
      implicit val t = timeout
      system.actorOf(Props[Tracer]) ?
        Tracer.StartListener(root, config, listener)
    }
}
