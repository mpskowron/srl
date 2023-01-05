package ai.srl.collection

import cats.Functor
import cats.*
import scala.reflect.ClassTag

object Tuples:
  extension [A](tuple2: (A, A))
    def fmap[B](f: A => B): (B, B) = (f(tuple2._1), f(tuple2._2))
    def unzip[B](f: (A, A) => B): B = f.tupled(tuple2)
