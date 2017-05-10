package bikemap

import java.io.File

import org.geotools.data.DataStoreFinder
import org.opengis.feature.simple.SimpleFeature

import scala.collection.JavaConverters._

/**
  * Created by pappmar on 10/05/2017.
  */
object RunLandPolygons {

  def main(args: Array[String]): Unit = {
    val file = new File("local/land-polygons-complete-4326/land_polygons.shp")

    val connect = Map(
      "url" -> file.getAbsoluteFile.toURI.toURL
    )

    val dataStore = DataStoreFinder.getDataStore(connect.asJava)
    val typeName = dataStore.getTypeNames.head
    val featureSource = dataStore.getFeatureSource(typeName)
    val collection = featureSource.getFeatures
    val iterator = collection.features()

    val it = new Iterator[SimpleFeature] {
      override def hasNext: Boolean = iterator.hasNext
      override def next(): SimpleFeature = iterator.next()
    }

    println(it.size)


  }

}
