package ai.srl.agent

import ai.srl.env.RlEnv
import ai.srl.policy.Policy

trait Agent[Action, Environment <: RlEnv[Action, ?, ?], P <: Policy[Action, Environment],TrainContext, TrainResult]:
  val policy: P

  def chooseAction(env: Environment): Action = policy.chooseAction(env)
  
  def trainBatch(trainContext: TrainContext): TrainResult
