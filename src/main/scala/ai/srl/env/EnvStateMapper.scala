package ai.srl.env

import ai.srl.collection.SizedChunk
import ai.srl.env.RLEnv.TimeseriesRLEnvError.TimeseriesHistorySizeTooSmall
import zio.ZIO.{fail, service, when}
import zio.{Tag, ZIO, ZLayer}

trait EnvStateMapper[-EnvState, AgentState, -Ac, E]:

  def mapState(observation: EnvState, state: AgentState, ac: Ac): Either[E, AgentState]

object EnvStateMapper:

  def timeSeriesLayer[TS <: Int: Tag: ValueOf, EnvState: Tag, AgentState: Tag, Ac: Tag, E: Tag]: ZLayer[
    EnvStateMapper[EnvState, AgentState, Ac, E],
    TimeseriesHistorySizeTooSmall,
    EnvStateMapper[SizedChunk[TS, EnvState], AgentState, Ac, E]
  ] = ZLayer {
    for
      mapper <- service[EnvStateMapper[EnvState, AgentState, Ac, E]]
      _      <- when(valueOf[TS] < 1)(fail[TimeseriesHistorySizeTooSmall](TimeseriesHistorySizeTooSmall(valueOf[TS], 1)))
    yield new EnvStateMapper:
      override def mapState(observation: SizedChunk[TS, EnvState], state: AgentState, ac: Ac): Either[E, AgentState] =
        mapper.mapState(observation.last, state, ac)
  }
