package bikemap

import java.sql.{DriverManager, Statement}

import org.postgresql.Driver

/**
  * Created by pappmar on 20/04/2017.
  */
object RunCreateDb {


  def main(args: Array[String]): Unit = {

    if (!Driver.isRegistered) Driver.register()

    createdb()
    setupdb()
  }

  def createdb() = {

    val c = DriverManager.getConnection("jdbc:postgresql:postgres")
    implicit val s = c.createStatement()
    import s._
    import DB._

    try {
      println(single("select version()"))

      quietly { execute(s"create user $UserName superuser password '$Password'") }
      quietly { execute(s"drop database if exists $DatabaseName") }
      quietly { execute(s"create database $DatabaseName owner $UserName template 'template0' encoding 'UTF8'") }

    } finally {
      c.close()
    }

  }

  def setupdb() = {
    import DB._

    val c = DriverManager.getConnection(s"jdbc:postgresql:${DatabaseName}", UserName, Password)
    implicit val s = c.createStatement()
    import s._

    try {
      println(single("select version()"))

      quietly { execute("CREATE EXTENSION postgis") }
      quietly { execute("CREATE EXTENSION postgis_topology") }

      println(single("select postgis_full_version()"))
      println(single("select ST_Point(1, 2) AS MyFirstPoint"))
      println(single("select ST_SetSRID(ST_Point(-77.036548, 38.895108),4326)"))

    } finally {
      c.close()
    }

  }

  def single[T](sql: String)(implicit stm: Statement) = {
    val r = stm.executeQuery(sql)
    r.next()
    r.getObject(1).asInstanceOf[T]
  }

  def quietly(fn: => Unit) = {
    try {
      fn
    } catch {
      case ex : Exception =>
        ex.printStackTrace()
    }
  }

}
