package ai.srl.experience.config

import ai.srl.logging.Description
import io.circe.Codec
import zio.ZLayer
import ai.srl.config.{ConfigError, Layers}

case class ExperienceConfig(replay: ReplayConfig, minBufferSize: Int) derives Codec.AsObject:
  export replay.*
  export replay.prioritised.defaultPriority
  def withBatchSize(batchSize: Int): ExperienceConfig   = copy(replay = replay.copy(batchSize = batchSize))
  def withBufferSize(bufferSize: Int): ExperienceConfig = copy(replay = replay.copy(bufferSize = bufferSize))

case class ReplayConfig(batchSize: Int, bufferSize: Int, prioritised: PriorityConfig) derives Codec.AsObject

object ReplayConfig {

  val layer: ZLayer[Any, ConfigError, ReplayConfig] = Layers.configLayer("experience.replay")
}

case class PriorityConfig(defaultPriority: Float)

object ExperienceConfig:
  given Description[ExperienceConfig] with
    extension (config: ExperienceConfig)
      def describe() =
        import config.*
        Seq(
          ("minBufferSize", minBufferSize.toString),
          ("batchSize", replay.batchSize.toString),
          ("bufferSize", replay.bufferSize.toString)
        )
