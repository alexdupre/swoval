// Do not edit this file manually. It is autogenerated.

package com.swoval.files

import com.swoval.files.FileTreeDataViews.Entry
import java.io.IOException
import java.nio.file.Path
import java.util.List

trait UpdatableFileTreeDataView[T <: AnyRef] {

  /**
   * Updates the CachedDirectory entry for a particular typed path.
   *
   * @param typedPath the path to update
   * @return a list of updates for the path. When the path is new, the updates have the
   *     oldCachedPath field set to null and will contain all of the children of the new path when
   *     it is a directory. For an existing path, the List contains a single Updates that contains
   *     the previous and new [[Entry]].
   */
  def update(typedPath: TypedPath): FileTreeViews.Updates[T]

  /**
   * Remove a path from the directory.
   *
   * @param path the path to remove
   * @return a List containing the Entry instances for the removed path. The result also contains
   *     the cache entries for any children of the path when the path is a non-empty directory.
   */
  def remove(path: Path): List[Entry[T]]

}
