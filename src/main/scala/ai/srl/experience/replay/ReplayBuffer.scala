package ai.srl.experience.replay
import ai.srl.experience.store.ExperienceStore

type ReplayBuffer[T] = ExperienceStore[T, T]
