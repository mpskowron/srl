package ai.srl.env

import ai.srl.step.BaseStep

trait RlEnv[Ac, Observation, Step]:
  def reset(): Unit
  def getObservation(): Observation
  def getActionSpace(): Vector[Ac]
  def step(action: Ac): Step
  def getState(): String
