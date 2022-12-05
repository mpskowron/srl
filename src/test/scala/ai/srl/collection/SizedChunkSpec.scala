package ai.srl.collection

import cats.Semigroup
import org.junit.runner.RunWith
import zio.ZIO
import zio.test.Assertion.*
import zio.test.*
import zio.Chunk
import zio.Chunk.AnyRefArray

import scala.compiletime.ops.int.*

@RunWith(classOf[zio.test.junit.ZTestJUnitRunner])
class SizedChunkSpec extends ZIOSpecDefault:

  def spec = suite("SizedChunk suite")(
    test("Correctly map") {
      val chunk: SizedChunk[3, Int] = SizedChunk[3,Int](Chunk(1,2,3))

      val mapped: SizedChunk[3, Int] = chunk.map(_ * 2)
      assertTrue(chunk.size == 3)
    },
    test("Chunk is reused on slice [it actually doesn't test anything but is a great playground]") {
      case class Test(a: Int, b: String)
      val chunk = Chunk(Test(4, "dsff"), Test(5, "sdloyhasiojf"), Test(8, "sdlfghaiosyhfvmas"), Test(2, "dslkfhjsaf"))
      def mapFunc(test: Test): Int = {
        println("in mapFunc:")
        println(test.a)
        test.a
      }
//      AnyRefArray(chunk.toArray, self.offset + offset, self.length - offset min length)
      val chunk2 = chunk.view.slice(1, 3).map(mapFunc) //.asInstanceOf[Chunk[Test]]//.map(mapFunc)

      assertTrue(chunk2(0) == 5) &&
        assertTrue(chunk2(0) == 5) &&
      assertTrue(chunk2(1) == 8) &&
        assertTrue(chunk2.toArray sameElements Array(5, 8))

    }
  )
