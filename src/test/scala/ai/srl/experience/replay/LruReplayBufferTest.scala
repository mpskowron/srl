package ai.srl.experience.replay

import ai.srl.collection.CanClose
import ai.srl.experience.replay.ReplayBufferAssertions.assertCorrectBatches

import scala.reflect.ClassTag

class LruReplayBufferTest extends munit.FunSuite:
  test("adds and removes correct elements") {
    val buffer = new LruReplayBuffer[Int](5, 12)

    buffer.addAll(1 to 100)
    assertCorrectBatches(buffer, (89 to 100))

    buffer.addAll(150 to 155)
    assertCorrectBatches(buffer, (95 to 100) ++ (150 to 155))
  }

  test("adds and removes correct closeable elements") {
    val items = (1 to 10).map(i => CanClose(Some(i)))
    val buffer = new LruReplayBuffer[CanClose](4, 4)
    buffer.addAll(items)

    items.take(6).foreach(item => assertEquals(item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assertEquals(actual.option.get, expected))

    assertCorrectBatches(buffer, items.drop(6))
  }
