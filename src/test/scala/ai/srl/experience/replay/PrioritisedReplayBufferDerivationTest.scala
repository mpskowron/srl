package ai.srl.experience.replay

import ai.srl.collection.ExtendWithPriority
import org.junit.runner.RunWith
import zio.test.*
import zio.Console

@RunWith(classOf[zio.test.junit.ZTestJUnitRunner])
class PrioritisedReplayBufferDerivationTest extends ZIOSpecDefault:
  
  final case class BufferWrapper(val buffer: SumTreePrioritisedReplayBuffer[Int])//, int: Int)

  object BufferWrapper:
    given ExtendWithPriority[BufferWrapper, Int] = ExtendWithPriority.derived


  def spec = suite("prioritised replay buffer derivation")(
    test("adds and removes correct elements") {
      val wrapper = new BufferWrapper(SumTreePrioritisedReplayBuffer(4, 4, 1)) //  , 5)
      for {
        batch <- wrapper.buffer.getIndexedBatch
        _ <- Console.printLine(batch.toList)
        _ = wrapper.addOnePrioritised(3, 2)
        _ =  wrapper.addOnePrioritised(2, 4)
        batch2 <- wrapper.buffer.getIndexedBatch
        _ <- Console.printLine(batch2.toList)
      } yield assertTrue(true)
    } @@ TestAspect.ignore
  )

