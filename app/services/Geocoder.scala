package services

import scala.concurrent.Future

case class LatLon(val lat: String, val lon: String)
case class Query(val country: Country, val region: Region, val city: City)

abstract class Geocoder {
  def lookup(q: Query): Future[LatLon]
}
