package bikemap

import java.io.FileInputStream
import java.util

import crosby.binary.osmosis.OsmosisReader
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer
import org.openstreetmap.osmosis.core.task.v0_6.Sink
import scala.collection.JavaConverters._

/**
  * Created by pappmar on 04/05/2017.
  */
object RunTestOsmPbf {

  def main(args: Array[String]): Unit = {
    val reader = new OsmosisReader(
      new FileInputStream("local/portugal-latest.osm.pbf")
    )

    reader.setSink(
      new Sink {
        var count = 0

        override def process(entityContainer: EntityContainer): Unit = {
          count += 1
        }

        override def initialize(metaData: util.Map[String, AnyRef]): Unit = {
          println(metaData.asScala)
        }

        override def complete(): Unit = {
          println(count)
        }

        override def release(): Unit = ()
      }
    )

    reader.run()

  }

}
