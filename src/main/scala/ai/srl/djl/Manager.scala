package ai.srl.djl

import ai.djl.ndarray.NDManager
import zio.ZIO.{attempt, fromAutoCloseable}
import zio.{Scope, ZIO, ZLayer}

object Manager:

  case class ManagerCreationError(throwable: Throwable)

  def layer(): ZLayer[Any, ManagerCreationError, NDManager] = ZLayer.scoped(baseManager())

  def baseManager(): ZIO[Scope, ManagerCreationError, NDManager] = newManager(NDManager.newBaseManager())

  def subManager(manager: NDManager): ZIO[Scope, ManagerCreationError, NDManager] = newManager(manager.newSubManager())

  private def newManager(constructor: => NDManager): ZIO[Scope, ManagerCreationError, NDManager] = fromAutoCloseable(
    attempt(constructor).mapError(ManagerCreationError.apply)
  )
