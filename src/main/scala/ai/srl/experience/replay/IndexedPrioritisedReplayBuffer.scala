package ai.srl.experience.replay

import ai.srl.experience.replay.IndexedPrioritisedReplayBuffer.{PrioritisedIndex, IndexedItem, PrioritisedIndexedItem}

trait IndexedPrioritisedReplayBuffer[T] extends PrioritisedReplayBuffer[T]:

  def getPrioritisedIndexedBatch(): Array[PrioritisedIndexedItem[T]]
  
  def getIndexedBatch(): Array[IndexedItem[T]]

  def addOnePrioritised(item: T, priority: Float): Unit

  def update(prioritisedIndex: PrioritisedIndex): Unit

  def updateBatch(prioritisedIndexes: IterableOnce[PrioritisedIndex]): Unit =
    prioritisedIndexes.iterator.foreach(update)
  /**
   * Add elements in order of iteration
   * @param items
   */

object IndexedPrioritisedReplayBuffer:
  case class PrioritisedIndexedItem[T](item: T, priority: Float, idx: Int)
  case class IndexedItem[T](item: T, idx: Int)
  opaque type PrioritisedIndex = (Int, Float)

  object PrioritisedIndex:
    def apply(idx: Int, priority: Float): PrioritisedIndex = (idx, priority)

  extension (prioritisedIndex: PrioritisedIndex)
    def idx = prioritisedIndex._1
    def priority = prioritisedIndex._2
    def tuple: (Int, Float) = (prioritisedIndex._1, prioritisedIndex._2)
