package bikemap

import java.awt.{Color, Graphics2D}
import java.awt.geom.{AffineTransform, Path2D}
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.sql.DriverManager
import javax.imageio.ImageIO

import gov.nasa.worldwind.avlist.{AVKey, AVList, AVListImpl}
import gov.nasa.worldwind.data.RasterServer
import gov.nasa.worldwind.geom.{Angle, LatLon, Sector}
import gov.nasa.worldwind.layers.BasicTiledImageLayer
import gov.nasa.worldwind.layers.mercator.MercatorSector
import gov.nasa.worldwind.retrieve.{LocalRasterServerRetriever, RetrievalPostProcessor, Retriever, RetrieverFactory}
import gov.nasa.worldwind.util.{LevelSet, WWUtil}
import org.postgis.{LineString, PGgeometry}
import org.postgresql.Driver

/**
  * Created by pappmar on 18/05/2017.
  */
object OSMLocal {



  def connect = {
    import DB._

    if (!Driver.isRegistered) Driver.register()

    DriverManager.getConnection(s"jdbc:postgresql:${DatabaseName}", UserName, Password)
  }

  def createLayer = {

    val renderer = new OSMLocalRenderer

    val rasterServer = new RasterServer {
      override def getSector: Sector = Sector.FULL_SPHERE
      override def getRasterAsByteBuffer(params: AVList): ByteBuffer = {
        val width = params.getValue(AVKey.WIDTH).asInstanceOf[Int]
        val height = params.getValue(AVKey.HEIGHT).asInstanceOf[Int]

        val sector = params.getValue(AVKey.SECTOR).asInstanceOf[Sector]

        renderer.synchronized {
          renderer.createImage(
            sector.getMinLongitude.degrees,
            sector.getMinLatitude.degrees,
            sector.getMaxLongitude.degrees,
            sector.getMaxLatitude.degrees,
            width,
            height
          )
        }
      }
    }


    val params = new AVListImpl

    params.setValue(AVKey.TILE_WIDTH, 512)
    params.setValue(AVKey.TILE_HEIGHT, 512)
    params.setValue(AVKey.DATA_CACHE_NAME, "Earth/osm-local")
    params.setValue(AVKey.DISPLAY_NAME, "osm-local.png")
    params.setValue(AVKey.DATASET_NAME, "h")
    params.setValue(AVKey.FORMAT_SUFFIX, ".png")
    params.setValue(AVKey.NUM_LEVELS, 20)
    params.setValue(AVKey.NUM_EMPTY_LEVELS, 0)
//    params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.POS180, Angle.POS360))
    params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)))
    params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180))

    val map = new BasicTiledImageLayer(
      new LevelSet(params)
    )
    map.setDetailHint(0.5)
//    map.setUseTransparentTextures(true)

    val rasterServerParams = params.copy
    val retrieverFactory = new RetrieverFactory() {
      def createRetriever(tileParams: AVList, postProcessor: RetrievalPostProcessor): Retriever = {
        val retriever = new LocalRasterServerRetriever(tileParams, rasterServer, postProcessor)
        val keysToCopy = Array[String](AVKey.DATASET_NAME, AVKey.DISPLAY_NAME, AVKey.FILE_STORE, AVKey.IMAGE_FORMAT, AVKey.FORMAT_SUFFIX)
        WWUtil.copyValues(rasterServerParams, retriever, keysToCopy, false)
        retriever
      }
    }

    map.setValue(AVKey.RETRIEVER_FACTORY_LOCAL, retrieverFactory)
    map.setEnabled(true)

    map


  }


}

class OSMLocalRenderer {

  val sc = OSMLocal.connect

  val s = sc.prepareStatement(
    """select
      |    (select ST_MakeLine(c.geom)
      |     from
      |       (select
      |           n.geom as geom
      |        from
      |           way_nodes wn
      |           inner join
      |             nodes n
      |           on
      |             n.id = wn.node_id
      |         where
      |           w.id = wn.way_id
      |         order by
      |           wn.sequence_id) c
      |     )
      |from
      |  ways w
      |where
      |  w.bbox && ST_MakeBox2D(ST_Point(?, ?), ST_Point(?, ?))
    """.stripMargin
  )


  def createImage(
    minLon: Double,
    minLat: Double,
    maxLon: Double,
    maxLat: Double,
    w: Double,
    h: Double
  ) = {
    val img = new BufferedImage(
      w.toInt,
      h.toInt,
      BufferedImage.TYPE_INT_ARGB
    )
    val g2 = img.createGraphics()
    draw(
      minLon,
      minLat,
      maxLon,
      maxLat,
      g2,
      w,
      h
    )

    val bo = new ByteArrayOutputStream()
    ImageIO.write(
      img,
      "png",
      bo
    )

    ByteBuffer.wrap(bo.toByteArray)
  }

  def draw(
    minLon: Double,
    minLat: Double,
    maxLon: Double,
    maxLat: Double,
    g2: Graphics2D,
    w: Double,
    h: Double
  ) = {

    val DifLon = maxLon - minLon
    val DifLat = maxLat - minLat

    val m00 = w / DifLon
    val m02 = - m00 * minLon
    val m11 = - h / DifLat
    val m12 = - m11 * maxLat

    val trf =
      new AffineTransform(
        m00, 0,
        0, m11,
        m02, m12
      )

    def drawPath(s: Path2D) = {
      s.transform(trf)
      g2.draw(s)
    }

    g2.setColor(Color.RED)

    s.setDouble(1, minLon)
    s.setDouble(2, minLat)
    s.setDouble(3, maxLon)
    s.setDouble(4, maxLat)

    val path = new Path2D.Double(Path2D.WIND_NON_ZERO)

    val rs = s.executeQuery()
    while (rs.next()) {
      val geom = rs.getObject(1)

      geom match {
        case pg: PGgeometry =>

          val ring = pg.getGeometry.asInstanceOf[LineString]
          val numPoints = ring.numPoints
          path.reset()

          val p0 = ring.getPoint(0)
          path.moveTo(p0.x, p0.y)
          (1 until numPoints).foreach { idx =>
            val p = ring.getPoint(idx)
            path.lineTo(p.x, p.y)
          }

          drawPath(path)
      }
    }



  }

  def close() = {
    s.close()
    sc.close()
  }


}

