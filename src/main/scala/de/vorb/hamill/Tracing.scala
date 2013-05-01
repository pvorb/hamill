package de.vorb.hamill

import java.nio.file.Path

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.typesafe.config.{ Config, ConfigFactory }

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

/**
 * Main component.
 */
class Tracing(config: Config) {
  val configuration = new Configuration(config)

  def this() = this(ConfigFactory.load())

  /**
   * Walks a file tree recursively (depth-first) starting at `root` and applies
   * an action on each file and/or directory depending on the given
   * configuration.
   */
  def walkFileTree(root: Path, action: PathContainer => Unit,
    timeout: Timeout = new Timeout(1 minute))(
      implicit system: ActorSystem): Future[Any] =
    {
      implicit val t = timeout
      system.actorOf(Props[Tracer]) ? Tracer.StartAction(root, configuration, action)
    }

  /**
   * Walks a file tree recursively (depth-first) starting at `root` and sends
   * either an [[de.vorb.hamill.Directory]] or an [[de.vorb.hamill.File]] to the
   * specified listener actor for each directory/file passed.
   */
  def walkFileTree(root: Path, listener: ActorRef,
    timeout: Timeout)(implicit system: ActorSystem): Future[Any] =
    {
      implicit val t = timeout
      system.actorOf(Props[Tracer]) ?
        Tracer.StartListener(root, configuration, listener)
    }
}
