package services

import scala.concurrent.Future

case class LatLon(val lat: String, val lon: String)
case class Country(val code2: String, val code3: String, val name: String)
case class Region(val code: String, val name: String)
case class City(val name: String)
case class Query(val country: Country, val region: Region, val city: City)

abstract class Geocoder {
  def lookup(q: Query): Future[LatLon]
}
