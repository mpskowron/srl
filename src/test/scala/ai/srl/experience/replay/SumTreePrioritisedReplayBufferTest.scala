package ai.srl.experience.replay

import ai.srl.collection.CanClose
import ai.srl.experience.store.IndexedPrioritisedExperienceStore.{IndexedItem, PrioritisedIndex, PrioritisedIndexedItem}
import ai.srl.experience.replay.ReplayBufferAssertions.assertCorrectBatches
import ai.srl.assertions.Assertions

import scala.reflect.ClassTag

class SumTreePrioritisedReplayBufferTest extends munit.FunSuite:
  private val batchSize = 5
  private val bufferSize = 12
  private val defaultPriority = 1
  def assertEquals(actual: Int, expected: Int) = Assertions.assertApproxEquals(actual, expected, math.max(700, expected / 250))

  test("updateLastBatch works correctly") {
    val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
    buffer.addAll(0 to 11)
    val batch = buffer.getBatch()
    // batch can contain repeated items, thus we calculate how many different items we had sampled
    val numberOfDifferentItems = batch.toSet.size
    buffer.updateLastBatch(List.fill(batch.size)(2f * defaultPriority))
    val baseCount = 10000
    var totalPriority = bufferSize * defaultPriority + numberOfDifferentItems * defaultPriority
    var sampleSize = (totalPriority * baseCount).toInt

    val manyBatches = (1 to sampleSize).flatMap(_ => buffer.getBatch())
    val counts = manyBatches.groupBy(identity).view.mapValues(_.size)
    counts.foreach { (item, count) => 
      val expected = baseCount * batchSize * defaultPriority * (if batch.contains(item) then 2 else 1)
      assertEquals(count, expected)
    } 
  }
  
  test("not full buffer can be queried correctly") {
    val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
    buffer.addAll(0 to 6)
    assertUniformGetBatch(buffer, 10000 * buffer.size(), batchSize)
  }

  test("empty buffer returns empty batches") {
    val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
    assert(buffer.getBatch().size == 0)
    assert(buffer.getIndexedBatch().size == 0)
    assert(buffer.getPrioritisedIndexedBatch().size == 0)
  }
  
  test("getBatch and getIndexedBatch give results according to priority after additions and updates") {
    val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
    buffer.addAll(1 to 100)
    var baseCount = 10000
    val samples = baseCount * bufferSize
    assertUniformGetBatch(buffer, samples, batchSize)
    
    var manyBatches = (1 to samples).flatMap(_ => buffer.getPrioritisedIndexedBatch().map(_.item))
    manyBatches.groupBy(identity).view.mapValues(_.size).values.foreach(assertEquals(_, baseCount * defaultPriority * batchSize))
    
    buffer.addOnePrioritised(1000, 50)
    buffer.update(PrioritisedIndex(3, 10.5))
    buffer.update(PrioritisedIndex(2, 0.2))
    
    baseCount = 1000
    var totalPriority = (bufferSize - 3 * defaultPriority) + 50 + 10.5 + 0.2
    var sampleSize = (totalPriority * baseCount).toInt
    
    manyBatches = (1 to sampleSize).flatMap(_ => buffer.getBatch())
    var counts = manyBatches.groupBy(identity).view.mapValues(_.size)
    counts.foreach {
      case (1000, c) => assertEquals(c, baseCount * 50 * batchSize)
      case (100, c) => assertEquals(c, (baseCount * 10.5f).round * batchSize)
      case (99, c) => assertEquals(c, (baseCount * 0.2f).round * batchSize)
      case (_, c) => assertEquals(c, baseCount * defaultPriority * batchSize)
    }
    
    val semiRichBatches = (1 to sampleSize).flatMap(_ => buffer.getIndexedBatch())
    val semiRichCounts = semiRichBatches.groupBy(identity).view.mapValues(_.size)
    semiRichCounts.foreach {
      case (IndexedItem(1000, i), c) => 
        assertEquals(i, 4)
        assertEquals(c, baseCount * 50 * batchSize)
      case (IndexedItem(100, i), c) =>
        assertEquals(i, 3)
        assertEquals(c, (baseCount * 10.5f).round * batchSize)
      case (IndexedItem(99, i), c) =>
        assertEquals(i, 2)
        assertEquals(c, (baseCount * 0.2f).round * batchSize)
      case (_, c) => assertEquals(c, baseCount * defaultPriority * batchSize)
    }
    
    val richBatches = (1 to sampleSize).flatMap(_ => buffer.getPrioritisedIndexedBatch())
    val richCounts = richBatches.groupBy(identity).view.mapValues(_.size)
    richCounts.foreach {
      case (PrioritisedIndexedItem(1000, p, i), c) => 
        assertEquals(i, 4)
        assertEquals(p, 50.0f)
        assertEquals(c, (baseCount * p).round * batchSize)
      case (PrioritisedIndexedItem(100, p, i), c) => 
        assertEquals(i, 3)
        assertEquals(p, 10.5f)
        assertEquals(c, (baseCount * p).round * batchSize)
      case (PrioritisedIndexedItem(99, p, i), c) =>
        assertEquals(i, 2)
        assertEquals(p, 0.2f)
        assertEquals(c, (baseCount * p).round * batchSize)
      case (_, c) => assertEquals(c, baseCount * defaultPriority * batchSize)
    }
    
    buffer.updateBatch(List(PrioritisedIndex(5, 1.5), PrioritisedIndex(11, 0.8)))
    totalPriority = totalPriority - 2 * defaultPriority + 1.5 + 0.5
    sampleSize = (totalPriority * baseCount).toInt
    
    manyBatches = (1 to sampleSize).flatMap(_ => buffer.getBatch())
    counts = manyBatches.groupBy(identity).view.mapValues(_.size)
    assertEquals(counts(90), (baseCount * 1.5f).round * batchSize)
    assertEquals(counts(96), (baseCount * 0.8f).round * batchSize)
  }

  test("adds and removes correct closeable elements") {
    val items = (1 to 10).map(i => CanClose(Some(i)))
    val buffer = new SumTreePrioritisedReplayBuffer[CanClose](4, 4, 1)
    buffer.addAll(items)
    buffer.update(PrioritisedIndex(0, 8.0f))

    items.take(6).foreach(item => assertEquals(item.option, None))
    items.drop(6).zip(7 to 10).foreach((actual, expected) => assert(actual.option.contains(expected)))
    assertCorrectBatches(buffer, items.drop(6))

    val thousand = CanClose(Some(1000))
    buffer.addOnePrioritised(thousand, 4.0f)
    assert(thousand.option.contains(1000))
    assert(items(6).option.isEmpty)
    
    val hundred = CanClose(Some(100))
    buffer.addOne(hundred)
    assert(hundred.option.contains(100))
    assert(items(7).option.isEmpty)
    
    items.drop(8).zip(9 to 10).foreach((actual, expected) => assert(actual.option.contains(expected)))

  }

  private def assertUniformGetBatch[RB, T](buffer: RB, samples: Int, batchSize: Int)(using ReplayBuffer[RB, T]) =
    var manyBatches = (1 to samples).flatMap(_ => buffer.getBatch())
    val occurrences = manyBatches.groupBy(identity).view.mapValues(_.size)
    occurrences.values.foreach(assertEquals(_, (defaultPriority * batchSize * samples) / buffer.size()))

