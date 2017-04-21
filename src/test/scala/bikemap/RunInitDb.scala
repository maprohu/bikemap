package bikemap

/**
  * Created by pappmar on 21/04/2017.
  */
object RunInitDb {

  def main(args: Array[String]): Unit = {
    import ammonite.ops._
    import ImplicitWd._

    %("initdb", "-E", "UTF8")
  }

}
