package ai.srl.step

final case class SimpleMultiStep[Ac, State, Observation](observation: Observation, actionsWithStatesAndRewards: Seq[(Ac, State, Float)], done: Boolean)
  extends MultiStep[Ac, State, Observation] :
  override def getActionsWithStatesAndRewards(): Seq[(Ac, State, Float)] = actionsWithStatesAndRewards

  override def getPreObservation(): Observation = observation

  override def isDone(): Boolean = done

  def toSimpleSteps[SimpleStepObservation](stepObservationConverstion: (Observation, State) => SimpleStepObservation) =
    actionsWithStatesAndRewards.map((action, state, reward) => SimpleStep(stepObservationConverstion(observation, state), action, reward, done))