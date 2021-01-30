package ai.srl.experience.config

import ai.srl.logging.Description
import io.circe.Codec

case class ExperienceConfig(replay: ReplayConfig, minBufferSize: Int) derives Codec.AsObject

case class ReplayConfig(batchSize: Int, bufferSize: Int)

given Description[ExperienceConfig] with
  extension (config: ExperienceConfig) 
    def describe() =
      import config._
      Seq(
        ("minBufferSize", minBufferSize.toString),
        ("batchSize", replay.batchSize.toString),
        ("bufferSize", replay.bufferSize.toString),
      )
