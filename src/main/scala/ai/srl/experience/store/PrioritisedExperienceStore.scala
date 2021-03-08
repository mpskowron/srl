package ai.srl.experience.store

import ai.srl.collection.{ExtendWithPriority, GetBatch, UpdateBatchPriorities}

trait PrioritisedExperienceStore[S, In, Out] extends ExperienceStore[S, In, Out] with ExtendWithPriority[S, In] with UpdateBatchPriorities[S]
