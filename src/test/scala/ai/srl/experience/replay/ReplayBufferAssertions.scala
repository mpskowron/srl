package ai.srl.experience.replay

import zio.test.TestResult.all
import zio.test.{TestResult, assertTrue}

import scala.reflect.ClassTag

object ReplayBufferAssertions:
  def assertCorrectBatches[RB, T: ClassTag](buffer: RB, expectedRange: Iterable[T], iterations: Int = 100)(using
      PureReplayBuffer[RB, T]
  ): TestResult =
    all((1 to iterations).map { _ =>
      assertTrue(buffer.getBatch().toSet.subsetOf(expectedRange.toSet))
    }*)
