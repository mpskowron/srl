package ai.srl.experience.replay

trait HasPrioritisedReplayBuffer[T, Item]:
  extension (hasPrioritisedReplayBuffer: T) def prioritisedReplayBuffer: PrioritisedReplayBuffer[Item]
