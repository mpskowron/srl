package ai.srl.convertion

trait Converter[From, To]:
  def convert(step: From): To
