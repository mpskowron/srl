package ai.srl.step

trait MultiStep[Ac, Observation] extends BaseStep[Ac, Observation]:
  override def getAction(): Ac = getActionsWithRewards().iterator.next()._1

  override def getReward(): Float = getActionsWithRewards().iterator.next()._2
  
  def getActionsWithRewards(): IterableOnce[(Ac, Float)]
