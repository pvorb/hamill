package de.vorb.hamill

import java.nio.file.Path

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.typesafe.config.{ Config, ConfigFactory }

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout

/**
 * Main component.
 */
class Tracing(config: Config, val system: ActorSystem) {
  val configuration = new Configuration(config)

  def this(config: Config) = this(config, ActorSystem("hamill",
    config.getConfig("hamill.akka").withFallback(ConfigFactory.empty())))

  def this() = this(ConfigFactory.load())

  private implicit val t = configuration.timeout

  /**
   * Walks a file tree recursively (depth-first) starting at `root` and applies
   * an action on each file and/or directory depending on the given
   * configuration.
   */
  def walkFileTree(root: Path, action: PathContainer => Unit): Future[Any] =
    {
      system.actorOf(Props[Tracer]) ?
        Tracer.StartAction(root, configuration, action)
    }

  /**
   * Walks a file tree recursively (depth-first) starting at `root` and sends
   * either an [[de.vorb.hamill.Directory]] or an [[de.vorb.hamill.File]] to the
   * specified listener actor for each directory/file passed.
   */
  def walkFileTree(root: Path, listener: ActorRef): Future[Any] =
    {
      system.actorOf(Props[Tracer]) ?
        Tracer.StartListener(root, configuration, listener)
    }
}
