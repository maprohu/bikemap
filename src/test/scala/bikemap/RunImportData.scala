package bikemap

import org.openstreetmap.osmosis.core.Osmosis

/**
  * Created by pappmar on 20/04/2017.
  */
object RunImportData {
  val OsmFile = "local/portugal-latest.osm.pbf"
//  val OsmFile = "local/andorra-latest.osm.pbf"

  def main(args: Array[String]): Unit = {
    import DB._

    Osmosis.run(
      Array(
        "--read-pbf", s"file=${OsmFile}",
        "--log-progress",
        "--write-pgsimp",
        "host=localhost:5432",
        s"database=${DatabaseName}",
        s"user=${UserName}",
        s"password=${Password}"
      )
    )


  }

}
