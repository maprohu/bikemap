package bikemap

import java.io.FileInputStream
import java.sql.{DriverManager, PreparedStatement}
import java.util
import java.util.{Timer, TimerTask}

import crosby.binary.osmosis.OsmosisReader
import org.h2.Driver
import org.h2gis.ext.H2GISExtension
import org.openstreetmap.osmosis.core.container.v0_6._
import org.openstreetmap.osmosis.core.domain.v0_6.Entity
import org.openstreetmap.osmosis.core.task.v0_6.Sink

import scala.collection.JavaConverters._

/**
  * Created by pappmar on 04/05/2017.
  */
object RunTestLoadH2 {

  val H2Dir = "local/h2"

  Driver.load()

  def connect = {
    DriverManager.getConnection(s"jdbc:h2:file:./${H2Dir}/osm", "sa", "sa")
  }

  def main(args: Array[String]): Unit = {
    import ammonite.ops._


    val h2Dir = Path(H2Dir, pwd)
    rm(h2Dir)
    mkdir(h2Dir)

    val connection =
      DriverManager.getConnection(s"jdbc:h2:file:./${H2Dir}/osm;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0", "sa", "sa")
    H2GISExtension.load(connection)

    val st = connection.createStatement()
    st.execute("CREATE TABLE nodes (id BIGINT not null, geom POINT)")
    st.execute("CREATE TABLE node_tags (node_id BIGINT not null, k TEXT not null, v TEXT not null)")
    st.execute("CREATE TABLE ways (id BIGINT not null)")
    st.execute("CREATE TABLE way_nodes (way_id LONG not null, node_id LONG not null, sequence_id INT not null)")
    st.execute("CREATE TABLE way_tags (way_id BIGINT not null, k TEXT not null, v TEXT not null)")
    st.execute("CREATE TABLE relations (id BIGINT not null)")
    st.execute("CREATE TABLE relation_members (relation_id BIGINT not null, member_id BIGINT not null, member_type TINYINT not null, member_role TEXT not null, sequence_id INT not null)")
    st.execute("CREATE TABLE relation_tags (relation_id BIGINT not null, k TEXT not null, v TEXT not null)")


    def createIndexes() = {
      st.execute("CREATE primary key ON nodes (id)")
      st.execute("CREATE primary key ON ways (id)")
      st.execute("CREATE primary key ON relations (id)")
      st.execute("CREATE primary key ON way_nodes (way_id, sequence_id)")
      st.execute("CREATE primary key ON relation_members (relation_id, sequence_id)")
      st.execute("CREATE INDEX idx_node_tags_node_id ON node_tags (node_id)")
      st.execute("CREATE SPATIAL INDEX idx_nodes_geom ON nodes (geom)")
      st.execute("CREATE INDEX idx_way_tags_way_id ON way_tags (way_id)")
      st.execute("CREATE INDEX idx_way_nodes_node_id ON way_nodes (node_id)")
      st.execute("CREATE INDEX idx_relation_tags_relation_id ON relation_tags (relation_id)")
    }

    createIndexes()

    val nodeStatement = connection.prepareStatement(
      "insert into nodes values (?, ST_MakePoint(?, ?))"
    )
    val nodeTagStatement = connection.prepareStatement(
      "insert into node_tags values (?, ?, ?)"
    )
    val wayStatement = connection.prepareStatement(
      "insert into ways values (?)"
    )
    val wayNodeStatement = connection.prepareStatement(
      "insert into way_nodes values (?, ?, ?)"
    )
    val wayTagStatement = connection.prepareStatement(
      "insert into way_tags values (?, ?, ?)"
    )
    val relationStatement = connection.prepareStatement(
      "insert into relations values (?)"
    )
    val relationMemberStatement = connection.prepareStatement(
      "insert into relation_members values (?, ?, ?, ?, ?)"
    )
    val relationTagStatement = connection.prepareStatement(
      "insert into relation_tags values (?, ?, ?)"
    )


    val reader = new OsmosisReader(
//      new FileInputStream("local/andorra-latest.osm.pbf")
      new FileInputStream("local/portugal-latest.osm.pbf")
    )


    def processTags(
      st: PreparedStatement,
      entity: Entity
    ) = {
      st.setLong(1, entity.getId)

      entity
        .getTags
        .asScala
        .foreach { tag =>
          st.setString(2, tag.getKey)
          st.setString(3, tag.getValue)
          st.executeUpdate()
        }
    }

    val entityProcessor = new EntityProcessor {
      override def process(bound: BoundContainer): Unit = ()

      override def process(node: NodeContainer): Unit = {
        val entity = node.getEntity
        nodeStatement.setLong(1, entity.getId)
        nodeStatement.setDouble(2, entity.getLongitude)
        nodeStatement.setDouble(3, entity.getLatitude)
        nodeStatement.executeUpdate()

        processTags(nodeTagStatement, entity)
      }

      override def process(way: WayContainer): Unit = {
        val entity = way.getEntity
        wayStatement.setLong(1, entity.getId)
        wayStatement.executeUpdate()

        wayNodeStatement.setLong(1, entity.getId)

        entity
          .getWayNodes
          .asScala
          .zipWithIndex
          .foreach({ case (wn, idx) =>
              wayNodeStatement.setLong(2, wn.getNodeId)
              wayNodeStatement.setInt(3, idx)
              wayNodeStatement.executeUpdate()
          })

        processTags(wayTagStatement, entity)
      }

      override def process(relation: RelationContainer): Unit = {
        val entity = relation.getEntity
        relationStatement.setLong(1, entity.getId)
        relationStatement.executeUpdate()

        relationMemberStatement.setLong(1, entity.getId)

        entity
          .getMembers
          .asScala
          .zipWithIndex
          .foreach({ case (m, idx) =>
            relationMemberStatement.setLong(2, m.getMemberId)
            relationMemberStatement.setByte(3, m.getMemberType.ordinal().toByte)
            relationMemberStatement.setString(4, m.getMemberRole)
            relationMemberStatement.setInt(5, idx)
            relationMemberStatement.executeUpdate()
          })

        processTags(relationTagStatement, entity)
      }
    }

    @volatile var count = 0
    val start = System.currentTimeMillis()

    val timer = new Timer()
    timer.schedule(
      new TimerTask {
        override def run(): Unit = {
          println(count/((System.currentTimeMillis() - start)/1000.0))
        }
      },
      1000L,
      1000L
    )

    reader.setSink(
      new Sink {

        override def process(entityContainer: EntityContainer): Unit = {
          count += 1
          entityContainer.process(entityProcessor)
        }

        override def initialize(metaData: util.Map[String, AnyRef]): Unit = {
          println(metaData.asScala)
        }

        override def complete(): Unit = {
          println(count)
        }

        override def release(): Unit = ()
      }
    )

    try {
      reader.run()


    } finally {
      timer.cancel()
      connection.close()
    }

  }

}
