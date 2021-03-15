package ai.srl.policy

import ai.srl.env.RlEnv

trait Policy[P, Action, Observation]:
  extension (p: P)
    def chooseAction(actionSpace: Vector[Action], observation: Observation): Action
