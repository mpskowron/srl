package ai.srl.experience.replay
import ai.srl.experience.store.ExperienceStore

type ReplayBuffer[S, T] = ExperienceStore[S, T, T]
