package bikemap

import java.awt.Frame
import java.awt.image.BufferedImage
import javax.swing.JFrame

import gov.nasa.worldwind.avlist.{AVKey, AVList, AVListImpl}
import gov.nasa.worldwind.{BasicFactory, BasicModel, Configuration, WorldWindowGLAutoDrawable}
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.formats.dds.DDSCompressor
import gov.nasa.worldwind.geom.{Angle, LatLon}
import gov.nasa.worldwind.globes.{Earth, EarthFlat, EllipsoidalGlobe}
import gov.nasa.worldwind.layers._
import gov.nasa.worldwind.layers.mercator.MercatorSector
import gov.nasa.worldwind.render.DrawContext
import gov.nasa.worldwind.terrain.ZeroElevationModel
import gov.nasa.worldwind.util.{LevelSet, OGLUtil, WWIO}

/**
  * Created by pappmar on 21/04/2017.
  */
object RunWorldWind {

  val TextureWidth = 256
  val TextureHeight = TextureWidth

  def main(args: Array[String]): Unit = {

    val frame = new JFrame()
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    val wwd = new WorldWindowGLCanvas()
    frame.getContentPane.add(wwd)

//    val osm =
//      BasicFactory
//        .create(
//          AVKey.LAYER_FACTORY,
//          "config/Earth/OpenStreetMap.xml"
//        )
//        .asInstanceOf[Layer]
//    osm.setEnabled(true)

    import Earth._
    val globe =
//      new Earth
      new EllipsoidalGlobe(
        WGS84_EQUATORIAL_RADIUS,
        WGS84_POLAR_RADIUS,
        WGS84_ES,
        new ZeroElevationModel
      )

    val renderer = new LandPolygonsRenderer

    val map = new BasicTiledImageLayer(
      {
        val params = new AVListImpl

        params.setValue(AVKey.TILE_WIDTH, TextureWidth)
        params.setValue(AVKey.TILE_HEIGHT, TextureHeight)
            params.setValue(AVKey.DATA_CACHE_NAME, "Earth/lang-polygons/land-polygons")
        //    params.setValue(AVKey.SERVICE, "http://a.tile.openstreetmap.org/")
            params.setValue(AVKey.DATASET_NAME, "h")
            params.setValue(AVKey.FORMAT_SUFFIX, ".png")
        params.setValue(AVKey.NUM_LEVELS, 20)
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0)
            params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(180d), Angle.fromDegrees(360d)))
//        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(22.5d), Angle.fromDegrees(45d)))
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180))
        //    params.setValue(AVKey.TILE_URL_BUILDER, new OSMMapnikLayer.URLBuilder)

        new LevelSet(params)
      }
    ) { layer =>
      override def requestTexture(dc: DrawContext, tile: TextureTile): Unit = {
        println(dc)
        println(tile)

        this.getRequestQ.add {
          new Runnable with Comparable[Runnable] {
            override def run(): Unit = {
              println(tile.getPath)

              val img = new BufferedImage(
                TextureWidth,
                TextureHeight,
                BufferedImage.TYPE_INT_ARGB
              )
              val g2 = img.createGraphics()

              renderer.synchronized {
                renderer.draw(
                  tile.getSector.getMinLongitude.degrees,
                  tile.getSector.getMinLatitude.degrees,
                  tile.getSector.getMaxLongitude.degrees,
                  tile.getSector.getMaxLatitude.degrees,
                  g2,
                  TextureWidth,
                  TextureHeight
                )

              }

              g2.drawString(
                tile.getLevelNumber.toString,
                TextureWidth / 2,
                TextureHeight / 2
              )

              val attributes = DDSCompressor.getDefaultCompressionAttributes()
              attributes.setBuildMipmaps(layer.isUseMipMaps)
              val buffer = new DDSCompressor().compressImage(img, attributes)

              val textureData = OGLUtil.newTextureData(
                Configuration.getMaxCompatibleGLProfile,
                WWIO.getInputStreamFromByteBuffer(buffer),
                layer.isUseMipMaps
              )

              tile.setTextureData(textureData)

              TextureTile.getMemoryCache.add(tile.getTileKey, tile)
            }

            override def compareTo(o: Runnable): Int = 0
          }
        }
      }

      override def forceTextureLoad(tile: TextureTile): Unit = ???
    }
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
