package ai.srl.experience.replay

import ai.srl.collection.{CanClose, ExtendWithPriority}
import ai.srl.experience.replay.ReplayBufferAssertions.assertCorrectBatches

import scala.reflect.ClassTag

class PrioritisedReplayBufferDerivationTest extends munit.FunSuite:
  
  final case class BufferWrapper(val buffer: SumTreePrioritisedReplayBuffer[Int])//, int: Int)

  object BufferWrapper:
    given ExtendWithPriority[BufferWrapper, Int] = ExtendWithPriority.derived


  test("adds and removes correct elements".ignore) {
    val wrapper = new BufferWrapper(SumTreePrioritisedReplayBuffer(4, 4, 1))//  , 5)
    println(wrapper.buffer.getPrioritisedIndexedBatch().toList)
    wrapper.addOnePrioritised(3, 2)
    wrapper.addOnePrioritised(2, 4)
    println(wrapper.buffer.getPrioritisedIndexedBatch().toList)
  }

