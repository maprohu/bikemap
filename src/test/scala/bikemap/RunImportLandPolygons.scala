package bikemap

import java.awt.geom.{AffineTransform, Line2D, Path2D}
import java.awt.{Frame, Graphics, Graphics2D, Shape}
import javax.swing.{JFrame, JPanel}

import bikemap.LandPolygons.TableName
import com.vividsolutions.jts.geom.{Envelope, MultiPolygon, Polygon}
import org.h2.tools.Server
import org.h2gis.utilities.{SFSUtilities, SpatialResultSet}

/**
  * Created by pappmar on 11/05/2017.
  */
object RunImportLandPolygons {

  def main(args: Array[String]): Unit = {
    val c = LandPolygons.recreateH2DB()
    LandPolygons.importFromShapeFile(c)
    c.close()

//    val c2 = LandPolygons.connectH2DbLock(
//      LandPolygons.h2Name()
//    )
//    val s = c2.createStatement()
//    s.execute(
//      s"alter table ${TableName} drop column fid"
//    )
//    s.close()
//    c2.close()
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

object RunImportedQuery {
  def main(args: Array[String]): Unit = {
    val c = LandPolygons.connectH2DbLock()

    val sc = SFSUtilities.wrapConnection(c)
    val s = sc.prepareStatement(
      """select *
        |from LAND
        |where the_geom && ST_MakeEnvelope(0,0,10,10)
      """.stripMargin
    )
    //    s.setObject(1, new Envelope(0, 1, 0, 1))
    val rs = s.executeQuery().unwrap(classOf[SpatialResultSet])
    while (rs.next()) {
      val g = rs.getGeometry()

      println(g)
    }


  }
}

object RunImportedRender {
  val MinLat = 40
  val MaxLat = 50
  val MinLon = 20
  val MaxLon = 50

  def main(args: Array[String]): Unit = {
    val renderer = new LandPolygonsRenderer


    val frame = new JFrame()
    frame.setExtendedState(Frame.MAXIMIZED_BOTH)

    val panel = new JPanel() {
      override def paint(g: Graphics): Unit = {
        val g2 = g.asInstanceOf[Graphics2D]

        val w = getWidth.toDouble
        val h = getHeight.toDouble

        renderer.draw(
          MinLon,
          MinLat,
          MaxLon,
          MaxLat,
          g2,
          w,
          h
        )

      }
    }
    frame.setContentPane(panel)


    frame.setVisible(true)



  }
}
