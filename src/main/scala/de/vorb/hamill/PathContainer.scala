package de.vorb.hamill

import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

/**
 * Container for Path.
 * 
 * @see java.nio.file.Path
 */
trait PathContainer

/**
 * Holds a reference to a file as a path and either an IOException or the file's
 * attributes.
 */
case class File(path: Path, result: Either[IOException, BasicFileAttributes]) extends PathContainer

/**
 * Holds a reference to a directory as a path and the directory's attributes.
 */
case class Directory(path: Path, attrs: BasicFileAttributes) extends PathContainer