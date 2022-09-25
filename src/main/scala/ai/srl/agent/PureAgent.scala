package ai.srl.agent

import ai.srl.env.RlEnv
import ai.srl.policy.PurePolicy

trait PureAgent[A, Action, Observation, P ,TrainContext, TrainResult](using PurePolicy[P, Action, Observation]):
  extension (agent: A)
    def policy: P

    def chooseAction(actionSpace: Vector[Action], observation: Observation): Action = policy.chooseAction(actionSpace, observation)
    
    def trainBatch(trainContext: TrainContext): TrainResult
