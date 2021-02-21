package ai.srl.policy

import ai.srl.env.RlEnv

trait Policy[Action, Environment <: RlEnv[Action, ?, ?]]:
  def chooseAction(env: Environment): Action
