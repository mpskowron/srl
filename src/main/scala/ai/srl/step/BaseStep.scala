package ai.srl.step

trait BaseStep[Ac, Observation]:
  def getPreObservation(): Observation

  def getAction(): Ac

  def getReward(): Float

  def isDone(): Boolean
