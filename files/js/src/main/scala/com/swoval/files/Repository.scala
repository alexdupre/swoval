// Do not edit this file manually. It is autogenerated.

package com.swoval.files

import com.swoval.functional.Filter
import java.nio.file.Path
import java.util.List

trait Repository extends AutoCloseable {

  /**
   * List all of the files for the {@code path}, returning only those files that are accepted by the
   * provided filter.
   *
   * @param path the root path to list
   * @param maxDepth the maximum depth of subdirectories to query
   * @param filter include only paths accepted by the filter
   * @return a List of [[java.nio.file.Path]] instances accepted by the filter.
   */
  def list(path: Path, maxDepth: Int, filter: Filter[_ >: Path]): List[Path]

}
