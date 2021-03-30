package ai.srl.step

trait MultiStep[Ac, State, Observation] extends BaseStep[Ac, Observation]:
  override def getAction(): Ac = getActionsWithStatesAndRewards().iterator.next()._1

  override def getReward(): Float = getActionsWithStatesAndRewards().iterator.next()._3
  
  def getActionsWithStatesAndRewards(): IterableOnce[(Ac, State, Float)]
