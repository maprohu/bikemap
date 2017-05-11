package bikemap

import java.io.File
import java.sql.{Connection, DriverManager}

import org.geotools.data.DataStoreFinder
import org.h2.Driver
import org.h2gis.ext.H2GISExtension

/**
  * Created by pappmar on 11/05/2017.
  */
object LandPolygons {

  val ShapeFilePath = "local/land-polygons-complete-4326/land_polygons.shp"
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

  def connectH2DbLock(dbName: String) = {
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

class LandPolygons {

  val featureSource = LandPolygons.featureSource

  def draw() = {


  }


}
