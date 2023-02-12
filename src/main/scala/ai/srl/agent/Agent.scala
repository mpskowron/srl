package ai.srl.agent

import ai.srl.env.RlEnv
import ai.srl.policy.PurePolicy
import zio.{ZIO, Tag}

trait Agent[-TrainContext, +Error, +TrainResult]:
  def train(trainContext: TrainContext): ZIO[Any, Error, TrainResult]

object Agent:
  def train[TrainContext: Tag, TrainResult: Tag, Error: Tag](
      trainContext: TrainContext
  ): ZIO[Agent[TrainContext, Error, TrainResult], Error, TrainResult] =
    ZIO.serviceWithZIO(_.train(trainContext))
