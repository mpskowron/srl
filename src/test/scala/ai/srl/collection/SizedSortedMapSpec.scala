package ai.srl.collection

import cats.Semigroup
import org.junit.runner.RunWith
import zio.{Chunk, ZIO}
import zio.test.*
import zio.test.Assertion.*

import scala.collection.immutable.TreeMap
import scala.compiletime.ops.int.*

@RunWith(classOf[zio.test.junit.ZTestJUnitRunner])
class SizedSortedMapSpec extends ZIOSpecDefault:

  def spec = suite("SizedSortedMap suite")(
    test("Correctly create SizedSortedMap") {
      val mapa: SizedSortedMap[3, Int, Int] = SizedSortedMap[3, Int, Int](TreeMap((1, 2), (0, 5), (3, 9)))
      assertTrue(mapa.toList == List((0, 5), (1, 2), (3, 9)))
    },
    test("Fail to construct illegal SizedSortedMap") {
      assertZIO(ZIO.attempt(SizedSortedMap[2, Int, Int](TreeMap((1, 1)))).exit)(fails(isSubtype[IllegalArgumentException](anything)))
    }
  )
