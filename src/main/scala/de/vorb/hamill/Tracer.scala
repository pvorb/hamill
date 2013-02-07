package de.vorb.hamill

import java.io.IOException
import java.nio.file.{ FileVisitOption, FileVisitResult, FileVisitor, Files, Path }
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumSet
import de.vorb.hamill.Configuration._
import akka.actor.{ Actor, actorRef2Scala }
import akka.actor.ActorRef

/**
 * Tracer actor. Used in [[Tracing]]'s `walkFileTree` methods.
 */
class Tracer extends Actor {
  import Tracer._

  def receive = {
    case start: Start =>
      val root = start.root
      val config = start.config

      val options =
        if (config.followLinks)
          EnumSet.of(FileVisitOption.FOLLOW_LINKS)
        else
          EnumSet.noneOf(classOf[FileVisitOption])

      // walk the file tree
      Files.walkFileTree(root, options, config.maxDepth, new FileVisitor[Path] {
        override def preVisitDirectory(dir: Path,
          attrs: BasicFileAttributes) = {
          if (config.directories)
            start match {
              case StartAction(_, _, action) =>
                action(Directory(dir, attrs))
              case StartListener(_, _, listener) =>
                listener ! Directory(dir, attrs)
            }

          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, err: IOException) = {
          if (err != null) {
            // handle error according to configuration
            if (config.directoryErrorBehavior == Configuration.Terminate)
              sender ! Result(Some(err))

            config.directoryErrorBehavior
          } else if (dir == root) {
            sender ! Result(None)
            FileVisitResult.TERMINATE
          } else {
            FileVisitResult.CONTINUE
          }
        }

        override def visitFile(file: Path, attrs: BasicFileAttributes) = {
          if (config.files)
            start match {
              case StartAction(_, _, action) =>
                action(File(file, Right(attrs)))
              case StartListener(_, _, listener) =>
                listener ! File(file, Right(attrs))
            }

          FileVisitResult.CONTINUE
        }

        override def visitFileFailed(file: Path, err: IOException) = {
          if (config.fileErrorBehavior == Configuration.Terminate)
            sender ! Result(Some(err))
          else
            start match {
              case StartAction(_, _, action) =>
                action(File(file, Left(err)))
              case StartListener(_, _, listener) =>
                listener ! File(file, Left(err))
            }

          config.fileErrorBehavior
        }
      })
  }
}

object Tracer {

  sealed trait Start {
    def root: Path
    def config: Configuration
  }

  case class StartAction(root: Path, config: Configuration,
    action: PathContainer => Unit) extends Start
  case class StartListener(root: Path, config: Configuration,
    listener: ActorRef) extends Start
  case class Result(error: Option[IOException])
}
