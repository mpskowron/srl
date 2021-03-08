package ai.srl.collection

/**
 *  Theoretically, this class could be more generic and not require GetBatch constraint. However, in such case, switching implementations
 *  would be error prone, since updating priorities like that assumes that some state is kept in class T.
  * @tparam T
 */ 
trait UpdateBatchPriorities[T] extends GetBatch[T, ?]:
  extension (t: T)
    /**
     * If getBatch is invoked multiple times only priorities from the most recent invocation can be updated using this method.
     * Item deletions from buffer which happen in between getBatch and updateLastBatch methods might result in updating incorrect items (when
     * deleted item is meant to be updated)
     * @param newPriorities new priorites for the items from the last batch - needs to maintain the same size and order as last batch
     *                      assert(newPriorities.size == t.getBatchSize())
     */
    def updateLastBatch(newPriorities: Seq[Float]): Unit 
