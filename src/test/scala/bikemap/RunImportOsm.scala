package bikemap

import java.sql.DriverManager

import org.h2.Driver
import org.h2.tools.Server
import org.h2gis.ext.H2GISExtension

import scala.io.StdIn

/**
  * Created by pappmar on 17/05/2017.
  */
object RunImportOsm {


  def main(args: Array[String]): Unit = {
    import ammonite.ops._

    rm(pwd / 'local / 'h2osm)
    Driver.load()

    val c = DriverManager.getConnection("jdbc:h2:file:./local/h2osm/osm;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0", "sa", "sa")
    H2GISExtension.load(c)

    val start = System.currentTimeMillis()

    val call = c.prepareCall(
      "CALL OSMRead(?, ?)"
    )
    call.setString(1, "local/portugal-latest.osm.bz2")
    call.setString(2, "OSM_PT")
    call.execute()

    println(s"done: ${System.currentTimeMillis() - start}")

    Server.startWebServer(c)

    StdIn.readLine("enter...")
    c.close()




  }

}

object RunImportedOsm {
  def main(args: Array[String]): Unit = {
    Driver.load()
    val c = DriverManager.getConnection("jdbc:h2:file:./local/h2osm/osm", "sa", "sa")
    Server.startWebServer(c)



  }
}