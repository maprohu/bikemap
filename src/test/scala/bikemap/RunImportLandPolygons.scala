package bikemap

import bikemap.LandPolygons.TableName
import org.h2.tools.Server

/**
  * Created by pappmar on 11/05/2017.
  */
object RunImportLandPolygons {

  def main(args: Array[String]): Unit = {
    val c = LandPolygons.recreateH2DB()
    LandPolygons.importFromShapeFile(c)
    c.close()

    val c2 = LandPolygons.connectH2DbLock(
      LandPolygons.h2Name()
    )
    val s = c2.createStatement()
    s.execute(
      s"alter table ${TableName} drop column fid"
    )
    s.close()
    c2.close()
  }

}

object RunImportLandPolygonsGui {

  def main(args: Array[String]): Unit = {
    val c = LandPolygons.connectH2DbLock(
      LandPolygons.h2Name()
    )
    Server.startWebServer(c)

  }

}
