package ai.srl.experience.replay

import ai.srl.experience.store.PrioritisedExperienceStore

type PrioritisedReplayBuffer[S, T] = PrioritisedExperienceStore[S, T, T]
