package ai.srl.experience.replay

import ai.srl.collection.SumTree.ValuedItem
import ai.srl.collection.{CyclicArray, MaxSize, Size, SumTree}
import ai.srl.experience.config.ReplayConfig
import ai.srl.experience.store.IndexedPrioritisedExperienceStore.{IndexedItem, PrioritisedIndex, PrioritisedIndexedItem}
import ai.srl.experience.replay.IndexedPrioritisedReplayBuffer

import scala.reflect.ClassTag
import alleycats.Empty
import cats.kernel.Eq

import scala.util.Random

class SumTreePrioritisedReplayBuffer[T: Empty: ClassTag](val batchSize: Int, val bufferSize: Int, val defaultPriority: Float):
  assert(batchSize > 0)
  assert(batchSize <= bufferSize)
  assert(defaultPriority >= 0)

  private val items = new SumTree[T](bufferSize)
  private var actualSize = 0
  private val random = Random()
  private var lastBatchIndexes: Seq[Int] = List.empty
  

  private def getBatchInternal(): IndexedSeq[(ValuedItem[T], Int)] =
    if actualSize == 0 then 
      IndexedSeq.empty
    else
      val priorities = (1 to batchSize).map(_ => random.nextFloat() * items.totalValue())
      val prioritisedIndexedBatch = priorities.map(items.get)
      lastBatchIndexes = prioritisedIndexedBatch.map(_._2)
      prioritisedIndexedBatch
  

object SumTreePrioritisedReplayBuffer:
  given [T: Empty : ClassTag]: IndexedPrioritisedReplayBuffer[SumTreePrioritisedReplayBuffer[T], T] with
    extension (prb: SumTreePrioritisedReplayBuffer[T])
      def updateLastBatch(newPriorities: Seq[Float]): Unit =
        assert(prb.lastBatchIndexes.size == newPriorities.size)
        prb.lastBatchIndexes.iterator.zip(newPriorities).foreach(prb.items.updateValue)
      
      def getBatch(): Array[T] =
        prb.getBatchInternal().map(_._1.item).toArray

      def getBatchSize(): Int = prb.batchSize
      
      @throws(classOf[Exception])
      def addOnePrioritised(item: T, priority: Float): Unit =
        prb.actualSize = math.min(prb.actualSize + 1, prb.bufferSize)
        prb.items.addOne(ValuedItem(item, priority))

      // TODO should I use nextFloat or nextDouble and convert it to float after multiplication?
      @throws(classOf[Exception])
      def addOne(item: T): Unit = prb.addOnePrioritised(item, prb.defaultPriority)

      def size: Int = prb.actualSize

      def maxSize(): Int = prb.bufferSize 
      
      def clearAll(): Unit =
        prb.items.clearAll()
        prb.actualSize = 0
        prb.lastBatchIndexes = List.empty

      //////////////// Indexed methods:
      
      def getPrioritisedIndexedBatch(): Array[PrioritisedIndexedItem[T]] =
        prb.getBatchInternal().map(item => PrioritisedIndexedItem(item._1.item, item._1.value, item._2)).toArray

      def getIndexedBatch(): Array[IndexedItem[T]] =
        prb.getBatchInternal().map(item => IndexedItem(item._1.item, item._2)).toArray

      override def update(prioritisedIndex: PrioritisedIndex): Unit =
        prb.items.updateValue(prioritisedIndex.idx, prioritisedIndex.priority)


  import ai.srl.collection.GetIterator
  given [T: ClassTag: Empty: Eq]: GetIterator[SumTreePrioritisedReplayBuffer[T], T] with
    extension (tree: SumTreePrioritisedReplayBuffer[T])
      def iterator = tree.items.iterator
