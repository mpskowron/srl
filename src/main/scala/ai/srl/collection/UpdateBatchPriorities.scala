package ai.srl.collection

trait UpdateBatchPriorities[T]:
  extension (t: T)
    /**
     * It is assummed that some method for getting Batch is invoked before this one (lets call it getBatch). If getBatch is invoked 
     * multiple times only priorities from the most recent invocation can be updated using this method.
     * Item deletions from buffer which happen in between getBatch and updateLastBatch methods might result in updating incorrect items (when
     * deleted item is meant to be updated)
     * @param newPriorities new priorites for the items from the last batch - needs to maintain the same size and order as last batch
     *                      assert(newPriorities.size == t.getBatchSize())
     */
    def updateLastBatch(newPriorities: Seq[Float]): Unit 
