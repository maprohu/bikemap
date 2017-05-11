package bikemap

import java.io.File

import org.geotools.data.DataStoreFinder
import org.geotools.filter.AttributeExpressionImpl
import org.geotools.filter.spatial.BBOXImpl
import org.opengis.feature.simple.SimpleFeature

import scala.collection.JavaConverters._

/**
  * Created by pappmar on 10/05/2017.
  */
object RunLandPolygons {

  def main(args: Array[String]): Unit = {
    val featureSource = LandPolygons.featureSource

    val schema = featureSource.getSchema
    val geomPropertyName = schema.getGeometryDescriptor.getLocalName

    val collection = featureSource.getFeatures(
      new BBOXImpl(
        new AttributeExpressionImpl(geomPropertyName),
        0,
        0,
        1,
        1,
        ""
      )

    )
    val iterator = collection.features(
    )

    val it = new Iterator[SimpleFeature] {
      override def hasNext: Boolean = iterator.hasNext
      override def next(): SimpleFeature = iterator.next()
    }

    println(it.size)


  }

}
