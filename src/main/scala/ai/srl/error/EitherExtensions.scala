package ai.srl.error

object EitherExtensions:
  extension [A](either: Either[?, A])
    def throwErr: A = either.fold(err => throw IllegalStateException(err.toString), identity)

    def orDie: A = either.throwErr
