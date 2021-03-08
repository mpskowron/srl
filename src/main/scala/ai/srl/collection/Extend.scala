package ai.srl.collection

trait Extend[T, Item]:
  extension (t: T)
    def addOne(item: Item): Unit

    /**
     * Add elements in order of iteration
     *
     * @param items
     */
    def addAll(items: IterableOnce[Item]): Unit =
      items.iterator.foreach(addOne(_))

