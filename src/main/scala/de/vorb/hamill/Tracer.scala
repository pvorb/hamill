package de.vorb.hamill

import java.io.IOException
import java.nio.file.{ FileVisitOption, FileVisitResult, FileVisitor, Files, Path }
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumSet

import de.vorb.hamill.Configuration._

import akka.actor.{ Actor, actorRef2Scala }

/**
 * Tracer actor. Provides the functionality behind [[Tracing.walkFileTree]].
 */
class Tracer extends Actor {
  import Tracer._

  def receive = {
    case Start(root, action, config) =>

      val options = if (config.followLinks)
        EnumSet.of(FileVisitOption.FOLLOW_LINKS)
      else
        EnumSet.noneOf(classOf[FileVisitOption])

      Files.walkFileTree(root, options, config.maxDepth,
        new FileVisitor[Path] {
          override def preVisitDirectory(dir: Path,
                                         attrs: BasicFileAttributes) = {
            if (config.directories)
              action(Directory(dir, attrs))

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
              action(de.vorb.hamill.File(file, Right(attrs)))

            FileVisitResult.CONTINUE
          }

          override def visitFileFailed(file: Path, err: IOException) = {
            if (config.fileErrorBehavior == Configuration.Terminate)
              sender ! Result(Some(err))
            else
              action(de.vorb.hamill.File(file, Left(err)))

            config.fileErrorBehavior
          }
        })
  }
}

object Tracer {
  case class Start(root: Path, action: PathContainer => Unit,
                   config: Configuration)
  case class Result(error: Option[IOException])
}