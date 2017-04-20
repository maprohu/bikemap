name := "bikemap"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies += "org.postgresql" % "postgresql" % "42.0.0"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-core" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-pbf" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-hstore-jdbc" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-pgsnapshot" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-pgsimple" % "0.45"
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "0.8.2"
