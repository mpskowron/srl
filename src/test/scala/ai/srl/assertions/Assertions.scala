package ai.srl.assertions

import munit.internal.difflib
object Assertions:
  def assertApproxEquals(actual: Float, expected: Float, diff: Float = 0.000001) =
    assert(actual - expected > -diff && actual - expected < diff)
