package ai.srl.experience.replay

import ai.srl.experience.store.IndexedPrioritisedExperienceStore

type IndexedPrioritisedReplayBuffer[T] = IndexedPrioritisedExperienceStore[T, T]
