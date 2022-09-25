package ai.srl.collection

import ai.srl.collection.SumTree.ValuedItem
import ai.srl.assertions.Assertions._

import scala.reflect.ClassTag

class SumTreeTest extends munit.FunSuite:
  import scala.language.implicitConversions
  
  test("correctly queries by summed value") {
    val tree = SumTree.emptyOfCapacity[Int](12)
    
    val items = (1 to 100).map(ValuedItem(_, 0.1))
    tree.addAll(items)
    assertApproxEquals(tree.totalValue(), 1.2f)
    (List(92, 100, 93, 98, 94, 89, 95, 97, 96, 90, 99, 91)).zip(List(7, 3, 8, 1, 9, 4, 10, 0, 11, 5, 2, 6))
      .zipWithIndex.foreach { case ((item, arrayIdx), treeIdx) =>
      assertEquals(tree.get(treeIdx * 0.1f + 0.001f), IndexedElement(arrayIdx, ValuedItem(item, 0.1)))
    }
    
    val addedItem = ValuedItem(1, 5)
    tree.addOne(addedItem)
    
    assertEquals(tree.get(0.02), IndexedElement(7, items(91)))
    assertEquals(tree.get(0.395), IndexedElement(1, items(97)))
    assertEquals(tree.get(4.2), IndexedElement(4, addedItem))
    assertEquals(tree.get(5.498), IndexedElement(4, addedItem))
    assertEquals(tree.get(5.501), IndexedElement(10, items(94)))
    
    tree.updateValue(IndexedElement[Float](2, 0.52))
    val updatedItem = ValuedItem(99, 0.52)
    
    assertEquals(tree.get(0.02), IndexedElement(7, items(91)))
    assertEquals(tree.get(0.395), IndexedElement(1, items(97)))
    assertEquals(tree.get(5.22), IndexedElement(4, addedItem))
    assertEquals(tree.get(5.71), IndexedElement(11, items(95)))
    assertEquals(tree.get(5.91), IndexedElement(2, updatedItem))
    assertEquals(tree.get(6.21), IndexedElement(2, updatedItem))
    assertEquals(tree.get(6.419), IndexedElement(2, updatedItem))
    assertEquals(tree.get(6.4201), IndexedElement(6, items(90)))
    assertEquals(tree.get(1000), IndexedElement(6, items(90)))
    
    assertApproxEquals(tree.totalValue(), 6.52f)
  }

  test("adds, removes and queries correct closeable elements") {
    val tree = SumTree.emptyOfCapacity[CanClose](4)
    val items = (1 to 10).map(i => ValuedItem(CanClose(Some(i)), 0.2))
    tree.addAll(items)

    items.take(6).foreach(item => assertEquals(item.item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assertEquals(actual.item.option.get, expected))

    tree.updateValue(IndexedElement[Float](1, 2))
    assertEquals(items(9).item.option.get, 10)
    assertEquals(items(9).getValue, 2f)
    assertEquals(tree.get(2), IndexedElement(1, items(9).copy(value = 2)))
    
    val twelve = ValuedItem(CanClose(Some(12)), 0.32)
    assertEquals(tree.addOne(twelve), 2)
    assertEquals(items(6).item.option, None)
    assertEquals(tree.get(2.39), IndexedElement(0, items(8)))
    assertEquals(tree.get(2.40), IndexedElement(2, twelve))
  }
