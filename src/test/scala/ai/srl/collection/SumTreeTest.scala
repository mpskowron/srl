package ai.srl.collection

import ai.srl.collection.SumTree.ValuedItem
import ai.srl.assertions.Assertions._

import scala.reflect.ClassTag

class SumTreeTest extends munit.FunSuite:
  import scala.language.implicitConversions
  
  test("correctly queries by summed value") {
    val tree = new SumTree[Int](12)
    
    val items = (1 to 100).map(ValuedItem(_, 0.1))
    tree.addAll(items)
    assertApproxEquals(tree.totalValue(), 1.2f)
    (List(92, 100, 93, 98, 94, 89, 95, 97, 96, 90, 99, 91)).zip(List(7, 3, 8, 1, 9, 4, 10, 0, 11, 5, 2, 6))
      .zipWithIndex.foreach { case ((item, arrayIdx), treeIdx) =>
      assertEquals(tree.get(treeIdx * 0.1f + 0.001f), (ValuedItem(item, 0.1), arrayIdx))
    }
    
    val addedItem = ValuedItem(1, 5)
    tree.addOne(addedItem)
    
    assertEquals(tree.get(0.02), (items(91), 7))
    assertEquals(tree.get(0.395), (items(97), 1))
    assertEquals(tree.get(4.2), (addedItem, 4))
    assertEquals(tree.get(5.498), (addedItem, 4))
    assertEquals(tree.get(5.501), (items(94), 10))
    
    tree.updateValue(2, 0.52)
    val updatedItem = ValuedItem(99, 0.52)
    
    assertEquals(tree.get(0.02), (items(91), 7))
    assertEquals(tree.get(0.395), (items(97), 1))
    assertEquals(tree.get(5.22), (addedItem, 4))
    assertEquals(tree.get(5.71), (items(95), 11))
    assertEquals(tree.get(5.91), (updatedItem, 2))
    assertEquals(tree.get(6.21), (updatedItem, 2))
    assertEquals(tree.get(6.419), (updatedItem, 2))
    assertEquals(tree.get(6.4201), (items(90), 6))
    assertEquals(tree.get(1000), (items(90), 6))
    
    assertApproxEquals(tree.totalValue(), 6.52f)
  }

  test("adds, removes and queries correct closeable elements") {
    val tree = new SumTree[CanClose](4)
    val items = (1 to 10).map(i => ValuedItem(CanClose(Some(i)), 0.2))
    tree.addAll(items)

    items.take(6).foreach(item => assertEquals(item.item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assertEquals(actual.item.option.get, expected))

    tree.updateValue(1, 2)
    assertEquals(items(9).item.option.get, 10)
    assertEquals(items(9).value, 2f)
    assertEquals(tree.get(2), (items(9).copy(value = 2), 1))
    
    val twelve = ValuedItem(CanClose(Some(12)), 0.32)
    assertEquals(tree.addOne(twelve), 2)
    assertEquals(items(6).item.option, None)
    assertEquals(tree.get(2.39), (items(8), 0))
    assertEquals(tree.get(2.40), (twelve, 2))
  }
