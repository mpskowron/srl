package ai.srl.collection

import ai.srl.collection.Closeable.closeIfNeeded
import ai.srl.collection.SumTree.{IndexedValue, IndexedValueItem, Node, ValuedItem, sumOrZero}

import scala.reflect.ClassTag
import cats.kernel.Eq

import scala.annotation.tailrec

/** This is not a standard sumtree implementation where only leaves are meaningful nodes, in this one every node
  * represents an added Item. It cannot be used in multithreaded environment at all, because ValuedItem.value is a var
  * and updating priorities of previously obtained indexes may update a recently inserted item instead (because it removes
  * items cyclically)
  *
  * @param capacity
  * @tparam T
  */
abstract class SumTree[T] private (val capacity: Int) extends AutoCloseable:
  private[SumTree] val array: CyclicArray[Node[T]]

  @tailrec
  private def updateSums(idx: Int): Unit =
    require(idx >= 0, s"Sum tree array is indexed only with numbers greater than 0, but got index==$idx")
    array.update(idx, node => node.sum = leftSum(idx) + rightSum(idx) + node.item.value)
    if idx > 0 then updateSums((idx - 1) / 2)

  private def leftSum(idx: Int): Float =
    val leftIdx = leftChildIdx(idx)
    if leftIdx < capacity then sumOrZero(array(leftIdx)) else 0

  private def leftChildIdx(idx: Int)  = idx * 2 + 1
  private def rightChildIdx(idx: Int) = idx * 2 + 2

  private def rightSum(idx: Int): Float =
    val rightIdx = rightChildIdx(idx)
    if rightIdx < capacity then sumOrZero(array(rightIdx)) else 0

  @tailrec
  private def getInternal(sumOfValues: Float, idx: Int): IndexedValueItem[T] =
    val lSum        = leftSum(idx)
    val rSum        = rightSum(idx)
    val currentItem = array(idx)
    if leftChildIdx(idx) < capacity && sumOfValues < lSum then getInternal(sumOfValues, leftChildIdx(idx))
    else
      val newSumOfValues: Float = sumOfValues - lSum - currentItem.item.value
      if newSumOfValues < 0 || rightChildIdx(idx) >= capacity || rSum == 0 then IndexedElement(index = idx, element = currentItem.item)
      else getInternal(newSumOfValues, rightChildIdx(idx))

  /** @param item
    *   @return index in which the item was inserted
    */
  // TODO It probably should remove the least/most valued item instead of the old one - not sure about that fully though
  def addOne(item: ValuedItem[T]): Int =
    val oldItem = array.head
    val idx     = array.add(Node(item, sumOrZero(oldItem)))
    updateSums(idx)
    idx

  def addAll(items: IterableOnce[ValuedItem[T]]): Unit =
    items.iterator.foreach(addOne)

  def updateValue(indexedValue: IndexedValue): Unit =
    array.update(indexedValue.index, _.item.value = indexedValue.element)
    updateSums(indexedValue.index)

  /** @param sumOfValues
    *   @return (item, index), index can be used to update value of the item
    */
  def get(sumOfValues: Float): IndexedValueItem[T] =
    assert(sumOfValues >= 0, s"sumOfValues should be >= 0, but is $sumOfValues")
    getInternal(sumOfValues, 0)

  def totalValue(): Float = array(0).sum

  override def close(): Unit = array.close()

object SumTree:
  type IndexedValueItem[T] = IndexedElement[ValuedItem[T]]
  type IndexedValue = IndexedElement[Float]

  extension [T](sumTree: SumTree[T])
    def clearAll(): Unit = sumTree.array.clearAll()

  def emptyOfCapacity[T](capacity: Int): SumTree[T] = new SumTree[T](capacity):
    override val array = CyclicArray.emptyOfSize(this.capacity)

  case class ValuedItem[T](item: T, private[SumTree] var value: Float):
    def getValue: Float = value
    def withValue(value: Float): ValuedItem[T] = copy(value = value)

  case class Node[T](item: ValuedItem[T], var sum: Float) extends AutoCloseable:
    override def close(): Unit = closeIfNeeded[T](item.item)

  def sumOrZero[T](node: Node[T]): Float = if node == null then 0 else node.sum

  given [T: Eq]: Eq[Node[T]] with
    def eqv(x: Node[T], y: Node[T]): Boolean = x.item == y.item

  given GetIterator[SumTree] with
    extension [T](tree: SumTree[T])
      def iterator: Iterator[T] = tree.array.iterator.map(_._1.item)
