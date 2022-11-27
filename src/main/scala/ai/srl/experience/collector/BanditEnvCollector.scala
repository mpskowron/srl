package ai.srl.experience.collector

import ai.srl.collection.SizedChunk
import ai.srl.env.{IndexedBanditEnv, IndexedObservations, RLEnv}
import ai.srl.step.{EnvStep, TimeseriesEnvStep}
import zio.{Chunk, Tag, ZLayer}

// This class might be just overcomplication - collection may happen in the Buffer class layer constructor instead
// Alternatively, InMemBanditEnv may implement this interface, which may simplify things
trait EnvStepCollector[Ac, Observation]:
  def collect(): Chunk[EnvStep[Ac, Observation]]

class TimeseriesBanditEnvCollector[Ac, EnvState, AgentState, Length <: Int](env: IndexedBanditEnv[Ac, EnvState, AgentState])
    extends EnvStepCollector[Ac, (SizedChunk[Length, EnvState], AgentState)]:
  override def collect(): Chunk[TimeseriesEnvStep[Ac, EnvState, AgentState, Length]] = ???

object TimeseriesBanditEnvCollector:
  def layer[Ac: Tag, EnvState: Tag, AgentState: Tag, Length <: Int: Tag]
      : ZLayer[IndexedBanditEnv[Ac, EnvState, AgentState], Nothing, TimeseriesBanditEnvCollector[Ac, EnvState, AgentState, Length]] =
    ZLayer.fromFunction(TimeseriesBanditEnvCollector[Ac, EnvState, AgentState, Length](_))
