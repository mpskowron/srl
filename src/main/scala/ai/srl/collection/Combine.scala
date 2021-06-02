package ai.srl.collection

import cats.Semigroup

trait Combine[A, B, C]:
  extension (a: A)
    def combine(b: B): C
    
object Combine:
  given [A: Semigroup]: Combine[A, A, A] with
    extension (a: A)
      def combine(b: A): A = Semigroup[A].combine(a, b)
