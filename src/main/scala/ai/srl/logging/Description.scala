package ai.srl.logging

import java.io.FileWriter
import java.nio.file.Path
import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference
import zio.ZIO
import Description.given

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

trait Description[A]:
  extension (d: A) def describe(): Seq[(String, String)]
  extension (d: A) def save(path: Path) =
    val (header, row) = d.describe().unzip
    val file = path.toFile
    file.getParentFile().mkdirs()
    val listWriter = new CsvListWriter(new FileWriter(file, true), CsvPreference.STANDARD_PREFERENCE)
    if file.length == 0 then listWriter.writeHeader(header: _*)
    listWriter.write(row: _*)
    listWriter.close()

  extension (d: A) def saveWithDateTime(path: Path): ZIO[Any, Throwable, Unit] =
    for
      dateTimeDesc <- Description.currentDateTime
      combinedDescription = d.describe() ++ Seq(dateTimeDesc, ("",""))
      _ <- ZIO.attempt(combinedDescription.save(path))
    yield ()

class Descriptable[A: Description](val a: A):
  def describe(): Seq[(String, String)] = a.describe()

object Description:
  given [T: Description]: Conversion[T, Descriptable[T]] with
    def apply(description: T): Descriptable[T] = Descriptable(description)

  given Description[(String, String)] with
    extension (desc: (String, String)) def describe() = Seq(desc)

  given Description[Seq[(String, String)]] with
    extension (desc: Seq[(String, String)]) def describe() = desc
  
  given Description[List[Descriptable[?]]] with
    extension (descs: List[Descriptable[?]]) def describe() = descs.flatMap(_.describe())

  def currentDateTime: ZIO[Any, Nothing, (String, String)] =
    for {
      clock <- ZIO.clock
      time <- clock.currentDateTime
    } yield ("dateTime", time.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MINUTES).toString)
