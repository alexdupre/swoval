// Do not edit this file manually. It is autogenerated.

package com.swoval.files

import com.swoval.files.Directory.Entry
import com.swoval.files.Directory.Observer
import com.swoval.files.Directory.OnChange
import com.swoval.files.Directory.OnError
import com.swoval.files.Directory.OnUpdate
import java.io.IOException
import java.nio.file.Path
import java.util.ArrayList
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.concurrent.atomic.AtomicInteger
import Observers._

private[files] object Observers {

  /**
   * Simple observer that fires the same callback for all regular events and ignores any errors.
   *
   * @param onchange The callback to fire when a file is created/updated/deleted
   * @tparam T The generic type of the [[Directory.Entry]]
   * @return An [[Observer]] instance
   */
  def apply[T](onchange: OnChange[T]): Observer[T] = new Observer[T]() {
    override def onCreate(newEntry: Entry[T]): Unit = {
      onchange.apply(newEntry)
    }

    override def onDelete(oldEntry: Entry[T]): Unit = {
      onchange.apply(oldEntry)
    }

    override def onUpdate(oldEntry: Entry[T], newEntry: Entry[T]): Unit = {
      onchange.apply(newEntry)
    }

    override def onError(path: Path, e: IOException): Unit = {}
  }

  def apply[T](oncreate: OnChange[T],
               onupdate: OnUpdate[T],
               ondelete: OnChange[T],
               onerror: OnError): Observer[T] = new Observer[T]() {
    override def onCreate(newEntry: Entry[T]): Unit = {
      oncreate.apply(newEntry)
    }

    override def onDelete(oldEntry: Entry[T]): Unit = {
      ondelete.apply(oldEntry)
    }

    override def onUpdate(oldEntry: Entry[T], newEntry: Entry[T]): Unit = {
      onupdate.apply(oldEntry, newEntry)
    }

    override def onError(path: Path, ex: IOException): Unit = {
      onerror.apply(path, ex)
    }
  }

}

/**
 * Container class that wraps multiple [[Observer]] and runs the callbacks for each whenever the
 * [[FileCache]] detects an event.
 *
 * @tparam T The data type for the [[FileCache]] to which the observers correspond
 */
private[files] class Observers[T] extends Observer[T] with AutoCloseable {

  private val counter: AtomicInteger = new AtomicInteger(0)

  private val observers: Map[Integer, Observer[T]] = new HashMap()

  override def onCreate(newEntry: Entry[T]): Unit = {
    var cbs: List[Observer[T]] = null
    observers.synchronized {
      cbs = new ArrayList(observers.values)
    }
    val it: Iterator[Observer[T]] = cbs.iterator()
    while (it.hasNext) it.next().onCreate(newEntry)
  }

  override def onDelete(oldEntry: Entry[T]): Unit = {
    var cbs: List[Observer[T]] = null
    observers.synchronized {
      cbs = new ArrayList(observers.values)
    }
    val it: Iterator[Observer[T]] = cbs.iterator()
    while (it.hasNext) it.next().onDelete(oldEntry)
  }

  override def onUpdate(oldEntry: Entry[T], newEntry: Entry[T]): Unit = {
    var cbs: List[Observer[T]] = null
    observers.synchronized {
      cbs = new ArrayList(observers.values)
    }
    val it: Iterator[Observer[T]] = cbs.iterator()
    while (it.hasNext) it.next().onUpdate(oldEntry, newEntry)
  }

  override def onError(path: Path, exception: IOException): Unit = {
    var cbs: List[Observer[T]] = null
    observers.synchronized {
      cbs = new ArrayList(observers.values)
    }
    val it: Iterator[Observer[T]] = cbs.iterator()
    while (it.hasNext) it.next().onError(path, exception)
  }

  def addObserver(observer: Observer[T]): Int = {
    val key: Int = counter.getAndIncrement
    observers.synchronized {
      observers.put(key, observer)
    }
    key
  }

  def removeObserver(handle: Int): Unit = {
    observers.synchronized {
      observers.remove(handle)
    }
  }

  override def close(): Unit = {
    observers.clear()
  }

}
