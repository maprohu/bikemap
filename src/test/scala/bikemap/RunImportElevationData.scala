package bikemap

import java.nio.{ByteBuffer, ByteOrder}
import java.nio.file.{Files, Paths}
import java.sql.Connection

import gov.nasa.worldwind.geom.Sector

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

object RunTestElevation {
  def main(args: Array[String]): Unit = {



    Iterator
      .iterate(0)(_ + 1)
      .take(3)
      .foreach(println)


    println(
      ElevationFiles.sections(
        0, 3, 12
      )
    )

    val f = ElevationFiles.read
    f.render(
      Sector.fromDegrees(0, 1, 0, 1),
      512,
      512
    )
  }
}

object RunDumpFile {
  def main(args: Array[String]): Unit = {
    val bb = ByteBuffer.wrap(
      Files.readAllBytes(
        Paths.get("local/viewfinderpanoramas/J29/N38W010.hgt")
      )
    )
    bb.order(ByteOrder.BIG_ENDIAN)
    val sb = bb.asShortBuffer()

    (0 until 100).foreach { row =>
      sb.position(row*1201 + 1200-450-10)

      (0 until 20).foreach { _ =>
        print(s"${sb.get()} ")
      }
      println()

    }

  }
}