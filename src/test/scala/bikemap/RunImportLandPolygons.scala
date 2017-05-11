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
  val MinLat = 30
  val MaxLat = 80
  val MinLon = 30
  val MaxLon = 170

  def main(args: Array[String]): Unit = {
    val c = LandPolygons.connectH2DbLock()

    val sc = SFSUtilities.wrapConnection(c)
    val s = sc.prepareStatement(
      """select *
        |from LAND
        |where the_geom && ST_MakeEnvelope(?, ?, ?, ?)
      """.stripMargin
    )
    s.setDouble(1, MinLon)
    s.setDouble(2, MinLat)
    s.setDouble(3, MaxLon)
    s.setDouble(4, MaxLat)

    val frame = new JFrame()
    frame.setExtendedState(Frame.MAXIMIZED_BOTH)

    val panel = new JPanel() {
      override def paint(g: Graphics): Unit = {
        val g2 = g.asInstanceOf[Graphics2D]

        val w = getWidth.toDouble
        val h = getHeight.toDouble

        val DifLon = MaxLon - MinLon
        val DifLat = MaxLat - MinLat

        val m00 = w / DifLon
        val m02 = - m00 * MinLon
        val m11 = - h / DifLat
        val m12 = - m11 * MaxLat

        val trf =
          new AffineTransform(
            m00, 0,
            0, m11,
            m02, m12
          )

//        def draw(s: Shape) = {
//          g2.draw(trf.createTransformedShape(s))
//        }

        def fillPath(s: Path2D) = {
          s.transform(trf)
          g2.fill(s)
        }

//        val line1 = new Line2D.Double(MinLon, MinLat, MaxLon, MaxLat)
//        draw(line1)
//        val line2 = new Line2D.Double(MinLon, MaxLat, MaxLon, MinLat)
//        draw(line2)

        val rs = s.executeQuery().unwrap(classOf[SpatialResultSet])
        while (rs.next()) {
          val geom = rs.getGeometry()

          geom match {
            case mp : MultiPolygon =>
              (0 until mp.getNumGeometries).foreach { idx =>
                val p = mp.getGeometryN(idx)
                val path = new Path2D.Double(Path2D.WIND_NON_ZERO)

                p match {
                  case pol : Polygon =>

                    val ring = pol.getExteriorRing
                    val numPoints = ring.getNumPoints
                    path.reset()

                    val p0 = ring.getCoordinateN(0)
                    path.moveTo(p0.x, p0.y)
                    (1 until numPoints).foreach { idx =>
                      val p = ring.getCoordinateN(idx)
                      path.lineTo(p.x, p.y)
                    }
                    path.closePath()

                    fillPath(path)
                }

              }

          }

        }
      }
    }
    frame.setContentPane(panel)


    frame.setVisible(true)



  }
}
