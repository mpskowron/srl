package ai.srl.experience.store

trait HasPrioritisedExperienceStore[T, In, Out]:
  extension (hasPrioritisedReplayBuffer: T) def prioritisedExpStore: PrioritisedExperienceStore[In, Out]
