package ai.srl.collection

import org.junit.runner.RunWith

import scala.compiletime.ops.int.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.Assertion
import zio.ZIO
import FixedSizeCollection.*
import cats.Semigroup
import zio.test.Gen

@RunWith(classOf[zio.test.junit.ZTestJUnitRunner])
class FixedSizeArraySpec extends DefaultRunnableSpec:

  def spec = suite("FixedSizeArray suite")(
    test("Correctly add fixed sized arrays") {
      val size1: Int = 2
      val size2: Int = 3
      val arr1       = FixedSizeCollection[size1.type, Int](Array(1, 2)).toOption.get
      val arr2       = FixedSizeCollection[size2.type, Int](Array(3, 2, 8)).toOption.get

      val combined: FixedSizeArray[size1.type + size2.type, Int] = arr1.combine(arr2)
      assert(combined.unwrap.toList)(equalTo(List(1, 2, 3, 2, 8)))
    },
    testM("Arrays have correct length after being added") {
      val arrayGen = Gen.listOf(Gen.int(Int.MinValue, Int.MaxValue)).map(_.toArray)
      check(arrayGen, arrayGen) { (left, right) =>
        val size1: Int = left.length
        val size2: Int = right.length
        val fixed1 = FixedSizeCollection[size1.type, Int](left).toOption.get
        val fixed2 = FixedSizeCollection[size2.type, Int](right).toOption.get

        val combined: FixedSizeArray[size1.type + size2.type, Int] = fixed1.combine(fixed2)
        assert(combined.unwrap.size)(equalTo(size1 + size2)) &&
        assert(combined.unwrap.toList)(equalTo((left ++ right).toList))
      }
    },
    testM("Fail compilation for incorrect types") {
      val initialization = """
           val size1: Int = 2
           val size2: Int = 3
           val arr1 = FixedSizeCollection[size1.type, Int](Array(1, 2)).toOption.get
           val arr2 = FixedSizeCollection[size2.type, Int](Array(3, 2, 8)).toOption.get
           """
      for
        wrongFixedSize <- typeCheck(
          initialization + "val comb: FixedSizeArray[size1.type + size2.type + 1, Int] = arr1.combine(arr2)"
        )
        // Added to make previous example hard to invalidate through some code refactoring, because failed compilation
        // message cannot be verified
        correctCode <- typeCheck(
          initialization + "val comb: FixedSizeArray[size1.type + size2.type, Int] = arr1.combine(arr2)"
        )
        wrongType <- typeCheck(
          initialization + "val comb: FixedSizeArray[size1.type + size2.type, Double] = arr1.combine(arr2)"
        )
      yield assert(wrongFixedSize)(isLeft(anything)) &&
        assert(wrongType)(isLeft(anything)) &&
        assert(correctCode)(isRight(anything))
    }
  )
