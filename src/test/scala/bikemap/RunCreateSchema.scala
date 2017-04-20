package bikemap

/**
  * Created by pappmar on 20/04/2017.
  */
object RunCreateSchema {

  def main(args: Array[String]): Unit = {
    import ammonite.ops._
    import DB._
    import ImplicitWd._

    %(
      "psql",
      "-U", UserName,
      "-d", DatabaseName,
      "-f", "src/test/resources/osmosis/script/pgsimple_schema_0.6.sql"
    )
  }

}
