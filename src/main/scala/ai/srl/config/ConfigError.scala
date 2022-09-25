package ai.srl.config

enum ConfigError(message: String):
  case LoadConfigError(path: String, message: String) extends ConfigError(message)
  case InternalConfigError(throwable: Throwable)      extends ConfigError(throwable.getMessage())
