package ai.srl.experience.config

import ai.srl.logging.Description
import io.circe.Codec

case class ExperienceConfig(replay: ReplayConfig, minBufferSize: Int) derives Codec.AsObject:
  def withBatchSize(batchSize: Int): ExperienceConfig = copy(replay = replay.copy(batchSize = batchSize))

case class ReplayConfig(batchSize: Int, bufferSize: Int, prioritised: PriorityConfig)

case class PriorityConfig(defaultPriority: Float)

object ExperienceConfig:
  given Description[ExperienceConfig] with
    extension (config: ExperienceConfig) 
      def describe() =
        import config._
        Seq(
          ("minBufferSize", minBufferSize.toString),
          ("batchSize", replay.batchSize.toString),
          ("bufferSize", replay.bufferSize.toString),
        )
