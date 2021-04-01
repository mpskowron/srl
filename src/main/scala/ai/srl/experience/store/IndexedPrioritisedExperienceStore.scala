package ai.srl.experience.store

import ai.srl.experience.store.IndexedPrioritisedExperienceStore.{IndexedItem, PrioritisedIndex, PrioritisedIndexedItem}

trait IndexedPrioritisedExperienceStore[S, In, Out] extends PrioritisedExperienceStore[S, In, Out]:
  extension (store: S)
    def getPrioritisedIndexedBatch(): Array[PrioritisedIndexedItem[Out]]
    
    def getIndexedBatch(): Array[IndexedItem[Out]]

    def update(prioritisedIndex: PrioritisedIndex): Unit

    def updateBatch(prioritisedIndexes: IterableOnce[PrioritisedIndex]): Unit =
      prioritisedIndexes.iterator.foreach(update)

object IndexedPrioritisedExperienceStore:
  case class PrioritisedIndexedItem[T](item: T, priority: Float, idx: Int)
  case class IndexedItem[T](item: T, idx: Int)
  opaque type PrioritisedIndex = (Int, Float)

  object PrioritisedIndex:
    def apply(idx: Int, priority: Float): PrioritisedIndex = (idx, priority)

  extension (prioritisedIndex: PrioritisedIndex)
    def idx = prioritisedIndex._1
    def priority = prioritisedIndex._2
    def tuple: (Int, Float) = (prioritisedIndex._1, prioritisedIndex._2)

  
