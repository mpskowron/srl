package ai.srl.policy

import scala.util.Random

object RandomPolicy {
  given [Action, Observation]: PurePolicy[RandomPolicy.type, Action, Observation] with
    extension (p: RandomPolicy.type)
      def chooseAction(actionSpace: Vector[Action], observation: Observation): Action = actionSpace(Random.nextInt(actionSpace.size))
}
