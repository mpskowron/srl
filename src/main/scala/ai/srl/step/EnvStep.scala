package ai.srl.step

import ai.djl.ndarray.{NDList, NDManager}
import ai.srl.action.DJLAction
import ai.srl.batch.{DJLBatchData, DJLBatchLabel, DJLBatchable}
import ai.srl.collection.SizedChunk
import ai.srl.env.RLEnv.TsObs
import ai.srl.observation.DJLNNInput

trait EnvStep[+Ac, +Observation]:
  def preObservation: Observation
  def action: Ac
  def reward: Float

object EnvStep:
  given [Ac: DJLAction, Obs: DJLNNInput, ES <: EnvStep[Ac, Obs]]: DJLBatchData[ES] with
    extension (step: ES)
      def toDJLBatchDataItem(using NDManager): NDList = DJLBatchable.buildDataRow(step.preObservation, step.action)

  given [Ac, Obs, ES <: EnvStep[Ac, Obs]]: DJLBatchLabel[ES] with
    extension (step: ES) def toDJLLabelItem(using manager: NDManager): NDList = NDList(manager.create(step.reward))

case class BaseEnvStep[Ac, Observation](action: Ac, preObservation: Observation, reward: Float) extends EnvStep[Ac, Observation]

case class TimeseriesEnvStep[Ac, EnvState, AgentState, TS <: Int](
    action: Ac,
    preObservation: TsObs[EnvState, AgentState, TS],
    reward: Float
) extends EnvStep[Ac, TsObs[EnvState, AgentState, TS]]
