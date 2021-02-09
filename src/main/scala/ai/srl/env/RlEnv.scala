package ai.srl.env

import ai.srl.step.BaseStep

trait RlEnv[Ac, Observation]:
  def reset(): Unit
  def getObservation(): Observation
  def getActionSpace(): Vector[Ac]
  def step(action: Ac): BaseStep[Ac, Observation]
  def getState(): String
