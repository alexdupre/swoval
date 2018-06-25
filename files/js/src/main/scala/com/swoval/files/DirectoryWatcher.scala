// Do not edit this file manually. It is autogenerated.

package com.swoval.files

import com.swoval.files.apple.AppleDirectoryWatcher
import com.swoval.files.apple.Flags
import com.swoval.functional.Consumer
import com.swoval.functional.Either
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import DirectoryWatcher._

object DirectoryWatcher {

  /**
   * Create a DirectoryWatcher for the runtime platform.
   *
   * @param latency The latency used by the [[com.swoval.files.apple.AppleDirectoryWatcher]] on
   *     osx
   * @param timeUnit The TimeUnit or the latency parameter
   * @param flags Flags used by the apple directory watcher
   * @param callback [[com.swoval.functional.Consumer]] to run on file events
   * @param executor provides a single threaded context to manage state
   * @return DirectoryWatcher for the runtime platform
   *     initialized
   *     initialization
   */
  def defaultWatcher(latency: Long,
                     timeUnit: TimeUnit,
                     flags: Flags.Create,
                     callback: Consumer[DirectoryWatcher.Event],
                     executor: Executor): DirectoryWatcher =
    if (Platform.isMac)
      new AppleDirectoryWatcher(timeUnit.toNanos(latency) / 1.0e9, flags, callback, executor)
    else new NioDirectoryWatcher(callback, executor)

  /**
   * Create a platform compatible DirectoryWatcher.
   *
   * @param callback [[com.swoval.functional.Consumer]] to run on file events
   * @param executor The executor to run internal callbacks on
   * @return DirectoryWatcher for the runtime platform
   *     initialized
   *     initialization
   */
  def defaultWatcher(callback: Consumer[DirectoryWatcher.Event],
                     executor: Executor): DirectoryWatcher =
    defaultWatcher(10,
                   TimeUnit.MILLISECONDS,
                   new Flags.Create().setNoDefer().setFileEvents(),
                   callback,
                   executor)

  /**
   * Instantiates new [[DirectoryWatcher]] instances with a [[com.swoval.functional.Consumer]]. This is primarily so that the [[DirectoryWatcher]] in
   * [[FileCache]] may be changed in testing.
   */
  trait Factory {

    /**
     * Creates a new DirectoryWatcher
     *
     * @param callback The callback to invoke on directory updates
     * @param executor The executor on which internal updates are invoked
     * @return A DirectoryWatcher instance
     *     this can occur on mac
     *     and windows
     */
    def create(callback: Consumer[DirectoryWatcher.Event], executor: Executor): DirectoryWatcher

  }

  val DEFAULT_FACTORY: Factory = new Factory() {
    override def create(callback: Consumer[DirectoryWatcher.Event],
                        executor: Executor): DirectoryWatcher =
      defaultWatcher(callback, executor)
  }

  object Event {

    val Create: Kind = new Kind("Create")

    val Delete: Kind = new Kind("Delete")

    val Modify: Kind = new Kind("Modify")

    val Overflow: Kind = new Kind("Overflow")

    /**
     * An enum like class to indicate the type of file event. It isn't an actual enum because the
     * scala.js codegen has problems with enum types.
     */
    class Kind(private val name: String) {

      override def toString(): String = name

      override def equals(other: Any): Boolean = other match {
        case other: Kind => other.name == this.name
        case _           => false

      }

      override def hashCode(): Int = name.hashCode

    }

  }

  /**
 Container for [[DirectoryWatcher]] events
   */
  class Event(val path: Path, val kind: Event.Kind) {

    override def equals(other: Any): Boolean = other match {
      case other: Event => {
        val that: Event = other
        this.path == that.path && this.kind == that.kind
      }
      case _ => false

    }

    override def hashCode(): Int = path.hashCode ^ kind.hashCode

    override def toString(): String = "Event(" + path + ", " + kind + ")"

  }

}

/**
 * Watches directories for file changes. The api permits recursive watching of directories unlike
 * the [[https://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html  java.nio.file.WatchService]]. Some of the behavior may vary by platform due to
 * fundamental differences in the underlying file event apis. For example, Linux doesn't support
 * recursive directory monitoring via inotify, so it's possible in rare cases to miss file events
 * for newly created files in newly created directories. On OSX, it is difficult to disambiguate
 * file creation and modify events, so the [[DirectoryWatcher.Event.Kind]] is best effort, but
 * should not be relied upon to accurately reflect the state of the file.
 */
abstract class DirectoryWatcher extends AutoCloseable {

  /**
   * Register a path to monitor for file events. The watcher will only watch child subdirectories up
   * to maxDepth. For example, with a directory structure like /foo/bar/baz, if we register the path
   * /foo/ with maxDepth 0, we will be notified for any files that are created, updated or deleted
   * in foo, but not bar. If we increase maxDepth to 1, then the files in /foo/bar are monitored,
   * but not the files in /foo/bar/baz.
   *
   * @param path The directory to watch for file events
   * @param maxDepth The maximum maxDepth of subdirectories to watch
   * @return an [[com.swoval.functional.Either]] containing the result of the registration or an
   *     IOException if registration fails. This method should be idempotent and return true the
   *     first time the directory is registered or when the depth is changed. Otherwise it should
   *     return false.
   */
  def register(path: Path, maxDepth: Int): Either[IOException, Boolean]

  /**
   * Register a path to monitor for file events. The monitoring may be recursive.
   *
   * @param path The directory to watch for file events
   * @param recursive Toggles whether or not to monitor subdirectories
   * @return an [[com.swoval.functional.Either]] containing the result of the registration or an
   *     IOException if registration fails. This method should be idempotent and return true the
   *     first time the directory is registered or when the depth is changed. Otherwise it should
   *     return false.
   */
  def register(path: Path, recursive: Boolean): Either[IOException, Boolean] =
    register(path, if (recursive) java.lang.Integer.MAX_VALUE else 0)

  /**
   * Register a path to monitor for file events recursively.
   *
   * @param path The directory to watch for file events
   * @return an [[com.swoval.functional.Either]] containing the result of the registration or an
   *     IOException if registration fails. This method should be idempotent and return true the
   *     first time the directory is registered or when the depth is changed. Otherwise it should
   *     return false.
   */
  def register(path: Path): Either[IOException, Boolean] = register(path, true)

  /**
   * Stop watching a directory
   *
   * @param path The directory to remove from monitoring
   */
  def unregister(path: Path): Unit

  /**
 Catch any exceptions in subclasses.
   */
  override def close(): Unit = {}

}
