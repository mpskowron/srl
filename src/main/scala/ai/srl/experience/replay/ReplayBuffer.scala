package ai.srl.experience.replay

trait ReplayBuffer[T]:

  def getBatch(): Array[T]

  def addElement(element: T): Unit

  /**
   * Add elements in order of iteration
   * @param elements
   */
  def addElements(elements: Iterable[T]): Unit =
    elements.foreach(addElement(_))
    
  def getBatchSize(): Int

  def getBufferSize(): Int

  def getCurrentBufferSize(): Int
