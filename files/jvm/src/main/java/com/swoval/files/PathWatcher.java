package com.swoval.files;

import com.swoval.files.PathWatchers.Event;
import com.swoval.functional.Either;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Watches directories for file changes. The api permits recursive watching of directories unlike
 * the <a href="https://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html"
 * target="_blank"> java.nio.file.WatchService</a>. Some of the behavior may vary by platform due to
 * fundamental differences in the underlying file event apis. For example, Linux doesn't support
 * recursive directory monitoring via inotify, so it's possible in rare cases to miss file events
 * for newly created files in newly created directories. On OSX, it is difficult to disambiguate
 * file creation and modify events, so the {@link Event.Kind} is best effort, but should not be
 * relied upon to accurately reflect the state of the file.
 */
public interface PathWatcher extends AutoCloseable {

  /**
   * Register a path to monitor for file events. The watcher will only watch child subdirectories up
   * to maxDepth. For example, with a directory structure like /foo/bar/baz, if we register the path
   * /foo/ with maxDepth 0, we will be notified for any files that are created, updated or deleted
   * in foo, but not bar. If we increase maxDepth to 1, then the files in /foo/bar are monitored,
   * but not the files in /foo/bar/baz.
   *
   * @param path the directory to watch for file events
   * @param maxDepth the maximum maxDepth of subdirectories to watch
   * @return an {@link com.swoval.functional.Either} containing the result of the registration or an
   *     IOException if registration fails. This method should be idempotent and return true the
   *     first time the directory is registered or when the depth is changed. Otherwise it should
   *     return false.
   */
  Either<IOException, Boolean> register(Path path, int maxDepth);

  /**
   * Stop watching a path.
   *
   * @param path the path to remove from monitoring
   */
  void unregister(Path path);

  /** Catch any exceptions in subclasses. */
  @Override
  void close();
}
