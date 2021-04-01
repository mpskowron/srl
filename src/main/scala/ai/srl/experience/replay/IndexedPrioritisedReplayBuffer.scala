package ai.srl.experience.replay

import ai.srl.experience.store.IndexedPrioritisedExperienceStore

type IndexedPrioritisedReplayBuffer[S, T] = IndexedPrioritisedExperienceStore[S, T, T]
