package bikemap

import java.awt.Frame
import javax.swing.JFrame

import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.{GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import gov.nasa.worldwind.avlist.{AVKey, AVList, AVListImpl}
import gov.nasa.worldwind.{BasicFactory, BasicModel, WorldWindowGLAutoDrawable}
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.geom.{Angle, LatLon}
import gov.nasa.worldwind.globes.{Earth, EarthFlat, EllipsoidalGlobe}
import gov.nasa.worldwind.layers._
import gov.nasa.worldwind.layers.mercator.MercatorSector
import gov.nasa.worldwind.render.DrawContext
import gov.nasa.worldwind.terrain.ZeroElevationModel
import gov.nasa.worldwind.util.LevelSet

/**
  * Created by pappmar on 21/04/2017.
  */
object RunWorldWind {

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


    val map = new TiledImageLayer(
      {
        val params = new AVListImpl

        params.setValue(AVKey.TILE_WIDTH, 256)
        params.setValue(AVKey.TILE_HEIGHT, 256)
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
    ) {
      override def requestTexture(dc: DrawContext, tile: TextureTile): Unit = {
        println(dc)
        println(tile)
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
