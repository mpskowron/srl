package ai.srl.math

import cats.data.NonEmptyList
import gcd.*

class GcdTest extends munit.FunSuite:
  import scala.language.implicitConversions
  
  test("Correctly calculates greatest common divisor for a non empty list of values") {
    assertEquals(gcd(NonEmptyList(10, List(100, 50))), 10)
    assertEquals(gcd(NonEmptyList(13, List(7, 33, 18))), 1)
    assertEquals(gcd(NonEmptyList(6, List(8, 22))), 2)
    assertEquals(gcd(NonEmptyList(6, List.empty)), 6)
    assertEquals(gcd(NonEmptyList(8, List(16, 22))), 2)
    assertEquals(gcd(NonEmptyList(8, List(16, 24, 80))), 8)
    assertEquals(gcd(NonEmptyList(8, List(16, 24, 80, 44))), 4)
    assertEquals(gcd(NonEmptyList(21, List(27, 90))), 3)
  }
