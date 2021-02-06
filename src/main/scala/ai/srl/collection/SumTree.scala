package ai.srl.collection

import ai.srl.collection.Closeable.closeIfNeeded
import ai.srl.collection.SumTree.{Node, ValuedItem}

import scala.reflect.ClassTag
import alleycats.Empty

import scala.annotation.tailrec

/**
 *
 * @param capacity
 * @param empty$T$0
 * @tparam T
 */
class SumTree[T: Empty](val capacity: Int)extends AutoCloseable :
  private val array: CyclicArray[Node[T]] = CyclicArray(capacity)

  @tailrec
  private def updateSums(idx: Int, valueChange: Float): Unit =
    if idx >= 0 then
      array.update(idx, _.sum += valueChange)
      if idx > 0 then
        updateSums((idx - 1) / 2, valueChange)

  private def leftSum(idx: Int): Float =
    val leftIdx = leftChildIdx(idx)
    if leftIdx < capacity then array(leftIdx).sum else 0
  
  private def leftChildIdx(idx: Int) = idx * 2 + 1
  private def rightChildIdx(idx: Int) = idx * 2 + 2
  
  private def rightSum(idx: Int): Float =
    val rightIdx = rightChildIdx(idx)
    if rightIdx < capacity then array(rightIdx).sum else 0

  @tailrec
  private def getInternal(sumOfValues: Float, idx: Int): (ValuedItem[T], Int) =
    val lSum = leftSum(idx)
    val rSum = rightSum(idx)
    val currentItem = array(idx)
    if leftChildIdx(idx) < capacity && sumOfValues < lSum then
      getInternal(sumOfValues, leftChildIdx(idx))
    else if sumOfValues < lSum + currentItem.item.value || rightChildIdx(idx) >= capacity then
      (currentItem.item, idx)
    else
      getInternal(sumOfValues - lSum - currentItem.item.value, rightChildIdx(idx))

    /**
     *
     * @param item
     * @return index in which the item was inserted
     */
  def addOne(item: ValuedItem[T]): Int =
    val oldItem = array.head
    val idx = array.add(Node(item, oldItem.sum))
    updateSums(idx, item.value - oldItem.item.value)
    idx


  def addAll(items: IterableOnce[ValuedItem[T]]): Unit =
    items.iterator.foreach(addOne)

  def updateValue(idx: Int, value: Float): Unit =
    val oldValue = array(idx).item.value
    array.update(idx, _.item.value = value)
    updateSums(idx, value - oldValue)

  /**
   *
   * @param sumOfValues
   * @return (item, index), index can be used to update value of the item
   */
  def get(sumOfValues: Float): (ValuedItem[T], Int) =
    assert(sumOfValues >= 0)
    getInternal(sumOfValues, 0)

  def totalValue(): Float = array(0).sum

  override def close(): Unit = array.close()


object SumTree:

  case class ValuedItem[T](item: T, var value: Float)

  case class Node[T](item: ValuedItem[T], var sum: Float)extends AutoCloseable :
    override def close(): Unit = closeIfNeeded[T](item.item)

  given emptyValue[T:Empty]: Empty[ValuedItem[T]] with
    def empty = ValuedItem(Empty[T].empty, 0f)

  given emptyNode[T:Empty]: Empty[Node[T]] with
    def empty = Node(Empty[ValuedItem[T]].empty, 0f)
    