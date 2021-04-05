package ai.srl.env

import ai.srl.step.BaseStep

trait RlEnv[E, Ac, Observation, Step]:
  extension (env: E)
    def reset(): Unit
    def getObservation(): Observation
    def getActionSpace(): Vector[Ac]
    def step(action: Ac): Step
    def getState(): String
