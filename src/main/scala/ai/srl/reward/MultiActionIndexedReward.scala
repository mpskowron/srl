package ai.srl.reward

trait MultiActionIndexedReward[R, Action, PreActionState]:
  extension (r: R)
    def multiActionReward(index: Int): Seq[(Action, PreActionState, Double)]
