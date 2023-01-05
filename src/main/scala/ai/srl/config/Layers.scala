package ai.srl.config

import com.typesafe.config.{Config, ConfigFactory}
import io.circe
import io.circe.Decoder
import io.circe.config.parser
import org.slf4j.LoggerFactory
import zio.config.ReadError
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.typesafe.TypesafeConfig
import zio.{Layer, Tag, Unsafe, ZEnvironment, ZIO, ZLayer}
import zio.config.*

object Layers:
  private val logger = LoggerFactory.getLogger(this.getClass)

  def configLayer[T: Tag: Decoder](path: String, fileName: String = "application.conf"): ZLayer[Any, ConfigError, T] =
    ZLayer.fromZIO {
      ZIO
        .fromEither {
          // TODO map to configuration load error here
          val config: Config                       = ConfigFactory.load(fileName)
          val configEither: Either[circe.Error, T] = parser.decodePath[T](config, path)
          configEither.foreach(config => logger.info(s"Config loaded $config"))
          configEither
        }
        .mapError(err => ConfigError.LoadConfigError(path, err.getMessage))
    }

  def loadConfigUnsafe[T: Decoder: Tag](path: String, fileName: String = "application.conf")(using Unsafe): T =
    zio.Runtime.default.unsafe
      .run {
        ZIO.service[T].provideLayer(configLayer[T](path, fileName))
      }
      .getOrThrowFiberFailure()

  def configLayer[A: Descriptor: Tag](path: String): Layer[ReadError[String], A] = configLayer(path, descriptor[A])

  def configLayer[A: Tag](path: String, desc: ConfigDescriptor[A]): Layer[ReadError[String], A] =
    TypesafeConfig.fromTypesafeConfig(ZIO.attempt(ConfigFactory.load().getConfig(path)), desc)
