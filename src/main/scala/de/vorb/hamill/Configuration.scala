package de.vorb.hamill

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult

/**
 * Configuration object.
 */
trait Configuration {
  import Configuration.FileErrorBehavior
  import Configuration.DirectoryErrorBehavior

  def hidden: Boolean
  def recursive: Boolean
  def files: Boolean
  def directories: Boolean
  def maxDepth: Int
  def followLinks: Boolean
  def fileErrorBehavior: FileErrorBehavior
  def directoryErrorBehavior: DirectoryErrorBehavior
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

  sealed trait FileErrorBehavior
  sealed trait DirectoryErrorBehavior
  case object Terminate extends FileErrorBehavior with DirectoryErrorBehavior
  case object SkipSiblings extends FileErrorBehavior
  case object SkipSubtree extends DirectoryErrorBehavior
  case object Continue extends FileErrorBehavior

  implicit def fileErrorBehavior2FileVisitResult(b: FileErrorBehavior): FileVisitResult =
    b match {
      case Terminate => FileVisitResult.TERMINATE
      case SkipSiblings => FileVisitResult.SKIP_SIBLINGS
      case Continue => FileVisitResult.CONTINUE
    }

  implicit def directoryErrorBehavior2FileVisitResult(b: DirectoryErrorBehavior): FileVisitResult =
    b match {
      case Terminate => FileVisitResult.TERMINATE
      case SkipSubtree => FileVisitResult.SKIP_SUBTREE
    }
}