package bikemap

import java.nio.{ByteBuffer, ByteOrder}
import java.sql.{Connection, DriverManager}
import ammonite.ops._

import gov.nasa.worldwind.geom.Angle
import org.h2.Driver

/**
  * Created by pappmar on 16/05/2017.
  */

object ElevationFiles {

  case class ElevationFile(
    file: Path,
    lat: Int,
    lon: Int
  )

  val Resolution = Angle.SECOND.radians * 3
  val DataPointsPerDegree = 1200


  def read = {

    val FileNamePattern = """([nNsS])(\d{2})([eEwW])(\d{3})\.[hH][gG][tT]""".r
    val dir = "local/viewfinderpanoramas"

    val Size3 = (DataPointsPerDegree+1) * (DataPointsPerDegree+1) * 2
    val Size1 = (DataPointsPerDegree*3+1) * (DataPointsPerDegree*3+1) * 2

    val RowCount = 180
    val ColCount = 360

    val EmptyRow =
      Vector.fill(ColCount)(Option.empty[ElevationFile])
    val EmptyFiles =
      Vector.fill(RowCount)(EmptyRow)




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
      .foreach(println)


  }


}
