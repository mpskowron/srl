package ai.srl.collection

trait Sized[S <: Int: ValueOf] {
  def size: Int = valueOf[S]
}
