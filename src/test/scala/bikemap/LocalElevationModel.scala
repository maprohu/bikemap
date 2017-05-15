package bikemap

import gov.nasa.worldwind.geom.{Angle, LatLon, Sector}
import ammonite.ops._

/**
  * Created by pappmar on 15/05/2017.
  */
abstract class LocalElevationModel(dir: Path) extends MinimalElevationModel {

  LocalElevationModel
    .scan(dir)
    .foreach(println)





}

object LocalElevationModel {

  type HashKey = Int
  val Resolution = Angle.SECOND.radians * 3

  val LongitudeLimit = 180 * 60 * 60
  val LatitudeLimit = 360 * 60 * 60
  val LatitudeShift = Integer.SIZE - Integer.numberOfLeadingZeros(LongitudeLimit)

  def hash(latLon: LatLon) : HashKey = {
    def reduce(value: Double, limit: Int) = {
      val r = ((value / Resolution) + 0.5).toInt

      if (r >= limit) {
        r - limit
      } else {
        r
      }
    }

    reduce(latLon.latitude.radians + Angle.POS90.radians, LatitudeLimit) << LatitudeShift |
      reduce(latLon.longitude.radians + Angle.POS180.radians, LongitudeLimit)
  }

  private [bikemap] val FileNamePattern = """([nNsS])(\d{2})([eEwW])(\d{3})\.[hH][gG][tT]""".r
  def scan(dir: Path) = {
    ls
      .rec(dir)
      .filter(_.isFile)
      .map(f => (f, f.name))
      .collect({
        case (f, FileNamePattern(ns, lat, ew, lon)) =>
          f
      })
  }

}