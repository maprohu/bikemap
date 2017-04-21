package bikemap

import java.awt.Frame
import javax.swing.JFrame

import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.{GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import gov.nasa.worldwind.avlist.AVKey
import gov.nasa.worldwind.{BasicFactory, BasicModel, WorldWindowGLAutoDrawable}
import gov.nasa.worldwind.awt.WorldWindowGLCanvas
import gov.nasa.worldwind.globes.Earth
import gov.nasa.worldwind.layers.{BasicLayerFactory, LatLonGraticuleLayer, Layer, LayerList}

/**
  * Created by pappmar on 21/04/2017.
  */
object RunWorldWind {

  def main(args: Array[String]): Unit = {

    val frame = new JFrame()
    val wwd = new WorldWindowGLCanvas()
    frame.getContentPane.add(wwd)

    val osm =
      BasicFactory
        .create(
          AVKey.LAYER_FACTORY,
          "config/Earth/OpenStreetMap.xml"
        )
        .asInstanceOf[Layer]
    osm.setEnabled(true)

    wwd.setModel(
      new BasicModel(
        new Earth,
        new LayerList(
          Array[Layer](
            osm,
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
