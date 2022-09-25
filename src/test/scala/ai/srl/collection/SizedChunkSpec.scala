package ai.srl.collection

import cats.Semigroup
import org.junit.runner.RunWith
import zio.ZIO
import zio.test.Assertion.*
import zio.test.*
import zio.Chunk

import scala.compiletime.ops.int.*

@RunWith(classOf[zio.test.junit.ZTestJUnitRunner])
class SizedChunkSpec extends ZIOSpecDefault:

  def spec = suite("SizedChunk suite")(
    test("Correctly add fixed sized arrays") {
      val chunk: SizedChunk[3, Int] = SizedChunk[3,Int](Chunk(1,2,3))

      val mapped: SizedChunk[3, Int] = chunk.map(_ * 2)
      assertTrue(chunk.size == 3)
    }
  )
