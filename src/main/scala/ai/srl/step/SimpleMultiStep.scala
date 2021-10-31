package ai.srl.step

final case class SimpleMultiStep[Ac, State, Observation](
    preObservation: Observation,
    actionsWithStatesAndRewards: Seq[(Ac, State, Float)],
    done: Boolean
):
  def toSimpleSteps[SimpleStepObservation](stepObservationConversion: (Observation, State) => SimpleStepObservation) =
    actionsWithStatesAndRewards.map((action, state, reward) =>
      SimpleStep(stepObservationConversion(preObservation, state), action, reward, done)
    )

object SimpleMultiStep:
  given [Ac, State, Observation]: MultiStep[SimpleMultiStep[Ac, State, Observation], Ac, State, Observation] with
    extension (step: SimpleMultiStep[Ac, State, Observation])
      def getActionsWithStatesAndRewards(): Seq[(Ac, State, Float)] = step.actionsWithStatesAndRewards

      def getPreObservation(): Observation = step.preObservation

      def isDone(): Boolean = step.done
