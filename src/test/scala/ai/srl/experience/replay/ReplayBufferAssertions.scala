package ai.srl.experience.replay

import scala.reflect.ClassTag

object ReplayBufferAssertions:
  def assertCorrectBatches[RB, T: ClassTag](buffer: RB, expectedRange: Iterable[T], iterations: Int = 100)(using ReplayBuffer[RB, T]): Unit =
    (1 to iterations).foreach { _ =>
      assert(buffer.getBatch().toSet.subsetOf(expectedRange.toSet))
    }
