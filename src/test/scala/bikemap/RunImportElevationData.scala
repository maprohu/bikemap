package bikemap

import java.sql.Connection

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
