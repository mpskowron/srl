package ai.srl.env

import ai.srl.collection.SizedChunk
import ai.srl.env.RLEnv.TimeseriesRLEnvError.TimeseriesHistorySizeTooSmall
import zio.ZIO.{fail, service, when}
import zio.{Tag, ZIO, ZLayer}

trait EnvStateMapper[-Observation, State, -Ac, E]:

  def mapState(observation: Observation, state: State, ac: Ac): Either[E, State]

object EnvStateMapper:

  def timeSeriesLayer[TS <: Int: Tag: ValueOf, Observation: Tag, State: Tag, Ac: Tag, E: Tag]: ZLayer[
    EnvStateMapper[Observation, State, Ac, E],
    TimeseriesHistorySizeTooSmall,
    EnvStateMapper[SizedChunk[TS, Observation], State, Ac, E]
  ] = ZLayer {
    for
      mapper <- service[EnvStateMapper[Observation, State, Ac, E]]
      _      <- when(valueOf[TS] < 1)(fail[TimeseriesHistorySizeTooSmall](TimeseriesHistorySizeTooSmall(valueOf[TS], 1)))
    yield new EnvStateMapper:
      override def mapState(observation: SizedChunk[TS, Observation], state: State, ac: Ac): Either[E, State] =
        mapper.mapState(observation.last, state, ac)

  }
