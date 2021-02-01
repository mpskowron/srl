package ai.srl.collection

import ai.srl.collection.SumTree.Value
import ai.srl.assertions.Assertions._

import scala.reflect.ClassTag

class SumTreeTest extends munit.FunSuite:
  import scala.language.implicitConversions
  
  test("correctly queries by summed value") {
    val tree = new SumTree[Int](12)
    
    val items = (1 to 100).map(Value(_, 0.1))
    tree.addAll(items)
    assertApproxEquals(tree.totalValue(), 1.2f)
    (List(92, 100, 93, 98, 94, 89, 95, 97, 96, 90, 99, 91)).zip(List(7, 3, 8, 1, 9, 4, 10, 0, 11, 5, 2, 6))
      .zipWithIndex.foreach { case ((item, arrayIdx), treeIdx) =>
      assertEquals(tree.get(treeIdx * 0.1f + 0.001f), (item, arrayIdx))
    }
    tree.add(Value(1, 5))
    assertEquals(tree.get(0.02), (92, 7))
    assertEquals(tree.get(0.395), (98, 1))
    assertEquals(tree.get(4.2), (1, 4))
    assertEquals(tree.get(5.498), (1, 4))
    assertEquals(tree.get(5.501), (95, 10))
    
    tree.updateValue(2, 0.52)
    assertEquals(tree.get(0.02), (92, 7))
    assertEquals(tree.get(0.395), (98, 1))
    assertEquals(tree.get(5.22), (1, 4))
    assertEquals(tree.get(5.71), (96, 11))
    assertEquals(tree.get(5.91), (99, 2))
    assertEquals(tree.get(6.21), (99, 2))
    assertEquals(tree.get(6.419), (99, 2))
    assertEquals(tree.get(6.4201), (91, 6))
    assertEquals(tree.get(1000), (91, 6))
    
    assertApproxEquals(tree.totalValue(), 6.52f)
  }

  test("adds, removes and queries correct closeable elements") {
    val tree = new SumTree[CanClose](4)
    val items = (1 to 10).map(i => Value(CanClose(Some(i)), 0.2))
    tree.addAll(items)

    items.take(6).foreach(item => assertEquals(item.item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assertEquals(actual.item.option.get, expected))

    tree.updateValue(1, 2)
    assertEquals(items(9).item.option.get, 10)
    assertEquals(items(9).value, 2f)
    assertEquals(tree.get(2), (items(9).item, 1))
    
    val twelve = Value(CanClose(Some(12)), 0.32)
    assertEquals(tree.add(twelve), 2)
    assertEquals(items(6).item.option, None)
    assertEquals(tree.get(2.39), (items(8).item, 0))
    assertEquals(tree.get(2.40), (twelve.item, 2))
  }
