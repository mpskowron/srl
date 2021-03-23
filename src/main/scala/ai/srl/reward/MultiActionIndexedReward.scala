package ai.srl.reward

import ai.srl.env.RlEnv

trait MultiActionIndexedReward[R, Action, PreActionState]:
  extension (r: R)
    def reward(index: Int): Map[(Action, PreActionState), Double]
