package ai.srl.step

trait MultiStep[S, Ac, State, Observation] extends BaseStep[S, Ac, Observation]:
  extension (step: S)
    override def getAction(): Ac = getActionsWithStatesAndRewards().iterator.next()._1

    override def getReward(): Float = getActionsWithStatesAndRewards().iterator.next()._3
    
    def getActionsWithStatesAndRewards(): IterableOnce[(Ac, State, Float)]
