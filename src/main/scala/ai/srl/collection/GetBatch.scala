package ai.srl.collection

trait GetBatch[T, BatchItem]:
  extension (t: T) 
    def getBatch(): Array[BatchItem]
    def getBatchSize(): Int
