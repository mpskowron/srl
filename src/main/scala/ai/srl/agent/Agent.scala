package ai.srl.agent

import ai.srl.env.RlEnv
import ai.srl.policy.PurePolicy
import zio.{ZIO, Tag}

trait Agent[-TrainContext, +TrainResult]:
    def train(trainContext: TrainContext): ZIO[Any, Nothing, TrainResult]

object Agent {
  def train[TrainContext: Tag, TrainResult: Tag](trainContext: TrainContext): ZIO[Agent[TrainContext, TrainResult], Nothing, TrainResult] =
    ZIO.serviceWithZIO(_.train(trainContext))
}
