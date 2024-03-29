package ai.srl.experience.replay

import ai.srl.collection.IndexedElement
import ai.srl.experience.replay.PrioritisedReplayBuffer.IndexedPriority
import zio.{Chunk, Tag, UIO, URIO, ZIO}

trait ReplayBuffer[+A]:
  def getBatch: UIO[Chunk[A]]

object ReplayBuffer:
  def getBatch[A: Tag]: URIO[ReplayBuffer[A], Chunk[A]] =
    ZIO.serviceWithZIO[ReplayBuffer[A]](_.getBatch)

trait IndexedReplayBuffer[+A] extends ReplayBuffer[A]:
  override def getBatch: UIO[Chunk[A]] = getIndexedBatch.map(_.map(_.element))
  def getIndexedBatch: UIO[Chunk[IndexedElement[A]]]

object IndexedReplayBuffer:

  def getIndexedBatch[A: Tag]: URIO[IndexedReplayBuffer[A], Chunk[IndexedElement[A]]] =
    ZIO.serviceWithZIO[IndexedReplayBuffer[A]](_.getIndexedBatch)

trait PrioritisedReplayBuffer[+A] extends IndexedReplayBuffer[A]:
  def updatePriorities(prioritisedIndexedElements: Iterable[IndexedPriority]): UIO[Unit]

object PrioritisedReplayBuffer:
  type IndexedPriority = IndexedElement[Float]

  def updatePriorities[A: Tag](prioritisedIndexedElements: Iterable[IndexedPriority]): URIO[PrioritisedReplayBuffer[A], Unit] =
    ZIO.serviceWithZIO[PrioritisedReplayBuffer[A]](_.updatePriorities(prioritisedIndexedElements))
