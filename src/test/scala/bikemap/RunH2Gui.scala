package bikemap

import org.h2.tools.Server

/**
  * Created by pappmar on 04/05/2017.
  */
object RunH2Gui {

  def main(args: Array[String]): Unit = {

    val server = Server.startWebServer(
      RunTestLoadH2.connect
    )

  }

}
