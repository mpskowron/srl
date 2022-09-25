package ai.srl.experience.collector

import ai.srl.collection.SizedChunk
import ai.srl.env.{BanditEnv, RLEnv}
import ai.srl.step.{EnvStep, TimeseriesEnvStep}
import zio.{Chunk, Tag, ZLayer}

trait EnvStepCollector[Ac, InputData, Env]:
  def collect(env: Env): Chunk[EnvStep[Ac, InputData]]

trait BanditEnvCollector[Ac, Observation, InputData]
    extends EnvStepCollector[Ac, InputData, BanditEnv[Ac, Observation]]:
  override def collect(env: BanditEnv[Ac, Observation]): Chunk[EnvStep[Ac, InputData]]

class TimeseriesBanditEnvCollector[Ac, Observation, Length <: Int]
    extends BanditEnvCollector[Ac, Observation, SizedChunk[Length, Observation]]:
  override def collect(env: BanditEnv[Ac, Observation]): Chunk[TimeseriesEnvStep[Ac, Observation, Length]] = ???

object TimeseriesBanditEnvCollector:
  def layer[Ac: Tag, Observation: Tag, Length <: Int: Tag]
      : ZLayer[Any, Nothing, TimeseriesBanditEnvCollector[Ac, Observation, Length]] =
    ZLayer.succeed(TimeseriesBanditEnvCollector[Ac, Observation, Length])
