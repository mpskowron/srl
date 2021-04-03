package ai.srl.step

import alleycats.Empty
import cats.kernel.Eq

case class SimpleStep[Ac, Observation](observation: Observation, action: Ac, reward: Float, done: Boolean)

object SimpleStep:
  given [Ac: Empty, Ob: Empty]: Empty[SimpleStep[Ac, Ob]] with
    def empty = SimpleStep(Empty[Ob].empty, Empty[Ac].empty, 0f, false)
  
  given [Ac, Ob]: Eq[SimpleStep[Ac, Ob]] with
    def eqv(x: SimpleStep[Ac, Ob], y: SimpleStep[Ac, Ob]): Boolean = x == y

  given [Ac, Observation]: BaseStep[SimpleStep[Ac, Observation], Ac, Observation] with
    extension (step: SimpleStep[Ac, Observation])
      def getPreObservation(): Observation = step.observation

      def getAction(): Ac = step.action

      def getReward(): Float = step.reward

      def isDone(): Boolean = step.done
