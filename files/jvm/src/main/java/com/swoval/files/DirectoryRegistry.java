package com.swoval.files;

import static java.util.Map.Entry;

import com.swoval.functional.Filter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

interface DirectoryRegistry extends Filter<Path>, AutoCloseable {
  boolean addDirectory(final Path path, final int maxDepth);

  int maxDepthFor(final Path path);

  Map<Path, Integer> registered();

  void removeDirectory(final Path path);

  boolean acceptPrefix(final Path path);

  @Override
  void close();
}

class DirectoryRegistries {
  private DirectoryRegistries() {}

  static Filter<TypedPath> toTypedPathFilter(final DirectoryRegistry registry) {
    return new Filter<TypedPath>() {
      @Override
      public boolean accept(final TypedPath typedPath) {
        return registry.accept(typedPath.getPath());
      }
    };
  }
}

class DirectoryRegistryImpl implements DirectoryRegistry {
  private final LockableMap<Path, RegisteredDirectory> registeredDirectoriesByPath =
      new LockableMap<>(new ConcurrentHashMap<Path, RegisteredDirectory>());

  @Override
  public boolean addDirectory(final Path path, final int maxDepth) {
    if (registeredDirectoriesByPath.lock()) {
      try {
        final RegisteredDirectory registeredDirectory = registeredDirectoriesByPath.get(path);
        if (registeredDirectory == null || maxDepth > registeredDirectory.maxDepth) {
          registeredDirectoriesByPath.put(path, new RegisteredDirectory(path, maxDepth));
          return true;
        } else {
          return false;
        }
      } finally {
        registeredDirectoriesByPath.unlock();
      }
    } else {
      return false;
    }
  }

  @Override
  public int maxDepthFor(final Path path) {
    if (registeredDirectoriesByPath.lock()) {
      try {
        int maxDepth = Integer.MIN_VALUE;
        final Iterator<RegisteredDirectory> it = registeredDirectoriesByPath.values().iterator();
        while (it.hasNext()) {
          final RegisteredDirectory dir = it.next();
          if (path.startsWith(dir.path)) {
            final int depth = dir.path.equals(path) ? 0 : dir.path.relativize(path).getNameCount();
            final int possibleMaxDepth = dir.maxDepth - depth;
            if (possibleMaxDepth > maxDepth) {
              maxDepth = possibleMaxDepth;
            }
          }
        }
        return maxDepth;
      } finally {
        registeredDirectoriesByPath.unlock();
      }
    } else {
      return -1;
    }
  }

  @Override
  public Map<Path, Integer> registered() {
    if (registeredDirectoriesByPath.lock()) {
      try {
        final Map<Path, Integer> result = new HashMap<>();
        final Iterator<RegisteredDirectory> it = registeredDirectoriesByPath.values().iterator();
        while (it.hasNext()) {
          final RegisteredDirectory dir = it.next();
          result.put(dir.path, dir.maxDepth);
        }
        return result;
      } finally {
        registeredDirectoriesByPath.unlock();
      }
    } else {
      return Collections.emptyMap();
    }
  }

  @Override
  public void removeDirectory(final Path path) {
    if (registeredDirectoriesByPath.lock()) {
      try {
        registeredDirectoriesByPath.remove(path);
      } finally {
        registeredDirectoriesByPath.unlock();
      }
    }
  }

  private boolean acceptImpl(final Path path, final boolean acceptPrefix) {
    if (registeredDirectoriesByPath.lock()) {
      try {
        boolean result = false;
        final Iterator<Entry<Path, RegisteredDirectory>> it =
            registeredDirectoriesByPath.iterator();
        while (!result && it.hasNext()) {
          final Entry<Path, RegisteredDirectory> entry = it.next();
          final RegisteredDirectory registeredDirectory = entry.getValue();
          final Path watchPath = entry.getKey();
          if (acceptPrefix && watchPath.startsWith(path)) {
            result = true;
          } else if (path.startsWith(watchPath)) {
            result = registeredDirectory.accept(path);
          }
        }
        return result;
      } finally {
        registeredDirectoriesByPath.unlock();
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean accept(final Path path) {
    return acceptImpl(path, false);
  }

  @Override
  public boolean acceptPrefix(final Path path) {
    return acceptImpl(path, true);
  }

  @Override
  public void close() {
    registeredDirectoriesByPath.clear();
  }

  @Override
  public String toString() {
    if (registeredDirectoriesByPath.lock()) {
      try {
        final StringBuilder result = new StringBuilder();
        result.append("DirectoryRegistry:\n");
        final Iterator<RegisteredDirectory> it = registeredDirectoriesByPath.values().iterator();
        while (it.hasNext()) {
          result.append("  ");
          result.append(it.next());
          result.append('\n');
        }
        return result.toString();
      } finally {
        registeredDirectoriesByPath.unlock();
      }
    } else {
      return "";
    }
  }

  private static class RegisteredDirectory {
    final Path path;
    final int maxDepth;
    final int compMaxDepth;

    RegisteredDirectory(final Path path, final int maxDepth) {
      this.path = path;
      this.maxDepth = maxDepth;
      compMaxDepth = maxDepth == Integer.MAX_VALUE ? maxDepth : maxDepth + 1;
    }

    public boolean accept(final Path path) {
      return path.startsWith(this.path)
          && (path.equals(this.path) || this.path.relativize(path).getNameCount() <= compMaxDepth);
    }

    @Override
    public String toString() {
      return "RegisteredDirectory(path = " + path + ", depth = " + maxDepth + ")";
    }
  }
}
