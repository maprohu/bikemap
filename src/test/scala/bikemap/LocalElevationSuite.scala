package bikemap

import gov.nasa.worldwind.geom.{Angle, LatLon}
import org.scalatest.FunSuite

/**
  * Created by pappmar on 15/05/2017.
  */
class LocalElevationSuite extends FunSuite {

  test("hashing works fine") {
    assert(
      LocalElevationModel.hash(
        new LatLon(
          Angle.NEG90,
          Angle.NEG180
        )
      ) == 0
    )

    assert(
      LocalElevationModel.hash(
        new LatLon(
          Angle.NEG90,
          Angle.fromDegrees(-179)
        )
      ) == 60 * 60 / 3
    )
  }

  test("file name pattern should extract fields") {
    val LocalElevationModel.FileNamePattern(ns, lat, ew, lon) = "S01E002.hgt"

    assert(ns.toLowerCase == "s")
    assert(lat.toInt == 1)
    assert(ew.toLowerCase == "e")
    assert(lon.toInt == 2)
  }

}
