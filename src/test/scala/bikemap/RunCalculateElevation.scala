package bikemap

/**
  * Created by pappmar on 15/05/2017.
  */
object RunCalculateElevation {

  def nextPowerOfTwo(v: Int) = {
    1 << (Integer.SIZE - Integer.numberOfLeadingZeros(v-1))
  }

  def main(args: Array[String]): Unit = {
    val lats = 180 * 60 * 60 / 3 + 1
    val lons = 360 * 60 * 60 / 3

    println(s"$lats - $lons")
    println(s"${nextPowerOfTwo(lats)} - ${nextPowerOfTwo(lons)}")
  }

}
