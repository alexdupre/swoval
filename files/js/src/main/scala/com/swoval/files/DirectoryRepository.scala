// Do not edit this file manually. It is autogenerated.

package com.swoval.files

import com.swoval.functional.Filter
import java.nio.file.Path
import java.util.List

/**
 * A repository for a directory. The [[com.swoval.files.Repository.list]]
 * method will only return non-empty results for paths that are children of the root directory,
 * specified by [[com.swoval.files.DirectoryRepository.getPath]].
 */
trait DirectoryRepository extends Repository {

  /**
   * Return the path of the root directory.
   *
   * @return the path of the root directory.
   */
  def getPath(): Path

  /**
   * List all of the files in the root directory, returning only those files that are accepted by
   * the provided filter.
   *
   * @param maxDepth the maximum depth of subdirectories to query
   * @param filter include only paths accepted by the filter
   * @return a List of [[java.nio.file.Path]] instances accepted by the filter.
   */
  def list(maxDepth: Int, filter: Filter[_ >: Path]): List[Path]

}
