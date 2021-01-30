package ai.srl.experience.replay

import scala.reflect.ClassTag

class LruReplayBufferTest extends munit.FunSuite:
  test("adds and removes correct elements") {
    val buffer = new LruReplayBuffer[Int](5, 12)

    buffer.addElements(1 to 100)
    assertCorrectBatches(buffer, (89 to 100))

    buffer.addElements(150 to 155)
    assertCorrectBatches(buffer, (95 to 100) ++ (150 to 155))
  }

  test("adds and removes correct closeable elements") {
    case class CanClose(var option: Option[Int]) extends AutoCloseable:
      override def close(): Unit = option = None

    val items = (1 to 10).map(i => CanClose(Some(i)))
    val buffer = new LruReplayBuffer[CanClose](4, 4)
    buffer.addElements(items)

    items.take(6).foreach(item => assertEquals(item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assertEquals(actual.option.get, expected))

    assertCorrectBatches(buffer, items.drop(6))
  }

  private def assertCorrectBatches[T: ClassTag](buffer: ReplayBuffer[T], expectedRange: Iterable[T], iterations: Int = 100): Unit =
    (1 to iterations).foreach { _ =>
      assert(buffer.getBatch().toSet.subsetOf(expectedRange.toSet))
    }
