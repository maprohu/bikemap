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
        "--write-pgsql",
        "host=localhost:5432",
        s"database=${DatabaseName}",
        s"user=${UserName}",
        s"password=${Password}"
      )
    )


  }

}

object RunImportData2 {
  val OsmFile = "local/portugal-latest.osm.pbf"
//    val OsmFile = "local/andorra-latest.osm.pbf"

  def main(args: Array[String]): Unit = {
    import DB._
    import ammonite.ops._
    val dir = pwd / 'local / 'pgimport
    rm(dir)
    mkdir(dir)

    Osmosis.run(
      Array(
        "--read-pbf", s"file=${OsmFile}",
        "--log-progress",
        "--write-pgsql-dump",
        "directory=local/pgimport",
        "enableBboxBuilder=yes"

      )
    )


  }

}

object RunImportData3 {
  def main(args: Array[String]): Unit = {
    import ammonite.ops._
    import DB._
    implicit val dir = pwd / 'local / 'pgimport

    %(
      "psql",
      "-U", UserName,
      "-d", DatabaseName,
      "-f", s"../../src/test/resources/osmosis/script/pgsnapshot_load_0.6.sql"
    )

  }
}

