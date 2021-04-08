package ai.srl.collection

import ai.srl.collection.Closeable.closeIfNeeded
import ai.srl.collection.SumTree.{Node, ValuedItem}

import scala.reflect.ClassTag
import alleycats.Empty
import cats.kernel.Eq

import scala.annotation.tailrec

/** This is not a standard sumtree implementation where only leaves are meaningful nodes, in this one every node represents an added Item
 *
 * @param capacity
 * @param empty$T$0
 * @tparam T
 */
class SumTree[T: Empty](val capacity: Int)extends AutoCloseable :
  private val array: CyclicArray[Node[T]] = CyclicArray(capacity)

  @tailrec
  private def updateSums(idx: Int): Unit =
    if idx >= 0 then
      array.update(idx, node => node.sum = leftSum(idx) + rightSum(idx) + node.item.value)
      if idx > 0 then
        updateSums((idx - 1) / 2)

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
    else if sumOfValues < lSum + currentItem.item.value || rightChildIdx(idx) >= capacity || rSum == 0 then
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
    updateSums(idx)
    idx


  def addAll(items: IterableOnce[ValuedItem[T]]): Unit =
    items.iterator.foreach(addOne)

  def updateValue(idx: Int, value: Float): Unit =
    val oldValue = array(idx).item.value
    array.update(idx, _.item.value = value)
    updateSums(idx)

  /**
   *
   * @param sumOfValues
   * @return (item, index), index can be used to update value of the item
   */
  def get(sumOfValues: Float): (ValuedItem[T], Int) =
    assert(sumOfValues >= 0, s"sumOfValues should be >= 0, but is $sumOfValues")
    getInternal(sumOfValues, 0)

  def totalValue(): Float = array(0).sum

  override def close(): Unit = array.close()

  def clearAll() = array.clearAll()

object SumTree:

  case class ValuedItem[T](item: T, var value: Float)

  case class Node[T](item: ValuedItem[T], var sum: Float)extends AutoCloseable :
    override def close(): Unit = closeIfNeeded[T](item.item)

  given emptyValue[T:Empty]: Empty[ValuedItem[T]] with
    def empty = ValuedItem(Empty[T].empty, 0f)

  given emptyNode[T:Empty]: Empty[Node[T]] with
    def empty = Node(Empty[ValuedItem[T]].empty, 0f)
  
  given [T: Empty : Eq]: Eq[Node[T]] with
    def eqv(x: Node[T], y: Node[T]): Boolean = x.item == y.item

  import ai.srl.collection.GetIterator
  given [T: Empty : Eq]: GetIterator[SumTree[T], T] with
    extension (tree: SumTree[T])
      def iterator = tree.array.iterator.map(_._1.item)
