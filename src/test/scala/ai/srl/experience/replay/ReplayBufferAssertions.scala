package ai.srl.experience.replay

import scala.reflect.ClassTag

object ReplayBufferAssertions:
  def assertCorrectBatches[T: ClassTag](buffer: ReplayBuffer[T], expectedRange: Iterable[T], iterations: Int = 100): Unit =
    (1 to iterations).foreach { _ =>
      assert(buffer.getBatch().toSet.subsetOf(expectedRange.toSet))
    }
