package ai.srl.experience.replay

trait ReplayBuffer[T]:

  def getBatch(): Array[T]

  def addOne(item: T): Unit

  /**
   * Add elements in order of iteration
   * @param items
   */
  def addAll(items: IterableOnce[T]): Unit =
    items.iterator.foreach(addOne(_))
    
  def getBatchSize(): Int

  def getBufferSize(): Int

  def getCurrentBufferSize(): Int
