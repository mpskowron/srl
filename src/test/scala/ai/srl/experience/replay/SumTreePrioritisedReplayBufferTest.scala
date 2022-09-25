package ai.srl.experience.replay

import ai.srl.assertions.Assertions
import ai.srl.collection.{CanClose, IndexedElement}
import ai.srl.experience.replay.ReplayBufferAssertions.assertCorrectBatches
import ai.srl.experience.store.IndexedPrioritisedExperienceStore.{IndexedItem, PrioritisedIndex}
import org.junit.Assert.assertEquals as jAssertEquals
import org.junit.runner.RunWith
import zio.test.TestResult.all
import zio.*
import zio.test.Assertion.approximatelyEquals
import zio.test.{test, *}

import scala.collection.MapView
import scala.reflect.ClassTag

@RunWith(classOf[zio.test.junit.ZTestJUnitRunner])
class SumTreePrioritisedReplayBufferTest extends ZIOSpecDefault:
  private val batchSize       = 5
  private val bufferSize      = 12
  private val defaultPriority = 1

  def assertApprox(expected: Int): Assertion[Int] = approximatelyEquals(expected, math.max(700, expected / 250))
  def assertApproxEquals(actual: Int, expected: Int): TestResult =
    assert(actual)(approximatelyEquals(expected, math.max(700, expected / 250)))

  def spec = suite("SumTreePrioritisedReplayBuffer")(
    test("updateLastBatch works correctly") {
      val buffer =
        SumTreePrioritisedReplayBuffer[Int, IterableOnce](batchSize, bufferSize, defaultPriority.toFloat, 0 to 11)
      for
        indexedBatch <- buffer.getIndexedBatch
        // batch can contain repeated items, thus we calculate how many different items we had sampled
        numberOfDifferentItems = indexedBatch.toSet.size
        batchUpdate            = List.fill(indexedBatch.size)(2f * defaultPriority)
        _ <- buffer.updatePriorities(indexedBatch.map(_.index).zip(batchUpdate).map(IndexedElement.apply))
        baseCount     = 10000
        totalPriority = bufferSize * defaultPriority + numberOfDifferentItems * defaultPriority
        sampleSize    = (totalPriority * baseCount).toInt
        manyBatches <- buffer.getBatch.replicateZIO(sampleSize).map(_.flatten)
        counts = manyBatches.groupBy(identity).view.mapValues(_.size)
      yield all(counts.map { (item, count) =>
        val rawBatch      = indexedBatch.map(_.element)
        val expected: Int = baseCount * batchSize * defaultPriority * (if rawBatch.contains(item) then 2 else 1)
        assert(count)(assertApprox(expected))
      }.toSeq*)
    },
    test("not full buffer can be queried correctly") {
      val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
      buffer.addAll(0 to 6)
      assertUniformGetBatch(buffer, 10000 * buffer.size, batchSize)
    },
    test("empty buffer returns empty batches") {
      val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
      for
        getBatch        <- buffer.getBatch
        getIndexedBatch <- buffer.getIndexedBatch
      yield assertTrue(getBatch.isEmpty) && assertTrue(getIndexedBatch.isEmpty)
    },
    test("clearAll removes all elements from the buffer") {
      val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
      buffer.addAll(1 to 100)
      for
        getBatch        <- buffer.getBatch
        getIndexedBatch <- buffer.getIndexedBatch
        _ = assertTrue(getBatch.size == batchSize)
        _ = assertTrue(getIndexedBatch.size == batchSize)
        _ = buffer.clearAll()
        getBatch        <- buffer.getBatch
        getIndexedBatch <- buffer.getIndexedBatch
      yield assertTrue(getBatch.isEmpty) && assertTrue(getIndexedBatch.isEmpty)
    },
    test("getBatch and getIndexedBatch give results according to priority after additions and updates") {
      val buffer = SumTreePrioritisedReplayBuffer[Int](batchSize, bufferSize, defaultPriority.toFloat)
      buffer.addAll(1 to 100)
      var baseCount = 10000
      val samples   = baseCount * bufferSize

      for
        _           <- assertUniformGetBatch(buffer, samples, batchSize)
        manyBatches <- buffer.getBatch.replicateZIO(samples).map(_.flatten)
        _ <- all(
          manyBatches
            .groupBy(identity)
            .view
            .mapValues(_.size)
            .values
            .map(assert(_)(assertApprox(baseCount * defaultPriority * batchSize)))
            .toSeq*
        )
        _             = buffer.addOnePrioritised(1000, 50)
        _             = buffer.update(PrioritisedIndex(3, 10.5))
        _             = buffer.update(PrioritisedIndex(2, 0.2))
        _             = baseCount = 1000
        totalPriority = (bufferSize - 3 * defaultPriority) + 50 + 10.5 + 0.2
        sampleSize    = (totalPriority * baseCount).toInt
        manyBatches <- buffer.getBatch.replicateZIO(sampleSize).map(_.flatten)
        counts = manyBatches.groupBy(identity).view.mapValues(_.size)
        _               <- checkGetBatchesSampledCorrectly(counts, baseCount)
        semiRichBatches <- buffer.getIndexedBatch.replicateZIO(sampleSize).map(_.flatten)
        semiRichCounts = semiRichBatches.groupBy(identity).view.mapValues(_.size)
        _ <- checkIndexedBatchesSampledCorrectly(semiRichCounts, baseCount)
        _ <- buffer.updatePriorities(List(IndexedElement(5, 1.5f), IndexedElement(11, 0.8f)))
        totalPriority2 = totalPriority - 2 * defaultPriority + 1.5 + 0.5
        sampleSize     = (totalPriority2 * baseCount).toInt
        batchesAfterPriorityUpdate <- buffer.getBatch.replicateZIO(sampleSize).map(_.flatten)
        counts = batchesAfterPriorityUpdate.groupBy(identity).view.mapValues(_.size)
      yield assertApproxEquals(counts(90), (baseCount * 1.5f).round * batchSize) && assertApproxEquals(
        counts(96),
        (baseCount * 0.8f).round * batchSize
      )
    },
    test("adds and removes correct closeable elements") {
      val items  = (1 to 10).map(i => CanClose(Some(i)))
      val buffer = SumTreePrioritisedReplayBuffer[CanClose](4, 4, 1)
      buffer.addAll(items)
      buffer.update(PrioritisedIndex(0, 8.0f))

      for
        _ <- all(items.take(6).map(item => assertTrue(item.option.isEmpty))*) &&
          all(items.drop(6).zip(7 to 10).map((actual, expected) => assertTrue(actual.option.contains(expected)))*)
          && assertCorrectBatches(buffer, items.drop(6))
        thousand = CanClose(Some(1000))
        _        = buffer.addOnePrioritised(thousand, 4.0f)
        _ <- assertTrue(thousand.option.contains(1000)) && assertTrue(items(6).option.isEmpty)
        hundred = CanClose(Some(100))
        _       = buffer.addOne(hundred)
        _ <- assertTrue(hundred.option.contains(100)) && assertTrue(items(7).option.isEmpty)
      yield all(items.drop(8).zip(9 to 10).map((actual, expected) => assertTrue(actual.option.contains(expected)))*)
    }
  )

  private def checkIndexedBatchesSampledCorrectly(semiRichCounts: MapView[IndexedElement[Int], Int], baseCount: Int) =
    all(semiRichCounts.map {
      case (IndexedElement(i, 1000), c) =>
        assertApproxEquals(i, 4)
        assertApproxEquals(c, baseCount * 50 * batchSize)
      case (IndexedElement(i, 100), c) =>
        assertApproxEquals(i, 3)
        assertApproxEquals(c, (baseCount * 10.5f).round * batchSize)
      case (IndexedElement(i, 99), c) =>
        assertApproxEquals(i, 2)
        assertApproxEquals(c, (baseCount * 0.2f).round * batchSize)
      case (_, c) => assertApproxEquals(c, baseCount * defaultPriority * batchSize)
    }.toSeq*)

  private def checkGetBatchesSampledCorrectly(bachCounts: MapView[Int, Int], baseCount: Int) =
    all(bachCounts.map {
      case (1000, c) => assert(c)(assertApprox(baseCount * 50 * batchSize))
      case (100, c)  => assert(c)(assertApprox((baseCount * 10.5f).round * batchSize))
      case (99, c)   => assert(c)(assertApprox((baseCount * 0.2f).round * batchSize))
      case (_, c)    => assert(c)(assertApprox(baseCount * defaultPriority * batchSize))
    }.toSeq*)

  private def assertUniformGetBatch[RB, T](buffer: RB, samples: Int, batchSize: Int)(using PureReplayBuffer[RB, T]) =
    val manyBatches = (1 to samples).flatMap(_ => buffer.getBatch())
    val occurrences = manyBatches.groupBy(identity).view.mapValues(_.size)
    all(occurrences.values.map(assert(_)(assertApprox((defaultPriority * batchSize * samples) / buffer.size))).toSeq*)
