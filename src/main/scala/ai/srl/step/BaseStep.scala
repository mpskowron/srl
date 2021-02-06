package ai.srl.step

import ai.srl.action.DJLAction
import ai.srl.observation.DJLObservation

trait BaseStep:
  def getPreObservation(): DJLObservation

  def getAction(): DJLAction

  def getReward(): Float

  def isDone(): Boolean
