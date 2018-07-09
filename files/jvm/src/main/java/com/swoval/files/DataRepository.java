package com.swoval.files;

import com.swoval.files.Directory.Entry;
import com.swoval.files.Directory.EntryFilter;
import java.nio.file.Path;
import java.util.List;

/**
 * A repository for which each {@link java.nio.file.Path} has an associated data value.
 *
 * @param <T> the data value for each path
 */
public interface DataRepository<T> extends AutoCloseable {
  /**
   * List all of the files for the {@code path</code> that are accepted by the <code>filter}.
   *
   * @param path the path to list. If this is a file, returns a list containing the Entry for the
   *     file or an empty list if the file is not monitored by the path.
   * @param maxDepth the maximum depth of subdirectories to return
   * @param filter include only paths accepted by this
   * @return a List of Entry instances accepted by the filter. The list will be empty if the path is
   *     not a subdirectory of this Directory or if it is a subdirectory, but the Directory was
   *     created without the recursive flag.
   */
  List<Entry<T>> list(final Path path, final int maxDepth, final EntryFilter<? super T> filter);
}
