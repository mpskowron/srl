package ai.srl.collection

import scala.reflect.ClassTag
import alleycats.Empty

class CappedSizeArrayTest extends munit.FunSuite:
  import scala.language.implicitConversions
  
  test("adds and removes correct elements") {
    case class IntWrp(var int: Int)
    given Empty[IntWrp] with 
      def empty = IntWrp(0)
    given Conversion[Int, IntWrp] = IntWrp(_)
    given toList: Conversion[Seq[Int], List[IntWrp]] = _.toList.map(IntWrp(_))

    val array = new CyclicArray[IntWrp](12)

    val indexes = array.addAll((1 to 100))
    assertEquals(indexes, (0, 100))
    assertEquals(array.fifoIterator().toList, toList(89 to 100))
    assertEquals(array.iterator.toList, toList((97 to 100) ++ (89 to 96)))
    
    (4 to 14).foreach { item => 
      assertEquals(array.add(item), item % 12)
    }
    assertEquals(array.iterator.toList, toList((12 to 14) ++ (100 to 100) ++ (4 to 11)))
    assertEquals(array.fifoIterator().toList, toList((100 to 100) ++ (4 to 14)))
    
    array.update(3, _.int = 708)
    array.update(11, _.int = 32)
    array.update(0, _.int = -1)
    assertEquals(array.iterator.toList, toList((-1 to -1) ++ (13 to 14) ++ (708 to 708) ++ (4 to 10) ++ (32 to 32)))
    assertEquals(array.fifoIterator().toList, toList((708 to 708) ++ (4 to 10) ++ (32 to 32) ++ (-1 to -1) ++ (13 to 14)))
  }

  test("adds and removes correct closeable elements") {
    val array = new CyclicArray[CanClose](4)
    val items = (1 to 10).map(i => CanClose(Some(i)))
    assertEquals(array.addAll(items), (0, 10))

    items.take(6).foreach(item => assertEquals(item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assertEquals(actual.option.get, expected))

    val someEleven = Some(11)
    array.update(1, _.option = someEleven)
    assertEquals(items(9).option, someEleven)
    
    val twelve = CanClose(Some(12))
    assertEquals(array.add(twelve), 2)
    assertEquals(items(6).option, None)
    assertEquals(array.iterator.toList.map(_.option.get), List(9, 11, 12, 8))
    assertEquals(array.fifoIterator().toList.map(_.option.get), List(8, 9, 11, 12))
  }
