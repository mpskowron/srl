package ai.srl.experience.store

import ai.srl.collection.{ClearAll, Extend, GetBatch, MaxSize, Size}

trait ExperienceStore[S, Input, Output]
    extends Extend[S, Input]
    with GetBatch[S, Output]
    with Size[S]
    with MaxSize[S]
    with ClearAll[S]
