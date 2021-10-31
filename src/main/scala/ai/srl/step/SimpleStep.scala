package ai.srl.step

import alleycats.Empty
import cats.kernel.Eq

case class SimpleStep[Ac, Observation](preObservation: Observation, action: Ac, reward: Float, done: Boolean)

object SimpleStep:
  given simpleStepEmptyInstance[Ac: Empty, Ob: Empty]: Empty[SimpleStep[Ac, Ob]] with
    def empty = SimpleStep(Empty[Ob].empty, Empty[Ac].empty, Float.NaN, false)

  given simpleStepEqInstance[Ac: Eq, Ob: Eq]: Eq[SimpleStep[Ac, Ob]] with
    def eqv(x: SimpleStep[Ac, Ob], y: SimpleStep[Ac, Ob]): Boolean =
      if Eq.eqv[Ob](x.preObservation, y.preObservation) && Eq.eqv[Ac](x.action, y.action) then
        require(
          (x.reward == y.reward) && (x.done == y.done),
          s"Found two simple steps with same actions but other fields differrent: rewards(${x.reward}, ${y.reward}); " +
            s"dones: (${x.done}, ${y.done})"
        )
        true
      else false

  given [Ac, Observation]: BaseStep[SimpleStep[Ac, Observation], Ac, Observation] with
    extension (step: SimpleStep[Ac, Observation])
      def getPreObservation(): Observation = step.preObservation

      def getAction(): Ac = step.action

      def getReward(): Float = step.reward

      def isDone(): Boolean = step.done
