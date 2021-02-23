package ai.srl.experience.store

trait ExperienceStore[InputStep, OutputStep]:
  def getBatch(): Array[OutputStep]

  def addOne(item: InputStep): Unit

  /**
   * Add elements in order of iteration
   *
   * @param items
   */
  def addAll(items: IterableOnce[InputStep]): Unit =
    items.iterator.foreach(addOne(_))

  def getBatchSize(): Int

  def getBufferSize(): Int

  def getCurrentBufferSize(): Int
