package ai.srl.env

import ai.srl.step.MultiStep

trait MultiStepRlEnv[E, Action, Observation, S <: MultiStep[Action, Observation]]:
  extension (e: E)
    def multiStep(): S
