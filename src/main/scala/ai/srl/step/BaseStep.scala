package ai.srl.step

trait BaseStep[Ac, Ob]:
  def getPreObservation(): Ob

  def getAction(): Ac

  def getReward(): Float

  def isDone(): Boolean
