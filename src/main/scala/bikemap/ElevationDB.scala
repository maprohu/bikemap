package bikemap

import java.nio.{ByteBuffer, ByteOrder}
import java.nio.channels.FileChannel
import java.nio.file.{Files, StandardOpenOption}
import java.sql.{Connection, DriverManager}

import ammonite.ops.{Path, ls}
import gov.nasa.worldwind.geom.Angle
import org.h2.Driver

/**
  * Created by pappmar on 16/05/2017.
  */
object ElevationDB {

  val dbName = "local/elevh2/elev"
  val Resolution = Angle.SECOND.radians * 3
  val DataPointsPerDegree = 1200


  def connectH2Db = {
    Driver.load()
    DriverManager.getConnection(s"jdbc:h2:file:./${dbName};LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0", "sa", "sa")
  }

  def recreateH2DB = {
    import ammonite.ops._
    rm(Path(dbName, pwd) / up)

    createH2Db
  }

  def createH2Db = {
    val connection = connectH2Db

    connection
  }

  def importData(c: Connection) = {
    val s = c.createStatement()

    s.execute(
      """create table IMPORTED (
        |  lat INT4,
        |  lon INT4,
        |  primary key (lat, lon)
        |)
      """.stripMargin
    )

    s.execute(
      """create table ELEVATION (
        |  lat INT4,
        |  lon INT4,
        |  alt INT2,
        |  primary key (lat, lon)
        |)
      """.stripMargin
    )

    val insert = c.prepareStatement(
      """merge into ELEVATION
        |key(lat, lon)
        |values(?, ?, ?)
      """.stripMargin
    )

    val FileNamePattern = """([nNsS])(\d{2})([eEwW])(\d{3})\.[hH][gG][tT]""".r
    val dir = "local/viewfinderpanoramas"
    import ammonite.ops._

    val Size3 = (DataPointsPerDegree+1) * (DataPointsPerDegree+1) * 2
    val Size1 = (DataPointsPerDegree*3+1) * (DataPointsPerDegree*3+1) * 2

    ls
      .rec(Path(dir, pwd))
      .filter(_.isFile)
      .map(f => (f, f.name))
      .collect({
        case (f, FileNamePattern(ns, lat, ew, lon)) =>
          println(f)

          def latNum = lat.toInt
          def lonNum = lon.toInt
          def latDeg = ns.toUpperCase() match { case "N" => latNum ; case "S" => -latNum ; case _ => ???  }
          def lonDeg = ew.toUpperCase() match { case "E" => lonNum ; case "W" => -lonNum ; case _ => ???  }

          val bytes =
            Files.readAllBytes(
              f.toNIO
            )

          val bb =
            ByteBuffer.wrap(
              bytes
            )
          bb.order(ByteOrder.BIG_ENDIAN)
          val sb = bb.asShortBuffer()

          val latBase = latDeg * DataPointsPerDegree
          val lonBase = lonDeg * DataPointsPerDegree

          bytes.length match {
            case Size3 =>
              ((DataPointsPerDegree).to(0,-1)).foreach { latIdx =>
                val latKey = latBase + latIdx
                insert.setInt(1, latKey)

                (0 to DataPointsPerDegree).foreach { lonIdx =>
                  val lonKey = lonBase + lonIdx

                  val alt = sb.get()
                  insert.setInt(2, lonKey)
                  insert.setShort(3, alt)
                  insert.execute()
                }

              }
            case Size1 =>
              ???
            case _ => ???
          }




          ()
      })


  }


}
