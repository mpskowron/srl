package ai.srl.experience.replay

import ai.srl.experience.store.PrioritisedExperienceStore

type PrioritisedReplayBuffer[T] = PrioritisedExperienceStore[T, T]
