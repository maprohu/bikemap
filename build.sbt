name := "bikemap"

version := "1.0"

scalaVersion := "2.12.2"

resolvers += "boundless" at "https://repo.boundlessgeo.com/main/"

libraryDependencies += "org.postgresql" % "postgresql" % "42.0.0"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-core" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-pbf" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-hstore-jdbc" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-pgsnapshot" % "0.45"
libraryDependencies += "org.openstreetmap.osmosis" % "osmosis-pgsimple" % "0.45"
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % "0.8.2"
libraryDependencies += "com.github.pcmehlitz" % "worldwind-pcm" % "2.1.0.+"
libraryDependencies += "com.h2database" % "h2" % "1.4.195"
libraryDependencies += "org.orbisgis" % "h2gis-ext" % "1.3.1"
libraryDependencies += "com.graphhopper" % "graphhopper-reader-osm" % "0.8.2"
libraryDependencies += "org.geotools" % "gt-shapefile" % "17.0"
