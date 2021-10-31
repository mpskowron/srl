package ai.srl.collection

import ai.srl.collection.Closeable.closeIfNeeded

import scala.collection.{View, mutable}
import scala.reflect.ClassTag
import cats.kernel.Eq
import scala.language.unsafeNulls

/** Cyclic array following FIFO queue insertion and remove order. It also closes all closeable when they are removed
  * from the array.
  *
  * @param size
  *   @tparam T
  */
abstract class CyclicArray[T] private (val size: Int) extends AutoCloseable:
  assert(size >= 0)
  private var next = 0
  private[CyclicArray] val array: Array[T]

  def apply(idx: Int): T =
    array(idx)

  /** @param item
    *   @return the index in which the element was inserted
    */
  def add(item: T): Int =
    closeIfNeeded(array(next))
    array(next) = item
    val oldNext = next
    next = (next + 1) % size
    oldNext

  /** @param items
    *   @return (first, n) the index starting from which n elements were inserted
    */
  def addAll(items: collection.IterableOnce[T]): (Int, Int) =
    val start = next
    val n = items.iterator.foldLeft(0) { (n, item) =>
      add(item)
      n + 1
    }
    (start, n)

  def update(idx: Int, operation: T => Unit): Unit =
    operation(array(idx))

  def getNextIndex(): Int = next

  def head: T = this(next)

  def fifoIterator(): Iterator[T] = array.view.slice(next, size).iterator ++ array.view.slice(0, next).iterator

  def close() = array.foreach(closeIfNeeded)

object CyclicArray:

  def emptyOfSize[T: ClassTag](size: Int) = new CyclicArray[T](size):
    override val array = Array.fill(this.size)(null.asInstanceOf[T])

  extension [T](arr: CyclicArray[T])
    def clearAll(): Unit =
      arr.close()
      arr.array.mapInPlace(_ => null.asInstanceOf[T])
      arr.next = 0

  given GetIterator[CyclicArray] with
    extension [T](array: CyclicArray[T])
      def iterator: Iterator[T] = array.array.iterator.filter(_ != null)
