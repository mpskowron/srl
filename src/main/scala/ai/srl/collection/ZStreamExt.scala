package ai.srl.collection
import zio.stream.ZStream
import zio.ZIO
import zio.ZManaged
import cats.syntax.bifunctor.toBifunctorOps

object ZStreamExt:
  extension [R, E, O](stream: ZStream[R, E, O])
    // Unfortunately this method collects the stream before splitting it to 2 other streams
    def flatMap2[O1, O2](f1: O => O1)(f2: O => O2): ZIO[R, E, (ZStream[Any, Nothing, O1], ZStream[Any, Nothing, O2])] =
      stream
        .mapM(o => ZIO.succeed(f1(o)).zipPar(ZIO.succeed(f2(o))))
        .runCollect
        .map(_.unzip.bimap(ZStream.fromChunk, ZStream.fromChunk))

    def flatMap2Either[O1, O2, E1, E2](
        f1: O => Either[E1, O1]
    )(f2: O => Either[E2, O2]): ZIO[R, E | E1 | E2, (ZStream[Any, Nothing, O1], ZStream[Any, Nothing, O2])] =
      stream
        .mapM[R, E | E1 | E2, (O1, O2)](o => ZIO.fromEither(f1(o)).zipPar(ZIO.fromEither(f2(o))))
        .runCollect
        .map(_.unzip.bimap(ZStream.fromChunk, ZStream.fromChunk))

// Alternative flatMap2 implementation with broadcasting (this one is probably lazy)
// .broadcast(2, 1).mapM {
//   // mapping to drop parts of the tuple after broadcast is quite ineeficiect but I couldn't find other simple and declarative way to do that
//   case left :: right :: Nil => ZIO.succeed(left.map(_._1), right.map(_._2))
//   case other                => ZIO.dieMessage(s"[BUG] expected 2 streams but got ${other.size}")
// }
