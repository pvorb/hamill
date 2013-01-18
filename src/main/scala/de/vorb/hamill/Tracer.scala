package de.vorb.hamill

import java.io.IOException
import java.nio.file.{
  FileVisitOption => FVOption,
  FileVisitResult => FVResult,
  Files,
  Path,
  SimpleFileVisitor => SFV
}
import java.nio.file.attribute.{ BasicFileAttributes => BFA }
import java.util.EnumSet
import akka.actor.Actor
import akka.actor.ActorRef

class Tracer extends Actor {
  import Tracer._

  def receive = {
    case Start(path, fl, md, handler) => startWalking(path, fl, md, handler)
  }

  /**
   * Starts to recursively walk the file tree.
   */
  def startWalking(path: Path, followLinks: Boolean, maxDepth: Int,
                   handler: ActorRef) {
    val opts: EnumSet[FVOption] =
      if (followLinks) EnumSet.of(FVOption.FOLLOW_LINKS)
      else EnumSet.noneOf(classOf[FVOption])

    Files.walkFileTree(path, opts, maxDepth, new SFV[Path] {
      override def preVisitDirectory(dir: Path, attrs: BFA) = {
        handler ! PreDirectory(dir, attrs)
        FVResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException) = {
        if (exc == null) {
          handler ! PostDirectory(dir)
          FVResult.CONTINUE
        } else {
          handler ! PostDirectoryFailed(dir, exc)
          FVResult.SKIP_SUBTREE
        }
      }

      override def visitFile(file: Path, attrs: BFA) = {
        handler ! File(file, attrs)
        FVResult.CONTINUE
      }

      override def visitFileFailed(file: Path, exc: IOException) = {
        handler ! FileFailed(file, exc)
        FVResult.CONTINUE
      }
    })
  }
}

object Tracer {
  case class Start(path: Path, followLinks: Boolean, maxDepth: Int,
      handler: ActorRef) {
    require(maxDepth > 0)
  }

  case class File(file: Path, attrs: BFA)
  case class FileFailed(file: Path, exc: IOException)
  case class PreDirectory(dir: Path, attrs: BFA)
  case class PostDirectory(dir: Path)
  case class PostDirectoryFailed(dir: Path, exc: IOException)
}
