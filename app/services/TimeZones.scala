package services

import scala.concurrent.{ Await }
import scala.concurrent.duration._

class TimeZones(
  geocoder: Geocoder = new NominatimGeocoder,
  api: GoogleTimeZoneApi = new GoogleTimeZoneApi()
) {
  val country_fns = Seq[(Country) => Option[Country]](
    (c: Country) => FindCountry.by_code2(c.code2),
    (c: Country) => FindCountry.by_code2(c.name),
    (c: Country) => FindCountry.by_name(c.name),
    (c: Country) => FindCountry.by_code3(c.code3),
    (c: Country) => FindCountry.by_code3(c.name)
  )
  val subdivision_fns = Seq[(String, Subdivision) => Option[Subdivision]](
    (cc: String, s: Subdivision) => FindSubdivision.by_full_code(s.code),
    (cc: String, s: Subdivision) => FindSubdivision.by_full_code(s.name),
    (cc: String, s: Subdivision) => FindSubdivision.by_name(cc, s.name)
  )

  def lookup(
    country: Option[Country], subdivision: Option[Subdivision], city: Option[City]
  ): Option[String] = {
    normalize_country(country) match {
      case Some(fc) => {
        lookup_timezone_by_location(
          lookup_location(fc, normalize_subdivision(fc.code2, subdivision), city)
        )
      }
      case None => None
    }
  }

  def lookup_location(
    country: Country, opt_subdivision: Option[Subdivision], opt_city: Option[City]
  ): Option[LatLon] = opt_city match {
    case None => lookup_location_by_subdivision(opt_subdivision) match {
      case None => Option(country.geo)
      case opt => opt
    }
    case Some(city) => lookup_location_by_city(country, city)
  }

  def lookup_location_by_subdivision(
    opt_subdivision: Option[Subdivision]
  ): Option[LatLon] = opt_subdivision match {
    case Some(subdivision) => Option(subdivision.geo)
    case None => None
  }

  def lookup_location_by_city(country: Country, city: City): Option[LatLon] = {
    val fut = geocoder.lookup(Query(country, city))
    Await.result(fut, 5.seconds) match {
      case (loc: LatLon) => Some(loc)
      case _ => None
    }
  }

  def lookup_timezone_by_location(opt_loc: Option[LatLon]): Option[String] = opt_loc match {
    case Some(loc) => {
      val fut = api.lookup(loc)
      Await.result(fut, 5.seconds) match {
        case (tzi: TimeZoneInfo) => Some(tzi.id)
        case _ => None
      }
    }
    case None => None
  }

  def normalize_country(opt_country: Option[Country]): Option[Country] = opt_country match {
    case Some(country) => {
      country_fns.map { fn => fn(country) }.flatten match {
        case (c: Country) :: tail => Some(c)
        case _ => Some(country)
      }
    }
    case None => None
  }

  def normalize_subdivision(
    country_code2: String, opt_subdivision: Option[Subdivision]
  ): Option[Subdivision] = opt_subdivision match {
    case Some(subdivision) => {
      subdivision_fns.map { fn => fn(country_code2, subdivision) }.flatten match {
        case (s: Subdivision) :: tail => Some(s)
        case _ => Some(subdivision)
      }
    }
    case None => None
  }
}
