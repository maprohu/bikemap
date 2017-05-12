package bikemap

import java.awt.Frame
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import javax.swing.JFrame

import gov.nasa.worldwind.avlist.{AVKey, AVList, AVListImpl}
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.data.{BasicRasterServer, RasterServer}
import gov.nasa.worldwind.formats.dds.DDSCompressor
import gov.nasa.worldwind.geom.{Angle, LatLon, Sector}
import gov.nasa.worldwind.globes.{Earth, EllipsoidalGlobe}
import gov.nasa.worldwind.layers._
import gov.nasa.worldwind.layers.mercator.MercatorSector
import gov.nasa.worldwind.render.DrawContext
import gov.nasa.worldwind.retrieve.{LocalRasterServerRetriever, RetrievalPostProcessor, Retriever, RetrieverFactory}
import gov.nasa.worldwind.terrain.ZeroElevationModel
import gov.nasa.worldwind.util.{LevelSet, OGLUtil, WWIO, WWUtil}
import gov.nasa.worldwind.{BasicModel, Configuration}

/**
  * Created by pappmar on 21/04/2017.
  */
object RunWorldWind2 {

  val TextureWidth = 512
  val TextureHeight = TextureWidth

  def main(args: Array[String]): Unit = {

    val frame = new JFrame()
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    val wwd = new WorldWindowGLCanvas()
    frame.getContentPane.add(wwd)

    import Earth._
    val globe =
      new EllipsoidalGlobe(
        WGS84_EQUATORIAL_RADIUS,
        WGS84_POLAR_RADIUS,
        WGS84_ES,
        new ZeroElevationModel
      )

    val renderer = new LandPolygonsRenderer

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

    params.setValue(AVKey.TILE_WIDTH, TextureWidth)
    params.setValue(AVKey.TILE_HEIGHT, TextureHeight)
    params.setValue(AVKey.DATA_CACHE_NAME, "Earth/lang-polygons/land-polygons")
    params.setValue(AVKey.DISPLAY_NAME, "land-polygons.png")
    params.setValue(AVKey.DATASET_NAME, "h")
    params.setValue(AVKey.FORMAT_SUFFIX, ".png")
    params.setValue(AVKey.NUM_LEVELS, 20)
    params.setValue(AVKey.NUM_EMPTY_LEVELS, 0)
    params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d)))
    //        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)))
    params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180))

    val map = new BasicTiledImageLayer(
        new LevelSet(params)
    )
    map.setDetailHint(0.5)

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

    wwd.setModel(
      new BasicModel(
        globe,
        new LayerList(
          Array[Layer](
            map,
//            osm,
            new LatLonGraticuleLayer
          )
        )
      )
    )

    frame.pack()
    frame.setExtendedState(Frame.MAXIMIZED_BOTH)
    frame.setVisible(true)



  }

}
