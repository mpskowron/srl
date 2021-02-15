package ai.srl.agent

import ai.srl.env.RlEnv

trait Agent[Action, Environment <: RlEnv[Action, ?, ?], TrainContext, TrainResult]:

  def chooseAction(env: Environment, training: Boolean): Action
  
  def trainBatch(trainContext: TrainContext): TrainResult
