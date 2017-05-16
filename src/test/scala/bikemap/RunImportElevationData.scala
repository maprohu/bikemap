package bikemap

import java.sql.Connection

import gov.nasa.worldwind.geom.Sector

/**
  * Created by pappmar on 16/05/2017.
  */
object RunRecreateImportElevationData {

  def main(args: Array[String]): Unit = {
    val c = ElevationDB.recreateH2DB
    RunImportElevationData.run(c)

  }

}

object RunImportElevationData {

  def run(c: Connection) = {
    ElevationDB.importData(c)
    c.close()

  }

}

object RunTestElevation {
  def main(args: Array[String]): Unit = {

    Iterator
      .iterate(0)(_ + 1)
      .take(3)
      .foreach(println)


    println(
      ElevationFiles.sections(
        0, 3, 12
      )
    )

    val f = ElevationFiles.read
    f.render(
      Sector.fromDegrees(0, 1, 0, 1),
      512,
      512
    )
  }
}