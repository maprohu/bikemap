package bikemap

/**
  * Created by pappmar on 20/04/2017.
  */
object RunCreateSchema {

  def main(args: Array[String]): Unit = {
    run("pgsimple_schema_0.6")
    run("pgsimple_schema_0.6_bbox")
  }

  def run(script: String): Unit = {
    import ammonite.ops._
    import DB._
    import ImplicitWd._

    %(
      "psql",
      "-U", UserName,
      "-d", DatabaseName,
      "-f", s"src/test/resources/osmosis/script/${script}.sql"
    )
  }

}
