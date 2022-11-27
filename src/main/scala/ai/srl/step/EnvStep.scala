package ai.srl.step

import ai.srl.collection.SizedChunk

trait EnvStep[+Ac, +Observation]:
  def preObservation: Observation
  def action: Ac
  def reward: Float

case class BaseEnvStep[Ac, Observation](action: Ac, preObservation: Observation, reward: Float) extends EnvStep[Ac, Observation]

case class TimeseriesEnvStep[Ac, EnvState, AgentState, S <: Int](
    action: Ac,
    preObservation: (SizedChunk[S, EnvState], AgentState),
    reward: Float
) extends EnvStep[Ac, (SizedChunk[S, EnvState], AgentState)]
