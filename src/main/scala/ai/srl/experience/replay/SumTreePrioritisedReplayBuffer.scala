package ai.srl.experience.replay

import ai.srl.collection.*
import ai.srl.collection.SumTree.{IndexedValueItem, ValuedItem}
import ai.srl.env.BanditEnv
import ai.srl.experience.collector.{BanditEnvCollector, TimeseriesBanditEnvCollector}
import ai.srl.experience.config.ReplayConfig
import ai.srl.experience.replay.IndexedPrioritisedReplayBuffer
import ai.srl.experience.replay.PrioritisedReplayBuffer.IndexedPriority
import ai.srl.experience.store.IndexedPrioritisedExperienceStore.{IndexedItem, PrioritisedIndex}
import ai.srl.step.{SimpleStep, TimeseriesEnvStep}
import cats.kernel.Eq
import org.slf4j.LoggerFactory
import zio.{Chunk, Tag, UIO, ZIO, ZLayer}

import scala.reflect.ClassTag
import scala.util.Random

abstract class SumTreePrioritisedReplayBuffer[T] private (
    val batchSize: Int,
    val bufferSize: Int,
    val defaultPriority: Float
) extends PrioritisedReplayBuffer[T]:
  assert(batchSize > 0)
  assert(batchSize <= bufferSize)
  assert(defaultPriority >= 0)

  private[SumTreePrioritisedReplayBuffer] val items: SumTree[T]
  private var actualSize = 0
  // TODO use random from ZIO
  private val random = Random()
  // TODO remove it
  private var lastBatchIndexes: Seq[Int] = List.empty

  // TODO test it
  override def updatePriorities(prioritisedIndexedElements: Seq[IndexedPriority]): UIO[Unit] =
    ZIO.succeed(prioritisedIndexedElements.foreach(items.updateValue))

  override def getIndexedBatch: UIO[Chunk[IndexedElement[T]]] = ZIO.succeed(Chunk.fromIterable(getBatchInternal))

  // TODO change it to return chunk as well
  private def getBatchInternal: IndexedSeq[IndexedElement[T]] =
    if actualSize == 0 then IndexedSeq.empty
    else
      val priorities: IndexedSeq[Float] = (1 to batchSize).map(_ => random.nextFloat() * items.totalValue())
      val prioritisedIndexedBatch: IndexedSeq[IndexedValueItem[T]] = priorities.map(items.get)
      lastBatchIndexes = prioritisedIndexedBatch.map(_.index)
      prioritisedIndexedBatch.map(element => IndexedElement(index = element.index, element = element.element.item))

object SumTreePrioritisedReplayBuffer:

  def layer[Ac: Tag, Observation: Tag, Length <: Int: Tag]: ZLayer[
    TimeseriesBanditEnvCollector[Ac, Observation, Length] & BanditEnv[Ac, Observation] & ReplayConfig,
    Nothing,
    SumTreePrioritisedReplayBuffer[TimeseriesEnvStep[Ac, Observation, Length]]
  ] = ZLayer {
    for
      replayConfig <- ZIO.service[ReplayConfig]
      banditEnv    <- ZIO.service[BanditEnv[Ac, Observation]]
      trajectories <- ZIO.serviceWith[TimeseriesBanditEnvCollector[Ac, Observation, Length]](_.collect(banditEnv))
    yield SumTreePrioritisedReplayBuffer(
      batchSize = replayConfig.batchSize,
      bufferSize = replayConfig.bufferSize,
      defaultPriority = replayConfig.prioritised.defaultPriority,
      items = trajectories
    )
  }

  def apply[T: ClassTag, C[_]: GetIterator](
      batchSize: Int,
      bufferSize: Int,
      defaultPriority: Float,
      items: C[T]
  ): SumTreePrioritisedReplayBuffer[T] =
    val replayBuffer = new SumTreePrioritisedReplayBuffer[T](batchSize, bufferSize, defaultPriority):
      override val items = SumTree.emptyOfCapacity(this.bufferSize)
    items.iterator.foreach(item => replayBuffer.addOne(item))
    replayBuffer

  def apply[T: ClassTag](
      batchSize: Int,
      bufferSize: Int,
      defaultPriority: Float
  ): SumTreePrioritisedReplayBuffer[T] = new SumTreePrioritisedReplayBuffer[T](batchSize, bufferSize, defaultPriority):
    override val items = SumTree.emptyOfCapacity(this.bufferSize)

  // TODO remove it completely
  given [T: ClassTag]: IndexedPrioritisedReplayBuffer[SumTreePrioritisedReplayBuffer[T], T] with
    extension (prb: SumTreePrioritisedReplayBuffer[T])
      def updateLastBatch(newPriorities: Seq[Float]): Unit =
        require(prb.lastBatchIndexes.size == newPriorities.size)
        prb.lastBatchIndexes.iterator.zip(newPriorities).map(IndexedElement.apply).foreach(prb.items.updateValue)

      def getBatch(): Array[T] =
        prb.getBatchInternal.map(_.element).toArray

      def getBatchSize(): Int = prb.batchSize

      @throws(classOf[Exception])
      def addOnePrioritised(item: T, priority: Float): Unit =
        prb.actualSize = math.min(prb.actualSize + 1, prb.bufferSize)
        prb.items.addOne(ValuedItem(item, priority))

      // TODO should I use nextFloat or nextDouble and convert it to float after multiplication?
      @throws(classOf[Exception])
      def addOne(item: T): Unit = prb.addOnePrioritised(item, prb.defaultPriority)

      def size: Int = prb.actualSize

      def maxSize(): Int = prb.bufferSize

      def clearAll(): Unit =
        prb.items.clearAll()
        prb.actualSize = 0
        prb.lastBatchIndexes = List.empty

      //////////////// Indexed methods:

//      def getPrioritisedIndexedBatch(): Array[PrioritisedIndexedElement[T]] = ???
//        prb.getBatchInternal().toArray

      def getIndexedBatch(): Array[IndexedItem[T]] =
        prb.getBatchInternal.map(item => IndexedItem(item.element, item.index)).toArray

      override def update(prioritisedIndex: PrioritisedIndex): Unit =
        prb.items.updateValue(IndexedElement(prioritisedIndex.idx, prioritisedIndex.priority))

  given sumTreePrioritisedRBGetIterator: GetIterator[SumTreePrioritisedReplayBuffer] with
    extension [T](tree: SumTreePrioritisedReplayBuffer[T]) def iterator: Iterator[T] = tree.items.iterator
