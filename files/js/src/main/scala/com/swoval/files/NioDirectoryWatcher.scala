package com.swoval.files

import java.io.{ File, FileFilter }

import java.nio.file.{ Files, FileSystemLoopException, Path, Paths }

import com.swoval.files.DirectoryWatcher.Callback
import com.swoval.files.DirectoryWatcher.Event
import com.swoval.files.DirectoryWatcher.Event.{ Create, Modify, Delete }
import io.scalajs.nodejs
import io.scalajs.nodejs.fs.{ FSWatcher, FSWatcherOptions, Fs }

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.scalajs.js

/**
 * Native directory watcher implementation for Linux and Windows
 * @param onFileEvent The callback to run on file events
 */
class NioDirectoryWatcher(val onFileEvent: Callback) extends DirectoryWatcher {
  def this(onFileEvent: Callback, registerable: Registerable) = this(onFileEvent)
  private object DirectoryFilter extends FileFilter {
    override def accept(pathname: File): Boolean = pathname.isDirectory
  }
  private[this] val options = new FSWatcherOptions(recursive = false)
  override def register(path: Path, maxDepth: Int): Boolean = {
    def impl(p: Path, depth: Int): Boolean = watchedDirs get p.toString match {
      case None                          => add(p, depth)
      case Some(d) if d.maxDepth < depth => add(p, depth)
      case _                             => false
    }
    def add(p: Path, depth: Int): Boolean = {
      val callback: js.Function2[nodejs.EventType, String, Unit] =
        (tpe: nodejs.EventType, name: String) => {
          val watchPath = p.resolve(Paths.get(name))
          val exists = Files.exists(watchPath)
          val kind: Event.Kind = tpe match {
            case "rename" if !exists => Delete
            case _                   => Modify
          }
          val events = new mutable.ArrayBuffer[Event]()
          if (depth > 0 && Files.isDirectory(watchPath)) {
            try {
              if (register(watchPath, if (depth == Integer.MAX_VALUE) depth else depth - 1)) {
                QuickList.list(watchPath, depth - 1).asScala foreach { newPath =>
                  events += new Event(watchPath, Create)
                }
              }
            } catch {
              case _: FileSystemLoopException =>
            }
          }
          onFileEvent(new Event(watchPath, kind))
          events.foreach(onFileEvent(_))
        }
      watchedDirs
        .put(p.toString, WatchedDir(Fs.watch(p.toString, options, callback), depth))
        .foreach(_.watcher.close())
      true
    }
    Files.exists(path) && {
      val realPath = path.toRealPath()
      if (!path.equals(realPath) && watchedDirs.contains(realPath.toString))
        throw new FileSystemLoopException(path.toString());
      impl(path, maxDepth)
    } && (maxDepth == 0) || {
      QuickList
        .list(path, 0, true, new Filter[QuickFile] {
          override def accept(file: QuickFile) = file.isDirectory
        })
        .asScala
        .forall { dir =>
          register(dir.asPath,
                   if (maxDepth == Integer.MAX_VALUE) Integer.MAX_VALUE else maxDepth - 1)
        }
    }
  }
  override def unregister(path: Path): Unit = {
    watchedDirs.remove(path.toString) foreach (_.watcher.close())
  }

  override def close(): Unit = {
    watchedDirs.values foreach (_.watcher.close())
    watchedDirs.clear()
  }

  private[this] case class WatchedDir(watcher: FSWatcher, maxDepth: Int) {
    private[this] val compDepth = if (maxDepth == Integer.MAX_VALUE) maxDepth else maxDepth + 1
    def accept(base: Path, child: Path): Boolean = {
      base.equals(child) || base.relativize(child).getNameCount <= compDepth
    }
  }
  private[this] val watchedDirs = mutable.Map.empty[String, WatchedDir]
}
