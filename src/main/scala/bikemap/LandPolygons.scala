package bikemap

import java.awt.{Color, Graphics2D}
import java.awt.geom.{AffineTransform, Path2D}
import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.nio.ByteBuffer
import java.sql.{Connection, DriverManager}
import javax.imageio.ImageIO

import com.vividsolutions.jts.geom.{MultiPolygon, Polygon}
import gov.nasa.worldwind.formats.dds.DDSCompressor
import org.geotools.data.DataStoreFinder
import org.h2.Driver
import org.h2gis.ext.H2GISExtension
import org.h2gis.utilities.{SFSUtilities, SpatialResultSet}

/**
  * Created by pappmar on 11/05/2017.
  */
object LandPolygons {

//  val ShapeFilePath = "local/land-polygons-complete-4326/land_polygons.shp"
  val ShapeFilePath = "local/land-polygons-split-4326/land_polygons.shp"
  val TableName = "LAND"

  def featureSource = {

    // http://data.openstreetmapdata.com/land-polygons-complete-4326.zip
    val file = new File(ShapeFilePath)

    val connect = Map(
      "url" -> file.getAbsoluteFile.toURI.toURL
    )

    import scala.collection.JavaConverters._
    val dataStore = DataStoreFinder.getDataStore(connect.asJava)
    val typeName = dataStore.getTypeNames.head

    dataStore.getFeatureSource(typeName)
  }

  def h2Name(level: Int = 0) = {
    s"local/landh2/${level}/land"
  }

  def recreateH2DB(level: Int = 0) = {
    val dbName = h2Name(level)
    import ammonite.ops._
    rm(Path(dbName, pwd) / up)

    createH2Db(dbName)
  }

  def connectH2Db(dbName: String) = {
    Driver.load()
    DriverManager.getConnection(s"jdbc:h2:file:./${dbName};LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0", "sa", "sa")
  }

  def connectH2DbLock(dbName: String = h2Name()) = {
    Driver.load()
    DriverManager.getConnection(s"jdbc:h2:file:./${dbName};LOCK_TIMEOUT=1000000", "sa", "sa")
  }

  def createH2Db(dbName: String) = {
    val connection = connectH2Db(dbName)
    H2GISExtension.load(connection)
    connection
  }

  def importFromShapeFile(connection: Connection) = {
    val call = connection.prepareCall(
      "CALL SHPRead(?, ?)"
    )
    call.setString(1, ShapeFilePath)
    call.setString(2, TableName)
    call.execute()
    call.close()

    println("imported")

    val s = connection.createStatement()
    s.execute(
      s"CREATE SPATIAL INDEX land_geom_idx ON ${TableName}(the_geom)"
    )
    s.close()
    println("indexed")
  }



}

class LandPolygonsRenderer {
  val sc = {
    val c = LandPolygons.connectH2DbLock()
    SFSUtilities.wrapConnection(c)
  }

  val s = sc.prepareStatement(
    """select *
      |from LAND
      |where the_geom && ST_MakeEnvelope(?, ?, ?, ?)
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
      BufferedImage.TYPE_INT_RGB
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

    def fillPath(s: Path2D) = {
      s.transform(trf)
      g2.fill(s)
    }

    g2.setColor(Color.BLUE)
    g2.fillRect(0, 0, w.toInt, h.toInt)
    g2.setColor(Color.GREEN)

    s.setDouble(1, minLon)
    s.setDouble(2, minLat)
    s.setDouble(3, maxLon)
    s.setDouble(4, maxLat)
    val rs = s.executeQuery().unwrap(classOf[SpatialResultSet])
    while (rs.next()) {
      val geom = rs.getGeometry()

      geom match {
        case mp: MultiPolygon =>
          (0 until mp.getNumGeometries).foreach { idx =>
            val p = mp.getGeometryN(idx)
            val path = new Path2D.Double(Path2D.WIND_NON_ZERO)

            p match {
              case pol: Polygon =>

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

  def close() = {
    s.close()
    sc.close()
  }


}


