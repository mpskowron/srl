package ai.srl.experience.replay

trait PrioritisedReplayBuffer[T] extends ReplayBuffer[T]:

  def addOnePrioritised(item: T, priority: Float): Unit

  /**
   * If getBatch is invoked multiple times only priorities from the most recent invocation can be updated using this method.
   * Item deletions from buffer which happen in between getBatch and updateLastBatch methods might result in updating incorrect items (when
   * deleted item is meant to be updated)
   * @param newPriorities new priorites for the items from the last batch - needs to maintain the same order as last batch
   */
  def updateLastBatch(newPriorities: IterableOnce[Float]): Unit
