package ai.srl.env

import ai.srl.step.MultiStep

trait MultiStepRlEnv[E, Action, Observation, State, Step](using MultiStep[Step, Action, State, Observation]):
  extension (e: E)
    def multiStep(action: Action): Step
