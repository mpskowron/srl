package ai.srl.experience.replay

import ai.srl.experience.store.HasPrioritisedExperienceStore

type HasPrioritisedReplayBuffer[T, Item] = HasPrioritisedExperienceStore[T, Item, Item]
