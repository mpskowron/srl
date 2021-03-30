package ai.srl.step

import alleycats.Empty
import cats.kernel.Eq

case class SimpleStep[Ac, Observation](observation: Observation, action: Ac, reward: Float, done: Boolean) extends BaseStep[Ac, Observation]:
  override def getPreObservation(): Observation = observation

  override def getAction(): Ac = action

  override def getReward(): Float = reward

  override def isDone(): Boolean = done

object SimpleStep:
  given [Ac: Empty, Ob: Empty]: Empty[SimpleStep[Ac, Ob]] with
    def empty = SimpleStep(Empty[Ob].empty, Empty[Ac].empty, 0f, false)
  
  given [Ac, Ob]: Eq[SimpleStep[Ac, Ob]] with
    def eqv(x: SimpleStep[Ac, Ob], y: SimpleStep[Ac, Ob]): Boolean = x == y
