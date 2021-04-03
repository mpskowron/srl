package ai.srl.step

final case class SimpleMultiStep[Ac, State, Observation](observation: Observation, actionsWithStatesAndRewards: Seq[(Ac, State, Float)], 
                                                         done: Boolean):
  def toSimpleSteps[SimpleStepObservation](stepObservationConverstion: (Observation, State) => SimpleStepObservation) =
    actionsWithStatesAndRewards.map((action, state, reward) => SimpleStep(stepObservationConverstion(observation, state), action, reward, done))

object SimpleMultiStep:
  given [Ac, State, Observation]: MultiStep[SimpleMultiStep[Ac,State, Observation], Ac, State, Observation] with
    extension (step: SimpleMultiStep[Ac, State, Observation])
      def getActionsWithStatesAndRewards(): Seq[(Ac, State, Float)] = step.actionsWithStatesAndRewards

      def getPreObservation(): Observation = step.observation

      def isDone(): Boolean = step.done
