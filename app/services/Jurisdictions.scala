package services

case class Country(
  val name: String = null,
  val code2: String = null,
  val code3: String = null,
  val continent: String = null,
  val region: String = null,
  val subregion: String = null,
  val subdivisions: Seq[Subdivision] = Seq(),
  val geo: LatLon = null,
)

case class Subdivision(
  val name: String = null,
  val code: String = null,
  val geo: LatLon = null)

case class Region(
  val name: String = null,
  val code: String = null,
  val geo: LatLon = null)

case class City(val name: String)
