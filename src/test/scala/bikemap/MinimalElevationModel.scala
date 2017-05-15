package bikemap

import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import java.{lang, util}
import java.util.Map

import gov.nasa.worldwind.avlist.AVList
import gov.nasa.worldwind.event.Message
import gov.nasa.worldwind.geom.{Angle, LatLon, Sector}
import gov.nasa.worldwind.globes.ElevationModel

/**
  * Created by pappmar on 15/05/2017.
  */
abstract class MinimalElevationModel extends ElevationModel {

  def fake = {
    ???
  }

  override def getName: String = fake


  override def getElevations(sector: Sector, latlons: util.List[_ <: LatLon], targetResolution: Array[Double], buffer: Array[Double]): Array[Double] = fake


  override def getExpiryTime: Long = fake

  override def setNetworkRetrievalEnabled(networkRetrievalEnabled: Boolean): Unit = fake


  override def getUnmappedElevations(sector: Sector, latlons: util.List[_ <: LatLon], targetResolution: Double, buffer: Array[Double]): Double = fake

  override def getUnmappedElevations(sector: Sector, latlons: util.List[_ <: LatLon], targetResolution: Array[Double], buffer: Array[Double]): Array[Double] = fake

  override def composeElevations(sector: Sector, latlons: util.List[_ <: LatLon], tileWidth: Int, buffer: Array[Double]): Unit = fake

  override def setExpiryTime(expiryTime: Long): Unit = fake

  override def getUnmappedLocalSourceElevation(latitude: Angle, longitude: Angle): Double = fake

  override def setEnabled(enabled: Boolean): Unit = fake

  override def contains(latitude: Angle, longitude: Angle): Boolean = fake


  override def getMissingDataSignal: Double = fake

  override def isEnabled: Boolean = fake

  override def isNetworkRetrievalEnabled: Boolean = fake

  override def setMissingDataSignal(flag: Double): Unit = fake

  override def getBestResolutions(sector: Sector): Array[Double] = fake

  override def setName(name: String): Unit = fake

  override def intersects(sector: Sector): Int = fake

  override def setExtremesCachingEnabled(enabled: Boolean): Unit = fake

  override def setMissingDataReplacement(missingDataValue: Double): Unit = fake

  override def getExtremeElevations(latitude: Angle, longitude: Angle): Array[Double] = fake


  override def isExtremesCachingEnabled: Boolean = fake

  override def getUnmappedElevation(latitude: Angle, longitude: Angle): Double = fake

  override def getMissingDataReplacement: Double = fake

  override def getLocalDataAvailability(sector: Sector, targetResolution: lang.Double): Double = fake

  override def restoreState(stateInXml: String): Unit = fake

  override def getRestorableState: String = fake

  override def propertyChange(evt: PropertyChangeEvent): Unit = fake

  override def dispose(): Unit = fake

  override def onMessage(msg: Message): Unit = fake

  override def setValues(avList: AVList): AVList = fake

  override def firePropertyChange(propertyName: String, oldValue: scala.Any, newValue: scala.Any): Unit = fake

  override def firePropertyChange(propertyChangeEvent: PropertyChangeEvent): Unit = fake

  override def removePropertyChangeListener(propertyName: String, listener: PropertyChangeListener): Unit = fake

  override def removePropertyChangeListener(listener: PropertyChangeListener): Unit = fake

  override def clearList(): AVList = fake

  override def getEntries: util.Set[Map.Entry[String, AnyRef]] = fake

  override def removeKey(key: String): AnyRef = fake

  override def copy(): AVList = fake

  override def addPropertyChangeListener(propertyName: String, listener: PropertyChangeListener): Unit = fake

  override def addPropertyChangeListener(listener: PropertyChangeListener): Unit = fake

  override def hasKey(key: String): Boolean = fake

  override def getStringValue(key: String): String = fake

  override def getValues: util.Collection[AnyRef] = fake

  override def getValue(key: String): AnyRef = fake

  override def setValue(key: String, value: scala.Any): AnyRef = fake

}
