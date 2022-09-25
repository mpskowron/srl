package ai.srl.assertions

import munit.internal.difflib
object Assertions:
  def assertApproxEquals(actual: Float, expected: Float, diff: Float = 0.000001): Unit =
    assert(approxE(actual, expected, diff), 
      s"actual: $actual, expected: $expected, actual_diff: ${(actual - expected).abs}, max_diff: $diff")
  
  def assertApproxEquals(actual: Int, expected: Int, diff: Int): Unit = 
    assertApproxEquals(actual.toFloat, expected.toFloat, diff.toFloat)

  def approxE(actual:Float, expected: Float, diff: Float = 0.000001): Boolean = actual - expected > -diff && actual - expected < diff
