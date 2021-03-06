// Do not edit this file manually. It is autogenerated.

package com.swoval.files.apple

object Flags {

  object CreateFlags {

    val None: Int = 0

    val UseCFTypes: Int = 0x00000001

    val NoDefer: Int = 0x00000002

    val WatchRoot: Int = 0x00000004

    val IgnoreSelf: Int = 0x00000008

    val FileEvents: Int = 0x00000010

    val MarkSelf: Int = 0x00000020

    val UseExtendedData: Int = 0x00000040

  }

  /**
   * Wrapper around the apple file events FsEventStreamCreateFlags. Using this helper class avoids
   * directly having to use bit manipulation.
   */
  class Create(val value: Int) {

    def this() = this(CreateFlags.None)

    def getValue(): Int = value

    def setUseCFTypes(): Create = new Create(value | CreateFlags.UseCFTypes)

    def setNoDefer(): Create = new Create(value | CreateFlags.NoDefer)

    def setWatchRoot(): Create = new Create(value | CreateFlags.WatchRoot)

    def setIgnoreSelf(): Create = new Create(value | CreateFlags.IgnoreSelf)

    def setFileEvents(): Create = new Create(value | CreateFlags.FileEvents)

    def setMarkSelf(): Create = new Create(value | CreateFlags.MarkSelf)

    def setUseExtendedData(): Create =
      new Create(value | CreateFlags.UseExtendedData)

    def hasUseCFTypes(): Boolean = (value & CreateFlags.UseCFTypes) > 0

    def hasNoDefer(): Boolean = (value & CreateFlags.NoDefer) > 0

    def hasWatchRoot(): Boolean = (value & CreateFlags.WatchRoot) > 0

    def hasIgnoreSelf(): Boolean = (value & CreateFlags.IgnoreSelf) > 0

    def hasFileEvents(): Boolean = (value & CreateFlags.FileEvents) > 0

    def hasMarkSelf(): Boolean = (value & CreateFlags.MarkSelf) > 0

    def hasUseExtendedData(): Boolean =
      (value & CreateFlags.UseExtendedData) > 0

  }

  object Event {

    val None: Int = 0

    val MustScanSubDirs: Int = 0x00000001

    val UserDropped: Int = 0x00000002

    val KernelDropped: Int = 0x00000004

    val EventIdsWrapped: Int = 0x00000008

    val HistoryDone: Int = 0x00000010

    val RootChanged: Int = 0x00000020

    val Mount: Int = 0x00000040

    val Unmount: Int = 0x00000080

    val ItemChangeOwner: Int = 0x00004000

    val ItemCreated: Int = 0x00000100

    val ItemFinderInfoMod: Int = 0x00002000

    val ItemInodeMetaMod: Int = 0x00000400

    val ItemIsDir: Int = 0x00020000

    val ItemIsFile: Int = 0x00010000

    val ItemIsHardlink: Int = 0x00100000

    val ItemIsLastHardlink: Int = 0x00200000

    val ItemIsSymlink: Int = 0x00040000

    val ItemModified: Int = 0x00001000

    val ItemRemoved: Int = 0x00000200

    val ItemRenamed: Int = 0x00000800

    val ItemXattrMod: Int = 0x00008000

    val OwnEvent: Int = 0x00080000

    val ItemCloned: Int = 0x00400000

    def flags(flag: com.swoval.files.apple.Event): String = {
      val builder: StringBuilder = new StringBuilder()
      builder.append("\n  mustScanSubDirs: " + flag.mustScanSubDirs())
      builder.append("\n  userDropped: " + flag.userDropped())
      builder.append("\n  kernelDropped: " + flag.kernelDropped())
      builder.append("\n  eventIdsWrapped: " + flag.eventIdsWrapped())
      builder.append("\n  historyDone: " + flag.historyDone())
      builder.append("\n  rootChanged: " + flag.rootChanged())
      builder.append("\n  mount: " + flag.mount())
      builder.append("\n  unmount: " + flag.unmount())
      builder.append("\n  itemChangeOwner: " + flag.itemChangeOwner())
      builder.append("\n  itemCreated: " + flag.itemCreated())
      builder.append("\n  itemFinderInfoMod: " + flag.itemFinderInfoMod())
      builder.append("\n  itemInodeMetaMod: " + flag.itemInodeMetaMod())
      builder.append("\n  itemIsDir: " + flag.itemIsDir())
      builder.append("\n  itemIsFile: " + flag.itemIsFile())
      builder.append("\n  itemIsHardlink: " + flag.itemIsHardlink())
      builder.append("\n  itemIsLastHardlink: " + flag.itemIsLastHardlink())
      builder.append("\n  itemIsSymlink: " + flag.itemIsSymlink())
      builder.append("\n  itemModified: " + flag.itemModified())
      builder.append("\n  itemRemoved: " + flag.itemRemoved())
      builder.append("\n  itemRenamed: " + flag.itemRenamed())
      builder.append("\n  itemXattrMod: " + flag.itemXattrMod())
      builder.append("\n  ownEvent: " + flag.ownEvent())
      builder.append("\n  itemCloned: " + flag.itemCloned())
      "EventStreamFlags(\n" + builder.toString + "\n)"
    }

  }

}
