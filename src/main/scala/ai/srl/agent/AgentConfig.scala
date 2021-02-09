package ai.srl.agent

import ai.srl.logging.Description
import io.circe.Codec
import io.circe.Codec.AsObject

import java.time.ZonedDateTime

case class AgentConfig(trainer: TrainerConfig) derives Codec.AsObject:
  def withLearningRate(learningRate: Float) = copy(trainer = TrainerConfig(OptimizerConfig(learningRate)))

case class TrainerConfig(optimizer: OptimizerConfig) derives Codec.AsObject

case class OptimizerConfig(learningRate: Float) derives Codec.AsObject

object AgentConfig:
  given Description[AgentConfig] with
    extension (config: AgentConfig)
      def describe() =
        Seq(
          ("learningRate", config.trainer.optimizer.learningRate.toString)
        )