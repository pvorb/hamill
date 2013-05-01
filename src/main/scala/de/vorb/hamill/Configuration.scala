package de.vorb.hamill

import java.nio.file.FileVisitResult

import scala.language.implicitConversions

/**
 * Configuration object.
 */
trait Configuration {
  def hidden: Boolean
  def recursive: Boolean
  def files: Boolean
  def directories: Boolean
  def maxDepth: Int
  def followLinks: Boolean
  def fileErrorBehavior: Configuration.FileErrorBehavior
  def directoryErrorBehavior: Configuration.DirectoryErrorBehavior
}

object Configuration {

  /**
   * Example Configuration.
   */
  object Default extends Configuration {
    def hidden = false
    def recursive = true
    def files = true
    def directories = true
    def maxDepth = Int.MaxValue
    def followLinks = false
    def fileErrorBehavior = Terminate
    def directoryErrorBehavior = Terminate
  }

  /**
   * Specifies the behavior on file errors.
   */
  sealed trait FileErrorBehavior

  /**
   * Specifies the behavior on directory errors.
   */
  sealed trait DirectoryErrorBehavior

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
