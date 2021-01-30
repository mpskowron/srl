package ai.srl.logging

import java.io.FileWriter
import java.nio.file.Path

import org.supercsv.io.CsvListWriter
import org.supercsv.prefs.CsvPreference

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
