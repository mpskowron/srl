package ai.srl.step

import ai.srl.action.DJLAction
import ai.srl.observation.DJLObservation

trait Step extends BaseStep:

  def getPostObservation(): DJLObservation

  def getPostActionSpace(): Vector[DJLAction]
