package de.vorb.hamill

import java.nio.file.FileVisitResult
import scala.language.implicitConversions
import com.typesafe.config.Config
import akka.util.Timeout
import scala.concurrent.duration.Duration

/**
 * Configuration.
 */
class Configuration(config: Config) {
  import Configuration._
  private val hc = config.getConfig("hamill")

  val hidden = hc.getBoolean("hidden")
  val recursive = hc.getBoolean("recursive")
  val files = hc.getBoolean("files")
  val directories = hc.getBoolean("directories")
  val maxDepth = hc.getInt("maxDepth")
  val followLinks = hc.getBoolean("followLinks")

  val fileErrorBehavior: FileErrorBehavior =
    fileErrorBehaviors(hc.getString("fileErrorBehavior"))
  val directoryErrorBehavior: DirectoryErrorBehavior =
    directoryErrorBehaviors(hc.getString("directoryErrorBehavior"))

  val timeout = Timeout(Duration.fromNanos(hc.getNanoseconds("timeout")))
}

object Configuration {
  lazy val fileErrorBehaviors = Map(
    "terminate" -> Terminate,
    "skipSiblings" -> SkipSiblings,
    "continue" -> Continue
  )

  lazy val directoryErrorBehaviors = Map(
    "terminate" -> Terminate,
    "skipSubtree" -> SkipSubtree
  )

  trait ErrorBehavior

  /**
   * Specifies the behavior on file errors.
   */
  sealed trait FileErrorBehavior extends ErrorBehavior

  /**
   * Specifies the behavior on directory errors.
   */
  sealed trait DirectoryErrorBehavior extends ErrorBehavior

  /**
   * Terminates the tracer.
   */
  case object Terminate extends FileErrorBehavior with DirectoryErrorBehavior

  /**
   * Skips the subtree of a directory that cannot be read.
   */
  case object SkipSubtree extends DirectoryErrorBehavior

  /**
   * Skips siblings that come after an error.
   */
  case object SkipSiblings extends FileErrorBehavior

  /**
   * Continues with the next file after a unreadable file.
   */
  case object Continue extends FileErrorBehavior

  implicit def fileErrBehaviorConv(b: FileErrorBehavior): FileVisitResult =
    b match {
      case Terminate    => FileVisitResult.TERMINATE
      case SkipSiblings => FileVisitResult.SKIP_SIBLINGS
      case Continue     => FileVisitResult.CONTINUE
    }

  implicit def dirErrBehaviorConv(b: DirectoryErrorBehavior): FileVisitResult =
    b match {
      case Terminate   => FileVisitResult.TERMINATE
      case SkipSubtree => FileVisitResult.SKIP_SUBTREE
    }
}
