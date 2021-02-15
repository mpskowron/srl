package ai.srl.experience.replay

import ai.srl.collection.SumTree.ValuedItem
import ai.srl.collection.{CyclicArray, SumTree}
import ai.srl.experience.config.ReplayConfig
import ai.srl.experience.replay.IndexedPrioritisedReplayBuffer.{IndexedItem, PrioritisedIndex, PrioritisedIndexedItem}
import ai.srl.experience.replay.ReplayBuffer

import scala.reflect.ClassTag
import alleycats.Empty

import scala.util.Random

class SumTreePrioritisedReplayBuffer[T: Empty: ClassTag](val batchSize: Int, val bufferSize: Int, val defaultPriority: Float) extends 
  IndexedPrioritisedReplayBuffer[T]:
  assert(batchSize > 0)
  assert(batchSize <= bufferSize)
  assert(defaultPriority >= 0)

  private val items = new SumTree[T](bufferSize)
  private var actualSize = 0
  private val random = Random()
  private var lastBatchIndexes: Seq[Int] = List.empty

  // TODO should I use nextFloat or nextDouble and convert it to float after multiplication?
  override def getBatch(): Array[T] =
    getBatchInternal().map(_._1.item).toArray

  @throws(classOf[Exception])
  override def addOne(item: T): Unit = addOnePrioritised(item, defaultPriority)
  
  override def getBatchSize(): Int = batchSize

  override def getBufferSize(): Int = bufferSize

  override def getPrioritisedIndexedBatch(): Array[PrioritisedIndexedItem[T]] =
    getBatchInternal().map(item => PrioritisedIndexedItem(item._1.item, item._1.value, item._2)).toArray
  
  override def getIndexedBatch(): Array[IndexedItem[T]] =
    getBatchInternal().map(item => IndexedItem(item._1.item, item._2)).toArray
  
  private def getBatchInternal(): IndexedSeq[(ValuedItem[T], Int)] =
    val priorities = (1 to batchSize).map(_ => random.nextFloat() * items.totalValue())
    val prioritisedIndexedBatch = priorities.map(items.get)
    lastBatchIndexes = prioritisedIndexedBatch.map(_._2)
    prioritisedIndexedBatch

  @throws(classOf[Exception])
  override def addOnePrioritised(item: T, priority: Float): Unit =
    actualSize = math.min(actualSize + 1, bufferSize)
    items.addOne(ValuedItem(item, priority))

  override def update(prioritisedIndex: PrioritisedIndex): Unit = 
    items.updateValue(prioritisedIndex.idx, prioritisedIndex.priority)

  override def updateLastBatch(newPriorities: Seq[Float]): Unit =
    assert(lastBatchIndexes.size == newPriorities.size)
    lastBatchIndexes.iterator.zip(newPriorities).foreach(items.updateValue)

  override def getCurrentBufferSize(): Int = actualSize
