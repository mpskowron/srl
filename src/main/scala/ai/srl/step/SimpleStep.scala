package ai.srl.step

import ai.srl.action.DJLAction
import ai.srl.observation.DJLObservation

case class SimpleStep(observation: DJLObservation, action: DJLAction, reward: Float, done: Boolean) extends BaseStep:
  override def getPreObservation(): DJLObservation = observation

  override def getAction(): DJLAction = action

  override def getReward(): Float = reward

  override def isDone(): Boolean = done
