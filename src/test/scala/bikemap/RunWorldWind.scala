package bikemap

import javax.swing.JFrame

import com.jogamp.newt.opengl.GLWindow
import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.{GLAutoDrawable, GLCapabilities, GLEventListener, GLProfile}
import gov.nasa.worldwind.{BasicModel, WorldWindowGLAutoDrawable}
import gov.nasa.worldwind.awt.WorldWindowGLCanvas

/**
  * Created by pappmar on 21/04/2017.
  */
object RunWorldWind {

  def main(args: Array[String]): Unit = {

    val frame = new JFrame()
    val wwd = new WorldWindowGLCanvas()
    frame.getContentPane.add(wwd)
    wwd.setModel(new BasicModel())
    frame.pack()
    frame.setVisible(true)



  }

}
