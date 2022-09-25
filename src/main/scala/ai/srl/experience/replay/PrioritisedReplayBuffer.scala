package ai.srl.experience.replay

import ai.srl.experience.store.PrioritisedExperienceStore

type PurePrioritisedReplayBuffer[S, T] = PrioritisedExperienceStore[S, T, T]
