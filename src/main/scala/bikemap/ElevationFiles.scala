package bikemap

import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode
import java.nio.file.StandardOpenOption
import java.nio.{ByteBuffer, ByteOrder}

import ammonite.ops._
import com.jogamp.common.nio.Buffers
import gov.nasa.worldwind.geom.{Angle, Sector}

import scala.collection.GenTraversable

/**
  * Created by pappmar on 16/05/2017.
  */

trait Elevations {
  def render(
    sector: Sector,
    width: Int,
    height: Int
  ) : ByteBuffer
}

object ElevationFiles {

  case class ElevationFile(
    file: Path,
    lat: Int,
    lon: Int
  )

  val Resolution = Angle.SECOND.radians * 3
  val DataPointsPerDegree = 1200


  val Size3 = (DataPointsPerDegree+1) * (DataPointsPerDegree+1) * 2
  val Size1 = (DataPointsPerDegree*3+1) * (DataPointsPerDegree*3+1) * 2

  def read = {

    val FileNamePattern = """([nNsS])(\d{2})([eEwW])(\d{3})\.[hH][gG][tT]""".r
    val dir = "local/viewfinderpanoramas"


    val RowCount = 180
    val ColCount = 360

    val EmptyRow =
      Vector.fill(ColCount)(Option.empty[ElevationFile])
    val EmptyFiles =
      Vector.fill(RowCount)(EmptyRow)




    val files =
      ls
        .rec(Path(dir, pwd))
        .filter(_.isFile)
        .map(f => (f, f.name))
        .collect({
          case (f, FileNamePattern(ns, lat, ew, lon)) =>
            def latNum = lat.toInt
            def lonNum = lon.toInt
            def latDeg = ns.toUpperCase() match { case "N" => latNum ; case "S" => -latNum ; case _ => ???  }
            def lonDeg = ew.toUpperCase() match { case "E" => lonNum ; case "W" => -lonNum ; case _ => ???  }

            (f, latDeg, lonDeg)
        })
        .foldLeft(EmptyFiles)({ (acc, item) =>
          val (f, lat, lon) = item

          val latIdx = lat + 90
          val lonIdx = lon + 180

          acc.updated(
            latIdx,
            acc(latIdx).updated(
              lonIdx,
              Some(
                ElevationFile(
                  f,
                  lat,
                  lon
                )

              )
            )
          )
        })

    new Elevations {
      override def render(sector: Sector, width: Int, height: Int): ByteBuffer = {

        val bb = Buffers.newDirectByteBuffer(width * height * 2)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val sb = bb.asShortBuffer()

        val lons = sections(
          sector.getMinLongitude.degrees,
          sector.getDeltaLonDegrees,
          width
        )
        val lats = sections(
          sector.getMinLatitude.degrees,
          sector.getDeltaLatDegrees,
          height
        )

        lats.foreach { lat =>
          val originRow = height - lat.offset - 1

          lons.foreach { lon =>
            val originCol = lon.offset

            sb.position(originRow * width + originCol)

            val file = files(lat.degree+90)(lon.degree+180)
            val reader = openSection(file)

            sb.mark()

            lat
              .fractions
              .foreach { latFraction =>

                lon
                  .fractions
                  .foreach { lonFraction =>
                    val v = reader.value(latFraction, lonFraction)
                    sb.put(v)
                  }

                sb.reset()
                val newPosition = sb.position() - width
                if (newPosition > 0) {
                  sb.position(newPosition)
                  sb.mark()
                }
              }

            reader.close()
          }
        }



        bb
      }
    }


  }

  case class Section(
    degree: Int,
    offset: Int,
    fractions: List[Double]
  )

  def sections(min: Double, delta: Double, count: Int) = {
    val diff = delta / count

    Iterator
      .iterate(min + diff / 2)(_ + diff)
      .take(count)
      .foldLeft(List.empty[Section])({ (acc, x) =>
        val int = math.floor(x)
        val degree = x.toInt
        val fraction = x - int

        acc match {
          case l @ h :: t =>
            if (h.degree == degree) {
              h.copy(fractions = fraction :: h.fractions) :: t
            } else {
              Section(degree, h.offset + h.fractions.size, List(fraction)) :: l
            }
          case _ =>
            List(
              Section(degree, 0, List(fraction))
            )
        }
      })
      .map({ s => s.copy(fractions = s.fractions.reverse)})
      .reverse









  }

  trait SectionReader {
    def value(lat: Double, lon: Double) : Short
    def close() : Unit
  }

  object MissingSectionReader extends SectionReader {
    override def value(lat: Double, lon: Double): Short = 0
    override def close(): Unit = ()
  }

  abstract class FileSectionReader(ch: FileChannel) extends SectionReader {

    val bb = ch.map(MapMode.READ_ONLY, 0, ch.size())
    bb.order(ByteOrder.BIG_ENDIAN)
    val sb = bb.asShortBuffer()

    override def close(): Unit = {
      ch.close()
    }
  }

  class DefaultFileSectionReader(ch: FileChannel, resolution: Int) extends FileSectionReader(ch) {
    val fileWidth = resolution + 1

    def moveTo(latIdx: Int, lonIdx: Int) = {
      sb.position(
        (resolution - latIdx) * fileWidth + lonIdx
      )
    }

    override def value(lat: Double, lon: Double): Short = {
      val x = lon * resolution
      val y = lat * resolution

      val x1 = math.floor(x)
      val lonIdx = x1.toInt
      val y1 = math.floor(y)
      val latIdx = y1.toInt

      val wQ2 = x - x1
      val wQ1 = 1 - wQ2


      moveTo(latIdx, lonIdx)
      val fQ11 = sb.get()
      val fQ21 = sb.get()
      moveTo(latIdx+1, lonIdx)
      val fQ12 = sb.get()
      val fQ22 = sb.get()

      val fxy1 = wQ1 * fQ11 + wQ2 * fQ21
      val fxy2 = wQ1 * fQ12 + wQ2 * fQ22

      val wf2 = y - y1
      val wf1 = 1 - wf2

      math.round(wf1 * fxy1 + wf2 * fxy2).toShort
    }
  }

  def openSection(fileOpt: Option[ElevationFile]) : SectionReader = {
    fileOpt
      .map({ file =>


        val ch = FileChannel.open(file.file.toNIO, StandardOpenOption.READ)
        ch.size() match {
          case Size3 =>
            new DefaultFileSectionReader(ch, 1200)
          case Size1 =>
            new DefaultFileSectionReader(ch, 3600)
          case _ => ???
        }
      })
      .getOrElse(MissingSectionReader)
  }


}
